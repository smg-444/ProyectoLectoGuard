package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.RatingRepository
import es.etg.lectoguard.domain.model.Rating

class GetUserRatingUseCase(private val repository: RatingRepository) {
    suspend operator fun invoke(bookId: Int, userId: String): Rating? {
        return repository.getUserRating(bookId, userId)
    }
}

