package es.etg.lectoguard.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "reading_list")
@TypeConverters(IntListConverter::class)
data class ReadingListEntity(
    @PrimaryKey val id: String,
    val userId: String, // Firebase UID
    val name: String,
    val description: String = "",
    val isPublic: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val bookIds: List<Int> = emptyList(), // IDs de libros en orden
    val followerCount: Int = 0
)

class IntListConverter {
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(",").mapNotNull { it.toIntOrNull() }
        }
    }
}

