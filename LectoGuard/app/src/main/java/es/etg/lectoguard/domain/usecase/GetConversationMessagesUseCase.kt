package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ChatRepository
import es.etg.lectoguard.domain.model.Message

class GetConversationMessagesUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(conversationId: String, limit: Int = 50): List<Message> =
        chatRepository.getConversationMessages(conversationId, limit)
}

