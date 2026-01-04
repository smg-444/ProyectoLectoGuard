package es.etg.lectoguard.ui.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivityChatBinding
import es.etg.lectoguard.data.repository.UserRepository
import es.etg.lectoguard.domain.model.Message
import es.etg.lectoguard.ui.viewmodel.ChatViewModel
import es.etg.lectoguard.utils.PrefsHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class ChatActivity : BaseActivity() {
    private lateinit var binding: ActivityChatBinding
    private val chatViewModel: ChatViewModel by viewModels()
    
    @Inject
    lateinit var userRepository: UserRepository
    
    private var conversationId: String? = null
    private var otherParticipantId: String? = null
    private var currentUserId: String? = null
    private var currentUserProfile: es.etg.lectoguard.domain.model.UserProfile? = null
    private var otherUserProfile: es.etg.lectoguard.domain.model.UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        conversationId = intent.getStringExtra("conversationId")
        otherParticipantId = intent.getStringExtra("otherParticipantId")
        currentUserId = PrefsHelper.getFirebaseUid(this) ?: FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (otherParticipantId == null) {
            Toast.makeText(this, "Error: Usuario destino no especificado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Cargar perfiles del usuario actual y del destinatario
        lifecycleScope.launch {
            currentUserProfile = userRepository.getRemoteProfile(currentUserId!!)
            if (currentUserProfile == null) {
                val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
                currentUserProfile = es.etg.lectoguard.domain.model.UserProfile(
                    uid = currentUserId!!,
                    displayName = email.substringBefore("@"),
                    email = email
                )
            }
            
            // Cargar perfil del destinatario para mostrar su nombre en el header
            otherUserProfile = userRepository.getRemoteProfile(otherParticipantId!!)
            updateHeaderTitle()
        }

        // Configurar RecyclerView
        binding.rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Mostrar mensajes desde abajo
        }

        // Observar mensajes
        chatViewModel.messages.observe(this) { messages ->
            binding.rvMessages.adapter = MessageAdapter(messages, currentUserId!!)
            // Scroll al final cuando hay nuevos mensajes
            if (messages.isNotEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }

        // Observar resultado de envío
        chatViewModel.sendMessageResult.observe(this) { messageId ->
            if (messageId != null) {
                binding.etMessage.text?.clear()
            } else {
                Toast.makeText(this, getString(R.string.error_sending_message), Toast.LENGTH_SHORT).show()
            }
        }

        // Botón enviar
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        // Obtener o crear conversación si no existe
        if (conversationId == null) {
            android.util.Log.d("ChatActivity", "Creando/obteniendo conversación entre $currentUserId y $otherParticipantId")
            lifecycleScope.launch {
                chatViewModel.getOrCreateConversation(currentUserId!!, otherParticipantId!!)
            }
        } else {
            android.util.Log.d("ChatActivity", "Conversación existente: $conversationId")
            // Iniciar listener en tiempo real para mensajes
            chatViewModel.startObservingMessages(conversationId!!)
            // Marcar como leídos
            chatViewModel.markAsRead(conversationId!!, currentUserId!!)
        }

        // Observar conversationId para iniciar el listener cuando se establezca
        chatViewModel.conversationId.observe(this) { id ->
            android.util.Log.d("ChatActivity", "conversationId actualizado: $id")
            if (id != null && this@ChatActivity.conversationId == null) {
                this@ChatActivity.conversationId = id
                android.util.Log.d("ChatActivity", "Conversación establecida: $id. Iniciando listener de mensajes.")
                // Iniciar listener en tiempo real para mensajes
                chatViewModel.startObservingMessages(id)
                // Marcar como leídos
                chatViewModel.markAsRead(id, currentUserId!!)
            }
        }

        // Título del header se actualizará cuando se cargue el perfil del destinatario
        updateHeaderTitle()
    }

    private fun sendMessage() {
        val messageText = binding.etMessage.text?.toString()?.trim()
        if (messageText.isNullOrEmpty()) {
            return
        }

        val currentUserId = this.currentUserId ?: return
        val currentUserProfile = this.currentUserProfile ?: return
        val otherParticipantId = this.otherParticipantId ?: return

        // Si no tenemos conversationId, intentar crearlo primero
        val conversationId = this.conversationId
        if (conversationId == null) {
            // Crear conversación y luego enviar el mensaje
            lifecycleScope.launch {
                // Si aún no se ha creado, crearla
                if (chatViewModel.conversationId.value == null) {
                    chatViewModel.getOrCreateConversation(currentUserId, otherParticipantId)
                }
                
                // Esperar hasta que tengamos el conversationId (con timeout)
                var attempts = 0
                var newConversationId: String? = null
                while (newConversationId == null && attempts < 10) {
                    delay(100)
                    newConversationId = chatViewModel.conversationId.value
                    attempts++
                }
                
                if (newConversationId != null) {
                    this@ChatActivity.conversationId = newConversationId
                    // Enviar el mensaje ahora que tenemos el conversationId
                    val message = Message(
                        conversationId = newConversationId,
                        senderId = currentUserId,
                        senderName = currentUserProfile.displayName,
                        senderAvatarUrl = currentUserProfile.avatarUrl,
                        content = messageText,
                        timestamp = System.currentTimeMillis(),
                        readBy = listOf(currentUserId)
                    )
                    chatViewModel.sendMessage(newConversationId, message)
                } else {
                    android.util.Log.e("ChatActivity", "No se pudo crear la conversación después de ${attempts} intentos")
                    Toast.makeText(this@ChatActivity, "Error al crear conversación. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                }
            }
            return
        }

        // Si ya tenemos conversationId, enviar directamente
        val message = Message(
            conversationId = conversationId,
            senderId = currentUserId,
            senderName = currentUserProfile.displayName,
            senderAvatarUrl = currentUserProfile.avatarUrl,
            content = messageText,
            timestamp = System.currentTimeMillis(),
            readBy = listOf(currentUserId) // El remitente ya ha leído su propio mensaje
        )

        chatViewModel.sendMessage(conversationId, message)
    }

    override fun onResume() {
        super.onResume()
        // Marcar mensajes como leídos al volver
        conversationId?.let {
            currentUserId?.let { userId ->
                lifecycleScope.launch {
                    chatViewModel.markAsRead(it, userId)
                }
            }
        }
    }
    
    private fun updateHeaderTitle() {
        val headerTitle = otherUserProfile?.displayName 
            ?: otherParticipantId 
            ?: getString(R.string.chat)
        supportFragmentManager.commit {
            replace(R.id.headerFragmentContainer, HeaderFragment.newInstance(headerTitle))
        }
    }
}

