package es.etg.lectoguard.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ItemFeedBinding
import es.etg.lectoguard.domain.model.FeedItem
import es.etg.lectoguard.domain.model.FeedItemType
import java.text.SimpleDateFormat
import java.util.*

class FeedAdapter(
    private var feedItems: List<FeedItem>,
    private val onItemClick: ((FeedItem) -> Unit)? = null
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    fun updateItems(newItems: List<FeedItem>) {
        feedItems = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = ItemFeedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bind(feedItems[position])
    }

    override fun getItemCount() = feedItems.size

    inner class FeedViewHolder(
        private val binding: ItemFeedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FeedItem) {
            android.util.Log.d("FeedAdapter", "Binding feed item: id=${item.id}, type=${item.type.name}, userId=${item.userId}, targetUserId=${item.targetUserId}, targetUserName=${item.targetUserName}")
            
            // Resetear todas las vistas primero
            resetViews()
            
            // Avatar del usuario
            Glide.with(binding.root.context)
                .load(item.userAvatarUrl)
                .circleCrop()
                .placeholder(R.drawable.default_avatar)
                .into(binding.ivUserAvatar)

            // Nombre del usuario
            binding.tvUserName.text = item.userName

            // Timestamp
            val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            binding.tvTimestamp.text = dateFormat.format(Date(item.timestamp))

            // Configurar según el tipo de actividad
            when (item.type) {
                FeedItemType.BOOK_SAVED -> {
                    binding.tvActivityText.text = binding.root.context.getString(R.string.saved_book)
                    showBookInfo(item)
                }
                FeedItemType.RATING -> {
                    binding.tvActivityText.text = binding.root.context.getString(R.string.rated_book)
                    showBookInfo(item)
                    showRating(item)
                }
                FeedItemType.REVIEW -> {
                    binding.tvActivityText.text = binding.root.context.getString(R.string.reviewed_book)
                    showBookInfo(item)
                    showRating(item)
                    showReviewText(item)
                }
                FeedItemType.FOLLOW -> {
                    val targetName = item.targetUserName ?: "un usuario"
                    binding.tvActivityText.text = binding.root.context.getString(R.string.followed_user, targetName)
                    android.util.Log.d("FeedAdapter", "Mostrando FOLLOW: targetName=$targetName")
                    // Para FOLLOW no mostramos información de libro
                }
            }

            // Click listener
            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
        
        private fun resetViews() {
            // Ocultar todo primero
            binding.cardBookInfo.visibility = android.view.View.GONE
            binding.ivBookCover.visibility = android.view.View.GONE
            binding.tvBookTitle.visibility = android.view.View.GONE
            binding.ratingBar.visibility = android.view.View.GONE
            binding.tvReviewText.visibility = android.view.View.GONE
            binding.tvFollowedUser.visibility = android.view.View.GONE
        }

        private fun showBookInfo(item: FeedItem) {
            binding.cardBookInfo.visibility = android.view.View.VISIBLE
            binding.ivBookCover.visibility = android.view.View.VISIBLE
            binding.tvBookTitle.visibility = android.view.View.VISIBLE

            // Portada del libro
            Glide.with(binding.root.context)
                .load(item.bookCoverUrl)
                .placeholder(R.drawable.default_avatar)
                .into(binding.ivBookCover)

            // Título del libro
            binding.tvBookTitle.text = item.bookTitle ?: "Libro"
        }

        private fun showRating(item: FeedItem) {
            item.rating?.let { rating ->
                binding.ratingBar.visibility = android.view.View.VISIBLE
                binding.ratingBar.rating = rating.toFloat()
            } ?: run {
                binding.ratingBar.visibility = android.view.View.GONE
            }
        }

        private fun showReviewText(item: FeedItem) {
            item.reviewText?.let { text ->
                binding.tvReviewText.visibility = android.view.View.VISIBLE
                binding.tvReviewText.text = text
            } ?: run {
                binding.tvReviewText.visibility = android.view.View.GONE
            }
        }
    }
}

