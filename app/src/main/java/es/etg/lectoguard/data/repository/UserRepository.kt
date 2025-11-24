package es.etg.lectoguard.data.repository

import com.google.firebase.auth.FirebaseAuth
import es.etg.lectoguard.data.local.UserDao
import es.etg.lectoguard.data.local.UserEntity
import kotlinx.coroutines.tasks.await
 
class UserRepository(
    private val userDao: UserDao,
    private val auth: FirebaseAuth
) {
    suspend fun login(email: String, password: String): UserEntity? {
        // Autenticación con Firebase Auth
        auth.signInWithEmailAndPassword(email, password).await()
        // Buscar usuario local por email; si no existe, crear uno mínimo para disponer de id local
        val existing = userDao.getUserByEmail(email)
        if (existing != null) return existing
        val generatedName = email.substringBefore("@")
        val newUser = UserEntity(
            name = generatedName,
            email = email,
            phone = "",
            password = "",
            signupDate = System.currentTimeMillis().toString()
        )
        val id = userDao.insert(newUser)
        return userDao.getUserById(id.toInt())
    }

    suspend fun register(user: UserEntity): Long {
        // Registro en Firebase Auth
        auth.createUserWithEmailAndPassword(user.email, user.password).await()
        // Persistencia local del perfil
        return userDao.insert(user)
    }

    suspend fun getUserById(id: Int) = userDao.getUserById(id)
} 