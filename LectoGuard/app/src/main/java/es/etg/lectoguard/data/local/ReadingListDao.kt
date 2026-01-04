package es.etg.lectoguard.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingListDao {
    @Query("SELECT * FROM reading_list WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getAllUserLists(userId: String): Flow<List<ReadingListEntity>>

    @Query("SELECT * FROM reading_list WHERE userId = :userId ORDER BY updatedAt DESC")
    suspend fun getAllUserListsSync(userId: String): List<ReadingListEntity>

    @Query("SELECT * FROM reading_list WHERE id = :listId")
    suspend fun getListById(listId: String): ReadingListEntity?

    @Query("SELECT * FROM reading_list WHERE id = :listId")
    fun getListByIdFlow(listId: String): Flow<ReadingListEntity?>

    @Query("SELECT * FROM reading_list WHERE isPublic = 1 ORDER BY followerCount DESC, updatedAt DESC LIMIT 50")
    suspend fun getPublicLists(): List<ReadingListEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ReadingListEntity)

    @Update
    suspend fun update(list: ReadingListEntity)

    @Delete
    suspend fun delete(list: ReadingListEntity)

    @Query("DELETE FROM reading_list WHERE id = :listId")
    suspend fun deleteById(listId: String)
}

