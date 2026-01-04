package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ReadingListRepository
import es.etg.lectoguard.domain.model.ReadingList
import kotlinx.coroutines.flow.Flow

class GetUserReadingListsUseCase(private val repository: ReadingListRepository) {
    operator fun invoke(userId: String): Flow<List<ReadingList>> =
        repository.getUserReadingLists(userId)
}

