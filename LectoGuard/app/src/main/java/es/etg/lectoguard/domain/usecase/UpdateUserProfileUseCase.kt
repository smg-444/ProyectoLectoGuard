package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.UserRepository
import es.etg.lectoguard.domain.model.UserProfile

class UpdateUserProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(profile: UserProfile) = repository.updateUserProfile(profile)
}


