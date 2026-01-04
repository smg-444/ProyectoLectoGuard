package es.etg.lectoguard.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.etg.lectoguard.databinding.ItemReadingListBinding
import es.etg.lectoguard.domain.model.ReadingList

class ReadingListAdapter(
    private var lists: List<ReadingList>,
    private val onListClick: (ReadingList) -> Unit,
    private val onListLongClick: ((ReadingList) -> Unit)? = null,
    private val bookId: Int? = null // Para marcar si el libro está en la lista
) : RecyclerView.Adapter<ReadingListAdapter.ReadingListViewHolder>() {

    fun updateLists(newLists: List<ReadingList>) {
        lists = newLists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingListViewHolder {
        val binding = ItemReadingListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReadingListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReadingListViewHolder, position: Int) {
        holder.bind(lists[position])
    }

    override fun getItemCount() = lists.size

    inner class ReadingListViewHolder(
        private val binding: ItemReadingListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(list: ReadingList) {
            binding.tvListName.text = list.name
            binding.tvDescription.text = list.description.ifEmpty { "Sin descripción" }
            binding.tvBookCount.text = "${list.bookIds.size} libro${if (list.bookIds.size != 1) "s" else ""}"
            
            // Mostrar icono de público si la lista es pública
            binding.ivPublic.visibility = if (list.isPublic) android.view.View.VISIBLE else android.view.View.GONE
            
            // Mostrar contador de seguidores solo si es pública y tiene seguidores
            if (list.isPublic && list.followerCount > 0) {
                binding.tvFollowerCount.visibility = android.view.View.VISIBLE
                binding.tvFollowerCount.text = " • ${list.followerCount} seguidor${if (list.followerCount != 1) "es" else ""}"
            } else {
                binding.tvFollowerCount.visibility = android.view.View.GONE
            }
            
            // Marcar visualmente si el libro está en esta lista
            if (bookId != null && list.bookIds.contains(bookId)) {
                binding.root.setCardBackgroundColor(0xFFE3F2FD.toInt()) // Azul claro
            } else {
                binding.root.setCardBackgroundColor(0xFFFFFFFF.toInt()) // Blanco
            }

            binding.root.setOnClickListener {
                onListClick(list)
            }

            onListLongClick?.let { longClick ->
                binding.root.setOnLongClickListener {
                    longClick(list)
                    true
                }
            }
        }
    }
}

