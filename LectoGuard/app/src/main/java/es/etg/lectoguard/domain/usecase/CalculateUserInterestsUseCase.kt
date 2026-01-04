package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.RecommendationRepository
import es.etg.lectoguard.domain.model.UserInterests

class CalculateUserInterestsUseCase(private val repository: RecommendationRepository) {
    suspend operator fun invoke(userId: Int, firebaseUid: String): UserInterests =
        repository.calculateUserInterests(userId, firebaseUid)
}

