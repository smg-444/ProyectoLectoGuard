package es.etg.lectoguard.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import es.etg.lectoguard.data.remote.ChatService
import es.etg.lectoguard.domain.model.Conversation
import es.etg.lectoguard.domain.model.Message
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val firestore: FirebaseFirestore
) {
    private val chatService by lazy { ChatService(firestore) }
    
    suspend fun getOrCreateConversation(userId1: String, userId2: String): String? =
        chatService.getOrCreateConversation(userId1, userId2)
    
    suspend fun getUserConversations(userId: String): List<Conversation> =
        chatService.getUserConversations(userId)
    
    fun observeUserConversations(userId: String): Flow<List<Conversation>> =
        chatService.observeUserConversations(userId)
    
    suspend fun sendMessage(conversationId: String, message: Message): String? =
        chatService.sendMessage(conversationId, message)
    
    suspend fun getConversationMessages(conversationId: String, limit: Int = 50): List<Message> =
        chatService.getConversationMessages(conversationId, limit)
    
    fun observeConversationMessages(conversationId: String, limit: Int = 50): Flow<List<Message>> =
        chatService.observeConversationMessages(conversationId, limit)
    
    suspend fun markMessagesAsRead(conversationId: String, userId: String): Boolean =
        chatService.markMessagesAsRead(conversationId, userId)
}

