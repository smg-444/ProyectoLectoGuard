package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ChatRepository

class GetOrCreateConversationUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(userId1: String, userId2: String): String? =
        chatRepository.getOrCreateConversation(userId1, userId2)
}

