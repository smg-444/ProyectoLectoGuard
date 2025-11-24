package es.etg.lectoguard.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.google.firebase.auth.FirebaseAuth
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivityProfileBinding
import es.etg.lectoguard.data.local.LectoGuardDatabase
import es.etg.lectoguard.data.repository.UserRepository
import es.etg.lectoguard.domain.usecase.LoginUseCase
import es.etg.lectoguard.domain.usecase.RegisterUseCase
import es.etg.lectoguard.ui.viewmodel.UserViewModel
import es.etg.lectoguard.utils.PrefsHelper

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = LectoGuardDatabase.getInstance(this)
        val userRepository = UserRepository(db.userDao(), FirebaseAuth.getInstance())
        userViewModel = UserViewModel(
            LoginUseCase(userRepository),
            RegisterUseCase(userRepository)
        )

        val userId = PrefsHelper.getUserId(this)
        userViewModel.user.observe(this) { user ->
            if (user != null) {
                binding.tvEmail.text = user.email
                binding.tvPhone.text = user.phone
                binding.tvSignupDate.text = user.signupDate
            }
        }

        binding.btnLogout.setOnClickListener {
            PrefsHelper.clear(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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
                else -> false
            }
        }

        supportFragmentManager.commit {
            replace(R.id.headerFragmentContainer, HeaderFragment.newInstance("Perfil"))
        }
    }
} 