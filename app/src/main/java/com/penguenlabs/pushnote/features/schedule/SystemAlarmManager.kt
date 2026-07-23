package com.penguenlabs.pushnote.features.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.penguenlabs.pushnote.data.local.entity.ScheduledNoteEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SystemAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(scheduledNote: ScheduledNoteEntity): Boolean {
        cancel(scheduledNote.id)

        val triggerTime = ScheduleAlarmManager.calculateNextTriggerTime(scheduledNote)
        val secondsUntil = (triggerTime - System.currentTimeMillis()) / 1000
        Log.d(TAG, "=== Scheduling system alarm ===")
        Log.d(TAG, "noteId=${scheduledNote.id} triggerTime=$triggerTime (in $secondsUntil seconds)")

        val pendingIntent = createPendingIntent(scheduledNote.id)
        var success = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                )
                success = true
                Log.d(TAG, "✅ system setExactAndAllowWhileIdle noteId=${scheduledNote.id}")
            } catch (e: SecurityException) {
                Log.w(TAG, "system setExactAndAllowWhileIdle denied: ${e.message}")
            } catch (e: Exception) {
                Log.w(TAG, "system setExactAndAllowWhileIdle failed: ${e.message}")
            }
        }

        if (!success) {
            try {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, null),
                    pendingIntent
                )
                success = true
                Log.d(TAG, "✅ system setAlarmClock noteId=${scheduledNote.id}")
            } catch (e: Exception) {
                Log.w(TAG, "system setAlarmClock failed: ${e.message}")
            }
        }

        if (!success) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
                success = true
                Log.d(TAG, "✅ system setExact noteId=${scheduledNote.id}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ system alarm all methods failed noteId=${scheduledNote.id}", e)
            }
        }

        return success
    }

    fun cancel(scheduledNoteId: Long) {
        val intent = Intent(context, SystemAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCodeFor(scheduledNoteId), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            pendingIntent.cancel()
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Cancelled system alarm for noteId=$scheduledNoteId")
        }
    }

    private fun createPendingIntent(scheduledNoteId: Long): PendingIntent {
        val intent = Intent(context, SystemAlarmReceiver::class.java).apply {
            putExtra(EXTRA_SYSTEM_ALARM_NOTE_ID, scheduledNoteId)
        }
        return PendingIntent.getBroadcast(
            context, requestCodeFor(scheduledNoteId), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun requestCodeFor(scheduledNoteId: Long): Int =
        (scheduledNoteId + SYSTEM_ALARM_REQUEST_CODE_OFFSET).toInt()

    companion object {
        private const val TAG = "SystemAlarmManager"
        private const val SYSTEM_ALARM_REQUEST_CODE_OFFSET = 1_000_000
        const val EXTRA_SYSTEM_ALARM_NOTE_ID = "extra_system_alarm_note_id"
    }
}
