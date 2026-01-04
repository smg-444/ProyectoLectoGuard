package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.UserRepository

class UpdateProfileBioUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(uid: String, bio: String): Boolean {
        return repository.updateProfileBio(uid, bio)
    }
}

