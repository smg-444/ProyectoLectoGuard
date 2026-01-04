package es.etg.lectoguard.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.etg.lectoguard.databinding.ItemMessageBinding
import es.etg.lectoguard.domain.model.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val messages: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding, currentUserId)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    inner class MessageViewHolder(
        private val binding: ItemMessageBinding,
        private val currentUserId: String
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            val isOwnMessage = message.senderId == currentUserId
            
            // Configurar layout según si es mensaje propio o ajeno
            val layoutParams = binding.cardMessage.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            
            if (isOwnMessage) {
                // Mensaje propio: alineado a la derecha
                layoutParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                binding.cardMessage.setCardBackgroundColor(
                    binding.root.context.getColor(android.R.color.holo_blue_light)
                )
                binding.tvMessageContent.setTextColor(
                    binding.root.context.getColor(android.R.color.white)
                )
            } else {
                // Mensaje ajeno: alineado a la izquierda
                layoutParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                binding.cardMessage.setCardBackgroundColor(
                    binding.root.context.getColor(android.R.color.white)
                )
                binding.tvMessageContent.setTextColor(
                    binding.root.context.getColor(android.R.color.black)
                )
            }
            binding.cardMessage.layoutParams = layoutParams
            
            // Mostrar nombre del remitente solo si no es nuestro mensaje
            if (isOwnMessage) {
                binding.tvSenderName.visibility = android.view.View.GONE
            } else {
                binding.tvSenderName.text = message.senderName
                binding.tvSenderName.visibility = android.view.View.VISIBLE
            }
            
            binding.tvMessageContent.text = message.content
            
            // Mostrar timestamp con formato amigable
            binding.tvTimestamp.text = es.etg.lectoguard.utils.TimeUtils.formatMessageTimestamp(message.timestamp)
        }
    }
}

