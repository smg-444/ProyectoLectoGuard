package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.BookRepository
import es.etg.lectoguard.data.local.UserBookEntity

class SaveBookUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(
        userBook: UserBookEntity,
        firebaseUid: String? = null,
        bookTitle: String? = null,
        bookCoverUrl: String? = null
    ): Boolean = repository.saveBook(userBook, firebaseUid, bookTitle, bookCoverUrl)
} 