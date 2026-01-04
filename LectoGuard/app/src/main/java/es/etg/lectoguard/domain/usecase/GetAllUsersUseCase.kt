package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.UserRepository
import es.etg.lectoguard.domain.model.UserProfile

class GetAllUsersUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(limit: Int = 50, excludeUid: String? = null): List<UserProfile> {
        return repository.getAllUsers(limit, excludeUid)
    }
}

