package com.example.solarradarapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.solarradarapp.MainActivity
import com.example.solarradarapp.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object NotificationHelper {

    const val CHANNEL_ID = "solar_radar_jobs"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Job Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when a new job is assigned to you"
            enableVibration(true)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showNotification(context: Context, title: String, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = NotificationManagerCompat.from(context)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    /** Call this after the user authenticates to store the device token in Firestore. */
    fun registerToken(uid: String) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .update("mobileFcmTokens", FieldValue.arrayUnion(token))
        }
    }
}
