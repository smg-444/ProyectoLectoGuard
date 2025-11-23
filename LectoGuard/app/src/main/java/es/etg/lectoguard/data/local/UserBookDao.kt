package es.etg.lectoguard.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserBookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userBook: UserBookEntity)

    @Query("SELECT * FROM user_book WHERE userId = :userId")
    suspend fun getBooksByUser(userId: Int): List<UserBookEntity>
}