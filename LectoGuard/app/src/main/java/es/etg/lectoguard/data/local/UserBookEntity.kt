package es.etg.lectoguard.data.local

import androidx.room.Entity

@Entity(tableName = "user_book", primaryKeys = ["userId", "bookId"])
data class UserBookEntity(
    val userId: Int,
    val bookId: Int,
    val savedDate: String
)