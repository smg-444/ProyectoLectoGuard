package es.etg.lectoguard.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivityReadingListDetailBinding
import es.etg.lectoguard.data.local.BookEntity
import es.etg.lectoguard.data.local.BookDao
import es.etg.lectoguard.ui.viewmodel.ReadingListViewModel
import es.etg.lectoguard.utils.PrefsHelper
import androidx.fragment.app.commit
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReadingListDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityReadingListDetailBinding
    private val viewModel: ReadingListViewModel by viewModels()
    
    @Inject
    lateinit var bookDao: BookDao
    
    private lateinit var adapter: BookInListAdapter
    private var listId: String = ""
    private var currentBooks: MutableList<BookEntity> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadingListDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listId = intent.getStringExtra("listId") ?: run {
            Toast.makeText(this, "Error: ID de lista no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        supportFragmentManager.commit {
            replace(R.id.headerFragmentContainer, HeaderFragment.newInstance("Detalle de Lista"))
        }

        // Configurar RecyclerView
        binding.rvBooks.layoutManager = LinearLayoutManager(this)
        adapter = BookInListAdapter(emptyList()) { book ->
            showRemoveBookDialog(book)
        }
        binding.rvBooks.adapter = adapter

        // Configurar ItemTouchHelper para drag & drop
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                // Actualizar lista local
                val movedBook = currentBooks.removeAt(fromPosition)
                currentBooks.add(toPosition, movedBook)
                adapter.notifyItemMoved(fromPosition, toPosition)

                // Actualizar orden en el ViewModel
                val newOrder = currentBooks.map { it.id }
                viewModel.updateListOrder(listId, newOrder)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // No implementamos swipe, solo drag & drop
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true // Permitir arrastrar con presión larga
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvBooks)

        // Observar lista actual
        lifecycleScope.launch {
            viewModel.currentList.collect { list ->
                if (list != null) {
                    updateListInfo(list)
                    loadBooksInList(list.bookIds)
                }
            }
        }

        // Botón para agregar libro
        binding.fabAddBook.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("addToListId", listId)
            startActivity(intent)
        }

        // Cargar lista
        viewModel.loadReadingList(listId)
    }

    private fun updateListInfo(list: es.etg.lectoguard.domain.model.ReadingList) {
        binding.tvListName.text = list.name
        binding.tvDescription.text = list.description.ifEmpty { "Sin descripción" }
        binding.tvBookCount.text = "${list.bookIds.size} libro${if (list.bookIds.size != 1) "s" else ""}"
        
        if (list.isPublic && list.followerCount > 0) {
            binding.tvFollowerCount.visibility = android.view.View.VISIBLE
            binding.tvFollowerCount.text = " • ${list.followerCount} seguidor${if (list.followerCount != 1) "es" else ""}"
        } else {
            binding.tvFollowerCount.visibility = android.view.View.GONE
        }
    }

    private fun loadBooksInList(bookIds: List<Int>) {
        lifecycleScope.launch {
            val books = mutableListOf<BookEntity>()
            
            // Cargar libros en el orden especificado
            for (bookId in bookIds) {
                bookDao.getBookById(bookId)?.let { book -> books.add(book) }
            }
            
            currentBooks = books.toMutableList()
            adapter.updateBooks(books)
            binding.tvEmpty.visibility = if (books.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun showRemoveBookDialog(book: BookEntity) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar libro")
            .setMessage("¿Quieres eliminar \"${book.title}\" de esta lista?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.removeBookFromList(listId, book.id)
                Toast.makeText(this, "Libro eliminado de la lista", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Recargar lista al volver a la actividad (por si se agregó un libro)
        viewModel.loadReadingList(listId)
    }
}

