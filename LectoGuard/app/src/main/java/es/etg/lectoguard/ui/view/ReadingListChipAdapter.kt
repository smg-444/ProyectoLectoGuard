package es.etg.lectoguard.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import es.etg.lectoguard.R
import es.etg.lectoguard.domain.model.ReadingList

class ReadingListChipAdapter(
    private var lists: List<ReadingList>,
    private val onListClick: ((ReadingList) -> Unit)? = null
) : RecyclerView.Adapter<ReadingListChipAdapter.ListViewHolder>() {

    fun updateLists(newLists: List<ReadingList>) {
        lists = newLists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val chip = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag, parent, false) as Chip
        return ListViewHolder(chip)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(lists[position])
    }

    override fun getItemCount() = lists.size

    inner class ListViewHolder(private val chip: Chip) : RecyclerView.ViewHolder(chip) {
        fun bind(list: ReadingList) {
            chip.text = list.name
            chip.isChecked = false
            chip.setOnClickListener {
                onListClick?.invoke(list)
            }
        }
    }
}

