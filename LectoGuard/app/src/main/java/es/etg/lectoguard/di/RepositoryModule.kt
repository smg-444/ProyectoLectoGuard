package es.etg.lectoguard.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.etg.lectoguard.data.local.*
import es.etg.lectoguard.data.remote.BookApiService
import es.etg.lectoguard.data.repository.*
import es.etg.lectoguard.data.remote.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): UserRepository {
        return UserRepository(userDao, firebaseAuth, firestore, storage)
    }

    @Provides
    @Singleton
    fun provideBookRepository(
        bookDao: BookDao,
        userBookDao: UserBookDao,
        bookApiService: BookApiService,
        userBookService: UserBookService
    ): BookRepository {
        return BookRepository(bookDao, userBookDao, bookApiService, userBookService)
    }

    @Provides
    @Singleton
    fun provideRatingRepository(
        firestore: FirebaseFirestore
    ): RatingRepository {
        return RatingRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore
    ): ChatRepository {
        return ChatRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideFeedRepository(
        firestore: FirebaseFirestore
    ): FeedRepository {
        return FeedRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideInterestService(
        firestore: FirebaseFirestore
    ): InterestService {
        return InterestService(firestore)
    }

    @Provides
    @Singleton
    fun provideUserBookService(
        firestore: FirebaseFirestore
    ): UserBookService {
        return UserBookService(firestore)
    }

    @Provides
    @Singleton
    fun provideFollowService(
        firestore: FirebaseFirestore
    ): FollowService {
        return FollowService(firestore)
    }

    @Provides
    @Singleton
    fun provideReadingListService(
        firestore: FirebaseFirestore
    ): ReadingListService {
        return ReadingListService(firestore)
    }

    @Provides
    @Singleton
    fun provideReadingListRepository(
        readingListDao: ReadingListDao,
        readingListService: ReadingListService
    ): ReadingListRepository {
        return ReadingListRepository(readingListDao, readingListService)
    }

    @Provides
    @Singleton
    fun provideRecommendationRepository(
        bookDao: BookDao,
        userBookDao: UserBookDao,
        interestService: InterestService,
        userBookService: UserBookService,
        followService: FollowService
    ): RecommendationRepository {
        return RecommendationRepository(
            bookDao,
            userBookDao,
            interestService,
            userBookService,
            followService
        )
    }
}

