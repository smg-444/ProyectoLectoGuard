package es.etg.lectoguard.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivityHomeBinding
import es.etg.lectoguard.domain.model.BookGenre
import es.etg.lectoguard.ui.viewmodel.BookViewModel
import es.etg.lectoguard.ui.viewmodel.RecommendationViewModel
import es.etg.lectoguard.utils.PrefsHelper
import es.etg.lectoguard.utils.NetworkUtils
import es.etg.lectoguard.utils.NavigationUtils
import androidx.fragment.app.commit

@AndroidEntryPoint
class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val bookViewModel: BookViewModel by viewModels()
    private val recommendationViewModel: RecommendationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = PrefsHelper.getUserName(this)
        binding.tvWelcome.text = getString(R.string.welcome, userName)
        
        // Solicitar permisos de notificación para Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }
        
        // Solicitar token FCM si aún no se ha guardado
        es.etg.lectoguard.utils.FCMHelper.requestAndSaveToken()

        binding.rvBooks.layoutManager = LinearLayoutManager(this)
        binding.rvRecommendations.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        
        supportFragmentManager.commit {
            replace(R.id.headerFragmentContainer, HeaderFragment.newInstance("Inicio"))
        }
        
        // Variables para filtros
        var selectedGenreFilter: BookGenre? = null
        var onlyFromFollowing = false
        val genreChips = mutableListOf<com.google.android.material.chip.Chip>()
        
        // Función helper para cargar recomendaciones con filtros
        fun loadRecommendationsWithFilters(genre: BookGenre?, onlyFollowing: Boolean) {
            val localUserId = PrefsHelper.getUserId(this)
            val firebaseUid = PrefsHelper.getFirebaseUid(this) ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (localUserId != -1 && firebaseUid != null) {
                recommendationViewModel.loadRecommendations(localUserId, firebaseUid, limit = 10, genre, onlyFollowing)
            }
        }
        
        // Crear chips dinámicos para cada género
        val genres = BookGenre.values().filter { it != BookGenre.OTHER } // Excluir "Otro" para mantener la lista más limpia
        genres.forEach { genre ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = genre.displayName
                isCheckable = true
                isChecked = false
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 8, 0)
                }
                
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        // Desmarcar todos los demás chips de género
                        binding.chipAllGenres.isChecked = false
                        genreChips.forEach { if (it != this) it.isChecked = false }
                        
                        selectedGenreFilter = genre
                        loadRecommendationsWithFilters(selectedGenreFilter, onlyFromFollowing)
                    } else {
                        // Si se desmarca, volver a "Todos"
                        if (selectedGenreFilter == genre) {
                            selectedGenreFilter = null
                            binding.chipAllGenres.isChecked = true
                            loadRecommendationsWithFilters(null, onlyFromFollowing)
                        }
                    }
                }
            }
            genreChips.add(chip)
            binding.llRecommendationFilters.addView(chip)
        }
        
        // Configurar filtros de recomendaciones
        binding.chipAllGenres.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Desmarcar todos los chips de género
                genreChips.forEach { it.isChecked = false }
                selectedGenreFilter = null
                loadRecommendationsWithFilters(selectedGenreFilter, onlyFromFollowing)
            }
        }
        
        binding.chipOnlyFollowing.setOnCheckedChangeListener { _, isChecked ->
            onlyFromFollowing = isChecked
            loadRecommendationsWithFilters(selectedGenreFilter, onlyFromFollowing)
        }
        
        // Observar recomendaciones
        recommendationViewModel.recommendations.observe(this) { recommendations ->
            android.util.Log.d("HomeActivity", "Recomendaciones recibidas: ${recommendations.size}")
            if (recommendations.isNotEmpty()) {
                binding.tvRecommendationsTitle.visibility = android.view.View.VISIBLE
                binding.hsvRecommendationFilters.visibility = android.view.View.VISIBLE
                binding.rvRecommendations.visibility = android.view.View.VISIBLE
                binding.rvRecommendations.adapter = RecommendationAdapter(
                    recommendations,
                    onBookClick = { recommendation ->
                        val intent = Intent(this, SaveBookActivity::class.java)
                        intent.putExtra("bookId", recommendation.bookId)
                        startActivity(intent)
                    },
                    onUserClick = { userId ->
                        // Navegar al perfil del usuario
                        val intent = Intent(this, UserProfileActivity::class.java)
                        intent.putExtra("targetUid", userId)
                        startActivity(intent)
                    }
                )
            } else {
                android.util.Log.d("HomeActivity", "No hay recomendaciones disponibles")
                binding.tvRecommendationsTitle.visibility = android.view.View.GONE
                binding.hsvRecommendationFilters.visibility = android.view.View.GONE
                binding.rvRecommendations.visibility = android.view.View.GONE
            }
        }
        
        // Observar estado de carga de recomendaciones
        recommendationViewModel.isLoading.observe(this) { isLoading ->
            android.util.Log.d("HomeActivity", "Cargando recomendaciones: $isLoading")
        }
        
        // Variables para búsqueda
        var allBooksList = emptyList<es.etg.lectoguard.data.local.BookEntity>()
        
        // Función para filtrar y mostrar libros
        fun filterAndDisplayBooks(query: String) {
            val filteredBooks = if (query.isEmpty()) {
                allBooksList
            } else {
                val queryLower = query.lowercase()
                allBooksList.filter { book ->
                    book.title.lowercase().contains(queryLower)
                }
            }
            
            binding.rvBooks.adapter = BookAdapter(filteredBooks) { book ->
                val intent = Intent(this, SaveBookActivity::class.java)
                intent.putExtra("bookId", book.id)
                startActivity(intent)
            }
            
            // Actualizar título según si hay búsqueda
            if (query.isEmpty()) {
                binding.tvAllBooksTitle.text = "Todos los libros"
            } else {
                binding.tvAllBooksTitle.text = "Resultados: ${filteredBooks.size} libro${if (filteredBooks.size != 1) "s" else ""}"
            }
        }
        
        // Observar libros
        bookViewModel.books.observe(this) { books ->
            android.util.Log.d("HomeActivity", "Libros recibidos: ${books.size}")
            allBooksList = if (books.isEmpty()) {
                android.util.Log.d("HomeActivity", "Lista vacía, usando mock books")
                bookViewModel.repository.getMockBooks()
            } else {
                android.util.Log.d("HomeActivity", "Usando ${books.size} libros de la API")
                books
            }
            filterAndDisplayBooks("")
        }
        
        // Configurar búsqueda
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                filterAndDisplayBooks(query)
                true
            } else {
                false
            }
        }
        
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s?.toString()?.trim() ?: ""
                filterAndDisplayBooks(query)
            }
        })
        
        val isOnline = NetworkUtils.isOnline(this)
        android.util.Log.d("HomeActivity", "Estado de conexión: isOnline=$isOnline")
        if (!isOnline) {
            Toast.makeText(this, "Sin conexión. Mostrando datos guardados.", Toast.LENGTH_SHORT).show()
        }
        bookViewModel.loadBooks(isOnline)
        
        // Cargar recomendaciones iniciales
        val localUserId = PrefsHelper.getUserId(this)
        val firebaseUid = PrefsHelper.getFirebaseUid(this) ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (localUserId != -1 && firebaseUid != null) {
            android.util.Log.d("HomeActivity", "Cargando recomendaciones para userId=$localUserId, firebaseUid=$firebaseUid")
            recommendationViewModel.loadRecommendations(localUserId, firebaseUid, limit = 10, null, false)
        } else {
            android.util.Log.w("HomeActivity", "No se pueden cargar recomendaciones: localUserId=$localUserId, firebaseUid=$firebaseUid")
        }

        // Establecer el item seleccionado
        binding.bottomNavigation.selectedItemId = R.id.menu_home
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_saved -> {
                    NavigationUtils.navigateToSavedBooks(this)
                    true
                }
                R.id.menu_profile -> {
                    NavigationUtils.navigateToProfile(this)
                    true
                }
                R.id.menu_home -> {
                    // Ya estamos en Home, no hacer nada
                    true
                }
                else -> false
            }
        }
    }
} 