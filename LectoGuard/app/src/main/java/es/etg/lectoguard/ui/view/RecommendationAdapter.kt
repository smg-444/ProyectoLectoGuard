package es.etg.lectoguard.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import es.etg.lectoguard.R
import es.etg.lectoguard.databinding.ItemRecommendationBinding
import es.etg.lectoguard.domain.model.BookRecommendation
import es.etg.lectoguard.data.remote.UserProfileService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class RecommendationAdapter(
    private val recommendations: List<BookRecommendation>,
    private val onBookClick: (BookRecommendation) -> Unit,
    private val onUserClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {
    
    private val profileService = UserProfileService(FirebaseFirestore.getInstance())
    private val userProfilesCache = mutableMapOf<String, String>() // UID -> DisplayName

    inner class RecommendationViewHolder(val binding: ItemRecommendationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recommendation: BookRecommendation) {
            android.util.Log.d("RecommendationAdapter", "Binding recommendation: ${recommendation.bookTitle}, reason=${recommendation.reason}, genre=${recommendation.genre}, users=${recommendation.recommendedBy.size}, score=${recommendation.similarityScore}")
            
            // Resetear todas las vistas primero
            binding.tvReason.visibility = android.view.View.VISIBLE
            binding.tvGenre.visibility = android.view.View.GONE
            binding.tvSimilarityScore.visibility = android.view.View.VISIBLE
            binding.tvRecommendedByLabel.visibility = android.view.View.GONE
            binding.llRecommendedByUsers.visibility = android.view.View.GONE
            binding.llRecommendedByUsers.removeAllViews()
            
            binding.tvTitle.text = recommendation.bookTitle
            
            // Mostrar razón (o texto por defecto si está vacía)
            val reasonText = if (recommendation.reason.isNotEmpty()) {
                recommendation.reason
            } else {
                "Recomendado basado en tus intereses"
            }
            binding.tvReason.text = reasonText
            android.util.Log.d("RecommendationAdapter", "Reason text set: $reasonText")
            
            // Mostrar género si está disponible
            if (recommendation.genre != null) {
                binding.tvGenre.text = recommendation.genre.displayName
                binding.tvGenre.visibility = android.view.View.VISIBLE
            } else {
                binding.tvGenre.visibility = android.view.View.GONE
            }
            
            // Mostrar score de similitud como porcentaje (siempre visible)
            val similarityPercent = (recommendation.similarityScore * 100).toInt()
            binding.tvSimilarityScore.text = "${similarityPercent}%"
            binding.tvSimilarityScore.visibility = android.view.View.VISIBLE
            
            // Color del score según el porcentaje
            val scoreColor = when {
                similarityPercent >= 80 -> "#4CAF50" // Verde
                similarityPercent >= 60 -> "#8BC34A" // Verde claro
                similarityPercent >= 40 -> "#FFC107" // Amarillo
                else -> "#FF9800" // Naranja
            }
            binding.tvSimilarityScore.setTextColor(android.graphics.Color.parseColor(scoreColor))
            
            // Asegurar que el LinearLayout que contiene género y score sea visible
            (binding.tvSimilarityScore.parent as? android.view.ViewGroup)?.visibility = android.view.View.VISIBLE
            android.util.Log.d("RecommendationAdapter", "Score set: ${similarityPercent}%, color: $scoreColor")
            
            // Mostrar información de usuarios que recomendaron
            val userCount = recommendation.recommendedBy.size
            android.util.Log.d("RecommendationAdapter", "User count: $userCount, users: ${recommendation.recommendedBy}")
            if (userCount > 0) {
                binding.tvRecommendedByLabel.visibility = android.view.View.VISIBLE
                binding.llRecommendedByUsers.visibility = android.view.View.VISIBLE
                
                // Cargar y mostrar nombres de usuarios (máximo 3 para no saturar la UI)
                val usersToShow = recommendation.recommendedBy.take(3)
                android.util.Log.d("RecommendationAdapter", "Showing ${usersToShow.size} users: $usersToShow")
                usersToShow.forEachIndexed { index, userId ->
                    // Mostrar separador si no es el primero
                    if (index > 0) {
                        val separator = android.widget.TextView(binding.root.context).apply {
                            text = ", "
                            textSize = 10f
                            setTextColor(android.graphics.Color.parseColor("#999999"))
                        }
                        binding.llRecommendedByUsers.addView(separator)
                    }
                    
                    // Crear TextView clickeable para el usuario
                    val userChip = android.widget.TextView(binding.root.context).apply {
                        text = userProfilesCache[userId] ?: "Usuario..."
                        textSize = 10f
                        setTextColor(android.graphics.Color.parseColor("#2196F3"))
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setPadding(0, 0, 0, 0)
                        
                        setOnClickListener {
                            onUserClick?.invoke(userId)
                        }
                        
                        // Efecto de click
                        setOnTouchListener { v, event ->
                            when (event.action) {
                                android.view.MotionEvent.ACTION_DOWN -> {
                                    v.alpha = 0.6f
                                }
                                android.view.MotionEvent.ACTION_UP,
                                android.view.MotionEvent.ACTION_CANCEL -> {
                                    v.alpha = 1.0f
                                }
                            }
                            false
                        }
                    }
                    binding.llRecommendedByUsers.addView(userChip)
                    
                    // Cargar perfil del usuario de forma asíncrona
                    if (!userProfilesCache.containsKey(userId)) {
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val profile = profileService.getProfile(userId)
                                profile?.displayName?.let { name ->
                                    userProfilesCache[userId] = name
                                    userChip.text = name
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("RecommendationAdapter", "Error cargando perfil de $userId: ${e.message}")
                            }
                        }
                    }
                }
                
                // Mostrar "+X más" si hay más usuarios
                if (userCount > 3) {
                    val moreText = android.widget.TextView(binding.root.context).apply {
                        text = " +${userCount - 3} más"
                        textSize = 10f
                        setTextColor(android.graphics.Color.parseColor("#999999"))
                    }
                    binding.llRecommendedByUsers.addView(moreText)
                }
            } else {
                binding.tvRecommendedByLabel.visibility = android.view.View.GONE
                binding.llRecommendedByUsers.visibility = android.view.View.GONE
            }
            
            Glide.with(binding.ivCover.context)
                .load(recommendation.bookCoverUrl)
                .placeholder(R.drawable.default_avatar)
                .into(binding.ivCover)
            
            binding.root.setOnClickListener { onBookClick(recommendation) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val binding = ItemRecommendationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecommendationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        holder.bind(recommendations[position])
    }

    override fun getItemCount() = recommendations.size
}

