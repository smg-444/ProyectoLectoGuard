package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.FeedRepository
import es.etg.lectoguard.domain.model.FeedItem

class CreateFeedItemUseCase(
    private val feedRepository: FeedRepository
) {
    suspend operator fun invoke(
        actorUserId: String,
        actorUserName: String,
        actorAvatarUrl: String?,
        feedItem: FeedItem
    ): Boolean {
        return feedRepository.createFeedItemForFollowers(
            actorUserId,
            actorUserName,
            actorAvatarUrl,
            feedItem
        )
    }
}

