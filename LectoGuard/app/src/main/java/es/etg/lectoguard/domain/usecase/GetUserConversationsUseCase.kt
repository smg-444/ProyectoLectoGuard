package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ChatRepository
import es.etg.lectoguard.domain.model.Conversation

class GetUserConversationsUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(userId: String): List<Conversation> =
        chatRepository.getUserConversations(userId)
}

