package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.RatingRepository
import es.etg.lectoguard.domain.model.Review

class SaveReviewUseCase(private val repository: RatingRepository) {
    suspend operator fun invoke(review: Review): String? {
        return repository.saveReview(review)
    }
}

