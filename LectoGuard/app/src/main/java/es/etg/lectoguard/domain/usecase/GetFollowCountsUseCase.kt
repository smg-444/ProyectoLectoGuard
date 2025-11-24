package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.UserRepository

class GetFollowCountsUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(uid: String) = repository.getFollowCounts(uid)
}


