package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.RatingRepository

class ToggleLikeReviewUseCase(private val repository: RatingRepository) {
    suspend operator fun invoke(reviewId: String, userId: String, isLiked: Boolean): Boolean {
        return repository.toggleLikeReview(reviewId, userId, isLiked)
    }
}

