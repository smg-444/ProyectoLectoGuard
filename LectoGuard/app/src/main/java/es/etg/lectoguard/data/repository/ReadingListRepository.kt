package es.etg.lectoguard.data.repository

import es.etg.lectoguard.data.local.ReadingListDao
import es.etg.lectoguard.data.local.ReadingListEntity
import es.etg.lectoguard.data.remote.ReadingListService
import es.etg.lectoguard.domain.model.ReadingList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import android.util.Log

class ReadingListRepository(
    private val readingListDao: ReadingListDao,
    private val readingListService: ReadingListService
) {
    /**
     * Convierte ReadingListEntity a ReadingList
     */
    private fun ReadingListEntity.toDomain(): ReadingList {
        return ReadingList(
            id = this.id,
            userId = this.userId,
            name = this.name,
            description = this.description,
            isPublic = this.isPublic,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            bookIds = this.bookIds,
            followerCount = this.followerCount
        )
    }

    /**
     * Convierte ReadingList a ReadingListEntity
     */
    private fun ReadingList.toEntity(): ReadingListEntity {
        return ReadingListEntity(
            id = this.id,
            userId = this.userId,
            name = this.name,
            description = this.description,
            isPublic = this.isPublic,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            bookIds = this.bookIds,
            followerCount = this.followerCount
        )
    }

    /**
     * Obtiene todas las listas del usuario (Flow para observación reactiva)
     */
    fun getUserReadingLists(userId: String): Flow<List<ReadingList>> {
        return readingListDao.getAllUserLists(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Obtiene todas las listas del usuario (síncrono)
     */
    suspend fun getUserReadingListsSync(userId: String): List<ReadingList> {
        return readingListDao.getAllUserListsSync(userId).map { it.toDomain() }
    }

    /**
     * Obtiene una lista por ID (Flow)
     */
    fun getReadingListById(listId: String): Flow<ReadingList?> {
        return readingListDao.getListByIdFlow(listId).map { it?.toDomain() }
    }

    /**
     * Obtiene una lista por ID (síncrono)
     */
    suspend fun getReadingListByIdSync(listId: String): ReadingList? {
        return readingListDao.getListById(listId)?.toDomain()
    }

    /**
     * Crea o actualiza una lista de lectura
     */
    suspend fun saveReadingList(list: ReadingList, syncToFirestore: Boolean = true): String? {
        return try {
            // Guardar en Firestore primero si está habilitado
            val listId = if (syncToFirestore) {
                readingListService.saveReadingList(list) ?: list.id.ifEmpty { return null }
            } else {
                list.id.ifEmpty { return null }
            }

            // Guardar en BD local
            val entity = list.copy(id = listId).toEntity()
            readingListDao.insert(entity)

            Log.d("ReadingListRepository", "Lista guardada: $listId")
            listId
        } catch (e: Exception) {
            Log.e("ReadingListRepository", "Error guardando lista: ${e.message}", e)
            null
        }
    }

    /**
     * Actualiza el orden de los libros en una lista
     */
    suspend fun updateListOrder(listId: String, bookIds: List<Int>, syncToFirestore: Boolean = true): Boolean {
        return try {
            val list = readingListDao.getListById(listId) ?: return false
            val updated = list.copy(
                bookIds = bookIds,
                updatedAt = System.currentTimeMillis()
            )

            // Actualizar en Firestore
            if (syncToFirestore) {
                readingListService.saveReadingList(updated.toDomain())
            }

            // Actualizar en BD local
            readingListDao.update(updated)

            Log.d("ReadingListRepository", "Orden actualizado para lista: $listId")
            true
        } catch (e: Exception) {
            Log.e("ReadingListRepository", "Error actualizando orden: ${e.message}", e)
            false
        }
    }

    /**
     * Agrega un libro a una lista
     */
    suspend fun addBookToList(listId: String, bookId: Int, syncToFirestore: Boolean = true): Boolean {
        return try {
            val list = readingListDao.getListById(listId) ?: return false
            val updatedBookIds = if (list.bookIds.contains(bookId)) {
                list.bookIds // Ya existe, no hacer nada
            } else {
                list.bookIds + bookId
            }

            updateListOrder(listId, updatedBookIds, syncToFirestore)
        } catch (e: Exception) {
            Log.e("ReadingListRepository", "Error agregando libro a lista: ${e.message}", e)
            false
        }
    }

    /**
     * Elimina un libro de una lista
     */
    suspend fun removeBookFromList(listId: String, bookId: Int, syncToFirestore: Boolean = true): Boolean {
        return try {
            val list = readingListDao.getListById(listId) ?: return false
            val updatedBookIds = list.bookIds.filter { it != bookId }

            updateListOrder(listId, updatedBookIds, syncToFirestore)
        } catch (e: Exception) {
            Log.e("ReadingListRepository", "Error eliminando libro de lista: ${e.message}", e)
            false
        }
    }

    /**
     * Elimina una lista
     */
    suspend fun deleteReadingList(listId: String, syncToFirestore: Boolean = true): Boolean {
        return try {
            // Eliminar de Firestore
            if (syncToFirestore) {
                readingListService.deleteReadingList(listId)
            }

            // Eliminar de BD local
            readingListDao.deleteById(listId)

            Log.d("ReadingListRepository", "Lista eliminada: $listId")
            true
        } catch (e: Exception) {
            Log.e("ReadingListRepository", "Error eliminando lista: ${e.message}", e)
            false
        }
    }

    /**
     * Obtiene listas públicas desde Firestore
     */
    suspend fun getPublicReadingLists(limit: Int = 50): List<ReadingList> {
        return try {
            val lists = readingListService.getPublicReadingLists(limit)
            
            // Guardar en BD local para acceso offline
            lists.forEach { list ->
                try {
                    readingListDao.insert(list.toEntity())
                } catch (e: Exception) {
                    Log.w("ReadingListRepository", "Error guardando lista pública en local: ${e.message}")
                }
            }

            lists
        } catch (e: Exception) {
            Log.e("ReadingListRepository", "Error obteniendo listas públicas: ${e.message}", e)
            // Fallback a BD local
            readingListDao.getPublicLists().map { it.toDomain() }
        }
    }

    /**
     * Sigue una lista pública
     */
    suspend fun followList(listId: String, userId: String): Boolean {
        return try {
            val success = readingListService.followList(listId, userId)
            if (success) {
                // Actualizar contador en BD local
                val list = readingListDao.getListById(listId)
                if (list != null) {
                    readingListDao.update(list.copy(followerCount = list.followerCount + 1))
                }
            }
            success
        } catch (e: Exception) {
            Log.e("ReadingListRepository", "Error siguiendo lista: ${e.message}", e)
            false
        }
    }

    /**
     * Deja de seguir una lista
     */
    suspend fun unfollowList(listId: String, userId: String): Boolean {
        return try {
            val success = readingListService.unfollowList(listId, userId)
            if (success) {
                // Actualizar contador en BD local
                val list = readingListDao.getListById(listId)
                if (list != null && list.followerCount > 0) {
                    readingListDao.update(list.copy(followerCount = list.followerCount - 1))
                }
            }
            success
        } catch (e: Exception) {
            Log.e("ReadingListRepository", "Error dejando de seguir lista: ${e.message}", e)
            false
        }
    }

    /**
     * Verifica si un usuario sigue una lista
     */
    suspend fun isFollowingList(listId: String, userId: String): Boolean {
        return try {
            readingListService.isFollowingList(listId, userId)
        } catch (e: Exception) {
            Log.e("ReadingListRepository", "Error verificando si sigue lista: ${e.message}", e)
            false
        }
    }

    /**
     * Obtiene las listas que sigue un usuario
     */
    suspend fun getFollowedLists(userId: String): List<ReadingList> {
        return try {
            readingListService.getFollowedLists(userId)
        } catch (e: Exception) {
            Log.e("ReadingListRepository", "Error obteniendo listas seguidas: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Sincroniza listas del usuario desde Firestore
     */
    suspend fun syncUserListsFromFirestore(userId: String) {
        try {
            val remoteLists = readingListService.getUserReadingLists(userId)
            remoteLists.forEach { list ->
                readingListDao.insert(list.toEntity())
            }
            Log.d("ReadingListRepository", "Listas sincronizadas desde Firestore: ${remoteLists.size}")
        } catch (e: Exception) {
            Log.e("ReadingListRepository", "Error sincronizando listas: ${e.message}", e)
        }
    }
}

