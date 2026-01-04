package es.etg.lectoguard.ui.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.commit
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivityProfileBinding
import es.etg.lectoguard.databinding.DialogEditProfileBinding
import es.etg.lectoguard.data.repository.UserRepository
import es.etg.lectoguard.data.repository.BookRepository
import es.etg.lectoguard.data.remote.BookApiService
import es.etg.lectoguard.domain.usecase.*
import es.etg.lectoguard.domain.model.ReadingStatus
import es.etg.lectoguard.ui.viewmodel.UserViewModel
import es.etg.lectoguard.ui.viewmodel.BookViewModel
import es.etg.lectoguard.utils.PrefsHelper
import es.etg.lectoguard.utils.NavigationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val userViewModel: UserViewModel by viewModels()
    
    @Inject
    lateinit var userRepository: UserRepository
    
    private lateinit var bookViewModel: BookViewModel
    private var selectedImageUri: Uri? = null
    
    private var editDialogBinding: DialogEditProfileBinding? = null
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                editDialogBinding?.let { binding ->
                    Glide.with(this)
                        .load(uri)
                        .circleCrop()
                        .into(binding.ivAvatar)
                }
            }
        }
    }

    @Inject
    lateinit var bookRepository: BookRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar BookViewModel para estadísticas (aún necesita migración completa)
        bookViewModel = BookViewModel(
            bookRepository,
            GetBooksUseCase(bookRepository),
            SaveBookUseCase(bookRepository),
            GetBookDetailUseCase(bookRepository),
            null,
            UpdateBookReadingStatusUseCase(bookRepository),
            GetUserBookUseCase(bookRepository),
            GetBooksByStatusUseCase(bookRepository),
            GetBookCountByStatusUseCase(bookRepository)
        )

        val selfUid = PrefsHelper.getFirebaseUid(this) ?: FirebaseAuth.getInstance().currentUser?.uid
        val targetUid = selfUid // en esta pantalla mostramos nuestro propio perfil
        val localUserId = PrefsHelper.getUserId(this)

        if (selfUid != null) {
            // Cargar perfil usando el ViewModel
            userViewModel.loadProfile(selfUid)
            // Cargar conteos de follow
            userViewModel.loadFollowState(selfUid, targetUid!!)
            userViewModel.followersCount.observe(this) { count ->
                binding.tvFollowersCount.text = "${count ?: 0}"
            }
            userViewModel.followingCount.observe(this) { count ->
                binding.tvFollowingCount.text = "${count ?: 0}"
            }
            
            // Cargar estadísticas de lectura
            if (localUserId != -1) {
                bookViewModel.loadBookCountsByStatus(localUserId)
                bookViewModel.bookCountByStatus.observe(this) { counts ->
                    binding.tvWantToReadCount.text = "${counts[ReadingStatus.WANT_TO_READ] ?: 0}"
                    binding.tvReadingCount.text = "${counts[ReadingStatus.READING] ?: 0}"
                    binding.tvReadCount.text = "${counts[ReadingStatus.READ] ?: 0}"
                    binding.tvAbandonedCount.text = "${counts[ReadingStatus.ABANDONED] ?: 0}"
                    
                    // Actualizar gráfico
                    binding.readingStatsChart?.updateStats(counts)
                }
            }
            // Si es nuestro perfil, ocultar botón de seguir
            binding.btnFollowToggle.visibility = android.view.View.GONE
            
            // Observar cambios en el perfil
            userViewModel.currentProfile.observe(this) { profile ->
                profile?.let { updateProfileUI(it) }
            }
            
            // Observar resultado de actualización
            userViewModel.updateProfileResult.observe(this) { success ->
                if (success) {
                    Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
                    // Recargar perfil usando el ViewModel para que se actualice el observer
                    if (selfUid != null) {
                        userViewModel.loadProfile(selfUid!!)
                    }
                } else {
                    Toast.makeText(this, getString(R.string.error_updating_profile), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog(selfUid)
        }

        binding.btnFeed.setOnClickListener {
            NavigationUtils.navigateToFeed(this)
        }

        binding.btnSearchUsers.setOnClickListener {
            NavigationUtils.navigateToSearchUsers(this)
        }

        binding.btnConversations.setOnClickListener {
            NavigationUtils.navigateToConversationsList(this)
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Configurar toggle de modo oscuro
        val switchDarkMode = binding.switchDarkMode
        switchDarkMode?.isChecked = PrefsHelper.isDarkModeEnabled(this)
        switchDarkMode?.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.setDarkMode(this, isChecked)
            refreshTheme()
        }

        // Establecer el item seleccionado
        binding.bottomNavigation.selectedItemId = R.id.menu_profile
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_saved -> {
                    NavigationUtils.navigateToSavedBooks(this)
                    true
                }
                R.id.menu_home -> {
                    NavigationUtils.navigateToHome(this)
                    true
                }
                R.id.menu_profile -> {
                    // Ya estamos en Profile, no hacer nada
                    true
                }
                else -> false
            }
        }

        supportFragmentManager.commit {
            replace(R.id.headerFragmentContainer, HeaderFragment.newInstance(getString(R.string.profile)))
        }
    }
    
    private fun updateProfileUI(profile: es.etg.lectoguard.domain.model.UserProfile) {
        binding.tvDisplayName.text = profile.displayName
        
        // Mostrar email
        binding.tvEmail.text = getString(R.string.email_label, profile.email ?: "")
        
        // Obtener teléfono del usuario local
        val localUser = PrefsHelper.getUserId(this)
        if (localUser != -1) {
            CoroutineScope(Dispatchers.Main).launch {
                val userEntity = userRepository.getUserById(localUser)
                val phone = userEntity?.phone ?: ""
                binding.tvPhone.text = getString(R.string.phone_label, phone)
            }
        } else {
            binding.tvPhone.text = getString(R.string.phone_label, "")
        }
        
        // Formatear fecha de alta
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(profile.createdAt))
        binding.tvSignupDate.text = getString(R.string.signup_date_label, formattedDate)
        
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
    
    private fun showEditProfileDialog(uid: String?) {
        if (uid == null) return
        
        selectedImageUri = null
        val dialogBinding = DialogEditProfileBinding.inflate(layoutInflater)
        editDialogBinding = dialogBinding
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        // Cargar datos actuales
        CoroutineScope(Dispatchers.Main).launch {
            val profile = userRepository.getRemoteProfile(uid)
            profile?.let {
                dialogBinding.etBio.setText(it.bio ?: "")
                if (!it.avatarUrl.isNullOrEmpty()) {
                    Glide.with(this@ProfileActivity)
                        .load(it.avatarUrl)
                        .circleCrop()
                        .into(dialogBinding.ivAvatar)
                }
            }
        }
        
        dialogBinding.btnChangeAvatar.setOnClickListener {
            openImagePicker()
        }
        
        dialogBinding.btnSave.setOnClickListener {
            val bio = dialogBinding.etBio.text.toString().trim()
            if (selectedImageUri != null) {
                userViewModel.updateProfileWithAvatar(uid, selectedImageUri!!, bio)
            } else if (bio.isNotEmpty()) {
                userViewModel.updateProfileBio(uid, bio)
            } else {
                Toast.makeText(this, getString(R.string.error_updating_profile), Toast.LENGTH_SHORT).show()
            }
            editDialogBinding = null
            dialog.dismiss()
        }
        
        dialogBinding.btnCancel.setOnClickListener {
            editDialogBinding = null
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
    
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_confirmation))
            .setPositiveButton(getString(R.string.logout)) { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun performLogout() {
        // Cerrar sesión en Firebase Auth
        FirebaseAuth.getInstance().signOut()
        
        // Limpiar preferencias locales
        PrefsHelper.clear(this)
        
        // Navegar a LoginActivity y limpiar el stack de actividades
        NavigationUtils.navigateToLogin(this)
        finish()
    }
} 