package es.etg.lectoguard.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import es.etg.lectoguard.R

class RatingStarsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val stars = mutableListOf<ImageView>()
    private var rating: Int = 0
    private var isEditable: Boolean = true
    private var onRatingChanged: ((Int) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.widget_rating_stars, this, true)
        
        stars.add(view.findViewById(R.id.star1))
        stars.add(view.findViewById(R.id.star2))
        stars.add(view.findViewById(R.id.star3))
        stars.add(view.findViewById(R.id.star4))
        stars.add(view.findViewById(R.id.star5))
        
        setupStars()
    }
    
    private fun setupStars() {
        stars.forEachIndexed { index, star ->
            if (isEditable) {
                star.setOnClickListener {
                    setRating(index + 1)
                    onRatingChanged?.invoke(rating)
                }
            } else {
                star.setOnClickListener(null)
                star.isClickable = false
            }
        }
    }
    
    fun setRating(rating: Int) {
        this.rating = rating.coerceIn(0, 5)
        updateStars()
    }
    
    fun getRating(): Int = rating
    
    fun setEditable(editable: Boolean) {
        this.isEditable = editable
        setupStars()
    }
    
    fun setOnRatingChangedListener(listener: (Int) -> Unit) {
        this.onRatingChanged = listener
    }
    
    private fun updateStars() {
        stars.forEachIndexed { index, star ->
            if (index < rating) {
                star.setImageResource(android.R.drawable.btn_star_big_on)
            } else {
                star.setImageResource(android.R.drawable.btn_star_big_off)
            }
        }
    }
}

