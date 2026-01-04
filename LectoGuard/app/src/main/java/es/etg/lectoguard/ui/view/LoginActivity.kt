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

@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                userViewModel.login(email, password)
            } else {
                Toast.makeText(this, getString(R.string.form_fix_errors), Toast.LENGTH_SHORT).show()
            }
        }

        userViewModel.loginResult.observe(this) { success ->
            binding.btnLogin.isEnabled = true
            binding.btnLogin.text = getString(R.string.login)
            if (success) {
                val user = userViewModel.user.value
                if (user != null) {
                    // Guardar datos del usuario de forma asíncrona
                    PrefsHelper.saveUser(this, user.id, user.name)
                    FirebaseAuth.getInstance().currentUser?.uid?.let { PrefsHelper.saveFirebaseUid(this, it) }
                    // Solicitar y guardar token FCM después del login (en background)
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        es.etg.lectoguard.utils.FCMHelper.requestAndSaveToken()
                    }
                    NavigationUtils.navigateToHome(this)
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegister.setOnClickListener {
            NavigationUtils.navigateToSignUp(this)
        }

        binding.btnExit.setOnClickListener {
            finish()
        }
    }
} 