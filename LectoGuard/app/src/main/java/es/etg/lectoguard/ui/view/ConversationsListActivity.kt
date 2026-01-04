package es.etg.lectoguard.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ActivityConversationsListBinding
import es.etg.lectoguard.domain.model.Conversation
import es.etg.lectoguard.ui.viewmodel.ChatViewModel
import es.etg.lectoguard.utils.PrefsHelper
import es.etg.lectoguard.utils.NavigationUtils
import es.etg.lectoguard.data.repository.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ConversationsListActivity : BaseActivity() {
    private lateinit var binding: ActivityConversationsListBinding
    private val chatViewModel: ChatViewModel by viewModels()
    
    @Inject
    lateinit var userRepository: UserRepository
    
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = PrefsHelper.getFirebaseUid(this) ?: FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Configurar RecyclerView
        binding.rvConversations.layoutManager = LinearLayoutManager(this)
        
        // Observar conversaciones
        chatViewModel.conversations.observe(this) { conversations ->
            if (conversations.isEmpty()) {
                binding.tvEmptyState.visibility = android.view.View.VISIBLE
                binding.rvConversations.visibility = android.view.View.GONE
            } else {
                binding.tvEmptyState.visibility = android.view.View.GONE
                binding.rvConversations.visibility = android.view.View.VISIBLE
                binding.rvConversations.adapter = ConversationAdapter(
                    conversations,
                    currentUserId!!,
                    userRepository
                ) { conversation ->
                    // Abrir chat
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("conversationId", conversation.id)
                    // Obtener el otro participante
                    val otherParticipantId = conversation.participants.firstOrNull { it != currentUserId }
                    intent.putExtra("otherParticipantId", otherParticipantId)
                    startActivity(intent)
                }
            }
        }

        // Iniciar listener en tiempo real para conversaciones
        chatViewModel.startObservingConversations(currentUserId!!)

        // Bottom navigation - Conversations no tiene item específico, usar home como base
        binding.bottomNavigation.selectedItemId = R.id.menu_home
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_saved -> {
                    NavigationUtils.navigateToSavedBooks(this)
                    finish()
                    true
                }
                R.id.menu_home -> {
                    NavigationUtils.navigateToHome(this)
                    finish()
                    true
                }
                R.id.menu_profile -> {
                    NavigationUtils.navigateToProfile(this)
                    finish()
                    true
                }
                else -> false
            }
        }

        supportFragmentManager.commit {
            replace(R.id.headerFragmentContainer, HeaderFragment.newInstance(getString(R.string.conversations)))
        }
    }

    // Los listeners en tiempo real se mantienen activos automáticamente
    // No necesitamos recargar en onResume porque los cambios se actualizan automáticamente
}

