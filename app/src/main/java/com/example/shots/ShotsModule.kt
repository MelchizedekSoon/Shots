package com.example.shots

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.shots.data.AppDatabase
import com.example.shots.data.AuthRemoteDataSource
import com.example.shots.data.AuthRepository
import com.example.shots.data.AuthRepositoryImpl
import com.example.shots.data.BlockedUserDao
import com.example.shots.data.BlockedUserLocalDataSource
import com.example.shots.data.BlockedUserRemoteDataSource
import com.example.shots.data.BlockedUserRemoteDataSourceImpl
import com.example.shots.data.BlockedUserRepository
import com.example.shots.data.BlockedUserRepositoryImpl
import com.example.shots.data.BookmarkDao
import com.example.shots.data.BookmarkLocalDataSource
import com.example.shots.data.BookmarkRemoteDataSource
import com.example.shots.data.BookmarkRemoteDataSourceImpl
import com.example.shots.data.BookmarkRepository
import com.example.shots.data.BookmarkRepositoryImpl
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.FirebaseRepositoryImpl
import com.example.shots.data.IfSeenReceivedShotDao
import com.example.shots.data.IfSeenReceivedShotLocalDataSource
import com.example.shots.data.IfSeenReceivedShotRemoteDataSource
import com.example.shots.data.IfSeenReceivedShotRemoteDataSourceImpl
import com.example.shots.data.IfSeenReceivedShotRepository
import com.example.shots.data.IfSeenReceivedShotRepositoryImpl
import com.example.shots.data.ReceivedLikeDao
import com.example.shots.data.ReceivedLikeLocalDataSource
import com.example.shots.data.ReceivedLikeRemoteDataSource
import com.example.shots.data.ReceivedLikeRemoteDataSourceImpl
import com.example.shots.data.ReceivedLikeRepository
import com.example.shots.data.ReceivedLikeRepositoryImpl
import com.example.shots.data.ReceivedShotDao
import com.example.shots.data.ReceivedShotLocalDataSource
import com.example.shots.data.ReceivedShotRemoteDataSource
import com.example.shots.data.ReceivedShotRemoteDataSourceImpl
import com.example.shots.data.ReceivedShotRepository
import com.example.shots.data.ReceivedShotRepositoryImpl
import com.example.shots.data.SentLikeDao
import com.example.shots.data.SentLikeLocalDataSource
import com.example.shots.data.SentLikeRemoteDataSource
import com.example.shots.data.SentLikeRemoteDataSourceImpl
import com.example.shots.data.SentLikeRepository
import com.example.shots.data.SentLikeRepositoryImpl
import com.example.shots.data.SentShotDao
import com.example.shots.data.SentShotLocalDataSource
import com.example.shots.data.SentShotRemoteDataSource
import com.example.shots.data.SentShotRemoteDataSourceImpl
import com.example.shots.data.SentShotRepository
import com.example.shots.data.SentShotRepositoryImpl
import com.example.shots.data.UserDao
import com.example.shots.data.UserLocalDataSource
import com.example.shots.data.UserLocalDataSourceImpl
import com.example.shots.data.UserRemoteDataSource
import com.example.shots.data.UserRemoteDataSourceImpl
import com.example.shots.data.UserRepository
import com.example.shots.data.UserRepositoryImpl
import com.example.shots.data.UserWhoBlockedYouDao
import com.example.shots.data.UserWhoBlockedYouLocalDataSource
import com.example.shots.data.UserWhoBlockedYouRemoteDataSource
import com.example.shots.data.UserWhoBlockedYouRemoteDataSourceImpl
import com.example.shots.data.UserWhoBlockedYouRepository
import com.example.shots.data.UserWhoBlockedYouRepositoryImpl
import com.example.shots.ui.theme.AuthViewModel
import com.example.shots.ui.theme.BlockViewModel
import com.example.shots.ui.theme.BlockedUserViewModel
import com.example.shots.ui.theme.BookmarkViewModel
import com.example.shots.ui.theme.EditProfileViewModel
import com.example.shots.ui.theme.FirebaseViewModel
import com.example.shots.ui.theme.IfSeenReceivedShotViewModel
import com.example.shots.ui.theme.LoginViewModel
import com.example.shots.ui.theme.ReceivedLikeViewModel
import com.example.shots.ui.theme.ReceivedShotViewModel
import com.example.shots.ui.theme.SentLikeViewModel
import com.example.shots.ui.theme.SentShotViewModel
import com.example.shots.ui.theme.SignupViewModel
import com.example.shots.ui.theme.UserViewModel
import com.example.shots.ui.theme.UserWhoBlockedYouViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import io.getstream.android.push.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Named("TestDispatcher") // Use a qualifier for the test dispatcher
    fun providesTestDispatcher(): CoroutineDispatcher = StandardTestDispatcher()

}

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "database-name"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(appDatabase: AppDatabase): BookmarkDao {
        return appDatabase.bookmarkDao()
    }

    @Provides
    @Singleton
    fun provideReceiveLikeDao(appDatabase: AppDatabase): ReceivedLikeDao {
        return appDatabase.receivedLikeDao()
    }

    @Provides
    @Singleton
    fun provideSentLikeDao(appDatabase: AppDatabase): SentLikeDao {
        return appDatabase.sentLikeDao()
    }

    @Provides
    @Singleton
    fun provideReceiveShotDao(appDatabase: AppDatabase): ReceivedShotDao {
        return appDatabase.receivedShotDao()
    }

    @Provides
    @Singleton
    fun provideSentShotDao(appDatabase: AppDatabase): SentShotDao {
        return appDatabase.sentShotDao()
    }

    @Provides
    @Singleton
    fun provideBlockedUserDao(appDatabase: AppDatabase): BlockedUserDao {
        return appDatabase.blockedUserDao()
    }

    @Provides
    @Singleton
    fun provideUserWhoBlockedYouDao(appDatabase: AppDatabase): UserWhoBlockedYouDao {
        return appDatabase.userWhoBlockedYouDao()
    }

    @Provides
    @Singleton
    fun provideIfSeenReceivedShotDao(appDatabase: AppDatabase): IfSeenReceivedShotDao {
        return appDatabase.ifSeenReceivedShotDao()
    }

}

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideBlockedUserLocalDataSource(
        firebaseAuth: FirebaseAuth,
        blockedUserDao: BlockedUserDao
    ): BlockedUserLocalDataSource {
        return BlockedUserLocalDataSource(firebaseAuth, blockedUserDao)
    }

    @Provides
    @Singleton
    fun provideBlockedRemoteLocalDataSource(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore
    ):
            BlockedUserRemoteDataSource {
        return BlockedUserRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)
    }


    @Provides
    @Singleton
    fun provideBookmarkLocalDataSource(
        firebaseAuth: FirebaseAuth,
        bookmarkDao: BookmarkDao
    ): BookmarkLocalDataSource {
        return BookmarkLocalDataSource(firebaseAuth, bookmarkDao)
    }

    @Provides
    @Singleton
    fun provideBookmarkRemoteDataSource(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore
    ):
            BookmarkRemoteDataSource {
        return BookmarkRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)
    }


    @Provides
    @Singleton
    fun provideReceivedLikeLocalDataSource(
        firebaseAuth: FirebaseAuth,
        receivedLikeDao: ReceivedLikeDao
    ): ReceivedLikeLocalDataSource {
        return ReceivedLikeLocalDataSource(firebaseAuth, receivedLikeDao)
    }

    @Provides
    @Singleton
    fun provideReceivedLikeRemoteDataSource(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore
    ):
            ReceivedLikeRemoteDataSource {
        return ReceivedLikeRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)
    }

    @Provides
    @Singleton
    fun provideReceivedShotLocalDataSource(
        firebaseAuth: FirebaseAuth,
        receivedShotDao: ReceivedShotDao
    ): ReceivedShotLocalDataSource {
        return ReceivedShotLocalDataSource(firebaseAuth, receivedShotDao)
    }

    @Provides
    @Singleton
    fun provideReceivedShotRemoteDataSource(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        firebaseStorage: FirebaseStorage
    ):
            ReceivedShotRemoteDataSource {
        return ReceivedShotRemoteDataSourceImpl(firebaseAuth, firebaseFirestore, firebaseStorage)
    }

    @Provides
    @Singleton
    fun provideSentLikeLocalDataSource(
        firebaseAuth: FirebaseAuth,
        sentLikeDao: SentLikeDao
    ): SentLikeLocalDataSource {
        return SentLikeLocalDataSource(firebaseAuth, sentLikeDao)
    }

    @Provides
    @Singleton
    fun provideSentLikeRemoteDataSource(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        sentLikeDao: SentLikeDao
    ):
            SentLikeRemoteDataSource {
        return SentLikeRemoteDataSourceImpl(firebaseAuth, firebaseFirestore, sentLikeDao)
    }

    @Provides
    @Singleton
    fun provideSentShotLocalDataSource(
        firebaseAuth: FirebaseAuth,
        sentShotDao: SentShotDao
    ): SentShotLocalDataSource {
        return SentShotLocalDataSource(firebaseAuth, sentShotDao)
    }

    @Provides
    @Singleton
    fun provideSentShotRemoteDataSource(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        firebaseStorage: FirebaseStorage
    ):
            SentShotRemoteDataSource {
        return SentShotRemoteDataSourceImpl(firebaseAuth, firebaseFirestore, firebaseStorage)
    }

    @Provides
    @Singleton
    fun provideUserLocalDataSource(
        firebaseAuth: FirebaseAuth,
        userDao: UserDao
    ): UserLocalDataSource {
        return UserLocalDataSourceImpl(firebaseAuth, userDao)
    }

    @Provides
    @Singleton
    fun provideUserRemoteDataSource(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        firebaseStorage: FirebaseStorage
    ): UserRemoteDataSource {
        return UserRemoteDataSourceImpl(
            firebaseAuth,
            firebaseFirestore, firebaseStorage
        )
    }

    @Provides
    @Singleton
    fun provideUserWhoBlockedYouLocalDataSource(
        firebaseAuth: FirebaseAuth,
        userWhoBlockedYouDao: UserWhoBlockedYouDao
    ): UserWhoBlockedYouLocalDataSource {
        return UserWhoBlockedYouLocalDataSource(
            firebaseAuth,
            userWhoBlockedYouDao
        )
    }

    @Provides
    @Singleton
    fun provideUserWhoBlockedYouRemoteDataSource(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore
    ): UserWhoBlockedYouRemoteDataSource {
        return UserWhoBlockedYouRemoteDataSourceImpl(
            firebaseAuth,
            firebaseFirestore
        )
    }

    @Provides
    @Singleton
    fun provideIfSeenReceivedShotRemoteDataSource(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        firebaseStorage: FirebaseStorage
    ): IfSeenReceivedShotRemoteDataSource {
        return IfSeenReceivedShotRemoteDataSourceImpl(
            firebaseAuth,
            firebaseFirestore, firebaseStorage
        )
    }

}

@Module
@InstallIn(SingletonComponent::class)
object GetStreamClientModule {

    @Provides
    @Singleton
    fun provideGetStreamClient(
        applicationContext: Context,
        userViewModel: UserViewModel,
        firebaseViewModel: FirebaseViewModel
    ): ChatClient {

        // 1 - Set up the OfflinePlugin for offline storage
        val offlinePluginFactory = StreamOfflinePluginFactory(appContext = applicationContext)
        val statePluginFactory =
            StreamStatePluginFactory(config = StatePluginConfig(), appContext = applicationContext)

        // 2 - Set up the client for API calls and with the plugin for offline storage
        //test api key = "uun7ywwamhs9"

        val apiKey = "8cb7tu482evr"

        val notificationConfig = NotificationConfig(
            pushDeviceGenerators = listOf(FirebasePushDeviceGenerator(providerName = "firebase"))
        )

        val client = ChatClient.Builder(apiKey, applicationContext)
            .notifications(notificationConfig)
            .withPlugins(offlinePluginFactory, statePluginFactory)
            .logLevel(ChatLogLevel.ALL) // Set to NOTHING in prod
            .build()

        Log.d("ShotsModule", "yourUserId = ${firebaseViewModel.getYourUserId()}")

        val yourUser = userViewModel.fetchUser()

        Log.d("ShotsModule", "yourUser = $yourUser")

        val user = User(
            id = yourUser?.id ?: "",
            name = yourUser?.displayName ?: "",
            image = yourUser?.mediaOne ?: "",
        )

        val apiSecret = "g4z7zzqynhaydf9zarcbpeq653tk26zs4rcfdf598avg4xq9skk8cw26vnztakae"

        var userId = user.id // Replace with the actual user ID

        val token = io.getstream.chat.java.models.User.createToken(apiSecret, userId, null, null)

        client.connectUser(
            user,
            token
        ).enqueue { result ->
            if (result.isSuccess) {
                // Logged in
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(
                            ContentValues.TAG,
                            "Fetching FCM registration token failed",
                            task.exception
                        )
                        return@OnCompleteListener
                    }

                    // Get new FCM registration token
                    val fcmRegistrationToken = task.result


                    client.addDevice(
                        Device(
                            token = task.result,
                            pushProvider = PushProvider.FIREBASE,
                            providerName = "ShotsPushConfiguration", // Optional, if adding to multi-bundle named provider
                        )
                    ).enqueue { result ->
                        if (result.isSuccess) {
                            Log.d("ShotsModule", "Device was successfully registered!")
                            // Device was successfully registered
                        } else {
                            Log.d("ShotsModule", "Device failed! = ${result.errorOrNull()}")
                            // Handle result.error()
                        }
                    }

                    // Log and toast
//                        val msg = getString(R.string.msg_token_fmt, token)
                    Log.d(ContentValues.TAG, "The obtained token - $fcmRegistrationToken")
//                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                })
                val user: User = result.getOrThrow().user
                val connectionId: String = result.getOrThrow().connectionId
            } else {
                // Handle result.error()
            }
        }




        return client

    }
}


@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    // If you want to use FirebaseAuth in ViewModel
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    @Singleton
    fun provideStorage(): FirebaseStorage {
        return Firebase.storage
    }

    @Provides
    @Singleton
    fun provideFirebaseRepository(
        firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore, firebaseStorage: FirebaseStorage
    ): FirebaseRepository {
        return FirebaseRepositoryImpl(firebaseAuth, firestore, firebaseStorage)
    }

}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideBlockedUserRepository(
        blockedUserLocalDataSource: BlockedUserLocalDataSource,
        blockedUserRemoteDataSource: BlockedUserRemoteDataSource
    ): BlockedUserRepository {
        return BlockedUserRepositoryImpl(blockedUserLocalDataSource, blockedUserRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideBookmarkRepository(
        bookmarkLocalDataSource: BookmarkLocalDataSource,
        bookmarkRemoteDataSource: BookmarkRemoteDataSource,
        dispatcher: CoroutineDispatcher
    ): BookmarkRepository {
        return BookmarkRepositoryImpl(bookmarkLocalDataSource, bookmarkRemoteDataSource, dispatcher)
    }

    @Provides
    @Singleton
    fun provideReceivedLikeRepository(
        receivedLikeLocalDataSource: ReceivedLikeLocalDataSource,
        receivedLikeRemoteDataSource: ReceivedLikeRemoteDataSource
    ): ReceivedLikeRepository {
        return ReceivedLikeRepositoryImpl(receivedLikeLocalDataSource, receivedLikeRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideReceivedShotRepository(
        receivedShotLocalDataSource: ReceivedShotLocalDataSource,
        receivedShotRemoteDataSource: ReceivedShotRemoteDataSource
    ): ReceivedShotRepository {
        return ReceivedShotRepositoryImpl(receivedShotLocalDataSource, receivedShotRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideSentLikeRepository(
        sentLikeLocalDataSource: SentLikeLocalDataSource,
        sentLikeRemoteDataSource: SentLikeRemoteDataSource
    ): SentLikeRepository {
        return SentLikeRepositoryImpl(sentLikeLocalDataSource, sentLikeRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideSentShotRepository(
        sentShotLocalDataSource: SentShotLocalDataSource,
        sentShotRemoteDataSource: SentShotRemoteDataSource
    ): SentShotRepository {
        return SentShotRepositoryImpl(sentShotLocalDataSource, sentShotRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userLocalDataSource: UserLocalDataSource,
        userRemoteDataSource: UserRemoteDataSource
    ): UserRepository {
        return UserRepositoryImpl(userLocalDataSource, userRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideUserWhoBlockedYouRepository(
        userWhoBlockedYouLocalDataSource: UserWhoBlockedYouLocalDataSource,
        userWhoBlockedYouRemoteDataSource: UserWhoBlockedYouRemoteDataSource
    ): UserWhoBlockedYouRepository {
        return UserWhoBlockedYouRepositoryImpl(
            userWhoBlockedYouLocalDataSource,
            userWhoBlockedYouRemoteDataSource
        )
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        authRemoteDataSource:
        AuthRemoteDataSource
    ): AuthRepository {
        return AuthRepositoryImpl(authRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideIfSeenReceivedShotRepository(
        ifSeenReceivedShotLocalDataSource: IfSeenReceivedShotLocalDataSource,
        ifSeenReceivedShotRemoteDataSource: IfSeenReceivedShotRemoteDataSource
    ): IfSeenReceivedShotRepository {
        return IfSeenReceivedShotRepositoryImpl(
            ifSeenReceivedShotLocalDataSource,
            ifSeenReceivedShotRemoteDataSource
        )
    }

}

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideAuthViewModel(
        firebaseRepository: FirebaseRepository
    ): AuthViewModel {
        return AuthViewModel(firebaseRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideLoginViewModel(authRepository: AuthRepository): LoginViewModel {
        return LoginViewModel(
            authRepository
        )
    }

    @Provides
    @ViewModelScoped
    fun provideSignupViewModel(
        authRepository: AuthRepository,
        firebaseRepository: FirebaseRepository
    ): SignupViewModel {
        return SignupViewModel(authRepository, firebaseRepository)
    }


    @Provides
    @ViewModelScoped
    fun provideEditProfileViewModel(
        firebaseRepository: FirebaseRepository,
        userRepository: UserRepository,
        dispatcher: CoroutineDispatcher
    ): EditProfileViewModel {
        return EditProfileViewModel(firebaseRepository, userRepository, dispatcher)
    }

    @Provides
    @ViewModelScoped
    fun provideUserViewModel(
        firebaseRepository: FirebaseRepository,
        userRepository: UserRepository,
        userWhoBlockedYouRepository: UserWhoBlockedYouRepository,
        blockedUserRepository: BlockedUserRepository,
        dispatcher: CoroutineDispatcher
    ): UserViewModel {
        return UserViewModel(
            firebaseRepository, userRepository, userWhoBlockedYouRepository,
            blockedUserRepository,
            dispatcher
        )
    }

    @Provides
    @ViewModelScoped
    fun provideSentLikeViewModel(
        firebaseRepository: FirebaseRepository,
        sentLikeRepository: SentLikeRepository
    ): SentLikeViewModel {
        return SentLikeViewModel(firebaseRepository, sentLikeRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideReceivedLikeViewModel(
        receivedLikeRepository: ReceivedLikeRepository,
        firebaseRepository: FirebaseRepository, firebaseAuth: FirebaseAuth,
        dispatcher: CoroutineDispatcher
    ): ReceivedLikeViewModel {
        return ReceivedLikeViewModel(
            receivedLikeRepository,
            firebaseRepository,
            firebaseAuth,
            dispatcher
        )
    }

    @Provides
    @ViewModelScoped
    fun provideSentShotViewModel(
        sentShotRepository: SentShotRepository,
        firebaseRepository: FirebaseRepository, firebaseAuth: FirebaseAuth,
        appDatabase: AppDatabase, userRepository: UserRepository
    ): SentShotViewModel {
        return SentShotViewModel(
            sentShotRepository,
            firebaseRepository, firebaseAuth, appDatabase,
            userRepository
        )
    }

    @Provides
    @ViewModelScoped
    fun provideReceivedShotViewModel(
        receivedShotRepository: ReceivedShotRepository,
        ifSeenReceivedShotRepository: IfSeenReceivedShotRepository,
        firebaseRepository: FirebaseRepository,
        dispatcher: CoroutineDispatcher
    ): ReceivedShotViewModel {
        return ReceivedShotViewModel(
            receivedShotRepository, firebaseRepository,
            dispatcher
        )
    }

    @Provides
    @ViewModelScoped
    fun provideBlockedUserViewModel(
        blockedUserRepository: BlockedUserRepository,
        dispatcher: CoroutineDispatcher
    ): BlockedUserViewModel {
        return BlockedUserViewModel(blockedUserRepository, dispatcher)
    }

    @Provides
    @ViewModelScoped
    fun provideUserWhoBlockedYouViewModel(
        userWhoBlockedYouRepository: UserWhoBlockedYouRepository
    ): UserWhoBlockedYouViewModel {
        return UserWhoBlockedYouViewModel(
            userWhoBlockedYouRepository
        )
    }

    @Provides
    @ViewModelScoped
    fun provideBlockViewModel(
        firebaseRepository: FirebaseRepository, blockedUserRepository: BlockedUserRepository,
        userWhoBlockedYouRepository: UserWhoBlockedYouRepository
    ): BlockViewModel {
        return BlockViewModel(
            firebaseRepository,
            blockedUserRepository,
            userWhoBlockedYouRepository
        )
    }

    @Provides
    @ViewModelScoped
    fun provideIfSeenReceivedShotViewModel(
        ifSeenReceivedShotRepository: IfSeenReceivedShotRepository,
        firebaseRepository: FirebaseRepository,
        dispatcher: CoroutineDispatcher
    ): IfSeenReceivedShotViewModel {
        return IfSeenReceivedShotViewModel(
            ifSeenReceivedShotRepository,
            firebaseRepository,
            dispatcher
        )
    }

    @Provides
    @ViewModelScoped
    fun provideBookmarkViewModel(
        firebaseRepository: FirebaseRepository,
        bookmarkRepository: BookmarkRepository,
        userRepository: UserRepository,
        dispatcher: CoroutineDispatcher
    ): BookmarkViewModel {
        return BookmarkViewModel(
            firebaseRepository,
            bookmarkRepository,
            userRepository,
            dispatcher
        )
    }

}


//@Module
//@InstallIn(SingletonComponent::class)
//object TestAppModule {
//    @Provides
//    fun provideTestFirestore(): FirebaseFirestore {
//        val firestore = FirebaseFirestore.getInstance()
//        firestore.firestoreSettings = firestoreSettings {}
//        firestore.useEmulator("10.0.2.2", 8080)
//        return firestore
//    }
//
//    @Provides
//    fun provideTestFirebaseAuth(): FirebaseAuth {
//        val firebaseAuth = FirebaseModule.provideFirebaseAuth()
//        firebaseAuth.useEmulator("10.0.2.2", 9099)
//        return firebaseAuth
//    }
//
//    @Provides
//    fun provideTestFirebaseStorage(): FirebaseStorage {
//        val firebaseStorage = FirebaseModule.provideStorage()
//        firebaseStorage.useEmulator("10.0.2.2", 9199)
//        return firebaseStorage
//    }
//
//    @Provides
//    fun provideInMemoryAppDatabase(@ApplicationContext context: Context): AppDatabase {
//        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
//            .allowMainThreadQueries() // Allow main thread queries for testing
//            .build()
//    }
//
//}
