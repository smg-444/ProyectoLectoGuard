package es.etg.lectoguard.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ItemConversationBinding
import es.etg.lectoguard.data.repository.UserRepository
import es.etg.lectoguard.domain.model.Conversation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(
    private val conversations: List<Conversation>,
    private val currentUserId: String,
    private val userRepository: UserRepository,
    private val onConversationClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(conversations[position])
    }

    override fun getItemCount() = conversations.size

    inner class ConversationViewHolder(
        private val binding: ItemConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            // Obtener el otro participante (no el usuario actual)
            val otherParticipantId = conversation.participants.firstOrNull { it != currentUserId }
            
            // Mostrar ID temporalmente mientras cargamos el perfil
            binding.tvUserName.text = otherParticipantId ?: getString(R.string.user)
            
            // Cargar perfil del usuario de forma asíncrona
            if (otherParticipantId != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    val profile = userRepository.getRemoteProfile(otherParticipantId)
                    if (profile != null) {
                        binding.tvUserName.text = profile.displayName
                        
                        // Cargar avatar si existe
                        if (!profile.avatarUrl.isNullOrEmpty()) {
                            Glide.with(binding.root.context)
                                .load(profile.avatarUrl)
                                .circleCrop()
                                .placeholder(R.drawable.default_avatar)
                                .into(binding.ivAvatar)
                        } else {
                            Glide.with(binding.root.context)
                                .load(R.drawable.default_avatar)
                                .circleCrop()
                                .into(binding.ivAvatar)
                        }
                    } else {
                        // Si no hay perfil, usar email o ID
                        val email = otherParticipantId.substringBefore("@").takeIf { it.contains("@") } 
                            ?: otherParticipantId.take(10) + "..."
                        binding.tvUserName.text = email
                        Glide.with(binding.root.context)
                            .load(R.drawable.default_avatar)
                            .circleCrop()
                            .into(binding.ivAvatar)
                    }
                }
            } else {
                binding.tvUserName.text = getString(R.string.user)
                Glide.with(binding.root.context)
                    .load(R.drawable.default_avatar)
                    .circleCrop()
                    .into(binding.ivAvatar)
            }
            
            // Mostrar último mensaje
            binding.tvLastMessage.text = conversation.lastMessage ?: getString(R.string.no_messages)
            
            // Mostrar timestamp
            val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            binding.tvTimestamp.text = dateFormat.format(Date(conversation.lastMessageTimestamp))
            
            // Mostrar contador de no leídos
            val unreadCount = conversation.unreadCount[currentUserId] ?: 0
            if (unreadCount > 0) {
                binding.badgeUnread.text = unreadCount.toString()
                binding.badgeUnread.visibility = android.view.View.VISIBLE
            } else {
                binding.badgeUnread.visibility = android.view.View.GONE
            }
            
            binding.root.setOnClickListener {
                onConversationClick(conversation)
            }
        }
        
        private fun getString(resId: Int): String {
            return binding.root.context.getString(resId)
        }
    }
}

