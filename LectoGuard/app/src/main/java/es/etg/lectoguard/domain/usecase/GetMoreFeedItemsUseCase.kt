package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.FeedRepository
import es.etg.lectoguard.domain.model.FeedItem

class GetMoreFeedItemsUseCase(
    private val feedRepository: FeedRepository
) {
    suspend operator fun invoke(userId: String, lastTimestamp: Long, limit: Int = 20): List<FeedItem> {
        return feedRepository.getMoreFeedItems(userId, lastTimestamp, limit)
    }
}

