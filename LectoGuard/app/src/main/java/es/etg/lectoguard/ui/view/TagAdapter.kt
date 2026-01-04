package es.etg.lectoguard.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import es.etg.lectoguard.databinding.ItemTagBinding

class TagAdapter(
    private var tags: List<String>,
    private val selectedTags: Set<String> = emptySet(),
    private val onTagClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    fun updateTags(newTags: List<String>) {
        tags = newTags
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val chip = LayoutInflater.from(parent.context)
            .inflate(es.etg.lectoguard.R.layout.item_tag, parent, false) as Chip
        return TagViewHolder(chip)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(tags[position], selectedTags.contains(tags[position]))
    }

    override fun getItemCount() = tags.size

    inner class TagViewHolder(private val chip: Chip) : RecyclerView.ViewHolder(chip) {
        fun bind(tag: String, isSelected: Boolean) {
            chip.text = tag
            chip.isChecked = isSelected
            chip.setOnClickListener {
                onTagClick?.invoke(tag)
            }
        }
    }
}

