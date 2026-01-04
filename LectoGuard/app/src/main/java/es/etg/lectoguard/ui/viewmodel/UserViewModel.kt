package es.etg.lectoguard.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.etg.lectoguard.data.local.UserEntity
import android.net.Uri
import es.etg.lectoguard.domain.model.UserProfile
import es.etg.lectoguard.domain.usecase.LoginUseCase
import es.etg.lectoguard.domain.usecase.RegisterUseCase
import es.etg.lectoguard.domain.usecase.IsFollowingUseCase
import es.etg.lectoguard.domain.usecase.GetFollowCountsUseCase
import es.etg.lectoguard.domain.usecase.FollowUserUseCase
import es.etg.lectoguard.domain.usecase.UnfollowUserUseCase
import es.etg.lectoguard.domain.usecase.UpdateUserProfileUseCase
import es.etg.lectoguard.domain.usecase.UpdateProfileWithAvatarUseCase
import es.etg.lectoguard.domain.usecase.UpdateProfileBioUseCase
import es.etg.lectoguard.domain.usecase.SearchUsersUseCase
import es.etg.lectoguard.domain.usecase.GetAllUsersUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val isFollowingUseCase: IsFollowingUseCase,
    private val getFollowCountsUseCase: GetFollowCountsUseCase,
    private val followUserUseCase: FollowUserUseCase,
    private val unfollowUserUseCase: UnfollowUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val updateProfileWithAvatarUseCase: UpdateProfileWithAvatarUseCase,
    private val updateProfileBioUseCase: UpdateProfileBioUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val userRepository: es.etg.lectoguard.data.repository.UserRepository
) : ViewModel() {

    val user = MutableLiveData<UserEntity?>()
    val loginResult = MutableLiveData<Boolean>()
    val registerResult = MutableLiveData<Boolean>()
    val isFollowing = MutableLiveData<Boolean?>()
    val followersCount = MutableLiveData<Int?>()
    val followingCount = MutableLiveData<Int?>()
    val updateProfileResult = MutableLiveData<Boolean>()
    val currentProfile = MutableLiveData<UserProfile?>()
    val searchResults = MutableLiveData<List<UserProfile>>()
    val allUsers = MutableLiveData<List<UserProfile>>()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = loginUseCase(email, password)
            user.postValue(result)
            loginResult.postValue(result != null)
        }
    }

    fun register(userEntity: UserEntity) {
        viewModelScope.launch {
            val id = registerUseCase(userEntity)
            registerResult.postValue(id > 0)
        }
    }

    fun loadFollowState(selfUid: String, targetUid: String) {
        viewModelScope.launch {
            isFollowing.postValue(isFollowingUseCase(selfUid, targetUid))
            val counts = getFollowCountsUseCase(targetUid)
            followersCount.postValue(counts.followers)
            followingCount.postValue(counts.following)
        }
    }

    fun follow(selfUid: String, targetUid: String, targetUserName: String? = null) {
        viewModelScope.launch {
            followUserUseCase(selfUid, targetUid)
            isFollowing.postValue(true)
            
            // Crear feed item cuando se sigue a un usuario
            val userName = targetUserName ?: currentProfile.value?.displayName ?: "Usuario"
            android.util.Log.d("UserViewModel", "Creando feed item para follow: selfUid=$selfUid, targetUid=$targetUid, targetUserName=$userName")
            es.etg.lectoguard.data.repository.FeedHelper.createFollowFeedItem(
                selfUid,
                targetUid,
                userName,
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
            )
            
            // Esperar un poco para que Firestore procese el cambio
            kotlinx.coroutines.delay(300)
            val counts = getFollowCountsUseCase(targetUid)
            android.util.Log.d("UserViewModel", "Conteos después de seguir: seguidores=${counts.followers}, siguiendo=${counts.following}")
            followersCount.postValue(counts.followers)
            followingCount.postValue(counts.following)
        }
    }

    fun unfollow(selfUid: String, targetUid: String) {
        viewModelScope.launch {
            unfollowUserUseCase(selfUid, targetUid)
            isFollowing.postValue(false)
            // Esperar un poco para que Firestore procese el cambio
            kotlinx.coroutines.delay(300)
            val counts = getFollowCountsUseCase(targetUid)
            android.util.Log.d("UserViewModel", "Conteos después de dejar de seguir: seguidores=${counts.followers}, siguiendo=${counts.following}")
            followersCount.postValue(counts.followers)
            followingCount.postValue(counts.following)
        }
    }

    fun updateProfile(profileUpdate: UserProfile) {
        viewModelScope.launch {
            updateUserProfileUseCase(profileUpdate)
            updateProfileResult.postValue(true)
        }
    }
    
    fun updateProfileWithAvatar(uid: String, imageUri: Uri, bio: String?) {
        viewModelScope.launch {
            val result = updateProfileWithAvatarUseCase(uid, imageUri, bio)
            updateProfileResult.postValue(result)
        }
    }
    
    fun updateProfileBio(uid: String, bio: String) {
        viewModelScope.launch {
            val result = updateProfileBioUseCase(uid, bio)
            updateProfileResult.postValue(result)
        }
    }
    
    fun loadProfile(uid: String) {
        viewModelScope.launch {
            val profile = userRepository.getRemoteProfile(uid)
            currentProfile.postValue(profile)
        }
    }
    
    fun searchUsers(query: String, excludeUid: String? = null) {
        viewModelScope.launch {
            val results = searchUsersUseCase(query, excludeUid = excludeUid)
            searchResults.postValue(results)
        }
    }
    
    fun loadAllUsers(excludeUid: String? = null) {
        viewModelScope.launch {
            val users = getAllUsersUseCase(excludeUid = excludeUid)
            allUsers.postValue(users)
        }
    }
} 