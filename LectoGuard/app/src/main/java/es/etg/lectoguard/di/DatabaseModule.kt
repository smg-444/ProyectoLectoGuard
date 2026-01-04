package es.etg.lectoguard.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.etg.lectoguard.data.local.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLectoGuardDatabase(
        @ApplicationContext context: Context
    ): LectoGuardDatabase {
        return Room.databaseBuilder(
            context,
            LectoGuardDatabase::class.java,
            "lectoguard_database"
        ).build()
    }

    @Provides
    fun provideUserDao(database: LectoGuardDatabase): UserDao = database.userDao()

    @Provides
    fun provideBookDao(database: LectoGuardDatabase): BookDao = database.bookDao()

    @Provides
    fun provideUserBookDao(database: LectoGuardDatabase): UserBookDao = database.userBookDao()

    @Provides
    fun provideReadingListDao(database: LectoGuardDatabase): ReadingListDao = database.readingListDao()
}

