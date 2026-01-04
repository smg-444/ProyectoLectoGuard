package es.etg.lectoguard.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.etg.lectoguard.data.local.BookEntity
import es.etg.lectoguard.data.local.UserBookEntity
import es.etg.lectoguard.data.remote.BookDetailResponse
import es.etg.lectoguard.data.repository.BookRepository
import es.etg.lectoguard.data.repository.RatingRepository
import es.etg.lectoguard.domain.model.Rating
import es.etg.lectoguard.domain.model.Review
import es.etg.lectoguard.domain.usecase.GetBooksUseCase
import es.etg.lectoguard.domain.usecase.SaveBookUseCase
import es.etg.lectoguard.domain.usecase.GetBookDetailUseCase
import es.etg.lectoguard.domain.usecase.SaveRatingUseCase
import es.etg.lectoguard.domain.usecase.GetUserRatingUseCase
import es.etg.lectoguard.domain.usecase.GetAverageRatingUseCase
import es.etg.lectoguard.domain.usecase.SaveReviewUseCase
import es.etg.lectoguard.domain.usecase.GetBookReviewsUseCase
import es.etg.lectoguard.domain.usecase.ToggleLikeReviewUseCase
import es.etg.lectoguard.domain.usecase.UpdateReviewUseCase
import es.etg.lectoguard.domain.usecase.DeleteReviewUseCase
import es.etg.lectoguard.domain.usecase.UpdateBookReadingStatusUseCase
import es.etg.lectoguard.domain.usecase.GetUserBookUseCase
import es.etg.lectoguard.domain.usecase.GetBooksByStatusUseCase
import es.etg.lectoguard.domain.usecase.GetBookCountByStatusUseCase
import es.etg.lectoguard.domain.usecase.UpdateBookTagsUseCase
import es.etg.lectoguard.domain.usecase.GetAllUserTagsUseCase
import es.etg.lectoguard.domain.usecase.GetBooksByTagUseCase
import es.etg.lectoguard.domain.model.ReadingStatus
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    val repository: BookRepository,
    private val getBooksUseCase: GetBooksUseCase,
    private val saveBookUseCase: SaveBookUseCase,
    private val getBookDetailUseCase: GetBookDetailUseCase,
    private val ratingRepository: RatingRepository? = null,
    private val updateReadingStatusUseCase: UpdateBookReadingStatusUseCase? = null,
    private val getUserBookUseCase: GetUserBookUseCase? = null,
    private val getBooksByStatusUseCase: GetBooksByStatusUseCase? = null,
    private val getBookCountByStatusUseCase: GetBookCountByStatusUseCase? = null,
    private val updateBookTagsUseCase: UpdateBookTagsUseCase? = null,
    private val getAllUserTagsUseCase: GetAllUserTagsUseCase? = null,
    private val getBooksByTagUseCase: GetBooksByTagUseCase? = null
) : ViewModel() {

    val books = MutableLiveData<List<BookEntity>>(emptyList())
    val bookDetail = MutableLiveData<BookDetailResponse?>()
    val saveResult = MutableLiveData<Boolean?>()
    val bookSynopsis = MutableLiveData<String?>()
    val savedBooks = MutableLiveData<List<BookEntity>>()
    
    // Valoraciones
    val userRating = MutableLiveData<Rating?>()
    val averageRating = MutableLiveData<Double>()
    val saveRatingResult = MutableLiveData<Boolean>()
    
    // Reseñas
    val reviews = MutableLiveData<List<Review>>()
    val saveReviewResult = MutableLiveData<String?>() // ID de la reseña guardada
    val toggleLikeResult = MutableLiveData<Boolean>()
    
    // Estados de lectura
    val currentUserBook = MutableLiveData<UserBookEntity?>()
    val updateReadingStatusResult = MutableLiveData<Boolean>()
    val booksByStatus = MutableLiveData<List<BookEntity>>()
    val bookCountByStatus = MutableLiveData<Map<ReadingStatus, Int>>()
    
    // Etiquetas
    val updateTagsResult = MutableLiveData<Boolean>()
    val allUserTags = MutableLiveData<Set<String>>()
    val booksByTag = MutableLiveData<List<BookEntity>>()

    fun loadBooks(isOnline: Boolean) {
        viewModelScope.launch {
            android.util.Log.d("BookViewModel", "loadBooks llamado con isOnline=$isOnline")
            val result = repository.getAllBooks(isOnline)
            android.util.Log.d("BookViewModel", "Resultado de getAllBooks: ${result?.size} libros")
            books.postValue(result ?: emptyList())
        }
    }

    fun saveBook(userBook: UserBookEntity, firebaseUid: String? = null, bookTitle: String? = null, bookCoverUrl: String? = null) {
        viewModelScope.launch {
            val result = saveBookUseCase(userBook, firebaseUid, bookTitle, bookCoverUrl)
            saveResult.postValue(result)
            
            // Crear feed item si se guardó exitosamente
            if (result && firebaseUid != null) {
                val title = bookTitle ?: bookDetail.value?.title ?: "Libro #${userBook.bookId}"
                val cover = bookCoverUrl ?: bookDetail.value?.coverImage
                android.util.Log.d("BookViewModel", "Creando feed item para libro guardado: userId=$firebaseUid, bookId=${userBook.bookId}, title=$title")
                es.etg.lectoguard.data.repository.FeedHelper.createBookSavedFeedItem(
                    firebaseUid,
                    userBook.bookId,
                    title,
                    cover,
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                )
            }
        }
    }

    fun getBookSynopsis(id: Int) {
        viewModelScope.launch {
            val response = repository.api.getBookSynopsis(id)
            if (response.isSuccessful) {
                bookSynopsis.postValue(response.body())
            } else {
                bookSynopsis.postValue(null)
            }
        }
    }

    fun getBookDetail(id: Int, isOnline: Boolean) {
        viewModelScope.launch {
            if (isOnline) {
                val response = repository.getBookDetailOnline(id)
                if (response.isSuccessful) {
                    val detail = response.body()
                    bookDetail.postValue(detail)
                    if (detail != null) {
                        // Convertir géneros de String a BookGenre
                        val genres = detail.genres.mapNotNull { genreName ->
                            try {
                                es.etg.lectoguard.domain.model.BookGenre.valueOf(genreName.uppercase().replace(" ", "_").replace("-", "_"))
                            } catch (e: Exception) {
                                // Intentar mapear nombres comunes o géneros no estándar
                                when (genreName.uppercase().replace(" ", "_").replace("-", "_")) {
                                    "FICTION", "FICCIÓN" -> es.etg.lectoguard.domain.model.BookGenre.FICTION
                                    "SCIENCE_FICTION", "SCIENCEFICTION", "CIENCIA_FICCIÓN" -> es.etg.lectoguard.domain.model.BookGenre.SCIENCE_FICTION
                                    "FANTASY", "FANTASÍA" -> es.etg.lectoguard.domain.model.BookGenre.FANTASY
                                    "MYSTERY", "MISTERIO" -> es.etg.lectoguard.domain.model.BookGenre.MYSTERY
                                    "THRILLER" -> es.etg.lectoguard.domain.model.BookGenre.THRILLER
                                    "ROMANCE" -> es.etg.lectoguard.domain.model.BookGenre.ROMANCE
                                    "HISTORICAL", "HISTÓRICA" -> es.etg.lectoguard.domain.model.BookGenre.HISTORICAL
                                    "CLASSIC", "CLÁSICO" -> es.etg.lectoguard.domain.model.BookGenre.CLASSIC
                                    "ADVENTURE", "AVENTURA" -> es.etg.lectoguard.domain.model.BookGenre.ADVENTURE
                                    "CHILDREN", "INFANTIL" -> es.etg.lectoguard.domain.model.BookGenre.CHILDREN
                                    "YOUNG_ADULT", "YOUNGADULT", "JUVENIL" -> es.etg.lectoguard.domain.model.BookGenre.YOUNG_ADULT
                                    "MAGICAL_REALISM", "MAGICALREALISM" -> es.etg.lectoguard.domain.model.BookGenre.FICTION
                                    "RELIGIOUS", "RELIGIOSO" -> es.etg.lectoguard.domain.model.BookGenre.NON_FICTION
                                    "SATIRE", "SÁTIRA" -> es.etg.lectoguard.domain.model.BookGenre.FICTION
                                    else -> null
                                }
                            }
                        }
                        repository.insertBook(BookEntity(
                            detail.id, 
                            detail.title, 
                            detail.coverImage,
                            genres = if (genres.isEmpty()) listOf(es.etg.lectoguard.domain.model.BookGenre.OTHER) else genres
                        ))
                    }
                } else {
                    val bookEntity = repository.getBookById(id)
                    bookDetail.postValue(
                        bookEntity?.let {
                            BookDetailResponse(
                                id = it.id,
                                title = it.title,
                                coverImage = it.coverImage,
                                sinopsis = "",
                                firstPage = "",
                                genres = it.genres.map { g -> g.name }
                            )
                        }
                    )
                }
            } else {
                val bookEntity = repository.getBookById(id)
                bookDetail.postValue(
                    bookEntity?.let {
                        BookDetailResponse(
                            id = it.id,
                            title = it.title,
                            coverImage = it.coverImage,
                            sinopsis = "",
                            firstPage = "",
                            genres = it.genres.map { g -> g.name }
                        )
                    }
                )
            }
        }
    }

    fun getSavedBooks(userId: Int) {
        viewModelScope.launch {
            val books = repository.getBooksByUser(userId)
            savedBooks.postValue(books)
        }
    }
    
    // Valoraciones
    fun saveRating(rating: Rating, bookTitle: String? = null, bookCoverUrl: String? = null) {
        val ratingRepository = this.ratingRepository ?: run {
            android.util.Log.e("BookViewModel", "RatingRepository es null")
            return
        }
        viewModelScope.launch {
            android.util.Log.d("BookViewModel", "Guardando valoración: ${rating.bookId}, ${rating.userId}, ${rating.rating}")
            val result = SaveRatingUseCase(ratingRepository)(rating)
            android.util.Log.d("BookViewModel", "Resultado guardar valoración: $result")
            saveRatingResult.postValue(result)
            if (result) {
                userRating.postValue(rating)
                loadAverageRating(rating.bookId)
                
                // Crear feed item (siempre, incluso si falta información del libro)
                val title = bookTitle ?: bookDetail.value?.title ?: "Libro #${rating.bookId}"
                val cover = bookCoverUrl ?: bookDetail.value?.coverImage
                android.util.Log.d("BookViewModel", "Creando feed item para valoración: userId=${rating.userId}, bookId=${rating.bookId}, title=$title")
                es.etg.lectoguard.data.repository.FeedHelper.createRatingFeedItem(
                    rating.userId,
                    rating.bookId,
                    rating.rating,
                    title,
                    cover,
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                )
            } else {
                android.util.Log.e("BookViewModel", "Error al guardar valoración")
            }
        }
    }
    
    fun loadUserRating(bookId: Int, userId: String) {
        val ratingRepository = this.ratingRepository ?: return
        viewModelScope.launch {
            val rating = GetUserRatingUseCase(ratingRepository)(bookId, userId)
            userRating.postValue(rating)
        }
    }
    
    fun loadAverageRating(bookId: Int) {
        val ratingRepository = this.ratingRepository ?: return
        viewModelScope.launch {
            val avg = GetAverageRatingUseCase(ratingRepository)(bookId)
            averageRating.postValue(avg)
        }
    }
    
    // Reseñas
    fun saveReview(review: Review, bookTitle: String? = null, bookCoverUrl: String? = null) {
        val ratingRepository = this.ratingRepository ?: return
        viewModelScope.launch {
            val reviewId = SaveReviewUseCase(ratingRepository)(review)
            saveReviewResult.postValue(reviewId)
            if (reviewId != null) {
                loadReviews(review.bookId)
                
                // Crear feed item (siempre, incluso si falta información del libro)
                val title = bookTitle ?: bookDetail.value?.title ?: "Libro #${review.bookId}"
                val cover = bookCoverUrl ?: bookDetail.value?.coverImage
                android.util.Log.d("BookViewModel", "Creando feed item para reseña: userId=${review.userId}, bookId=${review.bookId}, reviewId=$reviewId, title=$title")
                es.etg.lectoguard.data.repository.FeedHelper.createReviewFeedItem(
                    review.userId,
                    review.bookId,
                    reviewId,
                    review.text,
                    review.rating,
                    title,
                    cover,
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                )
            }
        }
    }
    
    fun loadReviews(bookId: Int) {
        val ratingRepository = this.ratingRepository ?: return
        viewModelScope.launch {
            val reviewsList = GetBookReviewsUseCase(ratingRepository)(bookId)
            reviews.postValue(reviewsList)
        }
    }
    
    fun toggleLikeReview(reviewId: String, userId: String, isLiked: Boolean) {
        val ratingRepository = this.ratingRepository ?: return
        viewModelScope.launch {
            val result = ToggleLikeReviewUseCase(ratingRepository)(reviewId, userId, isLiked)
            toggleLikeResult.postValue(result)
            // Recargar reseñas para actualizar los likes
            val currentReviews = reviews.value
            if (currentReviews != null) {
                val review = currentReviews.find { it.id == reviewId }
                if (review != null) {
                    val bookId = review.bookId
                    loadReviews(bookId)
                }
            }
        }
    }
    
    val updateReviewResult = MutableLiveData<Boolean?>()
    val deleteReviewResult = MutableLiveData<Boolean?>()
    
    fun updateReview(reviewId: String, userId: String, newText: String, newRating: Int) {
        val ratingRepository = this.ratingRepository ?: return
        viewModelScope.launch {
            val result = UpdateReviewUseCase(ratingRepository)(reviewId, userId, newText, newRating)
            updateReviewResult.postValue(result)
            if (result) {
                // Recargar reseñas
                val currentReviews = reviews.value
                if (currentReviews != null) {
                    val review = currentReviews.find { it.id == reviewId }
                    if (review != null) {
                        loadReviews(review.bookId)
                    }
                }
            }
        }
    }
    
    fun deleteReview(reviewId: String, userId: String) {
        val ratingRepository = this.ratingRepository ?: return
        viewModelScope.launch {
            val result = DeleteReviewUseCase(ratingRepository)(reviewId, userId)
            deleteReviewResult.postValue(result)
            if (result) {
                // Recargar reseñas
                val currentReviews = reviews.value
                if (currentReviews != null) {
                    val review = currentReviews.find { it.id == reviewId }
                    if (review != null) {
                        loadReviews(review.bookId)
                    }
                }
            }
        }
    }
    
    // Estados de lectura
    fun getUserBook(userId: Int, bookId: Int) {
        val getUserBookUseCase = this.getUserBookUseCase ?: run {
            viewModelScope.launch {
                val userBook = repository.getUserBook(userId, bookId)
                currentUserBook.postValue(userBook)
            }
            return
        }
        viewModelScope.launch {
            val userBook = getUserBookUseCase(userId, bookId)
            currentUserBook.postValue(userBook)
        }
    }
    
    fun updateReadingStatus(userId: Int, bookId: Int, status: ReadingStatus) {
        val updateReadingStatusUseCase = this.updateReadingStatusUseCase ?: run {
            viewModelScope.launch {
                val result = repository.updateReadingStatus(userId, bookId, status)
                updateReadingStatusResult.postValue(result)
                if (result) {
                    getUserBook(userId, bookId)
                }
            }
            return
        }
        viewModelScope.launch {
            val result = updateReadingStatusUseCase(userId, bookId, status)
            updateReadingStatusResult.postValue(result)
            if (result) {
                getUserBook(userId, bookId)
            }
        }
    }
    
    fun getBooksByStatus(userId: Int, status: ReadingStatus) {
        val getBooksByStatusUseCase = this.getBooksByStatusUseCase ?: run {
            viewModelScope.launch {
                val books = repository.getBooksByUserAndStatus(userId, status)
                booksByStatus.postValue(books)
            }
            return
        }
        viewModelScope.launch {
            val books = getBooksByStatusUseCase(userId, status)
            booksByStatus.postValue(books)
        }
    }
    
    fun loadBookCountsByStatus(userId: Int) {
        val getBookCountByStatusUseCase = this.getBookCountByStatusUseCase ?: run {
            viewModelScope.launch {
                val counts = mapOf(
                    ReadingStatus.WANT_TO_READ to repository.getBookCountByStatus(userId, ReadingStatus.WANT_TO_READ),
                    ReadingStatus.READING to repository.getBookCountByStatus(userId, ReadingStatus.READING),
                    ReadingStatus.READ to repository.getBookCountByStatus(userId, ReadingStatus.READ),
                    ReadingStatus.ABANDONED to repository.getBookCountByStatus(userId, ReadingStatus.ABANDONED)
                )
                bookCountByStatus.postValue(counts)
            }
            return
        }
        viewModelScope.launch {
            val counts = mapOf(
                ReadingStatus.WANT_TO_READ to getBookCountByStatusUseCase(userId, ReadingStatus.WANT_TO_READ),
                ReadingStatus.READING to getBookCountByStatusUseCase(userId, ReadingStatus.READING),
                ReadingStatus.READ to getBookCountByStatusUseCase(userId, ReadingStatus.READ),
                ReadingStatus.ABANDONED to getBookCountByStatusUseCase(userId, ReadingStatus.ABANDONED)
            )
            bookCountByStatus.postValue(counts)
        }
    }
    
    // Etiquetas
    fun updateBookTags(userId: Int, bookId: Int, tags: List<String>) {
        val updateBookTagsUseCase = this.updateBookTagsUseCase ?: run {
            viewModelScope.launch {
                val result = repository.updateBookTags(userId, bookId, tags)
                updateTagsResult.postValue(result)
                if (result) {
                    getUserBook(userId, bookId)
                }
            }
            return
        }
        viewModelScope.launch {
            val result = updateBookTagsUseCase(userId, bookId, tags)
            updateTagsResult.postValue(result)
            if (result) {
                getUserBook(userId, bookId)
            }
        }
    }
    
    fun loadAllUserTags(userId: Int) {
        val getAllUserTagsUseCase = this.getAllUserTagsUseCase ?: run {
            viewModelScope.launch {
                val tags = repository.getAllUserTags(userId)
                allUserTags.postValue(tags)
            }
            return
        }
        viewModelScope.launch {
            val tags = getAllUserTagsUseCase(userId)
            allUserTags.postValue(tags)
        }
    }
    
    fun getBooksByTag(userId: Int, tag: String) {
        val getBooksByTagUseCase = this.getBooksByTagUseCase ?: run {
            viewModelScope.launch {
                val books = repository.getBooksByTag(userId, tag)
                booksByTag.postValue(books)
            }
            return
        }
        viewModelScope.launch {
            val books = getBooksByTagUseCase(userId, tag)
            booksByTag.postValue(books)
        }
    }
} 