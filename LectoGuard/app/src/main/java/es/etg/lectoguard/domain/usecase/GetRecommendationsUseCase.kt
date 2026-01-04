package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.RecommendationRepository
import es.etg.lectoguard.domain.model.BookGenre
import es.etg.lectoguard.domain.model.BookRecommendation

class GetRecommendationsUseCase(private val repository: RecommendationRepository) {
    suspend operator fun invoke(
        userId: Int,
        firebaseUid: String,
        limit: Int = 10,
        genreFilter: BookGenre? = null,
        onlyFromFollowing: Boolean = false
    ): List<BookRecommendation> =
        repository.getRecommendations(userId, firebaseUid, limit, genreFilter, onlyFromFollowing)
}

