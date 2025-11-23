package es.etg.lectoguard.utils

object ValidationUtils {
    fun isValidEmail(email: String): Boolean =
        email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
 
    fun isValidPassword(password: String): Boolean =
        password.length >= 6
} 