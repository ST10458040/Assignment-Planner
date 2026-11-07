package com.example.assignmentplanner.receiver

import android.R
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.assignmentplanner.AssignmentDetailsActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Assignment Deadline Approaching"
        val id = intent.getLongExtra("id", 0L)
        
        val contentIntent = Intent(context, AssignmentDetailsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("assignmentId", id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "draft_channel") // reuse channel or create a new one
            .setSmallIcon(R.drawable.ic_dialog_alert)
            .setContentTitle("Deadline Reminder")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id.toInt() + 1000, builder.build())
    }
}
