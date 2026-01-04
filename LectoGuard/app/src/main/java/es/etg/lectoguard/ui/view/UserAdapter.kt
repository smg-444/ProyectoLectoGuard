package es.etg.lectoguard.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import es.etg.lectoguard.databinding.ItemUserBinding
import es.etg.lectoguard.domain.model.UserProfile

class UserAdapter(
    private val users: List<UserProfile>,
    private val onUserClick: (UserProfile) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.binding.tvDisplayName.text = user.displayName
        holder.binding.tvEmail.text = user.email
        
        // Cargar avatar
        if (!user.avatarUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.avatarUrl)
                .circleCrop()
                .into(holder.binding.ivAvatar)
        }
        
        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }

    override fun getItemCount() = users.size
}

