package es.etg.lectoguard.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivityReadingListsBinding
import es.etg.lectoguard.domain.model.ReadingList
import es.etg.lectoguard.ui.viewmodel.ReadingListViewModel
import es.etg.lectoguard.utils.PrefsHelper
import androidx.fragment.app.commit
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PublicReadingListsActivity : BaseActivity() {
    private lateinit var binding: ActivityReadingListsBinding
    private val viewModel: ReadingListViewModel by viewModels()
    private lateinit var adapter: ReadingListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadingListsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.commit {
            replace(R.id.headerFragmentContainer, HeaderFragment.newInstance("Listas Públicas"))
        }

        binding.tvTitle.text = "Listas Públicas"
        binding.fabAddList.visibility = android.view.View.GONE // Ocultar botón de crear en esta vista

        // Configurar RecyclerView
        binding.rvReadingLists.layoutManager = LinearLayoutManager(this)
        adapter = ReadingListAdapter(
            emptyList(),
            onListClick = { list ->
                // Abrir detalle de la lista
                val intent = Intent(this, ReadingListDetailActivity::class.java)
                intent.putExtra("listId", list.id)
                startActivity(intent)
            }
        )
        binding.rvReadingLists.adapter = adapter

        // Observar listas públicas
        viewModel.publicReadingLists.observe(this) { lists ->
            adapter.updateLists(lists)
            binding.tvEmpty.visibility = if (lists.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }

        // Observar resultado de seguir/dejar de seguir
        viewModel.followResult.observe(this) { success ->
            if (success != null) {
                Toast.makeText(
                    this,
                    if (success) "Lista seguida" else "Error al seguir la lista",
                    Toast.LENGTH_SHORT
                ).show()
                // Recargar listas públicas
                viewModel.loadPublicReadingLists()
            }
        }

        // Cargar listas públicas
        viewModel.loadPublicReadingLists()

        // Verificar estado de seguimiento para cada lista y actualizar UI
        lifecycleScope.launch {
            val firebaseUid = PrefsHelper.getFirebaseUid(this@PublicReadingListsActivity) ?:
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (firebaseUid != null) {
                viewModel.publicReadingLists.value?.forEach { list ->
                    viewModel.checkIfFollowing(list.id, firebaseUid)
                }
            }
        }
    }
}

