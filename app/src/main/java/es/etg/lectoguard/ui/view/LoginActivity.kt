package es.etg.lectoguard.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivityLoginBinding
import es.etg.lectoguard.data.local.LectoGuardDatabase
import es.etg.lectoguard.data.repository.UserRepository
import es.etg.lectoguard.domain.usecase.LoginUseCase
import es.etg.lectoguard.domain.usecase.RegisterUseCase
import es.etg.lectoguard.ui.viewmodel.UserViewModel
import es.etg.lectoguard.utils.PrefsHelper

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = LectoGuardDatabase.getInstance(this)
        val userRepository = UserRepository(db.userDao())
        userViewModel = UserViewModel(
            LoginUseCase(userRepository),
            RegisterUseCase(userRepository)
        )

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
                userViewModel.login(email, password)
            } else {
                Toast.makeText(this, getString(R.string.form_fix_errors), Toast.LENGTH_SHORT).show()
            }
        }

        userViewModel.loginResult.observe(this) { success ->
            if (success) {
                val user = userViewModel.user.value!!
                PrefsHelper.saveUser(this, user.id, user.name)
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, getString(R.string.error_login), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.btnExit.setOnClickListener {
            finish()
        }
    }
} 