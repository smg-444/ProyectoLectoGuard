package es.etg.lectoguard.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivitySavedBooksBinding
import es.etg.lectoguard.domain.model.ReadingStatus
import es.etg.lectoguard.ui.viewmodel.BookViewModel
import android.widget.ArrayAdapter
import android.widget.AdapterView
import es.etg.lectoguard.utils.PrefsHelper
import es.etg.lectoguard.utils.NavigationUtils
import androidx.fragment.app.commit

@AndroidEntryPoint
class SavedBooksActivity : BaseActivity() {
    private lateinit var binding: ActivitySavedBooksBinding
    private val bookViewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = PrefsHelper.getUserId(this)
        
        // Configurar chips de filtro (definir antes de usarlos)
        val chips = listOf(
            binding.chipAll,
            binding.chipWantToRead,
            binding.chipReading,
            binding.chipRead,
            binding.chipAbandoned
        )
        
        // Cargar todas las etiquetas del usuario
        bookViewModel.loadAllUserTags(userId)
        
        // Configurar Spinner de filtro por etiquetas
        val tagFilterAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mutableListOf("Todas las etiquetas"))
        tagFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTagFilter.adapter = tagFilterAdapter
        
        // Observar etiquetas disponibles y actualizar Spinner
        bookViewModel.allUserTags.observe(this) { tags ->
            val tagList = mutableListOf("Todas las etiquetas")
            tagList.addAll(tags.sorted())
            tagFilterAdapter.clear()
            tagFilterAdapter.addAll(tagList)
        }
        
        // Listener para filtro por etiqueta
        binding.spinnerTagFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedTag = if (position == 0) null else tagFilterAdapter.getItem(position)
                
                // Desmarcar todos los chips de estado cuando se filtra por etiqueta
                chips.forEach { it.isChecked = false }
                
                if (selectedTag != null) {
                    bookViewModel.getBooksByTag(userId, selectedTag)
                } else {
                    bookViewModel.getSavedBooks(userId)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Desmarcar otros chips
                    chips.forEach { if (it != chip) it.isChecked = false }
                    
                    // Resetear filtro de etiquetas
                    binding.spinnerTagFilter.setSelection(0)
                    
                    // Cargar libros según el filtro seleccionado
                    when (chip.id) {
                        R.id.chipAll -> bookViewModel.getSavedBooks(userId)
                        R.id.chipWantToRead -> bookViewModel.getBooksByStatus(userId, ReadingStatus.WANT_TO_READ)
                        R.id.chipReading -> bookViewModel.getBooksByStatus(userId, ReadingStatus.READING)
                        R.id.chipRead -> bookViewModel.getBooksByStatus(userId, ReadingStatus.READ)
                        R.id.chipAbandoned -> bookViewModel.getBooksByStatus(userId, ReadingStatus.ABANDONED)
                    }
                }
            }
        }
        
        // Cargar todos los libros inicialmente
        bookViewModel.getSavedBooks(userId)

        binding.rvSavedBooks.layoutManager = LinearLayoutManager(this)
        
        // Observar libros guardados
        bookViewModel.savedBooks.observe(this) { books ->
            binding.rvSavedBooks.adapter = BookAdapter(books) { book ->
                val intent = Intent(this, SaveBookActivity::class.java)
                intent.putExtra("bookId", book.id)
                startActivity(intent)
            }
        }
        
        // Observar libros filtrados por estado
        bookViewModel.booksByStatus.observe(this) { books ->
            binding.rvSavedBooks.adapter = BookAdapter(books) { book ->
                val intent = Intent(this, SaveBookActivity::class.java)
                intent.putExtra("bookId", book.id)
                startActivity(intent)
            }
        }
        
        // Observar libros filtrados por etiqueta
        bookViewModel.booksByTag.observe(this) { books ->
            binding.rvSavedBooks.adapter = BookAdapter(books) { book ->
                val intent = Intent(this, SaveBookActivity::class.java)
                intent.putExtra("bookId", book.id)
                startActivity(intent)
            }
        }

        // Establecer el item seleccionado
        binding.bottomNavigation.selectedItemId = R.id.menu_saved
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
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
                R.id.menu_saved -> {
                    // Ya estamos en Saved Books, no hacer nada
                    true
                }
                else -> false
            }
        }

        supportFragmentManager.commit {
            replace(R.id.headerFragmentContainer, HeaderFragment.newInstance(getString(R.string.header_guardados)))
        }

        // Botón para acceder a listas de lectura
        binding.btnReadingLists.setOnClickListener {
            val intent = Intent(this, ReadingListsActivity::class.java)
            startActivity(intent)
        }
    }
} 