package es.etg.lectoguard.ui.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivitySignUpBinding
import es.etg.lectoguard.data.local.UserEntity
import dagger.hilt.android.AndroidEntryPoint
import es.etg.lectoguard.ui.viewmodel.UserViewModel

@AndroidEntryPoint
class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            var valid = true

            if (name.isEmpty()) {
                binding.etName.error = getString(R.string.field_required)
                valid = false
            } else {
                binding.etName.error = null
            }

            if (email.isEmpty()) {
                binding.etEmail.error = getString(R.string.field_required)
                valid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = getString(R.string.invalid_email)
                valid = false
            } else {
                binding.etEmail.error = null
            }

            if (phone.isEmpty()) {
                binding.etPhone.error = getString(R.string.field_required)
                valid = false
            } else if (!phone.matches(Regex("^[0-9]{9,15}$"))) {
                binding.etPhone.error = getString(R.string.invalid_phone)
                valid = false
            } else {
                binding.etPhone.error = null
            }

            if (password.isEmpty()) {
                binding.etPassword.error = getString(R.string.field_required)
                valid = false
            } else if (password.length < 6) {
                binding.etPassword.error = getString(R.string.min_password)
                valid = false
            } else {
                binding.etPassword.error = null
            }

            if (valid) {
                val signupDate = System.currentTimeMillis().toString()
                val user = UserEntity(name = name, email = email, phone = phone, password = password, signupDate = signupDate)
                userViewModel.register(user)
            } else {
                Toast.makeText(this, getString(R.string.form_fix_errors), Toast.LENGTH_SHORT).show()
            }
        }

        userViewModel.registerResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.success_register), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.error_register), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
} 