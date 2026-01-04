package es.etg.lectoguard.domain.usecase

import android.net.Uri
import es.etg.lectoguard.data.repository.UserRepository

class UpdateProfileWithAvatarUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(uid: String, imageUri: Uri, bio: String?): Boolean {
        return repository.updateProfileWithAvatar(uid, imageUri, bio)
    }
}

