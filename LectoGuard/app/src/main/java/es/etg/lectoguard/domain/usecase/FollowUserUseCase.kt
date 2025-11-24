package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.UserRepository

class FollowUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(selfUid: String, targetUid: String) =
        repository.followUser(selfUid, targetUid)
}


