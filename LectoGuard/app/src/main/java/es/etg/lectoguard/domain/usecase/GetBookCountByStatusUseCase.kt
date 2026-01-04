package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.BookRepository
import es.etg.lectoguard.domain.model.ReadingStatus

class GetBookCountByStatusUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(userId: Int, status: ReadingStatus): Int =
        repository.getBookCountByStatus(userId, status)
}

