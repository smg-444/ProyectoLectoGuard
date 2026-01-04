package es.etg.lectoguard.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageService(
    private val storage: FirebaseStorage
) {
    suspend fun uploadAvatar(uid: String, imageUri: Uri): String? {
        return try {
            val storageRef = storage.reference
            val avatarRef = storageRef.child("avatars/$uid.jpg")
            
            android.util.Log.d("StorageService", "Subiendo avatar para usuario: $uid")
            val uploadTask = avatarRef.putFile(imageUri).await()
            android.util.Log.d("StorageService", "Avatar subido exitosamente")
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            android.util.Log.d("StorageService", "URL de descarga obtenida: $downloadUrl")
            downloadUrl.toString()
        } catch (e: Exception) {
            android.util.Log.e("StorageService", "Error subiendo avatar: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }
    
    suspend fun deleteAvatar(uid: String): Boolean {
        return try {
            val storageRef = storage.reference
            val avatarRef = storageRef.child("avatars/$uid.jpg")
            avatarRef.delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}

