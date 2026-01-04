package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ReadingListRepository

class IsFollowingListUseCase(private val repository: ReadingListRepository) {
    suspend operator fun invoke(listId: String, userId: String): Boolean =
        repository.isFollowingList(listId, userId)
}

