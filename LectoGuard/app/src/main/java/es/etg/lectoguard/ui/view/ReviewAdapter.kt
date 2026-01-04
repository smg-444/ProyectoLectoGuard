package es.etg.lectoguard.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ItemReviewBinding
import es.etg.lectoguard.domain.model.Review

class ReviewAdapter(
    private val reviews: List<Review>,
    private val currentUserId: String?,
    private val onLikeClick: (Review, Boolean) -> Unit,
    private val onEditClick: ((Review) -> Unit)? = null,
    private val onDeleteClick: ((Review) -> Unit)? = null
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.binding.tvUserName.text = review.userName
        holder.binding.tvReviewText.text = review.text
        holder.binding.ratingStarsReview.setRating(review.rating)
        holder.binding.ratingStarsReview.setEditable(false)
        
        // Cargar avatar
        if (!review.userAvatarUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(review.userAvatarUrl)
                .circleCrop()
                .into(holder.binding.ivUserAvatar)
        }
        
        // Mostrar likes
        holder.binding.tvLikes.text = "${review.likes} me gusta"
        
        // Configurar botón de like
        val isLiked = currentUserId != null && review.likedBy.contains(currentUserId)
        holder.binding.btnLike.text = if (isLiked) {
            holder.itemView.context.getString(R.string.liked)
        } else {
            holder.itemView.context.getString(R.string.like)
        }
        
        holder.binding.btnLike.setOnClickListener {
            onLikeClick(review, !isLiked)
        }
        
        // Mostrar botones de editar/eliminar solo si es el autor
        val isAuthor = currentUserId != null && review.userId == currentUserId
        if (isAuthor) {
            holder.binding.btnEdit.visibility = android.view.View.VISIBLE
            holder.binding.btnDelete.visibility = android.view.View.VISIBLE
            
            holder.binding.btnEdit.setOnClickListener {
                onEditClick?.invoke(review)
            }
            
            holder.binding.btnDelete.setOnClickListener {
                onDeleteClick?.invoke(review)
            }
        } else {
            holder.binding.btnEdit.visibility = android.view.View.GONE
            holder.binding.btnDelete.visibility = android.view.View.GONE
        }
    }

    override fun getItemCount() = reviews.size
}

