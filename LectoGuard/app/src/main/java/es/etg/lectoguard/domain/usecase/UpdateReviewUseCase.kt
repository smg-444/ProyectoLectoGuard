package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.RatingRepository

class UpdateReviewUseCase(private val repository: RatingRepository) {
    suspend operator fun invoke(reviewId: String, userId: String, newText: String, newRating: Int): Boolean =
        repository.updateReview(reviewId, userId, newText, newRating)
}

