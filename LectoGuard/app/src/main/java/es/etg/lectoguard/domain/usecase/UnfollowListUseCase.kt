package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ReadingListRepository

class UnfollowListUseCase(private val repository: ReadingListRepository) {
    suspend operator fun invoke(listId: String, userId: String): Boolean =
        repository.unfollowList(listId, userId)
}

