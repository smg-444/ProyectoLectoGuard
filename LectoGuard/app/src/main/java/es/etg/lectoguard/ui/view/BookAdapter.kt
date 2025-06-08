package es.etg.lectoguard.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.etg.lectoguard.data.local.BookEntity
import es.etg.lectoguard.databinding.ItemBookBinding
import com.bumptech.glide.Glide

class BookAdapter(
    private val books: List<BookEntity>,
    private val onClick: (BookEntity) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: BookEntity) {
            binding.tvTitle.text = book.title
            Glide.with(binding.ivCover.context)
                .load(book.coverImage)
                .into(binding.ivCover)
            binding.root.setOnClickListener { onClick(book) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size
} 