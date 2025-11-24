package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.UserRepository

class IsFollowingUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(selfUid: String, targetUid: String) =
        repository.isFollowing(selfUid, targetUid)
}


