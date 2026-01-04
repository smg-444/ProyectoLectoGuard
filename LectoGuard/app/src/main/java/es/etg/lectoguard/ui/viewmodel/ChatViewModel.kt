package es.etg.lectoguard.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.etg.lectoguard.domain.model.Conversation
import es.etg.lectoguard.domain.model.Message
import es.etg.lectoguard.domain.usecase.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getOrCreateConversationUseCase: GetOrCreateConversationUseCase,
    private val getUserConversationsUseCase: GetUserConversationsUseCase,
    private val observeUserConversationsUseCase: ObserveUserConversationsUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getConversationMessagesUseCase: GetConversationMessagesUseCase,
    private val observeConversationMessagesUseCase: ObserveConversationMessagesUseCase,
    private val markMessagesAsReadUseCase: MarkMessagesAsReadUseCase
) : ViewModel() {
    
    val conversations = MutableLiveData<List<Conversation>>()
    val messages = MutableLiveData<List<Message>>()
    val sendMessageResult = MutableLiveData<String?>() // conversationId o null si falla
    val conversationId = MutableLiveData<String?>()
    val isLoading = MutableLiveData<Boolean>()
    
    fun getOrCreateConversation(userId1: String, userId2: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val id = getOrCreateConversationUseCase(userId1, userId2)
            conversationId.postValue(id)
            isLoading.postValue(false)
        }
    }
    
    fun loadUserConversations(userId: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val convs = getUserConversationsUseCase(userId)
            conversations.postValue(convs)
            isLoading.postValue(false)
        }
    }
    
    /**
     * Inicia el listener en tiempo real para las conversaciones del usuario
     */
    fun startObservingConversations(userId: String) {
        observeUserConversationsUseCase(userId)
            .onEach { conversationsList ->
                conversations.postValue(conversationsList)
            }
            .launchIn(viewModelScope)
    }
    
    fun sendMessage(conversationId: String, message: Message) {
        viewModelScope.launch {
            val messageId = sendMessageUseCase(conversationId, message)
            sendMessageResult.postValue(messageId)
            // Los mensajes se actualizarán automáticamente con el listener en tiempo real
        }
    }
    
    fun loadMessages(conversationId: String, limit: Int = 50) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val msgs = getConversationMessagesUseCase(conversationId, limit)
            messages.postValue(msgs)
            isLoading.postValue(false)
        }
    }
    
    /**
     * Inicia el listener en tiempo real para los mensajes de una conversación
     */
    fun startObservingMessages(conversationId: String, limit: Int = 50) {
        observeConversationMessagesUseCase(conversationId, limit)
            .onEach { messagesList ->
                messages.postValue(messagesList)
            }
            .launchIn(viewModelScope)
    }
    
    fun markAsRead(conversationId: String, userId: String) {
        viewModelScope.launch {
            markMessagesAsReadUseCase(conversationId, userId)
            // Las conversaciones se actualizarán automáticamente con el listener
        }
    }
}

