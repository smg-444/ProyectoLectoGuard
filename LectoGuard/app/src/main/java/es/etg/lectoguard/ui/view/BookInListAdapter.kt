package es.etg.lectoguard.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import es.etg.lectoguard.databinding.ItemBookInListBinding
import es.etg.lectoguard.data.local.BookEntity

class BookInListAdapter(
    private var books: List<BookEntity>,
    private val onRemoveClick: (BookEntity) -> Unit
) : RecyclerView.Adapter<BookInListAdapter.BookViewHolder>() {

    fun updateBooks(newBooks: List<BookEntity>) {
        books = newBooks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookInListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size

    inner class BookViewHolder(
        private val binding: ItemBookInListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: BookEntity) {
            binding.tvBookTitle.text = book.title
            binding.tvBookId.text = "ID: ${book.id}"

            // Cargar imagen de portada
            Glide.with(binding.root.context)
                .load(book.coverImage)
                .into(binding.ivBookCover)

            // Botón eliminar
            binding.btnRemove.setOnClickListener {
                onRemoveClick(book)
            }
        }
    }
}

