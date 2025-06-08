package es.etg.lectoguard.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivitySavedBooksBinding
import es.etg.lectoguard.data.local.LectoGuardDatabase
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
import androidx.fragment.app.commit

class SavedBooksActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySavedBooksBinding
    private lateinit var bookViewModel: BookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedBooksBinding.inflate(layoutInflater)
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

        val userId = PrefsHelper.getUserId(this)
        bookViewModel.getSavedBooks(userId)

        binding.rvSavedBooks.layoutManager = LinearLayoutManager(this)
        bookViewModel.savedBooks.observe(this) { books ->
            binding.rvSavedBooks.adapter = BookAdapter(books) {}
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    finish()
                    true
                }
                R.id.menu_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        supportFragmentManager.commit {
            replace(R.id.headerFragmentContainer, HeaderFragment.newInstance("Libros Guardados"))
        }
    }
} 