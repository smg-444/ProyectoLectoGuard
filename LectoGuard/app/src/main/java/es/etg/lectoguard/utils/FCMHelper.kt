package es.etg.lectoguard.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Helper para gestionar tokens FCM
 */
object FCMHelper {
    private const val TAG = "FCMHelper"
    
    /**
     * Solicita el token FCM y lo guarda en Firestore
     * Debe llamarse después de que el usuario inicie sesión
     */
    fun requestAndSaveToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Error obteniendo token FCM", task.exception)
                return@addOnCompleteListener
            }
            
            val token = task.result
            Log.d(TAG, "Token FCM obtenido: $token")
            
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null && token != null) {
                saveTokenToFirestore(userId, token)
            } else {
                Log.w(TAG, "Usuario no autenticado o token nulo")
            }
        }
    }
    
    private fun saveTokenToFirestore(userId: String, token: String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "Token FCM guardado en Firestore para usuario $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error guardando token FCM: ${e.message}", e)
            }
    }
}

