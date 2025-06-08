package es.etg.lectoguard.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.etg.lectoguard.data.local.BookEntity
import es.etg.lectoguard.data.local.UserBookEntity
import es.etg.lectoguard.data.remote.BookDetailResponse
import es.etg.lectoguard.data.repository.BookRepository
import es.etg.lectoguard.domain.usecase.GetBooksUseCase
import es.etg.lectoguard.domain.usecase.SaveBookUseCase
import es.etg.lectoguard.domain.usecase.GetBookDetailUseCase
import kotlinx.coroutines.launch

class BookViewModel(
    val repository: BookRepository,
    private val getBooksUseCase: GetBooksUseCase,
    private val saveBookUseCase: SaveBookUseCase,
    private val getBookDetailUseCase: GetBookDetailUseCase
) : ViewModel() {

    val books = MutableLiveData<List<BookEntity>>(emptyList())
    val bookDetail = MutableLiveData<BookDetailResponse?>()
    val saveResult = MutableLiveData<Boolean?>()
    val bookSynopsis = MutableLiveData<String?>()
    val savedBooks = MutableLiveData<List<BookEntity>>()

    fun loadBooks(isOnline: Boolean) {
        viewModelScope.launch {
            val result = repository.getAllBooks(isOnline)
            books.postValue(result ?: emptyList())
        }
    }

    fun saveBook(userBook: UserBookEntity) {
        viewModelScope.launch {
            val result = saveBookUseCase(userBook)
            saveResult.postValue(result)
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
                        repository.insertBook(BookEntity(detail.id, detail.title, detail.coverImage))
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
                                firstPage = ""
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
                            firstPage = ""
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
} 