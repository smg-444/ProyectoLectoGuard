package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ReadingListRepository
import es.etg.lectoguard.domain.model.ReadingList
import kotlinx.coroutines.flow.Flow

class GetReadingListByIdUseCase(private val repository: ReadingListRepository) {
    operator fun invoke(listId: String): Flow<ReadingList?> =
        repository.getReadingListById(listId)
}

