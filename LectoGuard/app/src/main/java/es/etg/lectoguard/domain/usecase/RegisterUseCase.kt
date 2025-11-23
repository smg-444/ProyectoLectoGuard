package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.UserRepository
import es.etg.lectoguard.data.local.UserEntity
 
class RegisterUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(user: UserEntity) = repository.register(user)
} 