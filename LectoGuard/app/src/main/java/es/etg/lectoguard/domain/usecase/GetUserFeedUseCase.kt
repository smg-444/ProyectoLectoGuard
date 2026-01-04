package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.FeedRepository
import es.etg.lectoguard.domain.model.FeedItem

class GetUserFeedUseCase(
    private val feedRepository: FeedRepository
) {
    suspend operator fun invoke(userId: String, limit: Int = 50): List<FeedItem> {
        return feedRepository.getUserFeed(userId, limit)
    }
}

