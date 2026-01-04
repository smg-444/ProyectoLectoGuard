package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ReadingListRepository

class UpdateListOrderUseCase(private val repository: ReadingListRepository) {
    suspend operator fun invoke(listId: String, bookIds: List<Int>, syncToFirestore: Boolean = true): Boolean =
        repository.updateListOrder(listId, bookIds, syncToFirestore)
}

