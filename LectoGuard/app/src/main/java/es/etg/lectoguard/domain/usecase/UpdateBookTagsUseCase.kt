package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.BookRepository

class UpdateBookTagsUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(userId: Int, bookId: Int, tags: List<String>): Boolean =
        repository.updateBookTags(userId, bookId, tags)
}

