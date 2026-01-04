package es.etg.lectoguard.data.local

import androidx.room.Entity
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import es.etg.lectoguard.domain.model.ReadingStatus

@Entity(tableName = "user_book", primaryKeys = ["userId", "bookId"])
@TypeConverters(ReadingStatusConverter::class, StringListConverter::class)
data class UserBookEntity(
    val userId: Int,
    val bookId: Int,
    val savedDate: String,
    val readingStatus: ReadingStatus = ReadingStatus.WANT_TO_READ,
    val tags: List<String> = emptyList()
)

class ReadingStatusConverter {
    @TypeConverter
    fun fromReadingStatus(status: ReadingStatus): String {
        return status.name
    }

    @TypeConverter
    fun toReadingStatus(status: String): ReadingStatus {
        return ReadingStatus.fromString(status)
    }
}

class StringListConverter {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}
