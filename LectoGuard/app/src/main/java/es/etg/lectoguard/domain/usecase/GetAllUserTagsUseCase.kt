package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.BookRepository

class GetAllUserTagsUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(userId: Int): Set<String> =
        repository.getAllUserTags(userId)
}

