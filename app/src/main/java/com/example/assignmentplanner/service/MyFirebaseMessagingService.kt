package com.example.assignmentplanner.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.assignmentplanner.DashboardActivity
import com.example.assignmentplanner.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Service to handle incoming Firebase Cloud Messaging (FCM) notifications.
 * Processes data and notification payloads in the background or foreground.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when a message is received.
     * @param message The remote message containing the push payload.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM", "Message received from: ${message.from}")

        // Handle data payload
        if (message.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${message.data}")
        }

        // Handle notification payload
        message.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            sendNotification(it.title ?: "Assignment Planner", it.body ?: "New notification")
        }
    }

    /**
     * Called whenever a new FCM token is generated for the device.
     * @param token The new FCM token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")

        // Register the FCM token to the user's profile in the database.
        sendRegistrationToServer(token)
    }

    /**
     * Syncs the latest push token to the user's Firestore document.
     * This allows cloud functions to target this specific user/device.
     */
    private fun sendRegistrationToServer(token: String?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null && token != null) {
            val firestore = FirebaseFirestore.getInstance()
            val tokenData = hashMapOf("fcmToken" to token)

            firestore.collection("users").document(uid)
                .set(tokenData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("FCM", "FCM Token registered successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Error registering FCM Token", e)
                }
        }
    }

    /**
     * Constructs and displays a standard Android push notification.
     * @param title The notification title.
     * @param messageBody The primary text content.
     */
    private fun sendNotification(title: String, messageBody: String) {
        Log.i("FCM", "Building and showing local notification: $title")
        val intent = Intent(this, DashboardActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "planner_notification_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Assignment Planner Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
