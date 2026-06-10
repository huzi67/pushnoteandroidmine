package com.penguenlabs.pushnote.features.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.penguenlabs.pushnote.data.local.dao.ScheduledNoteDao
import com.penguenlabs.pushnote.pushnotification.sender.NotificationSender
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationSender: NotificationSender

    @Inject
    lateinit var scheduledNoteDao: ScheduledNoteDao

    @Inject
    lateinit var scheduleAlarmManager: ScheduleAlarmManager

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra(ScheduleAlarmManager.EXTRA_SCHEDULED_NOTE_ID, -1L)
        Log.d(TAG, "=== Alarm received ===")
        Log.d(TAG, "noteId=$noteId")

        if (noteId <= 0) {
            Log.w(TAG, "Invalid noteId, aborting")
            return
        }

        // Acquire wake lock to keep CPU on during processing (critical for Samsung)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "PushNote:ScheduleAlarm:$noteId"
        )
        wakeLock.acquire(30_000L) // 30 seconds timeout

        val goAsync = goAsync()

        scope.launch {
            try {
                val scheduledNote = scheduledNoteDao.getById(noteId)
                if (scheduledNote == null) {
                    Log.w(TAG, "ScheduledNote not found in DB for id=$noteId")
                    return@launch
                }

                Log.d(TAG, "Note found: '${scheduledNote.note}' repeat=${scheduledNote.repeatMode}")

                // Send the notification as pinned (same as manual push)
                notificationSender.sendPinnedNotification(
                    notificationEntityId = noteId,
                    pushNotificationText = scheduledNote.note
                )
                Log.d(TAG, "Notification posted for noteId=$noteId")

                // Reschedule repeating alarms
                if (scheduledNote.repeatMode != "NONE") {
                    scheduleAlarmManager.schedule(scheduledNote)
                    Log.d(TAG, "Rescheduled repeating alarm for noteId=$noteId")
                } else {
                    scheduledNoteDao.deactivate(noteId)
                    Log.d(TAG, "One-shot alarm completed, deactivated noteId=$noteId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing alarm noteId=$noteId", e)
            } finally {
                goAsync.finish()
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            }
        }
    }

    companion object {
        private const val TAG = "ScheduleBroadcastReceiver"
    }
}
