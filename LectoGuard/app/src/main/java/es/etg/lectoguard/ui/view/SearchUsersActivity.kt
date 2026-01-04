package es.etg.lectoguard.ui.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivitySearchUsersBinding
import es.etg.lectoguard.ui.viewmodel.UserViewModel
import es.etg.lectoguard.utils.PrefsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchUsersActivity : BaseActivity() {
    private lateinit var binding: ActivitySearchUsersBinding
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvUsers.layoutManager = LinearLayoutManager(this)

        // Obtener UID del usuario actual para excluirlo de la búsqueda
        val currentUid = PrefsHelper.getFirebaseUid(this) ?: FirebaseAuth.getInstance().currentUser?.uid
        android.util.Log.d("SearchUsersActivity", "UID del usuario actual: $currentUid")

        // Cargar todos los usuarios inicialmente (excluyendo al usuario actual)
        android.util.Log.d("SearchUsersActivity", "Cargando todos los usuarios...")
        userViewModel.loadAllUsers(excludeUid = currentUid)
        userViewModel.allUsers.observe(this) { users ->
            android.util.Log.d("SearchUsersActivity", "Usuarios recibidos (allUsers): ${users.size}")
            if (users.isNotEmpty()) {
                binding.rvUsers.adapter = UserAdapter(users) { user ->
                    val intent = Intent(this, UserProfileActivity::class.java)
                    intent.putExtra("targetUid", user.uid)
                    startActivity(intent)
                }
                binding.tvEmpty.visibility = android.view.View.GONE
            } else {
                android.util.Log.w("SearchUsersActivity", "No se encontraron usuarios")
                binding.tvEmpty.visibility = android.view.View.VISIBLE
            }
        }

        // Búsqueda con delay para evitar demasiadas consultas
        var searchJob: kotlinx.coroutines.Job? = null
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                val query = s?.toString()?.trim() ?: ""
                if (query.isEmpty()) {
                    userViewModel.loadAllUsers(excludeUid = currentUid)
                } else {
                    searchJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(500) // Esperar 500ms antes de buscar
                        userViewModel.searchUsers(query, excludeUid = currentUid)
                    }
                }
            }
        })

        userViewModel.searchResults.observe(this) { users ->
            android.util.Log.d("SearchUsersActivity", "Resultados de búsqueda recibidos: ${users.size}")
            if (users.isNotEmpty()) {
                binding.rvUsers.adapter = UserAdapter(users) { user ->
                    val intent = Intent(this, UserProfileActivity::class.java)
                    intent.putExtra("targetUid", user.uid)
                    startActivity(intent)
                }
                binding.tvEmpty.visibility = android.view.View.GONE
            } else if (binding.etSearch.text.toString().trim().isNotEmpty()) {
                android.util.Log.w("SearchUsersActivity", "No se encontraron resultados para la búsqueda")
                binding.tvEmpty.visibility = android.view.View.VISIBLE
            }
        }

        supportFragmentManager.commit {
            replace(R.id.headerFragmentContainer, HeaderFragment.newInstance("Buscar Usuarios"))
        }
    }
}

