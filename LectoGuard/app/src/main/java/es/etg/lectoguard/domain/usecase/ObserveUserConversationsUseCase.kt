package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ChatRepository
import es.etg.lectoguard.domain.model.Conversation
import kotlinx.coroutines.flow.Flow

class ObserveUserConversationsUseCase(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(userId: String): Flow<List<Conversation>> =
        chatRepository.observeUserConversations(userId)
}

