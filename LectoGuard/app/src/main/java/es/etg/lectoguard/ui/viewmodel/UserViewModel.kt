package es.etg.lectoguard.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.etg.lectoguard.data.local.UserEntity
import es.etg.lectoguard.domain.usecase.LoginUseCase
import es.etg.lectoguard.domain.usecase.RegisterUseCase
import es.etg.lectoguard.domain.usecase.IsFollowingUseCase
import es.etg.lectoguard.domain.usecase.GetFollowCountsUseCase
import es.etg.lectoguard.domain.usecase.FollowUserUseCase
import es.etg.lectoguard.domain.usecase.UnfollowUserUseCase
import es.etg.lectoguard.domain.usecase.UpdateUserProfileUseCase
import kotlinx.coroutines.launch

class UserViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val isFollowingUseCase: IsFollowingUseCase? = null,
    private val getFollowCountsUseCase: GetFollowCountsUseCase? = null,
    private val followUserUseCase: FollowUserUseCase? = null,
    private val unfollowUserUseCase: UnfollowUserUseCase? = null,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase? = null
) : ViewModel() {

    val user = MutableLiveData<UserEntity?>()
    val loginResult = MutableLiveData<Boolean>()
    val registerResult = MutableLiveData<Boolean>()
    val isFollowing = MutableLiveData<Boolean?>()
    val followersCount = MutableLiveData<Int?>()
    val followingCount = MutableLiveData<Int?>()

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
        val isFollowingUseCase = this.isFollowingUseCase ?: return
        val getFollowCountsUseCase = this.getFollowCountsUseCase ?: return
        viewModelScope.launch {
            isFollowing.postValue(isFollowingUseCase(selfUid, targetUid))
            val counts = getFollowCountsUseCase(targetUid)
            followersCount.postValue(counts.followers)
            followingCount.postValue(counts.following)
        }
    }

    fun follow(selfUid: String, targetUid: String) {
        val followUserUseCase = this.followUserUseCase ?: return
        val getFollowCountsUseCase = this.getFollowCountsUseCase ?: return
        viewModelScope.launch {
            followUserUseCase(selfUid, targetUid)
            isFollowing.postValue(true)
            val counts = getFollowCountsUseCase(targetUid)
            followersCount.postValue(counts.followers)
            followingCount.postValue(counts.following)
        }
    }

    fun unfollow(selfUid: String, targetUid: String) {
        val unfollowUserUseCase = this.unfollowUserUseCase ?: return
        val getFollowCountsUseCase = this.getFollowCountsUseCase ?: return
        viewModelScope.launch {
            unfollowUserUseCase(selfUid, targetUid)
            isFollowing.postValue(false)
            val counts = getFollowCountsUseCase(targetUid)
            followersCount.postValue(counts.followers)
            followingCount.postValue(counts.following)
        }
    }

    fun updateProfile(profileUpdate: es.etg.lectoguard.domain.model.UserProfile) {
        val updateUserProfileUseCase = this.updateUserProfileUseCase ?: return
        viewModelScope.launch {
            updateUserProfileUseCase(profileUpdate)
        }
    }
} 