package com.example.shots

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.room.Room
import com.example.shots.data.AppDatabase
import com.example.shots.data.BlockedUserDao
import com.example.shots.data.BookmarkDao
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.ReceivedLikeDao
import com.example.shots.data.ReceivedShotDao
import com.example.shots.data.SentLikeDao
import com.example.shots.data.SentShotDao
import com.example.shots.data.UserDao
import com.example.shots.data.UserWhoBlockedYouDao
import com.example.shots.ui.theme.AuthViewModel
import com.example.shots.ui.theme.BlockViewModel
import com.example.shots.ui.theme.BlockedUserViewModel
import com.example.shots.ui.theme.EditProfileViewModel
import com.example.shots.ui.theme.LoginViewModel
import com.example.shots.ui.theme.ReceivedLikeViewModel
import com.example.shots.ui.theme.ReceivedShotViewModel
import com.example.shots.ui.theme.SentLikeViewModel
import com.example.shots.ui.theme.SentShotViewModel
import com.example.shots.ui.theme.SignupViewModel
import com.example.shots.ui.theme.UserWhoBlockedYouViewModel
import com.example.shots.ui.theme.UsersViewModel
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
import javax.inject.Singleton


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

}

@Module
@InstallIn(SingletonComponent::class)
object GetStreamClientModule {

    @Provides
    @Singleton
    fun provideGetStreamClient(
        applicationContext: Context,
        usersViewModel: UsersViewModel
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

        val yourUser = usersViewModel.getUser()

        val user = User(
            id = yourUser?.id ?: "",
            name = yourUser?.displayName ?: "",
            image = yourUser?.mediaOne ?: "",
        )

        val firebaseAuth = FirebaseModule.provideFirebaseAuth()

        val currentUser = firebaseAuth.currentUser

        val apiSecret = "g4z7zzqynhaydf9zarcbpeq653tk26zs4rcfdf598avg4xq9skk8cw26vnztakae"

        val userId = user.id // Replace with the actual user ID

        val token = io.getstream.chat.java.models.User.createToken(apiSecret, userId, null, null);

        client.connectUser(
            user,
            token
        ).enqueue { result ->
            if (result.isSuccess) {
                // Logged in
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new FCM registration token
                    fcmRegistrationToken = task.result


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
        return FirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideAuthViewModel(
        firebaseRepository: FirebaseRepository, firestore: FirebaseFirestore
    ): AuthViewModel {
        return AuthViewModel(firebaseRepository, firestore)
    }

    @Provides
    @ViewModelScoped
    fun provideLoginViewModel(): LoginViewModel {
        return LoginViewModel(
            FirebaseModule.provideFirebaseRepository(
                FirebaseModule.provideFirebaseAuth(),
                FirebaseModule.provideFirestore(),
                FirebaseModule.provideStorage()
            )
        )
    }

    @Provides
    @ViewModelScoped
    fun provideSignupViewModel(): SignupViewModel {
        return SignupViewModel(
            FirebaseModule.provideFirebaseRepository(
                FirebaseModule.provideFirebaseAuth(),
                FirebaseModule.provideFirestore(),
                FirebaseModule.provideStorage()
            ), provideAuthViewModel(
                FirebaseModule.provideFirebaseRepository(
                    FirebaseModule.provideFirebaseAuth(),
                    FirebaseModule.provideFirestore(),
                    FirebaseModule.provideStorage()
                ), FirebaseModule.provideFirestore()
            )
        )
    }


    @Provides
    @ViewModelScoped
    fun provideEditProfileViewModel(
        firebaseRepository: FirebaseRepository, firebaseAuth: FirebaseAuth
    ): EditProfileViewModel {
        return EditProfileViewModel(firebaseRepository, firebaseAuth)
    }

    @Provides
    @ViewModelScoped
    fun provideUsersViewModel(
        firebaseRepository: FirebaseRepository, firebaseAuth: FirebaseAuth,
        appDatabase: AppDatabase
    ): UsersViewModel {
        return UsersViewModel(firebaseRepository, firebaseAuth, appDatabase)
    }

    @Provides
    @ViewModelScoped
    fun provideSentLikeViewModel(
        firebaseRepository: FirebaseRepository, firebaseAuth: FirebaseAuth,
        appDatabase: AppDatabase
    ): SentLikeViewModel {
        return SentLikeViewModel(firebaseRepository, firebaseAuth, appDatabase)
    }

    @Provides
    @ViewModelScoped
    fun provideReceivedLikeViewModel(
        firebaseRepository: FirebaseRepository, firebaseAuth: FirebaseAuth,
        appDatabase: AppDatabase
    ): ReceivedLikeViewModel {
        return ReceivedLikeViewModel(firebaseRepository, firebaseAuth, appDatabase)
    }

    @Provides
    @ViewModelScoped
    fun provideSentShotViewModel(
        firebaseRepository: FirebaseRepository, firebaseAuth: FirebaseAuth,
        appDatabase: AppDatabase
    ): SentShotViewModel {
        return SentShotViewModel(firebaseRepository, firebaseAuth, appDatabase)
    }

    @Provides
    @ViewModelScoped
    fun provideReceivedShotViewModel(
        firebaseRepository: FirebaseRepository, firebaseAuth: FirebaseAuth,
        appDatabase: AppDatabase
    ): ReceivedShotViewModel {
        return ReceivedShotViewModel(firebaseRepository, firebaseAuth, appDatabase)
    }

    @Provides
    @ViewModelScoped
    fun provideBlockedUserViewModel(
        firebaseRepository: FirebaseRepository, firebaseAuth: FirebaseAuth,
        appDatabase: AppDatabase
    ): BlockedUserViewModel {
        return BlockedUserViewModel(firebaseRepository, firebaseAuth, appDatabase)
    }

    @Provides
    @ViewModelScoped
    fun provideUserWhoBlockedYouViewModel(
        firebaseRepository: FirebaseRepository, firebaseAuth: FirebaseAuth,
        appDatabase: AppDatabase
    ): UserWhoBlockedYouViewModel {
        return UserWhoBlockedYouViewModel(firebaseRepository, firebaseAuth, appDatabase)
    }

    @Provides
    @ViewModelScoped
    fun provideBlockViewModel(
        firebaseRepository: FirebaseRepository, firebaseAuth: FirebaseAuth,
        appDatabase: AppDatabase
    ): BlockViewModel {
        return BlockViewModel(firebaseRepository, firebaseAuth, appDatabase)
    }

}
