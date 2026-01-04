package es.etg.lectoguard.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import es.etg.lectoguard.data.remote.RatingService
import es.etg.lectoguard.data.remote.ReviewService
import es.etg.lectoguard.domain.model.Rating
import es.etg.lectoguard.domain.model.Review

class RatingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val ratingService by lazy { RatingService(firestore) }
    private val reviewService by lazy { ReviewService(firestore) }
    
    suspend fun saveRating(rating: Rating) = ratingService.saveRating(rating)
    suspend fun getUserRating(bookId: Int, userId: String) = ratingService.getUserRating(bookId, userId)
    suspend fun getBookRatings(bookId: Int) = ratingService.getBookRatings(bookId)
    suspend fun getAverageRating(bookId: Int) = ratingService.getAverageRating(bookId)
    
    suspend fun saveReview(review: Review) = reviewService.saveReview(review)
    suspend fun getBookReviews(bookId: Int, limit: Int = 50) = reviewService.getBookReviews(bookId, limit)
    suspend fun toggleLikeReview(reviewId: String, userId: String, isLiked: Boolean) = 
        reviewService.toggleLikeReview(reviewId, userId, isLiked)
    suspend fun updateReview(reviewId: String, userId: String, newText: String, newRating: Int) =
        reviewService.updateReview(reviewId, userId, newText, newRating)
    suspend fun deleteReview(reviewId: String, userId: String) = reviewService.deleteReview(reviewId, userId)
}

