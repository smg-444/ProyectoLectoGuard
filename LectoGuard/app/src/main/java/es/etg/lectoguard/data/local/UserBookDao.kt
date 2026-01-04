package es.etg.lectoguard.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import es.etg.lectoguard.domain.model.ReadingStatus

@Dao
interface UserBookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userBook: UserBookEntity)

    @Update
    suspend fun update(userBook: UserBookEntity)

    @Query("SELECT * FROM user_book WHERE userId = :userId")
    suspend fun getBooksByUser(userId: Int): List<UserBookEntity>

    @Query("SELECT * FROM user_book WHERE userId = :userId AND bookId = :bookId")
    suspend fun getBookByUserAndBookId(userId: Int, bookId: Int): UserBookEntity?

    @Query("SELECT * FROM user_book WHERE userId = :userId AND readingStatus = :status")
    suspend fun getBooksByUserAndStatus(userId: Int, status: String): List<UserBookEntity>

    @Query("SELECT COUNT(*) FROM user_book WHERE userId = :userId AND readingStatus = :status")
    suspend fun getBookCountByStatus(userId: Int, status: String): Int
    
    @Query("SELECT * FROM user_book WHERE userId = :userId")
    suspend fun getAllUserBooks(userId: Int): List<UserBookEntity>
}