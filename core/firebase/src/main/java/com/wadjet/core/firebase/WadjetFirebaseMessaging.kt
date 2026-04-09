package com.wadjet.core.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class WadjetFirebaseMessaging : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        Timber.d("FCM token refreshed: %s", token.take(10))
        // Token is sent to backend when user authenticates
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.d("FCM message received: %s", message.data)

        val type = message.data["type"] ?: "general"
        val title = message.notification?.title ?: message.data["title"] ?: "Wadjet"
        val body = message.notification?.body ?: message.data["body"] ?: return

        showNotification(type, title, body, message.data)
    }

    private fun showNotification(
        type: String,
        title: String,
        body: String,
        data: Map<String, String>,
    ) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", type)
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            type.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: replace with Wadjet icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(type.hashCode(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Updates about new stories, landmarks, and tips"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "wadjet_updates"
        const val CHANNEL_NAME = "Wadjet Updates"
    }
}
