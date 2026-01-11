package es.etg.lectoguard.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivityLoginBinding
import es.etg.lectoguard.ui.viewmodel.UserViewModel
import es.etg.lectoguard.utils.PrefsHelper
import es.etg.lectoguard.utils.NavigationUtils
import es.etg.lectoguard.utils.NetworkUtils

@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observar resultado de login/sesión
        userViewModel.loginResult.observe(this) { success ->
            if (success) {
                val user = userViewModel.user.value
                if (user != null) {
                    // Guardar datos del usuario
                    PrefsHelper.saveUser(this, user.id, user.name)
                    FirebaseAuth.getInstance().currentUser?.uid?.let { PrefsHelper.saveFirebaseUid(this, it) }
                    
                    // Si es login manual (hay texto en los campos), guardar credenciales
                    val email = binding.etEmail.text.toString().trim()
                    val password = binding.etPassword.text.toString().trim()
                    val isOnline = NetworkUtils.isOnline(this)
                    
                    if (isOnline && email.isNotEmpty() && password.isNotEmpty()) {
                        PrefsHelper.saveUserCredentials(this, email, password)
                        // Solicitar token FCM
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            es.etg.lectoguard.utils.FCMHelper.requestAndSaveToken()
                        }
                    }
                    
                    NavigationUtils.navigateToHome(this)
                    finish()
                }
            } else {
                // Manejar errores solo si es login manual (hay texto en los campos)
                val email = binding.etEmail.text.toString().trim()
                if (email.isNotEmpty()) {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = getString(R.string.login)
                    val isOnline = NetworkUtils.isOnline(this)
                    if (!isOnline) {
                        Toast.makeText(this, "Sin conexión. Verifica tus credenciales guardadas.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
        // Verificar si hay una sesión existente después de configurar el observer
        checkExistingSession()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            var valid = true

            if (email.isEmpty()) {
                binding.etEmail.error = getString(R.string.field_required)
                valid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = getString(R.string.invalid_email)
                valid = false
            } else {
                binding.etEmail.error = null
            }

            if (password.isEmpty()) {
                binding.etPassword.error = getString(R.string.field_required)
                valid = false
            } else {
                binding.etPassword.error = null
            }

            if (valid) {
                binding.btnLogin.isEnabled = false
                binding.btnLogin.text = getString(R.string.logging_in)
                val isOnline = NetworkUtils.isOnline(this)
                if (!isOnline) {
                    // Si no hay conexión, intentar login offline
                    android.util.Log.d("LoginActivity", "Sin conexión, intentando login offline")
                }
                userViewModel.login(email, password, isOnline)
            } else {
                Toast.makeText(this, getString(R.string.form_fix_errors), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegister.setOnClickListener {
            NavigationUtils.navigateToSignUp(this)
        }

        binding.btnExit.setOnClickListener {
            finish()
        }
    }
    
    private fun checkExistingSession() {
        // Verificar si Firebase Auth tiene una sesión activa
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            android.util.Log.d("LoginActivity", "Sesión existente encontrada, verificando usuario local...")
            userViewModel.checkExistingSession()
        } else {
            // Si no hay sesión de Firebase, verificar si hay credenciales guardadas y estamos offline
            val isOnline = NetworkUtils.isOnline(this)
            if (!isOnline && PrefsHelper.hasStoredCredentials(this)) {
                android.util.Log.d("LoginActivity", "Sin conexión pero hay credenciales guardadas, intentando login offline...")
                val email = PrefsHelper.getUserEmail(this)
                val password = PrefsHelper.getUserPassword(this)
                if (email != null && password != null) {
                    binding.etEmail.setText(email)
                    // No prellenar la contraseña por seguridad, pero intentar login automático
                    userViewModel.login(email, password, isOnline = false)
                }
            }
        }
    }
} 