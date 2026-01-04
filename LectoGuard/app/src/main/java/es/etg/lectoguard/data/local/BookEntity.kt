package es.etg.lectoguard.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import es.etg.lectoguard.domain.model.BookGenre

@Entity(tableName = "book")
@TypeConverters(BookGenreListConverter::class)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val coverImage: String,
    val genres: List<BookGenre> = emptyList() // Géneros del libro desde la base de datos
)

class BookGenreListConverter {
    @TypeConverter
    fun fromGenreList(genres: List<BookGenre>): String {
        return genres.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toGenreList(genresString: String): List<BookGenre> {
        return if (genresString.isEmpty()) {
            emptyList()
        } else {
            genresString.split(",").mapNotNull { genreName ->
                try {
                    BookGenre.valueOf(genreName.trim())
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}

