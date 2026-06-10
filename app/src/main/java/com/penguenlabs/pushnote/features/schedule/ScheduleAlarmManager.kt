package com.penguenlabs.pushnote.features.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
        // Cancel any existing alarm for this ID first (prevents duplicate firing)
        cancel(scheduledNote.id)

        val now = System.currentTimeMillis()
        val triggerTime = calculateNextTriggerTime(scheduledNote)
        val secondsUntil = (triggerTime - now) / 1000
        Log.d(TAG, "=== Scheduling alarm ===")
        Log.d(TAG, "noteId=${scheduledNote.id} note='${scheduledNote.note}'")
        Log.d(TAG, "triggerTime=$triggerTime (in $secondsUntil seconds)")
        Log.d(TAG, "repeat=${scheduledNote.repeatMode} hour=${scheduledNote.hour} minute=${scheduledNote.minute}")

        val pendingIntent = createPendingIntent(scheduledNote.id)
        var success = false

        // Tier 1: setExactAndAllowWhileIdle (best for modern Android)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                )
                success = true
                Log.d(TAG, "✅ setExactAndAllowWhileIdle noteId=${scheduledNote.id}")
            } catch (e: SecurityException) {
                Log.w(TAG, "setExactAndAllowWhileIdle denied: ${e.message}")
            } catch (e: Exception) {
                Log.w(TAG, "setExactAndAllowWhileIdle failed: ${e.message}")
            }
        }

        // Tier 2: setAlarmClock (bypasses Doze, no permission needed)
        // IMPORTANT: showIntent must be null, otherwise Samsung loops the alarm
        if (!success) {
            try {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, null),
                    pendingIntent
                )
                success = true
                Log.d(TAG, "✅ setAlarmClock noteId=${scheduledNote.id}")
            } catch (e: Exception) {
                Log.w(TAG, "setAlarmClock failed: ${e.message}")
            }
        }

        // Tier 3: setExact / set (old API fallback)
        if (!success) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
                success = true
                Log.d(TAG, "✅ setExact noteId=${scheduledNote.id}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ All methods failed noteId=${scheduledNote.id}", e)
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
            Log.d(TAG, "Cancelled alarm for noteId=$scheduledNoteId")
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

            // Set common fields
            target.set(Calendar.HOUR_OF_DAY, entity.hour)
            target.set(Calendar.MINUTE, entity.minute)
            target.set(Calendar.SECOND, 0)
            target.set(Calendar.MILLISECOND, 0)

            when (entity.repeatMode) {
                "NONE" -> {
                    // Specific date or today at specified time
                    if (entity.year != null && entity.month != null && entity.day != null) {
                        target.set(Calendar.YEAR, entity.year)
                        target.set(Calendar.MONTH, entity.month)
                        target.set(Calendar.DAY_OF_MONTH, entity.day)
                    }
                    if (target.timeInMillis <= now.timeInMillis) {
                        target.add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                "DAILY" -> {
                    if (target.timeInMillis <= now.timeInMillis) {
                        target.add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                "WEEKLY" -> {
                    val targetDow = entity.dayOfWeek ?: (now.get(Calendar.DAY_OF_WEEK) - 1)
                    // Map: our 0=Monday to Calendar.SUNDAY=1, MONDAY=2...
                    val calendarDow = if (targetDow == 6) Calendar.SUNDAY else targetDow + 2
                    val currentDow = now.get(Calendar.DAY_OF_WEEK)
                    var daysUntil = calendarDow - currentDow
                    if (daysUntil < 0) daysUntil += 7
                    if (daysUntil == 0 && target.timeInMillis <= now.timeInMillis) daysUntil = 7
                    target.add(Calendar.DAY_OF_YEAR, daysUntil)
                }
                "MONTHLY" -> {
                    val targetDay = entity.day ?: now.get(Calendar.DAY_OF_MONTH)
                    target.set(Calendar.DAY_OF_MONTH, 1) // go to first day
                    target.set(Calendar.DAY_OF_MONTH, targetDay)
                    // If day overflowed into next month (e.g. Feb 31 -> Mar 3), use last day
                    if (target.get(Calendar.DAY_OF_MONTH) != targetDay.coerceAtMost(target.getActualMaximum(Calendar.DAY_OF_MONTH))) {
                        target.set(Calendar.DAY_OF_MONTH, target.getActualMaximum(Calendar.DAY_OF_MONTH))
                    }
                    if (target.timeInMillis <= now.timeInMillis) {
                        target.add(Calendar.MONTH, 1)
                        // Re-validate after adding a month
                        if (target.get(Calendar.DAY_OF_MONTH) < targetDay.coerceAtMost(target.getActualMaximum(Calendar.DAY_OF_MONTH))) {
                            target.set(Calendar.DAY_OF_MONTH, target.getActualMaximum(Calendar.DAY_OF_MONTH))
                        }
                    }
                }
            }

            // Safety: ensure trigger time is at least 5 seconds in the future
            val minTime = System.currentTimeMillis() + 5000
            if (target.timeInMillis < minTime) {
                val corrected = when (entity.repeatMode) {
                    "DAILY" -> { target.add(Calendar.DAY_OF_YEAR, 1); target }
                    "WEEKLY" -> { target.add(Calendar.DAY_OF_YEAR, 7); target }
                    "MONTHLY" -> { target.add(Calendar.MONTH, 1); target }
                    else -> { target.add(Calendar.DAY_OF_YEAR, 1); target }
                }
                Log.w(TAG, "Trigger time too close, corrected to ${corrected.timeInMillis}")
                return corrected.timeInMillis
            }

            return target.timeInMillis
        }
    }
}
