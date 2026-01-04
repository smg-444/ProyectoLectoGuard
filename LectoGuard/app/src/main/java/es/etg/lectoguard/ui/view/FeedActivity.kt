package es.etg.lectoguard.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivityFeedBinding
import es.etg.lectoguard.ui.viewmodel.FeedViewModel
import es.etg.lectoguard.utils.PrefsHelper
import es.etg.lectoguard.utils.NavigationUtils

@AndroidEntryPoint
class FeedActivity : BaseActivity() {
    private lateinit var binding: ActivityFeedBinding
    private val feedViewModel: FeedViewModel by viewModels()
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = PrefsHelper.getFirebaseUid(this) ?: FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Configurar RecyclerView
        val layoutManager = LinearLayoutManager(this)
        binding.rvFeed.layoutManager = layoutManager
        
        // Scroll listener para paginación
        binding.rvFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                
                // Cargar más cuando quedan 5 items por mostrar
                if (!feedViewModel.isLoadingMore.value!! && 
                    feedViewModel.hasMoreItems() && 
                    lastVisibleItem >= totalItemCount - 5) {
                    currentUserId?.let {
                        feedViewModel.loadMoreFeedItems(it)
                    }
                }
            }
        })

        // Observar feed items
        var adapter: FeedAdapter? = null
        feedViewModel.feedItems.observe(this) { items ->
            if (items.isEmpty()) {
                binding.tvEmptyState.visibility = android.view.View.VISIBLE
                binding.rvFeed.visibility = android.view.View.GONE
            } else {
                binding.tvEmptyState.visibility = android.view.View.GONE
                binding.rvFeed.visibility = android.view.View.VISIBLE
                
                if (adapter == null) {
                    adapter = FeedAdapter(items) { item ->
                        // Opcional: navegar al detalle del libro o perfil del usuario
                        if (item.bookId != null) {
                            val intent = Intent(this, SaveBookActivity::class.java)
                            intent.putExtra("bookId", item.bookId)
                            startActivity(intent)
                        } else if (item.userId.isNotEmpty()) {
                            val intent = Intent(this, UserProfileActivity::class.java)
                            intent.putExtra("targetUid", item.userId)
                            startActivity(intent)
                        }
                    }
                    binding.rvFeed.adapter = adapter
                } else {
                    // Actualizar la lista existente
                    adapter?.updateItems(items)
                }
            }
        }

        // Observar carga de más items
        feedViewModel.isLoadingMore.observe(this) { isLoading ->
            binding.progressBarLoadingMore.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        // Observar errores
        feedViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }

        // Iniciar listener en tiempo real para el feed
        feedViewModel.startObservingFeed(currentUserId!!)

        // Bottom navigation - Feed no tiene item específico, usar home como base
        binding.bottomNavigation.selectedItemId = R.id.menu_home
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_saved -> {
                    NavigationUtils.navigateToSavedBooks(this)
                    finish()
                    true
                }
                R.id.menu_home -> {
                    NavigationUtils.navigateToHome(this)
                    finish()
                    true
                }
                R.id.menu_profile -> {
                    NavigationUtils.navigateToProfile(this)
                    finish()
                    true
                }
                else -> false
            }
        }

        supportFragmentManager.commit {
            replace(R.id.headerFragmentContainer, HeaderFragment.newInstance(getString(R.string.header_feed)))
        }
    }

    override fun onResume() {
        super.onResume()
        // Asegurar que el listener esté activo
        currentUserId?.let {
            feedViewModel.startObservingFeed(it)
        }
    }
}

