package es.etg.lectoguard.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import es.etg.lectoguard.data.remote.FeedService
import es.etg.lectoguard.domain.model.FeedItem
import kotlinx.coroutines.flow.Flow

class FeedRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val feedService by lazy { FeedService(firestore) }
    
    suspend fun createFeedItemForFollowers(
        actorUserId: String,
        actorUserName: String,
        actorAvatarUrl: String?,
        feedItem: FeedItem
    ): Boolean = feedService.createFeedItemForFollowers(actorUserId, actorUserName, actorAvatarUrl, feedItem)
    
    suspend fun getUserFeed(userId: String, limit: Int = 50): List<FeedItem> =
        feedService.getUserFeed(userId, limit)
    
    suspend fun getMoreFeedItems(userId: String, lastTimestamp: Long, limit: Int = 20): List<FeedItem> =
        feedService.getMoreFeedItems(userId, lastTimestamp, limit)
    
    fun observeUserFeed(userId: String, limit: Int = 50): Flow<List<FeedItem>> =
        feedService.observeUserFeed(userId, limit)
}

