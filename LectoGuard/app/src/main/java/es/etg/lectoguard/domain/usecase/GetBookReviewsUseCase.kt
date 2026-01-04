package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.RatingRepository
import es.etg.lectoguard.domain.model.Review

class GetBookReviewsUseCase(private val repository: RatingRepository) {
    suspend operator fun invoke(bookId: Int, limit: Int = 50): List<Review> {
        return repository.getBookReviews(bookId, limit)
    }
}

