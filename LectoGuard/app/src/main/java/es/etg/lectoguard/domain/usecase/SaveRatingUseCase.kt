package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.RatingRepository
import es.etg.lectoguard.domain.model.Rating

class SaveRatingUseCase(private val repository: RatingRepository) {
    suspend operator fun invoke(rating: Rating): Boolean {
        return repository.saveRating(rating)
    }
}

