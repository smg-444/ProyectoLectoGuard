package es.etg.lectoguard.data.repository

import es.etg.lectoguard.data.local.BookDao
import es.etg.lectoguard.data.local.UserBookDao
import es.etg.lectoguard.data.local.BookEntity
import es.etg.lectoguard.data.local.UserBookEntity
import es.etg.lectoguard.data.remote.BookApiService
import es.etg.lectoguard.data.remote.UserBookService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookRepository(
    val bookDao: BookDao,
    private val userBookDao: UserBookDao,
    val api: BookApiService,
    private val userBookService: UserBookService? = null
) {
    suspend fun getAllBooks(isOnline: Boolean): List<BookEntity> {
        return if (isOnline) {
            // Intentar cargar desde Realtime Database
            try {
                android.util.Log.d("BookRepository", "Intentando cargar libros desde Realtime Database...")
                val response = api.getAllBooks()
                android.util.Log.d("BookRepository", "Respuesta recibida: isSuccessful=${response.isSuccessful}, code=${response.code()}")
                if (response.isSuccessful) {
                    val booksList = response.body()
                    android.util.Log.d("BookRepository", "Lista de libros recibida: ${booksList?.size} elementos, body is null: ${booksList == null}")
                    
                    // Si el body es null, intentar desde BD local como fallback
                    if (booksList == null) {
                        android.util.Log.w("BookRepository", "Response body es null. Verifica que libros.json esté subido a Realtime Database en /libros.json")
                        // Intentar desde BD local como fallback
                        val localBooks = bookDao.getAllBooks()
                        if (localBooks.isNotEmpty()) {
                            android.util.Log.d("BookRepository", "Usando ${localBooks.size} libros de BD local (fallback)")
                            return localBooks
                        }
                    }
                    
                    if (booksList != null) {
                        android.util.Log.d("BookRepository", "Lista recibida con ${booksList.size} elementos")
                        // Convertir el array a lista de BookEntity
                        // Realtime Database devuelve [null, {...}, {...}, ...]
                        val books = booksList
                            .filterNotNull() // Filtrar valores null (como el índice 0)
                            .mapNotNull { bookData ->
                                try {
                                    // Convertir Any? a Map<String, Any?> para acceder a los campos
                                    val bookMap = bookData as? Map<*, *> ?: return@mapNotNull null
                                    
                                    // Extraer campos del libro
                                    val id = (bookMap["id"] as? Number)?.toInt() ?: return@mapNotNull null
                                    val title = bookMap["title"] as? String ?: return@mapNotNull null
                                    val coverImage = bookMap["coverImage"] as? String ?: ""
                                    val genresList = (bookMap["genres"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                                    
                                    android.util.Log.d("BookRepository", "Procesando libro: id=$id, title=$title, genres=$genresList")
                                    
                                    // Convertir géneros de String a BookGenre
                                    val genres = genresList.mapNotNull { genreName ->
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
                                    
                                    BookEntity(
                                        id = id,
                                        title = title,
                                        coverImage = coverImage,
                                        genres = if (genres.isEmpty()) listOf(es.etg.lectoguard.domain.model.BookGenre.OTHER) else genres
                                    )
                                } catch (e: Exception) {
                                    android.util.Log.e("BookRepository", "Error procesando libro: ${e.message}", e)
                                    null
                                }
                            }
                            .sortedBy { it.id } // Ordenar por ID
                        
                        // Guardar en la base de datos local
                        books.forEach { book ->
                            try {
                                bookDao.insert(book)
                            } catch (e: Exception) {
                                // Ignorar errores de duplicados (si el libro ya existe)
                                android.util.Log.d("BookRepository", "Libro ${book.id} ya existe en BD local")
                            }
                        }
                        
                        android.util.Log.d("BookRepository", "Cargados ${books.size} libros desde Realtime Database")
                        return books
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BookRepository", "Error cargando libros desde Realtime Database: ${e.message}", e)
            }
            
            // Si falla, intentar desde BD local
            val localBooks = bookDao.getAllBooks()
            android.util.Log.d("BookRepository", "Libros en BD local: ${localBooks.size}")
            if (localBooks.isNotEmpty()) {
                android.util.Log.d("BookRepository", "Usando ${localBooks.size} libros de BD local (fallback después de error)")
                return localBooks
            } else {
                android.util.Log.d("BookRepository", "No hay libros en BD local, usando mock books")
                return getMockBooks()
            }
        } else {
            // Modo offline: usar BD local
            val localBooks = bookDao.getAllBooks()
            if (localBooks.isNotEmpty()) {
                localBooks
            } else {
                getMockBooks()
            }
        }
    }

    suspend fun getBookById(id: Int) = bookDao.getBookById(id)
    suspend fun saveBook(userBook: UserBookEntity, firebaseUid: String? = null, bookTitle: String? = null, bookCoverUrl: String? = null): Boolean {
        val existing = userBookDao.getBooksByUser(userBook.userId).any { it.bookId == userBook.bookId }
        return if (!existing) {
            userBookDao.insert(userBook)
            
            // Sincronizar con Firestore para recomendaciones
            if (firebaseUid != null && userBookService != null) {
                val title = bookTitle ?: bookDao.getBookById(userBook.bookId)?.title ?: "Libro #${userBook.bookId}"
                val cover = bookCoverUrl ?: bookDao.getBookById(userBook.bookId)?.coverImage
                
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        userBookService.saveUserBook(
                            firebaseUid,
                            userBook.bookId,
                            title,
                            cover
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("BookRepository", "Error sincronizando libro con Firestore: ${e.message}")
                    }
                }
            }
            
            true
        } else {
            false
        }
    }
    suspend fun getBooksByUser(userId: Int): List<BookEntity> {
        val userBooks = userBookDao.getBooksByUser(userId)
        val books = mutableListOf<BookEntity>()
        for (userBook in userBooks) {
            bookDao.getBookById(userBook.bookId)?.let { books.add(it) }
        }
        return books
    }
    suspend fun getBookDetailOnline(id: Int) = api.getBookDetail(id)
    suspend fun insertBook(book: BookEntity) {
        // Si el libro ya existe, actualizarlo con los géneros
        val existing = bookDao.getBookById(book.id)
        if (existing != null) {
            // Actualizar géneros si el libro ya existe
            val updated = existing.copy(genres = book.genres)
            bookDao.update(updated)
        } else {
            bookDao.insert(book)
        }
    }
    
    suspend fun updateBook(book: BookEntity) = bookDao.update(book)
    
    suspend fun getUserBook(userId: Int, bookId: Int): UserBookEntity? {
        return userBookDao.getBookByUserAndBookId(userId, bookId)
    }
    
    suspend fun updateReadingStatus(userId: Int, bookId: Int, status: es.etg.lectoguard.domain.model.ReadingStatus): Boolean {
        val userBook = userBookDao.getBookByUserAndBookId(userId, bookId)
        return if (userBook != null) {
            val updated = userBook.copy(readingStatus = status)
            userBookDao.update(updated)
            true
        } else {
            false
        }
    }
    
    suspend fun getBooksByUserAndStatus(userId: Int, status: es.etg.lectoguard.domain.model.ReadingStatus): List<BookEntity> {
        val userBooks = userBookDao.getBooksByUserAndStatus(userId, status.name)
        val books = mutableListOf<BookEntity>()
        for (userBook in userBooks) {
            bookDao.getBookById(userBook.bookId)?.let { books.add(it) }
        }
        return books
    }
    
    suspend fun getBookCountByStatus(userId: Int, status: es.etg.lectoguard.domain.model.ReadingStatus): Int {
        return userBookDao.getBookCountByStatus(userId, status.name)
    }
    
    suspend fun updateBookTags(userId: Int, bookId: Int, tags: List<String>): Boolean {
        val userBook = userBookDao.getBookByUserAndBookId(userId, bookId)
        return if (userBook != null) {
            val updated = userBook.copy(tags = tags)
            userBookDao.update(updated)
            true
        } else {
            false
        }
    }
    
    suspend fun getAllUserTags(userId: Int): Set<String> {
        val userBooks = userBookDao.getAllUserBooks(userId)
        return userBooks.flatMap { it.tags }.toSet()
    }
    
    suspend fun getBooksByTag(userId: Int, tag: String): List<BookEntity> {
        val userBooks = userBookDao.getAllUserBooks(userId)
        val filteredUserBooks = userBooks.filter { it.tags.contains(tag) }
        val books = mutableListOf<BookEntity>()
        for (userBook in filteredUserBooks) {
            bookDao.getBookById(userBook.bookId)?.let { books.add(it) }
        }
        return books
    }

    fun getMockBooks(): List<BookEntity> = listOf(
        BookEntity(1, "El Quijote", "https://media.gettyimages.com/id/1172200712/es/vector/don-quijote-y-sancho-panza.jpg?s=612x612&w=gi&k=20&c=MCfC-fRkJHKp-RZNPDKXsVxGGalsQMkUpN-cfi8V3qk=", genres = listOf(es.etg.lectoguard.domain.model.BookGenre.CLASSIC, es.etg.lectoguard.domain.model.BookGenre.FICTION)),
        BookEntity(2, "Cien años de soledad", "https://m.media-amazon.com/images/I/91TvVQS7loL.jpg", genres = listOf(es.etg.lectoguard.domain.model.BookGenre.FICTION)),
        BookEntity(3, "La sombra del viento", "https://www.planetadelibros.com/usuaris/libros/fotos/222/original/portada___201609051317.jpg", genres = listOf(es.etg.lectoguard.domain.model.BookGenre.MYSTERY, es.etg.lectoguard.domain.model.BookGenre.THRILLER))
    )
} 