package com.penguenlabs.pushnote.features.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.penguenlabs.pushnote.data.local.entity.ScheduledNoteEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

class ScheduleAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(scheduledNote: ScheduledNoteEntity): Boolean {
        val now = System.currentTimeMillis()
        val triggerTime = calculateNextTriggerTime(scheduledNote)
        val secondsUntil = (triggerTime - now) / 1000
        Log.d(TAG, "=== Scheduling alarm ===")
        Log.d(TAG, "noteId=${scheduledNote.id} note='${scheduledNote.note}'")
        Log.d(TAG, "triggerTime=$triggerTime (in $secondsUntil seconds)")
        Log.d(TAG, "repeat=${scheduledNote.repeatMode} " +
                "hour=${scheduledNote.hour} minute=${scheduledNote.minute}")

        val pendingIntent = createPendingIntent(scheduledNote.id)
        var success = false

        // Tier 1: setExactAndAllowWhileIdle (requires SCHEDULE_EXACT_ALARM on API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                )
                success = true
                Log.d(TAG, "✅ Alarm set via setExactAndAllowWhileIdle for noteId=${scheduledNote.id}")
            } catch (e: SecurityException) {
                Log.w(TAG, "setExactAndAllowWhileIdle denied: ${e.message}")
            } catch (e: Exception) {
                Log.w(TAG, "setExactAndAllowWhileIdle failed: ${e.message}")
            }
        }

        // Tier 2: setAlarmClock with showIntent (bypasses Doze, no permission needed)
        if (!success) {
            try {
                val showIntent = PendingIntent.getActivity(
                    context, 0,
                    Intent(context, Class.forName("com.penguenlabs.pushnote.features.MainActivity")),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, showIntent),
                    pendingIntent
                )
                success = true
                Log.d(TAG, "✅ Alarm set via setAlarmClock for noteId=${scheduledNote.id}")
            } catch (e: Exception) {
                Log.w(TAG, "setAlarmClock failed: ${e.message}")
            }
        }

        // Tier 3: setExact (older API fallback)
        if (!success) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
                success = true
                Log.d(TAG, "✅ Alarm set via setExact for noteId=${scheduledNote.id}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ All alarm methods failed for noteId=${scheduledNote.id}", e)
            }
        }

        return success
    }

    fun cancel(scheduledNoteId: Long) {
        val intent = Intent(context, ScheduleBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, scheduledNoteId.toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            pendingIntent.cancel()
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun createPendingIntent(scheduledNoteId: Long): PendingIntent {
        val intent = Intent(context, ScheduleBroadcastReceiver::class.java).apply {
            putExtra(EXTRA_SCHEDULED_NOTE_ID, scheduledNoteId)
        }
        return PendingIntent.getBroadcast(
            context, scheduledNoteId.toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        private const val TAG = "ScheduleAlarmManager"
        const val EXTRA_SCHEDULED_NOTE_ID = "extra_scheduled_note_id"

        fun calculateNextTriggerTime(entity: ScheduledNoteEntity): Long {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance()

            if (entity.year != null && entity.month != null && entity.day != null) {
                target.set(Calendar.YEAR, entity.year)
                target.set(Calendar.MONTH, entity.month)
                target.set(Calendar.DAY_OF_MONTH, entity.day)
                target.set(Calendar.HOUR_OF_DAY, entity.hour)
                target.set(Calendar.MINUTE, entity.minute)
                target.set(Calendar.SECOND, 0)
                target.set(Calendar.MILLISECOND, 0)
                if (target.timeInMillis <= now.timeInMillis && entity.repeatMode == "NONE") {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                return target.timeInMillis
            }

            target.set(Calendar.HOUR_OF_DAY, entity.hour)
            target.set(Calendar.MINUTE, entity.minute)
            target.set(Calendar.SECOND, 0)
            target.set(Calendar.MILLISECOND, 0)

            when (entity.repeatMode) {
                "NONE" -> { if (target.timeInMillis <= now.timeInMillis) target.add(Calendar.DAY_OF_YEAR, 1) }
                "DAILY" -> { if (target.timeInMillis <= now.timeInMillis) target.add(Calendar.DAY_OF_YEAR, 1) }
                "WEEKLY" -> { if (target.timeInMillis <= now.timeInMillis) target.add(Calendar.DAY_OF_YEAR, 7) }
                "MONTHLY" -> { if (target.timeInMillis <= now.timeInMillis) target.add(Calendar.MONTH, 1) }
            }

            return target.timeInMillis
        }
    }
}
