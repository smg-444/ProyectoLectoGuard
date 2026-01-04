package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.RatingRepository

class GetAverageRatingUseCase(private val repository: RatingRepository) {
    suspend operator fun invoke(bookId: Int): Double {
        return repository.getAverageRating(bookId)
    }
}

