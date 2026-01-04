package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ReadingListRepository
import es.etg.lectoguard.domain.model.ReadingList

class GetPublicReadingListsUseCase(private val repository: ReadingListRepository) {
    suspend operator fun invoke(limit: Int = 50): List<ReadingList> =
        repository.getPublicReadingLists(limit)
}

