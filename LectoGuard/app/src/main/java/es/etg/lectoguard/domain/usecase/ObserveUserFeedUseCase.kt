package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.FeedRepository
import es.etg.lectoguard.domain.model.FeedItem
import kotlinx.coroutines.flow.Flow

class ObserveUserFeedUseCase(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(userId: String, limit: Int = 50): Flow<List<FeedItem>> {
        return feedRepository.observeUserFeed(userId, limit)
    }
}

