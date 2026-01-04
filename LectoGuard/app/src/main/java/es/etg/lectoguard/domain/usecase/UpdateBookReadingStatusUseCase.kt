package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.BookRepository
import es.etg.lectoguard.domain.model.ReadingStatus

class UpdateBookReadingStatusUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(userId: Int, bookId: Int, status: ReadingStatus): Boolean =
        repository.updateReadingStatus(userId, bookId, status)
}

