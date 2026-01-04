package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.UserRepository
import es.etg.lectoguard.domain.model.UserProfile

class SearchUsersUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(query: String, limit: Int = 20, excludeUid: String? = null): List<UserProfile> {
        return repository.searchUsers(query, limit, excludeUid)
    }
}

