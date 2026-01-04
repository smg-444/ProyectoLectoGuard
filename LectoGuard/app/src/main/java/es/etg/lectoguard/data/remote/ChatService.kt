package es.etg.lectoguard.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import es.etg.lectoguard.domain.model.Conversation
import es.etg.lectoguard.domain.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import android.util.Log

class ChatService(
    private val firestore: FirebaseFirestore
) {
    private fun conversationsCollection() = firestore.collection("conversations")
    
    /**
     * Obtiene o crea una conversación entre dos usuarios
     */
    suspend fun getOrCreateConversation(userId1: String, userId2: String): String? {
        return try {
            // Buscar conversación existente
            val participants = listOf(userId1, userId2).sorted()
            // Usar userId1 (usuario actual) en la consulta para que las reglas de Firestore funcionen
            // La regla requiere que el usuario autenticado esté en el array participants
            val query = conversationsCollection()
                .whereArrayContains("participants", userId1)
                .get()
                .await()
            
            val existingConversation = query.documents.firstOrNull { doc ->
                val convParticipants = (doc.data?.get("participants") as? List<*>)?.mapNotNull { it as? String }
                convParticipants?.containsAll(participants) == true && convParticipants.size == 2
            }
            
            if (existingConversation != null) {
                Log.d("ChatService", "Conversación existente encontrada: ${existingConversation.id}")
                existingConversation.id
            } else {
                // Crear nueva conversación
                val newConversation = hashMapOf(
                    "participants" to participants,
                    "lastMessage" to null,
                    "lastMessageTimestamp" to System.currentTimeMillis(),
                    "lastMessageSenderId" to null,
                    "unreadCount" to hashMapOf(
                        userId1 to 0,
                        userId2 to 0
                    ),
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
                val docRef = conversationsCollection().document()
                docRef.set(newConversation).await()
                Log.d("ChatService", "Nueva conversación creada: ${docRef.id}")
                docRef.id
            }
        } catch (e: Exception) {
            Log.e("ChatService", "Error obteniendo/creando conversación: ${e.message}", e)
            null
        }
    }
    
    /**
     * Obtiene todas las conversaciones de un usuario (consulta única)
     */
    suspend fun getUserConversations(userId: String): List<Conversation> {
        return try {
            val results = conversationsCollection()
                .whereArrayContains("participants", userId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            results.documents.mapNotNull { doc ->
                mapConversation(doc)
            }
        } catch (e: Exception) {
            Log.e("ChatService", "Error obteniendo conversaciones: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Escucha cambios en tiempo real de las conversaciones de un usuario
     */
    fun observeUserConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val listenerRegistration = conversationsCollection()
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatService", "Error en listener de conversaciones: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val conversations = snapshot.documents.mapNotNull { doc ->
                        mapConversation(doc)
                    }
                    trySend(conversations)
                }
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    /**
     * Mapea un DocumentSnapshot a Conversation
     */
    private fun mapConversation(doc: com.google.firebase.firestore.DocumentSnapshot): Conversation? {
        return try {
            val data = doc.data ?: return null
            val participants = (data["participants"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val unreadCountMap = (data["unreadCount"] as? Map<*, *>)?.mapNotNull { entry ->
                val key = entry.key as? String
                val value = (entry.value as? Number)?.toInt()
                if (key != null && value != null) key to value else null
            }?.toMap() ?: emptyMap()
            
            Conversation(
                id = doc.id,
                participants = participants,
                lastMessage = data["lastMessage"] as? String,
                lastMessageTimestamp = (data["lastMessageTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                lastMessageSenderId = data["lastMessageSenderId"] as? String,
                unreadCount = unreadCountMap,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e("ChatService", "Error mapeando conversación ${doc.id}: ${e.message}")
            null
        }
    }
    
    /**
     * Envía un mensaje a una conversación
     */
    suspend fun sendMessage(conversationId: String, message: Message): String? {
        return try {
            val messagesCollection = conversationsCollection()
                .document(conversationId)
                .collection("messages")
            
            val docRef = messagesCollection.document()
            val data = hashMapOf(
                "conversationId" to conversationId,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "senderAvatarUrl" to (message.senderAvatarUrl ?: ""),
                "content" to message.content,
                "timestamp" to message.timestamp,
                "readBy" to message.readBy
            )
            
            docRef.set(data).await()
            
            // Actualizar última información de la conversación
            val conversationRef = conversationsCollection().document(conversationId)
            val conversationDoc = conversationRef.get().await()
            val conversationData = conversationDoc.data ?: emptyMap()
            
            val participants = (conversationData["participants"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val otherParticipantId = participants.firstOrNull { it != message.senderId }
            
            val unreadCount = (conversationData["unreadCount"] as? Map<*, *>) ?: emptyMap<Any, Any>()
            val unreadCountMap = unreadCount.mapNotNull { entry ->
                val key = entry.key as? String
                val value = (entry.value as? Number)?.toInt() ?: 0
                if (key != null) key to value else null
            }.toMap().toMutableMap()
            
            // Incrementar contador de no leídos para el otro participante
            if (otherParticipantId != null) {
                unreadCountMap[otherParticipantId] = (unreadCountMap[otherParticipantId] ?: 0) + 1
            }
            
            conversationRef.update(
                mapOf(
                    "lastMessage" to message.content,
                    "lastMessageTimestamp" to message.timestamp,
                    "lastMessageSenderId" to message.senderId,
                    "unreadCount" to unreadCountMap,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            
            // NOTA: Las notificaciones push se envían automáticamente mediante Cloud Functions
            // cuando se crea un mensaje en Firestore.
            
            Log.d("ChatService", "Mensaje enviado: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e("ChatService", "Error enviando mensaje: ${e.message}", e)
            null
        }
    }
    
    /**
     * Obtiene los mensajes de una conversación (consulta única)
     */
    suspend fun getConversationMessages(conversationId: String, limit: Int = 50): List<Message> {
        return try {
            val results = conversationsCollection()
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            results.documents.mapNotNull { doc ->
                mapMessage(doc, conversationId)
            }.reversed() // Invertir para mostrar los más antiguos primero
        } catch (e: Exception) {
            Log.e("ChatService", "Error obteniendo mensajes: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Escucha cambios en tiempo real de los mensajes de una conversación
     */
    fun observeConversationMessages(conversationId: String, limit: Int = 50): Flow<List<Message>> = callbackFlow {
        val listenerRegistration = conversationsCollection()
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatService", "Error en listener de mensajes: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        mapMessage(doc, conversationId)
                    }.reversed() // Invertir para mostrar los más antiguos primero
                    trySend(messages)
                }
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    /**
     * Mapea un DocumentSnapshot a Message
     */
    private fun mapMessage(doc: com.google.firebase.firestore.DocumentSnapshot, conversationId: String): Message? {
        return try {
            val data = doc.data ?: return null
            val readBy = (data["readBy"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            
            Message(
                id = doc.id,
                conversationId = data["conversationId"] as? String ?: conversationId,
                senderId = data["senderId"] as? String ?: "",
                senderName = data["senderName"] as? String ?: "",
                senderAvatarUrl = (data["senderAvatarUrl"] as? String)?.takeIf { it.isNotBlank() },
                content = data["content"] as? String ?: "",
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                readBy = readBy
            )
        } catch (e: Exception) {
            Log.e("ChatService", "Error mapeando mensaje ${doc.id}: ${e.message}")
            null
        }
    }
    
    /**
     * Marca los mensajes de una conversación como leídos por un usuario
     */
    suspend fun markMessagesAsRead(conversationId: String, userId: String): Boolean {
        return try {
            // Obtener todos los mensajes de la conversación que no han sido leídos por el usuario
            val allMessages = conversationsCollection()
                .document(conversationId)
                .collection("messages")
                .get()
                .await()
            
            // Filtrar mensajes no leídos por este usuario
            val unreadMessages = allMessages.documents.filter { doc ->
                val readBy = (doc.data?.get("readBy") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                val senderId = doc.data?.get("senderId") as? String
                // Solo mensajes enviados por otros que no han sido leídos
                val isNotRead = !readBy.contains(userId)
                val isFromOther = senderId != userId
                isNotRead && isFromOther
            }
            
            if (unreadMessages.isNotEmpty()) {
                // Actualizar cada mensaje para agregar userId a readBy
                val batch = firestore.batch()
                unreadMessages.forEach { doc ->
                    val readBy = (doc.data?.get("readBy") as? List<*>)?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()
                    if (!readBy.contains(userId)) {
                        readBy.add(userId)
                        batch.update(doc.reference, "readBy", readBy)
                    }
                }
                batch.commit().await()
            }
            
            // Actualizar contador de no leídos en la conversación
            val conversationRef = conversationsCollection().document(conversationId)
            val conversation = conversationRef.get().await()
            val unreadCount = (conversation.data?.get("unreadCount") as? Map<*, *>)?.mapNotNull { entry ->
                val key = entry.key as? String
                val value = (entry.value as? Number)?.toInt() ?: 0
                if (key != null) key to value else null
            }?.toMap()?.toMutableMap() ?: mutableMapOf()
            unreadCount[userId] = 0
            conversationRef.update("unreadCount", unreadCount, "updatedAt", System.currentTimeMillis()).await()
            
            Log.d("ChatService", "Mensajes marcados como leídos para usuario $userId")
            true
        } catch (e: Exception) {
            Log.e("ChatService", "Error marcando mensajes como leídos: ${e.message}", e)
            false
        }
    }
}

