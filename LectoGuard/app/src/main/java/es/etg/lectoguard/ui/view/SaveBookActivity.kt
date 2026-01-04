package es.etg.lectoguard.ui.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivitySaveBookBinding
import es.etg.lectoguard.databinding.DialogWriteReviewBinding
import es.etg.lectoguard.databinding.DialogManageListsBinding
import es.etg.lectoguard.data.repository.RatingRepository
import es.etg.lectoguard.data.repository.UserRepository
import es.etg.lectoguard.data.local.BookEntity
import es.etg.lectoguard.data.local.UserBookEntity
import es.etg.lectoguard.domain.model.Rating
import es.etg.lectoguard.domain.model.Review
import es.etg.lectoguard.domain.model.ReadingStatus
import es.etg.lectoguard.domain.model.BookGenre
import es.etg.lectoguard.ui.viewmodel.BookViewModel
import es.etg.lectoguard.ui.viewmodel.ReadingListViewModel
import es.etg.lectoguard.utils.PrefsHelper
import android.widget.ArrayAdapter
import android.widget.AdapterView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import es.etg.lectoguard.utils.NetworkUtils
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class SaveBookActivity : BaseActivity() {
    private lateinit var binding: ActivitySaveBookBinding
    private val bookViewModel: BookViewModel by viewModels()
    
    @Inject
    lateinit var ratingRepository: RatingRepository
    
    @Inject
    lateinit var userRepository: UserRepository
    
    private val readingListViewModel: ReadingListViewModel by viewModels()
    
    private var bookId: Int = -1
    private var userId: String? = null
    private var currentUserProfile: es.etg.lectoguard.domain.model.UserProfile? = null
    private var currentLists: MutableSet<String> = mutableSetOf() // IDs de listas donde está el libro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = PrefsHelper.getFirebaseUid(this) ?: FirebaseAuth.getInstance().currentUser?.uid
        
        android.util.Log.d("SaveBookActivity", "UserId: $userId")
        
        // Cargar perfil del usuario inmediatamente
        if (userId != null) {
            CoroutineScope(Dispatchers.Main).launch {
                currentUserProfile = userRepository.getRemoteProfile(userId!!)
                // Si no existe perfil, crear uno básico
                if (currentUserProfile == null) {
                    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
                    currentUserProfile = es.etg.lectoguard.domain.model.UserProfile(
                        uid = userId!!,
                        displayName = email.substringBefore("@"),
                        email = email
                    )
                    userRepository.updateUserProfile(currentUserProfile!!)
                }
                android.util.Log.d("SaveBookActivity", "Perfil cargado: ${currentUserProfile?.displayName}")
            }
        } else {
            Toast.makeText(this, getString(R.string.error_user_not_authenticated), Toast.LENGTH_SHORT).show()
        }
        
        android.util.Log.d("SaveBookActivity", "RatingRepository inicializado: ${ratingRepository != null}")

        bookId = intent.getIntExtra("bookId", -1)
        val localUserId = PrefsHelper.getUserId(this)
        val firebaseUid = PrefsHelper.getFirebaseUid(this) ?: FirebaseAuth.getInstance().currentUser?.uid

        // Configurar Spinner de estado de lectura
        val readingStatuses = ReadingStatus.values().map { status ->
            when (status) {
                ReadingStatus.WANT_TO_READ -> getString(R.string.want_to_read)
                ReadingStatus.READING -> getString(R.string.reading)
                ReadingStatus.READ -> getString(R.string.read)
                ReadingStatus.ABANDONED -> getString(R.string.abandoned)
            }
        }
        val readingStatusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, readingStatuses)
        readingStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerReadingStatus.adapter = readingStatusAdapter
        
        // Cargar estado actual del libro si está guardado
        if (localUserId != -1 && bookId != -1) {
            bookViewModel.getUserBook(localUserId, bookId)
        }
        
        // Configurar RecyclerView de listas actuales (usando LinearLayoutManager horizontal)
        binding.rvCurrentLists.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        
        // Cargar todas las listas del usuario
        if (firebaseUid != null) {
            readingListViewModel.loadUserReadingLists(firebaseUid)
        }
        
        // Observar listas del usuario y actualizar display cuando cambien
        lifecycleScope.launch {
            readingListViewModel.userReadingLists.collect { lists ->
                updateListsDisplay(lists)
            }
        }
        
        // Observar cambios en las listas para actualizar el display
        readingListViewModel.saveResult.observe(this) { success ->
            if (success == true) {
                // Recargar listas después de crear/actualizar
                val firebaseUid = PrefsHelper.getFirebaseUid(this) ?: FirebaseAuth.getInstance().currentUser?.uid
                if (firebaseUid != null) {
                    readingListViewModel.loadUserReadingLists(firebaseUid)
                }
            }
        }
        
        // Observar estado actual del libro
        bookViewModel.currentUserBook.observe(this) { userBook ->
            if (userBook != null) {
                val currentStatusIndex = ReadingStatus.values().indexOf(userBook.readingStatus)
                if (currentStatusIndex >= 0) {
                    binding.spinnerReadingStatus.setSelection(currentStatusIndex)
                }
            }
        }
        
        // Listener para cambio de estado
        binding.spinnerReadingStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (localUserId != -1 && bookId != -1) {
                    val selectedStatus = ReadingStatus.values()[position]
                    val currentUserBook = bookViewModel.currentUserBook.value
                    
                    // Solo actualizar si el libro está guardado y el estado cambió
                    if (currentUserBook != null && currentUserBook.readingStatus != selectedStatus) {
                        bookViewModel.updateReadingStatus(localUserId, bookId, selectedStatus)
                    }
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Observar resultado de actualización
        bookViewModel.updateReadingStatusResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.reading_status_updated), Toast.LENGTH_SHORT).show()
            }
        }
        
        // Botón para gestionar listas
        binding.btnManageLists.setOnClickListener {
            showManageListsDialog()
        }

        val isOnline = NetworkUtils.isOnline(this)
        if (!isOnline) {
            Toast.makeText(this, getString(R.string.offline_message), Toast.LENGTH_SHORT).show()
        }
        bookViewModel.getBookDetail(bookId, isOnline)
        bookViewModel.bookDetail.observe(this) { detail ->
            if (detail != null) {
                android.util.Log.d("SaveBookActivity", "Detalle cargado: id=${detail.id}, title=${detail.title}, sinopsis=${detail.sinopsis.take(50)}...")
                binding.tvTitle.text = detail.title
                binding.tvSinopsis.text = if (detail.sinopsis.isNotEmpty()) detail.sinopsis else getString(R.string.no_synopsis_available)
                binding.tvFirstPage.text = if (detail.firstPage.isNotEmpty()) detail.firstPage else getString(R.string.no_first_page_available)
                
                if (detail.coverImage.isNotEmpty()) {
                    Glide.with(this)
                        .load(detail.coverImage)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(binding.ivCover)
                } else {
                    android.util.Log.w("SaveBookActivity", "URL de portada vacía para libro ${detail.id}")
                }
                
                // Convertir géneros y guardar libro completo
                lifecycleScope.launch {
                    val genres = detail.genres.mapNotNull { genreName ->
                        try {
                            BookGenre.valueOf(genreName.uppercase().replace(" ", "_").replace("-", "_"))
                        } catch (e: Exception) {
                            when (genreName.uppercase().replace(" ", "_").replace("-", "_")) {
                                "FICTION", "FICCIÓN" -> BookGenre.FICTION
                                "SCIENCE_FICTION", "SCIENCEFICTION", "CIENCIA_FICCIÓN" -> BookGenre.SCIENCE_FICTION
                                "FANTASY", "FANTASÍA" -> BookGenre.FANTASY
                                "MYSTERY", "MISTERIO" -> BookGenre.MYSTERY
                                "THRILLER" -> BookGenre.THRILLER
                                "ROMANCE" -> BookGenre.ROMANCE
                                "HISTORICAL", "HISTÓRICA" -> BookGenre.HISTORICAL
                                "CLASSIC", "CLÁSICO" -> BookGenre.CLASSIC
                                "ADVENTURE", "AVENTURA" -> BookGenre.ADVENTURE
                                "CHILDREN", "INFANTIL" -> BookGenre.CHILDREN
                                "YOUNG_ADULT", "YOUNGADULT", "JUVENIL" -> BookGenre.YOUNG_ADULT
                                else -> null
                            }
                        }
                    }
                    bookViewModel.repository.insertBook(
                        BookEntity(
                            detail.id,
                            detail.title,
                            detail.coverImage,
                            genres = if (genres.isEmpty()) listOf(BookGenre.OTHER) else genres
                        )
                    )
                }
            } else {
                android.util.Log.w("SaveBookActivity", "Detalle del libro es null para bookId=$bookId")
            }
        }

        bookViewModel.getBookSynopsis(bookId)
        bookViewModel.bookSynopsis.observe(this) { sinopsis ->
            if (sinopsis != null) {
                binding.tvTitle.text = sinopsis
            }
        }

        // Configurar RecyclerView de reseñas
        binding.rvReviews.layoutManager = LinearLayoutManager(this)
        
        // Cargar valoraciones y reseñas
        if (bookId != -1 && userId != null) {
            bookViewModel.loadUserRating(bookId, userId!!)
            bookViewModel.loadAverageRating(bookId)
            bookViewModel.loadReviews(bookId)
        }
        
        // Observar valoración del usuario
        bookViewModel.userRating.observe(this) { rating ->
            if (rating != null) {
                binding.ratingStars.setRating(rating.rating)
            } else {
                binding.ratingStars.setRating(0)
            }
        }
        
        // Observar valoración promedio
        bookViewModel.averageRating.observe(this) { avg ->
            if (avg > 0) {
                val df = DecimalFormat("#.#")
                binding.tvAverageRating.text = "${df.format(avg)} / 5.0"
            } else {
                binding.tvAverageRating.text = getString(R.string.no_ratings)
            }
        }
        
        // Observar reseñas
        bookViewModel.reviews.observe(this) { reviewsList ->
            if (reviewsList.isNotEmpty()) {
                binding.rvReviews.adapter = ReviewAdapter(
                    reviewsList, 
                    userId,
                    onLikeClick = { review, isLiked ->
                        if (userId != null) {
                            bookViewModel.toggleLikeReview(review.id, userId!!, isLiked)
                        }
                    },
                    onEditClick = { review ->
                        showEditReviewDialog(review)
                    },
                    onDeleteClick = { review ->
                        showDeleteReviewConfirmation(review)
                    }
                )
            } else {
                binding.rvReviews.adapter = null
            }
        }
        
        // Observar resultado de actualizar reseña
        bookViewModel.updateReviewResult.observe(this) { success ->
            if (success == true) {
                Toast.makeText(this, "Reseña actualizada", Toast.LENGTH_SHORT).show()
            } else if (success == false) {
                Toast.makeText(this, "Error al actualizar la reseña", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observar resultado de eliminar reseña
        bookViewModel.deleteReviewResult.observe(this) { success ->
            if (success == true) {
                Toast.makeText(this, "Reseña eliminada", Toast.LENGTH_SHORT).show()
            } else if (success == false) {
                Toast.makeText(this, "Error al eliminar la reseña", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observar resultado de guardar valoración
        bookViewModel.saveRatingResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.rating_saved), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.error_saving_rating), Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observar resultado de guardar reseña
        bookViewModel.saveReviewResult.observe(this) { reviewId ->
            if (reviewId != null) {
                Toast.makeText(this, getString(R.string.review_saved), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.error_saving_review), Toast.LENGTH_SHORT).show()
            }
        }
        
        // Configurar componente de estrellas para valorar
        binding.ratingStars.setOnRatingChangedListener { rating ->
            if (rating > 0 && userId != null && bookId != -1) {
                android.util.Log.d("SaveBookActivity", "Guardando valoración: bookId=$bookId, userId=$userId, rating=$rating")
                val ratingModel = Rating(
                    bookId = bookId,
                    userId = userId!!,
                    rating = rating
                )
                val bookTitle = bookViewModel.bookDetail.value?.title
                val bookCover = bookViewModel.bookDetail.value?.coverImage
                bookViewModel.saveRating(ratingModel, bookTitle, bookCover)
            } else {
                android.util.Log.w("SaveBookActivity", "No se puede guardar valoración: rating=$rating, userId=$userId, bookId=$bookId")
            }
        }
        
        // Botón para escribir reseña
        binding.btnWriteReview.setOnClickListener {
            showWriteReviewDialog()
        }
        
        binding.btnSave.setOnClickListener {
            val localUserId = PrefsHelper.getUserId(this)
            if (localUserId == -1) {
                Toast.makeText(this, getString(R.string.error_user_not_identified), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedStatus = ReadingStatus.values()[binding.spinnerReadingStatus.selectedItemPosition]
            
            val userBook = UserBookEntity(
                userId = localUserId, 
                bookId = bookId, 
                savedDate = System.currentTimeMillis().toString(),
                readingStatus = selectedStatus,
                tags = emptyList<String>() // Las etiquetas ya no se usan, se usan listas
            )
            val bookTitle = bookViewModel.bookDetail.value?.title
            val bookCover = bookViewModel.bookDetail.value?.coverImage
            bookViewModel.saveBook(userBook, userId, bookTitle, bookCover)
        }

        bookViewModel.saveResult.observe(this) { saved ->
            if (saved == true) {
                Toast.makeText(this, getString(R.string.book_saved), Toast.LENGTH_SHORT).show()
                finish()
            } else if (saved == false) {
                Toast.makeText(this, getString(R.string.already_saved), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun updateListsDisplay(allLists: List<es.etg.lectoguard.domain.model.ReadingList>) {
        // Filtrar listas que contienen este libro
        val listsWithBook = allLists.filter { it.bookIds.contains(bookId) }
        currentLists = listsWithBook.map { it.id }.toMutableSet()
        
        binding.rvCurrentLists.adapter = ReadingListChipAdapter(listsWithBook) { list ->
            // Al hacer clic, quitar de la lista
            readingListViewModel.removeBookFromList(list.id, bookId)
            Toast.makeText(this, "Libro eliminado de \"${list.name}\"", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showManageListsDialog() {
        val dialogBinding = DialogManageListsBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        val firebaseUid = PrefsHelper.getFirebaseUid(this) ?: FirebaseAuth.getInstance().currentUser?.uid
        if (firebaseUid == null) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Configurar RecyclerView de listas
        dialogBinding.rvLists.layoutManager = LinearLayoutManager(this)
        
        // Variable para mantener referencia al adapter
        var currentAdapter: ReadingListAdapter? = null
        
        // Observar listas del usuario
        lifecycleScope.launch {
            readingListViewModel.userReadingLists.collect { lists ->
                currentAdapter = ReadingListAdapter(
                    lists,
                    onListClick = { list ->
                        // Toggle: agregar o quitar libro de la lista
                        if (list.bookIds.contains(bookId)) {
                            readingListViewModel.removeBookFromList(list.id, bookId)
                            Toast.makeText(this@SaveBookActivity, getString(R.string.book_removed_from_list, list.name), Toast.LENGTH_SHORT).show()
                        } else {
                            readingListViewModel.addBookToList(list.id, bookId)
                            Toast.makeText(this@SaveBookActivity, getString(R.string.book_added_to_list, list.name), Toast.LENGTH_SHORT).show()
                        }
                        // El display se actualizará automáticamente cuando cambien las listas
                    },
                    bookId = bookId
                )
                dialogBinding.rvLists.adapter = currentAdapter
            }
        }
        
        // Botón para crear nueva lista
        dialogBinding.btnCreateList.setOnClickListener {
            dialog.dismiss()
            // Abrir actividad de listas para crear una nueva
            val intent = Intent(this@SaveBookActivity, ReadingListsActivity::class.java)
            startActivity(intent)
        }
        
        dialog.show()
    }
    
    private fun showWriteReviewDialog() {
        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Si no tenemos el perfil cargado, intentar cargarlo
        if (currentUserProfile == null) {
            CoroutineScope(Dispatchers.Main).launch {
                currentUserProfile = userRepository.getRemoteProfile(userId!!)
                if (currentUserProfile == null) {
                    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
                    currentUserProfile = es.etg.lectoguard.domain.model.UserProfile(
                        uid = userId!!,
                        displayName = email.substringBefore("@"),
                        email = email
                    )
                    userRepository.updateUserProfile(currentUserProfile!!)
                }
                // Mostrar diálogo después de cargar el perfil
                showWriteReviewDialogInternal()
            }
            return
        }
        
        showWriteReviewDialogInternal()
    }
    
    private fun showWriteReviewDialogInternal() {
        if (userId == null || currentUserProfile == null) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }
        
        val dialogBinding = DialogWriteReviewBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        var selectedRating = 0
        
        dialogBinding.ratingStars.setOnRatingChangedListener { rating ->
            selectedRating = rating
        }
        
        dialogBinding.btnSaveReview.setOnClickListener {
            val reviewText = dialogBinding.etReviewText.text.toString().trim()
            
            if (selectedRating == 0) {
                Toast.makeText(this, "Selecciona una valoración", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (reviewText.isEmpty()) {
                Toast.makeText(this, "Escribe una reseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val review = Review(
                bookId = bookId,
                userId = userId!!,
                userName = currentUserProfile!!.displayName,
                userAvatarUrl = currentUserProfile!!.avatarUrl,
                rating = selectedRating,
                text = reviewText
            )
            
            val bookTitle = bookViewModel.bookDetail.value?.title
            val bookCover = bookViewModel.bookDetail.value?.coverImage
            bookViewModel.saveReview(review, bookTitle, bookCover)
            dialog.dismiss()
        }
        
        dialogBinding.btnCancelReview.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showEditReviewDialog(review: Review) {
        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }
        
        val dialogBinding = DialogWriteReviewBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("Editar Reseña")
            .create()
        
        // Prellenar con datos existentes
        dialogBinding.etReviewText.setText(review.text)
        dialogBinding.ratingStars.setRating(review.rating)
        var selectedRating = review.rating
        
        dialogBinding.ratingStars.setOnRatingChangedListener { rating ->
            selectedRating = rating
        }
        
        dialogBinding.btnSaveReview.text = "Guardar Cambios"
        dialogBinding.btnSaveReview.setOnClickListener {
            val reviewText = dialogBinding.etReviewText.text.toString().trim()
            
            if (selectedRating == 0) {
                Toast.makeText(this, "Selecciona una valoración", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (reviewText.isEmpty()) {
                Toast.makeText(this, "Escribe una reseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            bookViewModel.updateReview(review.id, userId!!, reviewText, selectedRating)
            dialog.dismiss()
        }
        
        dialogBinding.btnCancelReview.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showDeleteReviewConfirmation(review: Review) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Reseña")
            .setMessage("¿Estás seguro de que quieres eliminar esta reseña?")
            .setPositiveButton("Eliminar") { _, _ ->
                if (userId != null) {
                    bookViewModel.deleteReview(review.id, userId!!)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
} 