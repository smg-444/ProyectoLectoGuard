package es.etg.lectoguard.utils

import android.content.Context

object PrefsHelper {
    private const val PREFS_NAME = "lectoguard_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"

    fun saveUser(context: Context, id: Int, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_USER_ID, id).putString(KEY_USER_NAME, name).apply()
    }

    fun getUserId(context: Context): Int = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_USER_ID, -1)
    fun getUserName(context: Context): String? = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_USER_NAME, null)
    fun clear(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
} 