package es.etg.lectoguard.data.repository

import es.etg.lectoguard.data.local.UserDao
import es.etg.lectoguard.data.local.UserEntity
 
class UserRepository(private val userDao: UserDao) {
    suspend fun login(email: String, password: String) = userDao.login(email, password)
    suspend fun register(user: UserEntity) = userDao.insert(user)
    suspend fun getUserById(id: Int) = userDao.getUserById(id)
} 