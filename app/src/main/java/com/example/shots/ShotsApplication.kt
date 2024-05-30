package com.example.shots

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.shots.data.AppDatabase
import com.example.shots.data.FirebaseRepository
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.initialize
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import dagger.hilt.android.HiltAndroidApp
import io.getstream.chat.android.client.ChatClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileInputStream
import javax.inject.Inject


object DataStoreSingleton {
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "screen_counter")


    private lateinit var applicationContext: Context

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "screen_counter")

// NOTE: Replace the below with your own ONESIGNAL_APP_ID
const val ONESIGNAL_APP_ID = "772687fc-521e-4b2c-9ace-60ce2cb533b6"

var client: ChatClient? = null
var fcmRegistrationToken: String? = null

@HiltAndroidApp
class ShotsApplication : Application() {
    // At the top level of your kotlin file:

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(context = this)
//        Firebase.appCheck.installAppCheckProviderFactory(
//            PlayIntegrityAppCheckProviderFactory.getInstance(),
//        )
//        Firebase.appCheck.installAppCheckProviderFactory(
//            DebugAppCheckProviderFactory.getInstance(),
//        )

//        val serviceAccount = FileInputStream("path/to/serviceAccountKey.json")

//        val options: FirebaseOptions = FirebaseOptions.Builder()
//            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//            .setDatabaseUrl("https://shots-de6a5-default-rtdb.firebaseio.com")
//            .build()
//

//        FirebaseApp.initializeApp(options)

        FirebaseApp.initializeApp(this)

        // Other initialization code for your app

        //Setting up the chat client for GetStream (Testing it out)

//        // 1 - Set up the OfflinePlugin for offline storage
//        val offlinePluginFactory = StreamOfflinePluginFactory(appContext = applicationContext)
//        val statePluginFactory =
//            StreamStatePluginFactory(config = StatePluginConfig(), appContext = this)
//
//        // 2 - Set up the client for API calls and with the plugin for offline storage
//        val client = ChatClient.Builder("uun7ywwamhs9", applicationContext)
//            .withPlugins(offlinePluginFactory, statePluginFactory)
//            .logLevel(ChatLogLevel.ALL) // Set to NOTHING in prod
//            .build()

        // Get the current user's ID from Firebase authentication


        // Verbose Logging set to help debug issues, remove before releasing your app.
        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        // OneSignal Initialization
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)


        // requestPermission will show the native Android notification permission prompt.
        // NOTE: It's recommended to use a OneSignal In-App Message to prompt instead.
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }



    }
}

