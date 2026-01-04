package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.local.UserBookEntity
import es.etg.lectoguard.data.repository.BookRepository

class GetUserBookUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(userId: Int, bookId: Int): UserBookEntity? =
        repository.getUserBook(userId, bookId)
}

