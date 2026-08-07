package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("nextfitness_prefs", Context.MODE_PRIVATE)
        val dailyReminderEnabled = prefs.getBoolean("daily_reminder_enabled", true)

        if (!dailyReminderEnabled) return

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastWorkoutDate = prefs.getString("last_workout_date", "")

        // If no workout has been logged today, trigger notification
        if (todayDate != lastWorkoutDate) {
            WorkoutNotificationHelper.showWorkoutReminderNotification(context)
        }
    }
}
