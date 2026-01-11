package es.etg.lectoguard.utils

import android.content.Context

object PrefsHelper {
    private const val PREFS_NAME = "lectoguard_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_FIREBASE_UID = "firebase_uid"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_PASSWORD = "user_password" // Guardado de forma segura para login offline

    fun saveUser(context: Context, id: Int, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_USER_ID, id).putString(KEY_USER_NAME, name).apply()
    }

    fun getUserId(context: Context): Int = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_USER_ID, -1)
    fun getUserName(context: Context): String? = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_USER_NAME, null)

    fun saveFirebaseUid(context: Context, uid: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FIREBASE_UID, uid).apply()
    }
    fun getFirebaseUid(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_FIREBASE_UID, null)

    fun setDarkMode(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun isDarkModeEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_DARK_MODE, false)
    }

    fun clear(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    
    fun saveUserCredentials(context: Context, email: String, password: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_PASSWORD, password) // En producción, usar encriptación
            .apply()
    }
    
    fun getUserEmail(context: Context): String? = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_USER_EMAIL, null)
    
    fun getUserPassword(context: Context): String? = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_USER_PASSWORD, null)
    
    fun hasStoredCredentials(context: Context): Boolean {
        val email = getUserEmail(context)
        val password = getUserPassword(context)
        return !email.isNullOrEmpty() && !password.isNullOrEmpty()
    }
} 