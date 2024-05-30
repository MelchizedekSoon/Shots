package com.example.shots

//import com.sendbird.android.SendbirdChat
//import com.sendbird.android.channel.OpenChannel
//import com.sendbird.android.exception.SendbirdException
//import com.sendbird.android.handler.InitResultHandler
//import com.sendbird.android.params.InitParams
//import com.sendbird.android.params.OpenChannelCreateParams
//import com.sendbird.android.params.UserMessageCreateParams
//import com.sendbird.android.SendbirdChat
//import com.sendbird.android.channel.OpenChannel
//import com.sendbird.android.exception.SendbirdException
//import com.sendbird.android.handler.InitResultHandler
//import com.sendbird.android.params.InitParams
//import com.sendbird.android.params.OpenChannelCreateParams
//import com.sendbird.android.params.UserMessageCreateParams

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.shots.ui.theme.AuthViewModel
import com.example.shots.ui.theme.BlockViewModel
import com.example.shots.ui.theme.BlockedUserViewModel
import com.example.shots.ui.theme.BookmarkViewModel
import com.example.shots.ui.theme.EditProfileViewModel
import com.example.shots.ui.theme.LikeViewModel
import com.example.shots.ui.theme.LocationViewModel
import com.example.shots.ui.theme.LoginViewModel
import com.example.shots.ui.theme.PlayShotViewModel
import com.example.shots.ui.theme.ReceivedLikeViewModel
import com.example.shots.ui.theme.ReceivedShotViewModel
import com.example.shots.ui.theme.SearchViewModel
import com.example.shots.ui.theme.SentLikeViewModel
import com.example.shots.ui.theme.SentShotViewModel
import com.example.shots.ui.theme.ShotsNav
import com.example.shots.ui.theme.ShotsTheme
import com.example.shots.ui.theme.SignupViewModel
import com.example.shots.ui.theme.UserWhoBlockedYouViewModel
import com.example.shots.ui.theme.UsersViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }


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
                    val blockedUserViewModel: BlockedUserViewModel by viewModels()
                    val userWhoBlockedYouViewModel: UserWhoBlockedYouViewModel by viewModels()
                    val blockViewModel: BlockViewModel by viewModels()
                    val editProfileViewModel: EditProfileViewModel by viewModels()
                    val loginViewModel: LoginViewModel by viewModels()
                    val signupViewModel: SignupViewModel by viewModels()
                    val usersViewModel: UsersViewModel by viewModels()
                    val bookmarkViewModel: BookmarkViewModel by viewModels()
                    val receivedLikeViewModel: ReceivedLikeViewModel by viewModels()
                    val sentLikeViewModel: SentLikeViewModel by viewModels()
                    val likeViewModel: LikeViewModel by viewModels()
                    val receivedShotViewModel: ReceivedShotViewModel by viewModels()
                    val sentShotViewModel: SentShotViewModel by viewModels()
                    val locationViewModel: LocationViewModel by viewModels()
                    val playShotViewModel: PlayShotViewModel by viewModels()
                    val searchViewModel: SearchViewModel by viewModels()

//                    var user by remember { mutableStateOf(usersViewModel.getUser()) }

                    // Register the permissions callback, which handles the user's response to the
// system permissions dialog. Save the return value, an instance of
// ActivityResultLauncher. You can use either a val, as shown in this snippet,
// or a lateinit var in your onAttach() or onCreate() method.

                    val scope = rememberCoroutineScope()

                    var currentLocation: Location
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

                    when {
                        ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            // You can use the API that requires the permission.
//                            fusedLocationClient.getCurrentLocation(
//                                Priority.PRIORITY_HIGH_ACCURACY,
//                                object : CancellationToken() {
//                                    override fun onCanceledRequested(p0: OnTokenCanceledListener) =
//                                        CancellationTokenSource().token
//
//                                    override fun isCancellationRequested() = false
//                                })
//                                .addOnSuccessListener { location: Location? ->
//                                    if (location == null)
//                                        Toast.makeText(
//                                            this,
//                                            "Cannot get location.",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    else {
////                                        val lat = location.latitude
////                                        val lon = location.longitude
//
////                                        locationViewModel.latitude = location.latitude
////                                        locationViewModel.longitude = location.longitude
////                                        locationViewModel.saveLocationToFirebase(
////                                            user?.id ?: "", location.latitude,
////                                            location.longitude, context, usersViewModel
////                                        )
//                                    }
//
//                                }
//                            fusedLocationClient.lastLocation
//                                .addOnSuccessListener { location: Location? ->
//                                    // Got last known location. In some rare situations this can be null.
//                                    if (location != null) {
//                                        currentLocation = location
//                                    }
//                                }

                            val user = usersViewModel.getUser()
                            val userId = user?.id ?: ""
                            fusedLocationClient.getCurrentLocation(
                                Priority.PRIORITY_HIGH_ACCURACY,
                                object : CancellationToken() {
                                    override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                                        CancellationTokenSource().token

                                    override fun isCancellationRequested() = false
                                })
                                .addOnSuccessListener { location: Location? ->
                                    if (location == null)
                                        fusedLocationClient.lastLocation
                                            .addOnSuccessListener { location: Location? ->
                                                // Got last known location. In some rare situations this can be null.
                                                if (location != null) {
                                                    val existingUser = usersViewModel.getUser()

                                                    var updatedExistingUser =
                                                        existingUser?.copy(latitude = location.latitude)
                                                            ?.copy(longitude = location.longitude)

                                                    val userData: MutableMap<String, Any> =
                                                        mutableMapOf()
                                                    val mediaItems: MutableMap<String, Uri> =
                                                        mutableMapOf()

                                                    if (updatedExistingUser != null) {
                                                        userData["latitude"] =
                                                            updatedExistingUser.latitude ?: 0
                                                        userData["longitude"] =
                                                            updatedExistingUser.longitude ?: 0
                                                        usersViewModel.saveUserDataToFirebase(
                                                            userId, userData,
                                                            mediaItems, context
                                                        ) {

                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Cannot get location.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    else {
//                                        val lat = location.latitude
//                                        val lon = location.longitude

                                        locationViewModel.latitude = location.latitude
                                        locationViewModel.longitude = location.longitude
                                        locationViewModel.saveLocationToFirebase(
                                            userId, location.latitude,
                                            location.longitude, context, usersViewModel
                                        )
                                    }
                                }


                        }

                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this, android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) -> {
                            // In an educational UI, explain to the user why your app requires this
                            // permission for a specific feature to behave as expected, and what
                            // features are disabled if it's declined. In this UI, include a
                            // "cancel" or "no thanks" button that lets the user continue
                            // using your app without granting the permission.
                            DialogExamples()
//                            showInContextUI(...)
                        }

                        else -> {
                            // You can directly ask for the permission.
                            // The registered ActivityResultCallback gets the result of this request.
                            requestPermissionLauncher.launch(
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        }
                    }


//                    val appDatabase = RoomModule.provideAppDatabase(LocalContext.current)
//                    val userDao = RoomModule.provideUserDao(appDatabase)
//                    val scope = rememberCoroutineScope()
//                    LaunchedEffect(Unit) {
//                        scope.launch {
//                            val listOfUsers = userDao.getAll()
//                            for (eachUser in listOfUsers) {
//                                userDao.delete(eachUser)
//                            }
//                        }
//                    }


                    val appDatabase = RoomModule.provideAppDatabase(LocalContext.current)
                    val userDao = RoomModule.provideUserDao(appDatabase)
                    val bookmarkDao = RoomModule.provideBookmarkDao(appDatabase)
                    val receivedLikeDao = RoomModule.provideReceiveLikeDao(appDatabase)
                    val sentLikeDao = RoomModule.provideSentLikeDao(appDatabase)


                    val isLoggedIn =
                        !FirebaseModule.provideFirebaseAuth().currentUser?.displayName.isNullOrBlank()
                    var hasSignedUp by remember {
                        mutableStateOf(false)
                    }
                    var currentUser by remember { mutableStateOf<com.example.shots.data.User?>(null) }


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
//                            preferences[intPreferencesKey("currentScreen")] = 13
//                            preferences[booleanPreferencesKey("hasSignedUp")] = true
//                            preferences[booleanPreferencesKey("isLoggedIn")] = true
//                        }
//                    }

                    Log.d("MainActivity", "isLoggedIn: $isLoggedIn")
//                    Log.d("MainActivity", "hasSignedUp: $hasSignedUp")


                    LaunchedEffect(Unit) {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                val users = usersViewModel.getUsersFromRepo()
                                Log.d("MainActivity", "users: $users")
                                userDao.insertAll(users)
                            }
                        }
                    }


                    LaunchedEffect(isLoggedIn) {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                val users = usersViewModel.getUsersFromRepo()
                                Log.d("MainActivity", "users: $users")
//                                usersViewModel.storeUsersInRoom(users)
                                userDao.insertAll(users)

                                var user = usersViewModel.getUser()

                                Log.d("MainActivity", "user: $user")

                                Log.d("MainActivity", "Are we in here?")
//                                usersViewModel.storeUserInRoom(user?.id ?: "")
                                Log.d("MainActivity", "We've stored user")
                                bookmarkViewModel.storeBookmarkInRoom(user?.id ?: "")
                                Log.d("MainActivity", "We've stored bookmark")
                                receivedLikeViewModel.storeReceivedLikeInRoom(user?.id ?: "")
                                Log.d("MainActivity", "We've stored receivedLike")
                                sentLikeViewModel.storeSentLikeInRoom(user?.id ?: "")
                                Log.d("MainActivity", "We've stored sentLike")
                                receivedShotViewModel.storeReceivedShotInRoom(
                                    false,
                                    context,
                                    user?.id ?: ""
                                )
                                Log.d("MainActivity", "We've stored receivedShot")
                                sentShotViewModel.storeSentShotInRoom(user?.id ?: "")
                                Log.d("MainActivity", "We've stored sentShot")
                                blockedUserViewModel.storeBlockedUserInRoom(user?.id ?: "")
                                Log.d("MainActivity", "We've stored blockedUser")
                                userWhoBlockedYouViewModel.storeUserWhoBlockedYouInRoom(
                                    user?.id ?: ""
                                )
                                Log.d("MainActivity", "We've stored userWhoBlockedYou")

//                                if (!user?.id.isNullOrBlank()) {
//                                    Log.d(
//                                        "MainActivity",
//                                        "userId = ${firebaseAuth.currentUser?.email}"
//                                    )
////                                    usersViewModel.userDao.insertAll(usersViewModel.getUsersFromRepo())
//                                    if (!user?.id.isNullOrBlank()) {
//                                        usersViewModel.userDao.insert(user!!)
//                                    } else {
////                                        user = usersViewModel.getInitialUser()
////                                        user = user.copy(id = user?.id ?: "")
////                                        usersViewModel.userDao.insert(user)
//                                    }
//                                    val bookmark = Bookmark(
//                                        user?.id ?: "",
//                                        bookmarkViewModel.getBookmarksFromFirebase(user?.id ?: "")
//                                            .toMutableList()
//                                    )
//                                    bookmarkViewModel.storeBookmarkInRoom(user?.id ?: "")
//                                    Log.d("MainActivity", "bookmarkList: ${bookmark.bookmarks}")
//                                    bookmarkDao.insert(bookmark)
//                                    val sentLike = SentLike(
//                                        user?.id ?: "",
//                                        sentLikeViewModel.getSentLikesFromFirebase(user?.id ?: "")
//                                            .toMutableList()
//                                    )
//                                    sentLikeDao.insert(sentLike)
//                                    val receivedLike = ReceivedLike(
//                                        user?.id ?: "",
//                                        receivedLikeViewModel.getReceivedLikesFromFirebase(
//                                            user?.id ?: ""
//                                        )
//                                            .toMutableList()
//                                    )
//                                    receivedLikeDao.insert(receivedLike)
//                                }

//                                if (firebaseAuth.currentUser?.email == user?.id &&
//                                    !firebaseAuth.currentUser?.email.isNullOrBlank() &&
//                                    !user?.id.isNullOrBlank()
//                                ) {
////                                    try {
////                                        usersViewModel.storeUserInRoom(user?.id ?: "")
////                                        Log.d(
////                                            "MainActivity", "We've stored user with the" +
////                                                    " id = ${user?.id}"
////                                        )
////                                    } catch (e: Exception) {
////                                        Log.d("MainActivity", "Exception: $e")
////                                    }
//                                    try {
//                                        bookmarkViewModel.storeBookmarkInRoom(
//                                            user?.id ?: ""
//                                        )
//                                        Log.d("MainActivity", "We've stored bookmark")
//                                    } catch (e: Exception) {
//                                        Log.d("MainActivity", "Exception: $e")
//                                    }
//                                    try {
//                                        receivedLikeViewModel.storeReceivedLikeInRoom(
//                                            user?.id ?: ""
//                                        )
//                                        Log.d("MainActivity", "We've stored receivedLike")
//                                    } catch (e: Exception) {
//                                        Log.d("MainActivity", "Exception: $e")
//                                    }
//                                    try {
//                                        sentLikeViewModel.storeSentLikeInRoom(user?.id ?: "")
//                                        Log.d("MainActivity", "We've stored sentLike")
//                                    } catch (e: Exception) {
//                                        Log.d("MainActivity", "Exception: $e")
//                                    }
//                                    try {
//                                        receivedShotViewModel.storeReceivedShotInRoom(
//                                            false,
//                                            context,
//                                            user?.id ?: ""
//                                        )
//                                        Log.d("MainActivity", "We've stored receivedShot")
//                                    } catch (e: Exception) {
//                                        Log.d("MainActivity", "Exception: $e")
//                                    }
//                                    try {
//                                        sentShotViewModel.storeSentShotInRoom(user?.id ?: "")
//                                        Log.d("MainActivity", "We've stored sentShot")
//                                    } catch (e: Exception) {
//                                        Log.d("MainActivity", "Exception: $e")
//                                    }
//                                }
                            }
                        }
                    }


                    LaunchedEffect(isLoggedIn) {

                        dataStore.data.collect { preferences ->
                            hasSignedUp = preferences[booleanPreferencesKey("hasSignedUp")] ?: false
                        }

                        val userId = firebaseAuth.currentUser?.displayName ?: ""

                        NetworkBoundResource().createUser(userId)

                        val user = usersViewModel.getUserDataFromRepo(userId)

                        if (isLoggedIn && hasSignedUp && userId.isNotBlank()) {
                            Log.d("MainActivity", "Are we in here?")
//                            usersViewModel.storeUserInRoom(user?.id ?: "")
                            Log.d("MainActivity", "We've stored user")
                            bookmarkViewModel.storeBookmarkInRoom(user?.id ?: "")
                            Log.d("MainActivity", "We've stored bookmark")
                            receivedLikeViewModel.storeReceivedLikeInRoom(user?.id ?: "")
                            Log.d("MainActivity", "We've stored receivedLike")
                            sentLikeViewModel.storeSentLikeInRoom(user?.id ?: "")
                            Log.d("MainActivity", "We've stored sentLike")
                            receivedShotViewModel.storeReceivedShotInRoom(
                                false,
                                context,
                                user?.id ?: ""
                            )
                            Log.d("MainActivity", "We've stored receivedShot")
                            sentShotViewModel.storeSentShotInRoom(user?.id ?: "")
                            Log.d("MainActivity", "We've stored sentShot")
                            blockedUserViewModel.storeBlockedUserInRoom(user?.id ?: "")
                            Log.d("MainActivity", "We've stored blockedUser")
                        }
                    }


                    // Create the NotificationChannel.
                    val channelId = stringResource(id = R.string.default_notification_channel_id)
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

                    LaunchedEffect(Unit) {
                        withContext(Dispatchers.IO) {
                            client = GetStreamClientModule.provideGetStreamClient(
                                context,
                                usersViewModel
                            )
                        }
                    }


//                    SignupMediaScreen(
//                        navController = rememberNavController(),
//                        signupViewModel = signupViewModel,
//                        usersViewModel = usersViewModel,
//                        dataStore = dataStore
//                    )

//                    SignupHabitsScreen(
//                        navController = rememberNavController(),
//                        signupViewModel = signupViewModel,
//                        usersViewModel = usersViewModel,
//                        dataStore = dataStore
//                    )


                    ShotsNav(
                        authViewModel,
                        editProfileViewModel,
                        loginViewModel,
                        signupViewModel,
                        usersViewModel,
                        locationViewModel,
                        bookmarkViewModel,
                        receivedLikeViewModel,
                        sentLikeViewModel,
                        likeViewModel,
                        receivedShotViewModel,
                        sentShotViewModel,
                        blockedUserViewModel,
                        userWhoBlockedYouViewModel,
                        blockViewModel,
                        playShotViewModel,
                        searchViewModel,
                        dataStore
                    )

//                    SignupMediaProfileVideoScreen(
//                        navController = rememberNavController(),
//                        signupViewModel = signupViewModel,
//                        usersViewModel = usersViewModel,
//                        dataStore = dataStore
//                    )

//                    SignupDetailsScreen(
//                        navController = rememberNavController(),
//                        signupViewModel = signupViewModel,
//                        usersViewModel = usersViewModel,
//                        dataStore = dataStore
//                    )
                }
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
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

