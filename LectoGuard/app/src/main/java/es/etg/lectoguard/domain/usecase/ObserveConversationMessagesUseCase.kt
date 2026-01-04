package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ChatRepository
import es.etg.lectoguard.domain.model.Message
import kotlinx.coroutines.flow.Flow

class ObserveConversationMessagesUseCase(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(conversationId: String, limit: Int = 50): Flow<List<Message>> =
        chatRepository.observeConversationMessages(conversationId, limit)
}

