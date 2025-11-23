package es.etg.lectoguard.ui.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import es.etg.lectoguard.databinding.ActivitySaveBookBinding
import es.etg.lectoguard.data.local.LectoGuardDatabase
import es.etg.lectoguard.data.local.BookEntity
import es.etg.lectoguard.data.local.UserBookEntity
import es.etg.lectoguard.data.remote.BookApiService
import es.etg.lectoguard.data.repository.BookRepository
import es.etg.lectoguard.domain.usecase.GetBooksUseCase
import es.etg.lectoguard.domain.usecase.SaveBookUseCase
import es.etg.lectoguard.domain.usecase.GetBookDetailUseCase
import es.etg.lectoguard.ui.viewmodel.BookViewModel
import es.etg.lectoguard.utils.PrefsHelper
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import es.etg.lectoguard.utils.NetworkUtils

class SaveBookActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySaveBookBinding
    private lateinit var bookViewModel: BookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = LectoGuardDatabase.getInstance(this)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://lectoguard-default-rtdb.europe-west1.firebasedatabase.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        val api = retrofit.create(BookApiService::class.java)
        val bookRepository = BookRepository(db.bookDao(), db.userBookDao(), api)
        bookViewModel = BookViewModel(
            bookRepository,
            GetBooksUseCase(bookRepository),
            SaveBookUseCase(bookRepository),
            GetBookDetailUseCase(bookRepository)
        )

        val bookId = intent.getIntExtra("bookId", -1)
        val userId = PrefsHelper.getUserId(this)

        val isOnline = NetworkUtils.isOnline(this)
        if (!isOnline) {
            Toast.makeText(this, "Sin conexión. Mostrando datos guardados.", Toast.LENGTH_SHORT).show()
        }
        bookViewModel.getBookDetail(bookId, isOnline)
        bookViewModel.bookDetail.observe(this) { detail ->
            if (detail != null) {
                binding.tvTitle.text = detail.title
                binding.tvSinopsis.text = detail.sinopsis
                binding.tvFirstPage.text = detail.firstPage
                Glide.with(this).load(detail.coverImage).into(binding.ivCover)
                lifecycleScope.launch {
                    bookRepository.insertBook(
                        BookEntity(
                            detail.id,
                            detail.title,
                            detail.coverImage
                        )
                    )
                }
            }
        }

        bookViewModel.getBookSynopsis(bookId)
        bookViewModel.bookSynopsis.observe(this) { sinopsis ->
            if (sinopsis != null) {
                binding.tvTitle.text = sinopsis
            }
        }

        binding.btnSave.setOnClickListener {
            val userBook = UserBookEntity(userId, bookId, System.currentTimeMillis().toString())
            bookViewModel.saveBook(userBook)
        }

        bookViewModel.saveResult.observe(this) { saved ->
            if (saved == true) {
                Toast.makeText(this, "Libro guardado", Toast.LENGTH_SHORT).show()
                finish()
            } else if (saved == false) {
                Toast.makeText(this, "Este libro ya está guardado", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
} 