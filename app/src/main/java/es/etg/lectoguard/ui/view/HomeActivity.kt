package es.etg.lectoguard.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivityHomeBinding
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
import es.etg.lectoguard.utils.NetworkUtils
import androidx.fragment.app.commit

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var bookViewModel: BookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
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

        val userName = PrefsHelper.getUserName(this)
        binding.tvWelcome.text = "Bienvenido, $userName"

        binding.rvBooks.layoutManager = LinearLayoutManager(this)
        supportFragmentManager.commit {
            replace(R.id.headerFragmentContainer, HeaderFragment.newInstance("Inicio"))
        }
        bookViewModel.books.observe(this) { books ->
            val allBooks = if (books.size <= 1) bookRepository.getMockBooks() else books
            binding.rvBooks.adapter = BookAdapter(allBooks) { book ->
                val intent = Intent(this, SaveBookActivity::class.java)
                intent.putExtra("bookId", book.id)
                startActivity(intent)
            }
        }
        val isOnline = NetworkUtils.isOnline(this)
        if (!isOnline) {
            Toast.makeText(this, "Sin conexión. Mostrando datos guardados.", Toast.LENGTH_SHORT).show()
        }
        bookViewModel.loadBooks(isOnline)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_saved -> {
                    startActivity(Intent(this, SavedBooksActivity::class.java))
                    true
                }
                R.id.menu_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
} 