package es.etg.lectoguard.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.etg.lectoguard.data.repository.*
import es.etg.lectoguard.domain.usecase.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // User UseCases
    @Provides
    @Singleton
    fun provideLoginUseCase(userRepository: UserRepository): LoginUseCase {
        return LoginUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideRegisterUseCase(userRepository: UserRepository): RegisterUseCase {
        return RegisterUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideIsFollowingUseCase(userRepository: UserRepository): IsFollowingUseCase {
        return IsFollowingUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideGetFollowCountsUseCase(userRepository: UserRepository): GetFollowCountsUseCase {
        return GetFollowCountsUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideFollowUserUseCase(userRepository: UserRepository): FollowUserUseCase {
        return FollowUserUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideUnfollowUserUseCase(userRepository: UserRepository): UnfollowUserUseCase {
        return UnfollowUserUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateUserProfileUseCase(userRepository: UserRepository): UpdateUserProfileUseCase {
        return UpdateUserProfileUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateProfileWithAvatarUseCase(userRepository: UserRepository): UpdateProfileWithAvatarUseCase {
        return UpdateProfileWithAvatarUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateProfileBioUseCase(userRepository: UserRepository): UpdateProfileBioUseCase {
        return UpdateProfileBioUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideSearchUsersUseCase(userRepository: UserRepository): SearchUsersUseCase {
        return SearchUsersUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideGetAllUsersUseCase(userRepository: UserRepository): GetAllUsersUseCase {
        return GetAllUsersUseCase(userRepository)
    }

    // Book UseCases
    @Provides
    @Singleton
    fun provideGetBooksUseCase(bookRepository: BookRepository): GetBooksUseCase {
        return GetBooksUseCase(bookRepository)
    }

    @Provides
    @Singleton
    fun provideSaveBookUseCase(bookRepository: BookRepository): SaveBookUseCase {
        return SaveBookUseCase(bookRepository)
    }

    @Provides
    @Singleton
    fun provideGetBookDetailUseCase(bookRepository: BookRepository): GetBookDetailUseCase {
        return GetBookDetailUseCase(bookRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateBookReadingStatusUseCase(bookRepository: BookRepository): UpdateBookReadingStatusUseCase {
        return UpdateBookReadingStatusUseCase(bookRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserBookUseCase(bookRepository: BookRepository): GetUserBookUseCase {
        return GetUserBookUseCase(bookRepository)
    }

    @Provides
    @Singleton
    fun provideGetBooksByStatusUseCase(bookRepository: BookRepository): GetBooksByStatusUseCase {
        return GetBooksByStatusUseCase(bookRepository)
    }

    @Provides
    @Singleton
    fun provideGetBookCountByStatusUseCase(bookRepository: BookRepository): GetBookCountByStatusUseCase {
        return GetBookCountByStatusUseCase(bookRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateBookTagsUseCase(bookRepository: BookRepository): UpdateBookTagsUseCase {
        return UpdateBookTagsUseCase(bookRepository)
    }

    @Provides
    @Singleton
    fun provideGetAllUserTagsUseCase(bookRepository: BookRepository): GetAllUserTagsUseCase {
        return GetAllUserTagsUseCase(bookRepository)
    }

    @Provides
    @Singleton
    fun provideGetBooksByTagUseCase(bookRepository: BookRepository): GetBooksByTagUseCase {
        return GetBooksByTagUseCase(bookRepository)
    }

    // Rating UseCases
    @Provides
    @Singleton
    fun provideSaveRatingUseCase(ratingRepository: RatingRepository): SaveRatingUseCase {
        return SaveRatingUseCase(ratingRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserRatingUseCase(ratingRepository: RatingRepository): GetUserRatingUseCase {
        return GetUserRatingUseCase(ratingRepository)
    }

    @Provides
    @Singleton
    fun provideGetAverageRatingUseCase(ratingRepository: RatingRepository): GetAverageRatingUseCase {
        return GetAverageRatingUseCase(ratingRepository)
    }

    @Provides
    @Singleton
    fun provideSaveReviewUseCase(ratingRepository: RatingRepository): SaveReviewUseCase {
        return SaveReviewUseCase(ratingRepository)
    }

    @Provides
    @Singleton
    fun provideGetBookReviewsUseCase(ratingRepository: RatingRepository): GetBookReviewsUseCase {
        return GetBookReviewsUseCase(ratingRepository)
    }

    @Provides
    @Singleton
    fun provideToggleLikeReviewUseCase(ratingRepository: RatingRepository): ToggleLikeReviewUseCase {
        return ToggleLikeReviewUseCase(ratingRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateReviewUseCase(ratingRepository: RatingRepository): UpdateReviewUseCase {
        return UpdateReviewUseCase(ratingRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteReviewUseCase(ratingRepository: RatingRepository): DeleteReviewUseCase {
        return DeleteReviewUseCase(ratingRepository)
    }

    // Chat UseCases
    @Provides
    @Singleton
    fun provideGetOrCreateConversationUseCase(chatRepository: ChatRepository): GetOrCreateConversationUseCase {
        return GetOrCreateConversationUseCase(chatRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserConversationsUseCase(chatRepository: ChatRepository): GetUserConversationsUseCase {
        return GetUserConversationsUseCase(chatRepository)
    }

    @Provides
    @Singleton
    fun provideObserveUserConversationsUseCase(chatRepository: ChatRepository): ObserveUserConversationsUseCase {
        return ObserveUserConversationsUseCase(chatRepository)
    }

    @Provides
    @Singleton
    fun provideSendMessageUseCase(chatRepository: ChatRepository): SendMessageUseCase {
        return SendMessageUseCase(chatRepository)
    }

    @Provides
    @Singleton
    fun provideGetConversationMessagesUseCase(chatRepository: ChatRepository): GetConversationMessagesUseCase {
        return GetConversationMessagesUseCase(chatRepository)
    }

    @Provides
    @Singleton
    fun provideObserveConversationMessagesUseCase(chatRepository: ChatRepository): ObserveConversationMessagesUseCase {
        return ObserveConversationMessagesUseCase(chatRepository)
    }

    @Provides
    @Singleton
    fun provideMarkMessagesAsReadUseCase(chatRepository: ChatRepository): MarkMessagesAsReadUseCase {
        return MarkMessagesAsReadUseCase(chatRepository)
    }

    // Feed UseCases
    @Provides
    @Singleton
    fun provideGetUserFeedUseCase(feedRepository: FeedRepository): GetUserFeedUseCase {
        return GetUserFeedUseCase(feedRepository)
    }

    @Provides
    @Singleton
    fun provideObserveUserFeedUseCase(feedRepository: FeedRepository): ObserveUserFeedUseCase {
        return ObserveUserFeedUseCase(feedRepository)
    }

    @Provides
    @Singleton
    fun provideGetMoreFeedItemsUseCase(feedRepository: FeedRepository): GetMoreFeedItemsUseCase {
        return GetMoreFeedItemsUseCase(feedRepository)
    }

    // Recommendation UseCases
    @Provides
    @Singleton
    fun provideCalculateUserInterestsUseCase(recommendationRepository: RecommendationRepository): CalculateUserInterestsUseCase {
        return CalculateUserInterestsUseCase(recommendationRepository)
    }

    @Provides
    @Singleton
    fun provideGetRecommendationsUseCase(recommendationRepository: RecommendationRepository): GetRecommendationsUseCase {
        return GetRecommendationsUseCase(recommendationRepository)
    }

    // Reading List UseCases
    @Provides
    @Singleton
    fun provideGetUserReadingListsUseCase(readingListRepository: ReadingListRepository): GetUserReadingListsUseCase {
        return GetUserReadingListsUseCase(readingListRepository)
    }

    @Provides
    @Singleton
    fun provideGetReadingListByIdUseCase(readingListRepository: ReadingListRepository): GetReadingListByIdUseCase {
        return GetReadingListByIdUseCase(readingListRepository)
    }

    @Provides
    @Singleton
    fun provideSaveReadingListUseCase(readingListRepository: ReadingListRepository): SaveReadingListUseCase {
        return SaveReadingListUseCase(readingListRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateListOrderUseCase(readingListRepository: ReadingListRepository): UpdateListOrderUseCase {
        return UpdateListOrderUseCase(readingListRepository)
    }

    @Provides
    @Singleton
    fun provideAddBookToListUseCase(readingListRepository: ReadingListRepository): AddBookToListUseCase {
        return AddBookToListUseCase(readingListRepository)
    }

    @Provides
    @Singleton
    fun provideRemoveBookFromListUseCase(readingListRepository: ReadingListRepository): RemoveBookFromListUseCase {
        return RemoveBookFromListUseCase(readingListRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteReadingListUseCase(readingListRepository: ReadingListRepository): DeleteReadingListUseCase {
        return DeleteReadingListUseCase(readingListRepository)
    }

    @Provides
    @Singleton
    fun provideGetPublicReadingListsUseCase(readingListRepository: ReadingListRepository): GetPublicReadingListsUseCase {
        return GetPublicReadingListsUseCase(readingListRepository)
    }

    @Provides
    @Singleton
    fun provideFollowListUseCase(readingListRepository: ReadingListRepository): FollowListUseCase {
        return FollowListUseCase(readingListRepository)
    }

    @Provides
    @Singleton
    fun provideUnfollowListUseCase(readingListRepository: ReadingListRepository): UnfollowListUseCase {
        return UnfollowListUseCase(readingListRepository)
    }

    @Provides
    @Singleton
    fun provideIsFollowingListUseCase(readingListRepository: ReadingListRepository): IsFollowingListUseCase {
        return IsFollowingListUseCase(readingListRepository)
    }
}

