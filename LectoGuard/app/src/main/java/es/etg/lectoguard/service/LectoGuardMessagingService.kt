package es.etg.lectoguard.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import es.etg.lectoguard.R
import es.etg.lectoguard.ui.view.ChatActivity

class LectoGuardMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("LectoGuardMessaging", "Nuevo token FCM: $token")
        // Guardar el token en Firestore
        saveTokenToFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("LectoGuardMessaging", "Mensaje recibido de: ${remoteMessage.from}")

        // Verificar si el mensaje contiene datos
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("LectoGuardMessaging", "Datos del mensaje: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Verificar si el mensaje contiene notificación
        remoteMessage.notification?.let {
            Log.d("LectoGuardMessaging", "Notificación recibida: ${it.title} - ${it.body}")
            showNotification(it.title ?: "Nuevo mensaje", it.body ?: "", remoteMessage.data)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        when (type) {
            "chat_message" -> {
                val conversationId = data["conversationId"]
                val senderName = data["senderName"] ?: "Usuario"
                val messageText = data["message"] ?: ""
                
                // Mostrar notificación
                showNotification(
                    senderName,
                    messageText,
                    data
                )
            }
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Crear canal de notificación para Android 8.0+
        val channelId = "lectoguard_chat_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de mensajes de chat"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir ChatActivity cuando se toca la notificación
        val conversationId = data["conversationId"]
        val otherParticipantId = data["senderId"] // El remitente es el otro participante
        val intent = Intent(this, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (conversationId != null) {
                putExtra("conversationId", conversationId)
            }
            if (otherParticipantId != null) {
                putExtra("otherParticipantId", otherParticipantId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir notificación
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Icono de notificación personalizado
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Mostrar notificación
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
    }

    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.w("LectoGuardMessaging", "Usuario no autenticado, no se puede guardar token")
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("LectoGuardMessaging", "Token FCM guardado en Firestore para usuario $userId")
            }
            .addOnFailureListener { e ->
                Log.e("LectoGuardMessaging", "Error guardando token FCM: ${e.message}", e)
            }
    }
}

