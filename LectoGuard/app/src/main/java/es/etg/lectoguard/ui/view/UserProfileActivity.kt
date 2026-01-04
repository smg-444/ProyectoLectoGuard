package es.etg.lectoguard.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.commit
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivityUserProfileBinding
import es.etg.lectoguard.data.repository.UserRepository
import es.etg.lectoguard.ui.viewmodel.UserViewModel
import es.etg.lectoguard.utils.PrefsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class UserProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private val userViewModel: UserViewModel by viewModels()
    
    @Inject
    lateinit var userRepository: UserRepository
    
    private var targetUid: String? = null
    private var selfUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        targetUid = intent.getStringExtra("targetUid")
        if (targetUid == null) {
            Toast.makeText(this, "Error: Usuario no especificado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        selfUid = PrefsHelper.getFirebaseUid(this) ?: FirebaseAuth.getInstance().currentUser?.uid

        // Si es nuestro propio perfil, redirigir a ProfileActivity
        if (selfUid == targetUid) {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
            return
        }

        loadFollowState()

        // Observar cambios en el perfil
        var profileLoaded = false
        userViewModel.currentProfile.observe(this) { profile ->
            if (profile != null) {
                profileLoaded = true
                updateProfileUI(profile)
            } else if (profileLoaded) {
                // Solo mostrar error si ya habíamos cargado un perfil antes y ahora es null
                // Esto evita mostrar el error al inicio cuando el perfil aún se está cargando
                Toast.makeText(this@UserProfileActivity, "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Cargar el perfil inicial usando el ViewModel
        if (targetUid != null) {
            userViewModel.loadProfile(targetUid!!)
        }

        // Observar estado de seguimiento
        userViewModel.isFollowing.observe(this) { isFollowing ->
            updateFollowButton(isFollowing ?: false)
        }

        // Observar conteos
        userViewModel.followersCount.observe(this) { count ->
            binding.tvFollowersCount.text = "${count ?: 0}"
        }
        userViewModel.followingCount.observe(this) { count ->
            binding.tvFollowingCount.text = "${count ?: 0}"
        }
        
        // Observar cambios después de seguir/dejar de seguir para actualizar conteos
        userViewModel.isFollowing.observe(this) { isFollowing ->
            if (isFollowing != null && targetUid != null) {
                // Recargar conteos cuando cambia el estado de seguimiento
                loadFollowState()
            }
        }

        binding.btnFollowToggle.setOnClickListener {
            if (selfUid != null && targetUid != null) {
                val isFollowing = userViewModel.isFollowing.value ?: false
                if (isFollowing) {
                    userViewModel.unfollow(selfUid!!, targetUid!!)
                } else {
                    val targetUserName = userViewModel.currentProfile.value?.displayName
                    userViewModel.follow(selfUid!!, targetUid!!, targetUserName)
                }
            }
        }

        binding.btnChat.setOnClickListener {
            if (selfUid != null && targetUid != null) {
                // Abrir chat con este usuario
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("otherParticipantId", targetUid)
                startActivity(intent)
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_saved -> {
                    startActivity(Intent(this, SavedBooksActivity::class.java))
                    true
                }
                R.id.menu_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.menu_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        supportFragmentManager.commit {
            replace(R.id.headerFragmentContainer, HeaderFragment.newInstance("Perfil"))
        }
    }


    private fun loadFollowState() {
        if (selfUid != null && targetUid != null) {
            userViewModel.loadFollowState(selfUid!!, targetUid!!)
        }
    }

    private fun updateProfileUI(profile: es.etg.lectoguard.domain.model.UserProfile) {
        binding.tvDisplayName.text = profile.displayName
        binding.tvEmail.text = profile.email

        // Mostrar avatar
        if (!profile.avatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profile.avatarUrl)
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(binding.ivAvatar)
        } else {
            // Mostrar avatar por defecto si no hay avatar
            Glide.with(this)
                .load(R.drawable.default_avatar)
                .circleCrop()
                .into(binding.ivAvatar)
        }

        // Mostrar bio
        if (!profile.bio.isNullOrEmpty()) {
            binding.tvBio.text = profile.bio
        } else {
            binding.tvBio.text = getString(R.string.bio_hint)
        }
    }

    private fun updateFollowButton(isFollowing: Boolean) {
        if (isFollowing) {
            binding.btnFollowToggle.text = getString(R.string.unfollow)
        } else {
            binding.btnFollowToggle.text = getString(R.string.follow)
        }
    }
}

