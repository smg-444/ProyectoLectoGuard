package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.RatingRepository

class DeleteReviewUseCase(private val repository: RatingRepository) {
    suspend operator fun invoke(reviewId: String, userId: String): Boolean =
        repository.deleteReview(reviewId, userId)
}

