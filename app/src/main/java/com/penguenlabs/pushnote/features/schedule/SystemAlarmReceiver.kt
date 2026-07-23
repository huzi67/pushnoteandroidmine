package com.penguenlabs.pushnote.features.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SystemAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra(SystemAlarmManager.EXTRA_SYSTEM_ALARM_NOTE_ID, -1L)
        Log.d(TAG, "=== System alarm received noteId=$noteId ===")

        if (noteId <= 0) {
            Log.w(TAG, "Invalid noteId, aborting")
            return
        }

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "PushNote:SystemAlarm:$noteId"
        )
        wakeLock.acquire(60_000L)

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = alarmUri?.let { RingtoneManager.getRingtone(context, it) }

        if (ringtone != null) {
            ringtone.play()
            Log.d(TAG, "Playing system alarm ringtone for noteId=$noteId")
            // Stop after 15 seconds so it doesn't ring forever
            Handler(Looper.getMainLooper()).postDelayed({
                if (ringtone.isPlaying) {
                    ringtone.stop()
                    Log.d(TAG, "Stopped system alarm ringtone for noteId=$noteId")
                }
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            }, 15_000L)
        } else {
            Log.w(TAG, "No ringtone available for system alarm noteId=$noteId")
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }

    companion object {
        private const val TAG = "SystemAlarmReceiver"
    }
}
