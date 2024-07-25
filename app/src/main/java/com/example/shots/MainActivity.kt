package com.example.shots

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.shots.data.User
import com.example.shots.ui.theme.AuthViewModel
import com.example.shots.ui.theme.BlockViewModel
import com.example.shots.ui.theme.BlockedUserViewModel
import com.example.shots.ui.theme.BookmarkViewModel
import com.example.shots.ui.theme.EditProfileViewModel
import com.example.shots.ui.theme.FilterViewModel
import com.example.shots.ui.theme.FirebaseViewModel
import com.example.shots.ui.theme.IfSeenReceivedShotViewModel
import com.example.shots.ui.theme.LikeViewModel
import com.example.shots.ui.theme.LocationViewModel
import com.example.shots.ui.theme.LoginViewModel
import com.example.shots.ui.theme.PlayShotViewModel
import com.example.shots.ui.theme.ProfileViewModel
import com.example.shots.ui.theme.ReceivedLikeViewModel
import com.example.shots.ui.theme.ReceivedShotViewModel
import com.example.shots.ui.theme.SearchViewModel
import com.example.shots.ui.theme.SentLikeViewModel
import com.example.shots.ui.theme.SentShotViewModel
import com.example.shots.ui.theme.ShotsNav
import com.example.shots.ui.theme.ShotsTheme
import com.example.shots.ui.theme.SignupViewModel
import com.example.shots.ui.theme.UserViewModel
import com.example.shots.ui.theme.UserWhoBlockedYouViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    //may need later, already have one dataStore, closing this one out

//    private val SHOTS_DATASTORE_NAME = "shots_datastore"
//
//    private val Context.dataStore by preferencesDataStore(
//        name = SHOTS_DATASTORE_NAME
//    )

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            ShotsTheme {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {

                    val firebaseAuth: FirebaseAuth = FirebaseModule.provideFirebaseAuth()
                    val context = LocalContext.current

                    Log.d("MainActivity", "userId = ${firebaseAuth.currentUser?.displayName}")

                    val authViewModel: AuthViewModel by viewModels()
                    val profileViewModel: ProfileViewModel by viewModels()
                    val filterViewModel: FilterViewModel by viewModels()
                    val blockedUserViewModel: BlockedUserViewModel by viewModels()
                    val userWhoBlockedYouViewModel: UserWhoBlockedYouViewModel by viewModels()
                    val blockViewModel: BlockViewModel by viewModels()
                    val editProfileViewModel: EditProfileViewModel by viewModels()
                    val loginViewModel: LoginViewModel by viewModels()
                    val signupViewModel: SignupViewModel by viewModels()
                    val userViewModel: UserViewModel by viewModels()
                    val bookmarkViewModel: BookmarkViewModel by viewModels()
                    val ifSeenReceivedShotViewModel: IfSeenReceivedShotViewModel by viewModels()
                    val receivedLikeViewModel: ReceivedLikeViewModel by viewModels()
                    val sentLikeViewModel: SentLikeViewModel by viewModels()
                    val likeViewModel: LikeViewModel by viewModels()
                    val receivedShotViewModel: ReceivedShotViewModel by viewModels()
                    val sentShotViewModel: SentShotViewModel by viewModels()
                    val locationViewModel: LocationViewModel by viewModels()
                    val playShotViewModel: PlayShotViewModel by viewModels()
                    val searchViewModel: SearchViewModel by viewModels()
                    val firebaseViewModel: FirebaseViewModel by viewModels()


                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

                    var user: User?

                    val isLoggedIn =
                        !FirebaseModule.provideFirebaseAuth().currentUser?.displayName.isNullOrBlank()
                    var hasSignedUp by remember {
                        mutableStateOf(false)
                    }

                    LaunchedEffect(Unit) {
                        dataStore.edit { preferences ->
                            // this needs adjustment and logic figuring
                            preferences[intPreferencesKey("currentScreen")] = 0
                            preferences[booleanPreferencesKey("hasSignedUp")] = false
                            preferences[booleanPreferencesKey("isLoggedIn")] = false
                        }
                    }


//                    LaunchedEffect(Unit) {
//                        dataStore.edit { preferences ->
//                            // this needs adjustment and logic figuring
//                            preferences[intPreferencesKey("currentScreen")] = 1
//                            preferences[booleanPreferencesKey("hasSignedUp")] = true
//                            preferences[booleanPreferencesKey("isLoggedIn")] = true
//                        }
//                    }

                    Log.d("MainActivity", "isLoggedIn: $isLoggedIn")
                    Log.d("MainActivity", "hasSignedUp: $hasSignedUp")

                    LaunchedEffect(Unit) {
                        withContext(Dispatchers.IO) {

                            user = userViewModel.fetchUserFromRoom(
                                firebaseAuth.currentUser?.displayName ?: ""
                            )

                            Log.d("MainActivity", "user = ${user?.id}")

                            if (user?.id?.isNotEmpty() == true) {

                                userViewModel.loadUsers()

                                bookmarkViewModel.loadBookmarks()

                                receivedLikeViewModel.loadReceivedLikes()

                                sentLikeViewModel.loadSentLikes()

                                ifSeenReceivedShotViewModel.loadIfSeenReceivedShots()

                                receivedShotViewModel.loadReceivedShots()

                                sentShotViewModel.loadSentShots()

                                blockedUserViewModel.loadBlockedUsers()

                                userWhoBlockedYouViewModel.loadUsersWhoBlockedYou()

                                editProfileViewModel.loadEditProfileOptions()

                                dataStore.data.collect { preferences ->
                                    hasSignedUp =
                                        preferences[booleanPreferencesKey("hasSignedUp")]
                                            ?: false
                                }

                                NetworkBoundResource().createUser(userViewModel)

                                client = GetStreamClientModule.provideGetStreamClient(
                                    context,
                                    userViewModel,
                                    firebaseViewModel
                                )

                                Log.d("MainActivity", "client on MainActivity = $client")

                            }

                        }
                    }

                    // Create the NotificationChannel.
                    val channelId =
                        stringResource(id = R.string.default_notification_channel_id)
                    val name = getString(R.string.channel_name)
                    val descriptionText = getString(R.string.channel_description)
                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                    val mChannel = NotificationChannel(channelId, name, importance)
                    mChannel.description = descriptionText
                    // Register the channel with the system. You can't change the importance
                    // or other notification behaviors after this.
                    val notificationManager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(mChannel)

//                    SignupAgeScreen(
//                        navController = rememberNavController(),
//                        signupViewModel = signupViewModel,
//                        userViewModel = userViewModel,
//                        dataStore = dataStore
//                    )

//                    SignupScreen(rememberNavController(), userViewModel, signupViewModel, dataStore)

//                    SignupMediaScreen(
//                        navController = rememberNavController(),
//                        signupViewModel = signupViewModel,
//                        userViewModel = userViewModel,
//                        dataStore = dataStore
//                    )

//                    SignupHabitsScreen(
//                        navController = rememberNavController(),
//                        signupViewModel = signupViewModel,
//                        userViewModel = userViewModel,
//                        dataStore = dataStore
//                    )

                    ShotsNav(
                        authViewModel,
                        profileViewModel,
                        filterViewModel,
                        editProfileViewModel,
                        loginViewModel,
                        signupViewModel,
                        userViewModel,
                        locationViewModel,
                        bookmarkViewModel,
                        receivedLikeViewModel,
                        sentLikeViewModel,
                        likeViewModel,
                        ifSeenReceivedShotViewModel,
                        receivedShotViewModel,
                        sentShotViewModel,
                        blockedUserViewModel,
                        userWhoBlockedYouViewModel,
                        blockViewModel,
                        playShotViewModel,
                        searchViewModel,
                        firebaseViewModel,
                        dataStore
                    )

//                    SignupMediaProfileVideoScreen(
//                        navController = rememberNavController(),
//                        signupViewModel = signupViewModel,
//                        userViewModel = userViewModel,
//                        dataStore = dataStore
//                    )

//                    SignupDetailsScreen(
//                        navController = rememberNavController(),
//                        signupViewModel = signupViewModel,
//                        userViewModel = userViewModel,
//                        dataStore = dataStore
//                    )

                }
            }
        }
    }

}


@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}

@Composable
fun DialogExamples() {
    // ...
    var openAlertDialog by remember { mutableStateOf(false) }

    // ...
    when {
        // ...
        openAlertDialog -> {
            AlertDialogExample(
                onDismissRequest = { openAlertDialog = false },
                onConfirmation = {
                    openAlertDialog = false
                    println("Confirmation registered") // Add logic here to handle confirmation.
                },
                dialogTitle = "Alert dialog example",
                dialogText = "This is an example of an alert dialog with buttons.",
                icon = Icons.Default.Info
            )
        }
    }
}

