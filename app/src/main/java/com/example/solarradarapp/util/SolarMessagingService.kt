package com.example.solarradarapp.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SolarMessagingService : FirebaseMessagingService() {

    /**
     * Called when FCM issues a new registration token (first install, token rotation, etc.).
     * Re-stores the fresh token so the Cloud Function can always reach this device.
     */
    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .update("mobileFcmTokens", FieldValue.arrayUnion(token))
    }

    /**
     * Called when a message arrives while the app is in the foreground.
     * Android suppresses the system notification in that case, so we show it ourselves.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: return          // nothing to show

        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""

        NotificationHelper.showNotification(this, title, body)
    }
}
