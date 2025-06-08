package es.etg.lectoguard.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, BookEntity::class, UserBookEntity::class],
    version = 1
)
abstract class LectoGuardDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao
    abstract fun userBookDao(): UserBookDao

    companion object {
        @Volatile
        private var INSTANCE: LectoGuardDatabase? = null

        fun getInstance(context: Context): LectoGuardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LectoGuardDatabase::class.java,
                    "lectoguard-db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}