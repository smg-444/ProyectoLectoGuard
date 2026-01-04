package es.etg.lectoguard.utils

import android.content.Context
import android.content.Intent
import es.etg.lectoguard.ui.view.*

/**
 * Utilidad para navegación entre Activities usando Navigation Component
 * Proporciona métodos helper para navegar de forma consistente
 */
object NavigationUtils {

    /**
     * Navega a HomeActivity
     */
    fun navigateToHome(context: Context) {
        val intent = Intent(context, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        context.startActivity(intent)
    }

    /**
     * Navega a SavedBooksActivity
     */
    fun navigateToSavedBooks(context: Context) {
        val intent = Intent(context, SavedBooksActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        context.startActivity(intent)
    }

    /**
     * Navega a ProfileActivity
     */
    fun navigateToProfile(context: Context) {
        val intent = Intent(context, ProfileActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        context.startActivity(intent)
    }

    /**
     * Navega a FeedActivity
     */
    fun navigateToFeed(context: Context) {
        context.startActivity(Intent(context, FeedActivity::class.java))
    }

    /**
     * Navega a SaveBookActivity con el ID del libro
     */
    fun navigateToSaveBook(context: Context, bookId: Int) {
        val intent = Intent(context, SaveBookActivity::class.java)
        intent.putExtra("bookId", bookId)
        context.startActivity(intent)
    }

    /**
     * Navega a SearchUsersActivity
     */
    fun navigateToSearchUsers(context: Context) {
        context.startActivity(Intent(context, SearchUsersActivity::class.java))
    }

    /**
     * Navega a UserProfileActivity con el UID del usuario objetivo
     */
    fun navigateToUserProfile(context: Context, targetUid: String) {
        val intent = Intent(context, UserProfileActivity::class.java)
        intent.putExtra("targetUid", targetUid)
        context.startActivity(intent)
    }

    /**
     * Navega a ConversationsListActivity
     */
    fun navigateToConversationsList(context: Context) {
        context.startActivity(Intent(context, ConversationsListActivity::class.java))
    }

    /**
     * Navega a ChatActivity con el ID de la conversación y el participante
     */
    fun navigateToChat(
        context: Context,
        conversationId: String? = null,
        otherParticipantId: String
    ) {
        val intent = Intent(context, ChatActivity::class.java)
        conversationId?.let { intent.putExtra("conversationId", it) }
        intent.putExtra("otherParticipantId", otherParticipantId)
        context.startActivity(intent)
    }

    /**
     * Navega a ReadingListsActivity
     */
    fun navigateToReadingLists(context: Context) {
        context.startActivity(Intent(context, ReadingListsActivity::class.java))
    }

    /**
     * Navega a ReadingListDetailActivity con el ID de la lista
     */
    fun navigateToReadingListDetail(context: Context, listId: String) {
        val intent = Intent(context, ReadingListDetailActivity::class.java)
        intent.putExtra("listId", listId)
        context.startActivity(intent)
    }

    /**
     * Navega a PublicReadingListsActivity
     */
    fun navigateToPublicReadingLists(context: Context) {
        context.startActivity(Intent(context, PublicReadingListsActivity::class.java))
    }

    /**
     * Navega a LoginActivity (útil para logout)
     */
    fun navigateToLogin(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    /**
     * Navega a SignUpActivity
     */
    fun navigateToSignUp(context: Context) {
        context.startActivity(Intent(context, SignUpActivity::class.java))
    }
}

