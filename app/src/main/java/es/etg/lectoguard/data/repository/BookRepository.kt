package es.etg.lectoguard.data.repository

import es.etg.lectoguard.data.local.BookDao
import es.etg.lectoguard.data.local.UserBookDao
import es.etg.lectoguard.data.local.BookEntity
import es.etg.lectoguard.data.local.UserBookEntity
import es.etg.lectoguard.data.remote.BookApiService

class BookRepository(
    val bookDao: BookDao,
    private val userBookDao: UserBookDao,
    val api: BookApiService
) {
    suspend fun getAllBooks(isOnline: Boolean): List<BookEntity> {
        return if (isOnline) {
            val localBooks = bookDao.getAllBooks()
            if (localBooks.isNotEmpty()) {
                localBooks
            } else {
                getMockBooks()
            }
        } else {
            val localBooks = bookDao.getAllBooks()
            if (localBooks.isNotEmpty()) {
                localBooks
            } else {
                getMockBooks()
            }
        }
    }

    suspend fun getBookById(id: Int) = bookDao.getBookById(id)
    suspend fun saveBook(userBook: UserBookEntity): Boolean {
        val existing = userBookDao.getBooksByUser(userBook.userId).any { it.bookId == userBook.bookId }
        return if (!existing) {
            userBookDao.insert(userBook)
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
    suspend fun insertBook(book: BookEntity) = bookDao.insert(book)

    fun getMockBooks(): List<BookEntity> = listOf(
        BookEntity(1, "El Quijote", "https://media.gettyimages.com/id/1172200712/es/vector/don-quijote-y-sancho-panza.jpg?s=612x612&w=gi&k=20&c=MCfC-fRkJHKp-RZNPDKXsVxGGalsQMkUpN-cfi8V3qk="),
        BookEntity(2, "Cien años de soledad", "https://m.media-amazon.com/images/I/91TvVQS7loL.jpg"),
        BookEntity(3, "La sombra del viento", "https://www.planetadelibros.com/usuaris/libros/fotos/222/original/portada___201609051317.jpg")
    )
} 