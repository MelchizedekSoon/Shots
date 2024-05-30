package com.example.shots.ui.theme

import android.net.Uri
import android.util.Log
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.addCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.example.shots.GetStreamClientModule
import com.example.shots.NetworkBoundResource
import com.example.shots.R
import com.example.shots.RoomModule
import com.example.shots.ViewModelModule
import com.example.shots.client
import com.example.shots.data.Bookmark
import com.example.shots.data.ReceivedLike
import com.example.shots.data.SentLike
import com.example.shots.data.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun LoginScreen(
    navController: NavController,
    signupViewModel: SignupViewModel,
    usersViewModel: UsersViewModel,
    bookmarkViewModel: BookmarkViewModel,
    receivedLikeViewModel: ReceivedLikeViewModel,
    sentLikeViewModel: SentLikeViewModel,
    receivedShotViewModel: ReceivedShotViewModel,
    sentShotViewModel: SentShotViewModel,
    dataStore: DataStore<Preferences>,
) {
    val loginViewModel = ViewModelModule.provideLoginViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    var destination by rememberSaveable {
        mutableStateOf(0)
    }
    val appDatabase = RoomModule.provideAppDatabase(LocalContext.current)
    val userDao = RoomModule.provideUserDao(appDatabase)
    val bookmarkDao = RoomModule.provideBookmarkDao(appDatabase)
    val receivedLikeDao = RoomModule.provideReceiveLikeDao(appDatabase)
    val sentLikeDao = RoomModule.provideSentLikeDao(appDatabase)

    val (snackbarVisibleState, setSnackBarState) = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var user by remember { mutableStateOf<User?>(null) }

    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    DisposableEffect(Unit) {
        val backCallback = onBackPressedDispatcher?.addCallback(enabled = true) {
            navController.popBackStack("login", inclusive = false)
        }

        onDispose {
            backCallback?.remove()
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize(),
        snackbarHost = { ->
            SnackbarHost(
                hostState = snackbarHostState
            )
        }) { it ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            LaunchedEffect(Unit) {
//                val screenFlow: Flow<Int> = dataStore.data
//                    .map { preferences ->
//                        preferences[intPreferencesKey("currentScreen")] ?: 0
//                    }
//                    .filterNotNull()
//
//
//                screenFlow.collect { screen ->
//                    // Perform actions with the most recent userScore value
//                    Log.d(ContentValues.TAG, "Current screen: $screen")
//                    destination = screen
//                }

                dataStore.data.collect { preferences ->
                    destination = preferences[intPreferencesKey("currentScreen")] ?: 0
                }

            }
            val emailState = remember { mutableStateOf(loginViewModel.emailText.value) }
            val passwordState = remember { mutableStateOf(loginViewModel.passwordText.value) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painterResource(R.drawable.shots_3_cropped),
                    "Shots Logo",
                    modifier = Modifier
                        .height(360.dp)
                        .aspectRatio(1f)
                )
                Text(
                    text = "Log in", fontSize = 24.sp,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 0.dp)

                    //padding originally 120
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    modifier = Modifier
                        //padding originally 200
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                    value = emailState.value,
                    onValueChange = { emailState.value = it },
                    label = {
                        Text(
                            "Email",
                            style = Typography.bodyMedium
                        )
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        //padding is 288.dp
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                    value = passwordState.value,
                    onValueChange = { passwordState.value = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        scope.launch {
                            var firebaseUser =
                                loginViewModel.signIn(
                                    emailState.value.text.trim(),
                                    passwordState.value.text.trim()
                                )
                            val snackbar = snackbarHostState
                            if (firebaseUser == null) {
                                snackbar.showSnackbar(
                                    message = "No account was found. Please try another email or password.",
                                    actionLabel = "OK"
                                )
                            } else {
                                firebaseUser.verifyBeforeUpdateEmail(emailState.value.text)
                                firebaseUser.updatePassword(passwordState.value.text)
//                                if (AppPreferences.isFirstTimeLaunch(context)) {
//                                    // Show the verification dialog
//                                    showVerificationDialog(context)
//
//                                    // Update the preferences to indicate that the dialog has been shown
//                                    AppPreferences.setVerificationDialogShown(context)
//                                }
                                usersViewModel.storeUsersInRoom(usersViewModel.getUsersFromRepo())
                                val userId = firebaseUser.displayName
                                user = usersViewModel.getUserDataFromRepo(userId ?: "")
                                Log.d("LoginScreen", "user = $user")
                                Log.d("LoginScreen", "userId = $userId")
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        dataStore.edit { preferences ->
                                            preferences[booleanPreferencesKey("isLoggedIn")] = true
                                        }

                                        client = GetStreamClientModule.provideGetStreamClient(
                                            context,
                                            usersViewModel
                                        )

                                        NetworkBoundResource().createUser(userId)

                                        if (!user?.id.isNullOrBlank()) {

//                                    usersViewModel.userDao.insertAll(usersViewModel.getUsersFromRepo())
                                            val users = usersViewModel.getUsersFromRepo()
                                            Log.d("MainActivity", "users: $users")
                                            userDao.insertAll(users)

                                            val userData: MutableMap<String, Any> = mutableMapOf()
                                            val mediaItems: MutableMap<String, Uri> = mutableMapOf()

                                            userData["latitude"] = user?.latitude ?: 0.0
                                            userData["longitude"] = user?.longitude ?: 0.0

                                            usersViewModel.saveUserDataToFirebase(
                                                user?.id ?: "",
                                                userData,
                                                mediaItems,
                                                context
                                            ) {}


                                            val bookmark = Bookmark(
                                                user?.id ?: "",
                                                bookmarkViewModel.getBookmarksFromFirebase(
                                                    user?.id ?: ""
                                                )
                                                    .toMutableList()
                                            )
                                            bookmarkDao.insert(bookmark)
                                            val sentLike = SentLike(
                                                user?.id ?: "",
                                                sentLikeViewModel.getSentLikesFromFirebase(
                                                    user?.id ?: ""
                                                )
                                                    .toMutableList()
                                            )
                                            sentLikeDao.insert(sentLike)
                                            val receivedLike = ReceivedLike(
                                                user?.id ?: "",
                                                receivedLikeViewModel.getReceivedLikesFromFirebase(
                                                    user?.id ?: ""
                                                )
                                                    .toMutableList()
                                            )
                                            receivedLikeDao.insert(receivedLike)
                                        }

                                        try {
                                            receivedLikeViewModel.storeReceivedLikeInRoom(
                                                user?.id ?: ""
                                            )
                                            Log.d("MainActivity", "We've stored receivedLike")
                                        } catch (e: Exception) {
                                            Log.d("MainActivity", "Exception: $e")
                                        }
                                        try {
                                            sentLikeViewModel.storeSentLikeInRoom(user?.id ?: "")
                                            Log.d("MainActivity", "We've stored sentLike")
                                        } catch (e: Exception) {
                                            Log.d("MainActivity", "Exception: $e")
                                        }
                                        try {
                                            receivedShotViewModel.storeReceivedShotInRoom(
                                                false,
                                                context,
                                                user?.id ?: ""
                                            )
                                            Log.d("MainActivity", "We've stored receivedShot")
                                        } catch (e: Exception) {
                                            Log.d("MainActivity", "Exception: $e")
                                        }
                                        try {
                                            sentShotViewModel.storeSentShotInRoom(user?.id ?: "")
                                            Log.d("MainActivity", "We've stored sentShot")
                                        } catch (e: Exception) {
                                            Log.d("MainActivity", "Exception: $e")
                                        }
                                    }
                                }

                                Log.d("LoginScreen", "userId = ${userId}")
                                Log.d("LoginScreen", "destination = $destination")

                                navController.navigate(
                                    when (destination) {
                                        0 -> "users"
                                        1 -> "signupUsername"
                                        2 -> "signupAge"
                                        3 -> "signupDisplayName"
                                        4 -> "signupMedia"
                                        5 -> "signupProfileVideo"
                                        6 -> "signupAboutMe"
                                        7 -> "signupPrompts"
                                        8 -> "signupLink"
                                        9 -> "signupDetails"
                                        10 -> "signupEssentials"
                                        11 -> "signupFilter"
                                        12 -> "signupHabits"
                                        13 -> "users"
                                        else -> "login"
                                    }
                                )

                                snackbar.showSnackbar(
                                    message = "Your account has been found!",
                                    actionLabel = "OK"
                                )

                            }

                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        //padding is 376
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    Text("Log in with Email", fontSize = 16.sp)
                }
//            Text(
//                text = "No account? Sign up",
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .padding(0.dp, 0.dp, 0.dp, 16.dp)
//                    .clickable { navController.navigate("signup") }, // Navigate to "signupScreen" destination
//                fontSize = 24.sp,
//                style = Typography.bodySmall
//            )
                Row(modifier = Modifier.padding(0.dp, 32.dp, 0.dp, 0.dp)) {
                    Text(
                        text = "No account? ",
//                        modifier = Modifier
//                            //padding 16 from bottom
//                            .padding(0.dp, 0.dp, 0.dp, 16.dp),
                        fontSize = 16.sp,
                        style = Typography.bodySmall
                    )
                    Text(
                        text = "Sign up",
                        modifier = Modifier
                            //padding 16 from bottom
                            .clickable { navController.navigate("signup") }, // Navigate to "signupScreen" destination
                        fontSize = 16.sp,
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(it)
//        ) {
//            val emailState = remember { mutableStateOf(loginViewModel.emailText.value) }
//            val passwordState = remember { mutableStateOf(loginViewModel.passwordText.value) }
//            Image(painterResource(R.drawable.shots_4),
//                "Shots Logo",
//                modifier = Modifier.align(Alignment.TopCenter)
//                    .height(600.dp).width(600.dp)
//                    .padding(0.dp, 0.dp, 0.dp, 240.dp))
//            Text(
//                text = "Log in", fontSize = 24.sp,
//                modifier = Modifier
//                    .align(Alignment.TopCenter)
//                    //padding originally 120
//                    .padding(0.dp, 120.dp, 0.dp, 0.dp)
//            )
//            OutlinedTextField(
//                modifier = Modifier
//                    .align(Alignment.TopCenter)
//                    //padding originally 200
//                    .padding(32.dp, 200.dp, 32.dp, 0.dp)
//                    .fillMaxWidth()
//                    .height(60.dp),
//                value = emailState.value,
//                onValueChange = { emailState.value = it },
//                label = {
//                    Text(
//                        "Email",
//                        style = Typography.bodyMedium
//                    )
//                }
//            )
//            OutlinedTextField(
//                modifier = Modifier
//                    .align(Alignment.TopCenter)
//                    .fillMaxWidth()
//                    //padding is 288.dp
//                    .padding(32.dp, 288.dp, 32.dp, 0.dp)
//                    .height(60.dp),
//                value = passwordState.value,
//                onValueChange = { passwordState.value = it },
//                label = { Text("Password") },
//                visualTransformation = PasswordVisualTransformation()
//            )
//            Button(
//                shape = RoundedCornerShape(0.dp),
//                onClick = {
//                    scope.launch {
//                        val user =
//                            loginViewModel.signIn(emailState.value.text, passwordState.value.text)
//                        val snackbar = snackbarHostState
//                        if (user == null) {
//                            snackbar.showSnackbar(
//                                message = "No account was found. Please try another email or password.",
//                                actionLabel = "OK"
//                            )
//                        } else {
//                            if (AppPreferences.isFirstTimeLaunch(context)) {
//                                // Show the verification dialog
//                                showVerificationDialog(context)
//
//                                // Update the preferences to indicate that the dialog has been shown
//                                AppPreferences.setVerificationDialogShown(context)
//                            }
//                            navController.navigate("users")
//                            snackbar.showSnackbar(
//                                message = "Your account has been found!",
//                                actionLabel = "OK"
//                            )
//                        }
//                    }
//                },
//                modifier = Modifier
//                    .align(Alignment.TopCenter)
//                    .fillMaxWidth()
//                    //padding is 376
//                    .padding(32.dp, 376.dp, 32.dp, 0.dp)
//                    .height(60.dp),
//                elevation = ButtonDefaults.buttonElevation(
//                    defaultElevation = 0.dp,
//                    pressedElevation = 0.dp,
//                    disabledElevation = 0.dp
//                )
//            ) {
//                Text("Log in with Email", fontSize = 16.sp)
//            }
////            Text(
////                text = "No account? Sign up",
////                modifier = Modifier
////                    .align(Alignment.BottomCenter)
////                    .padding(0.dp, 0.dp, 0.dp, 16.dp)
////                    .clickable { navController.navigate("signup") }, // Navigate to "signupScreen" destination
////                fontSize = 24.sp,
////                style = Typography.bodySmall
////            )
//            Row(modifier = Modifier.align(Alignment.BottomCenter)) {
//                Text(
//                    text = "No account? ",
//                    modifier = Modifier
//                        //padding 16 from bottom
//                        .padding(0.dp, 0.dp, 0.dp, 16.dp),
//                    fontSize = 16.sp,
//                    style = Typography.bodySmall
//                )
//                Text(
//                    text = "Sign up",
//                    modifier = Modifier
//                        //padding 16 from bottom
//                        .padding(0.dp, 0.dp, 0.dp, 16.dp)
//                        .clickable { navController.navigate("signup") }, // Navigate to "signupScreen" destination
//                    fontSize = 16.sp,
//                    style = Typography.bodySmall,
//                    color = MaterialTheme.colorScheme.primary
//                )
//            }
//        }

}

private class DummyOnBackPressedDispatcherOwner(
    override val lifecycle: Lifecycle,
) : OnBackPressedDispatcherOwner {
    override val onBackPressedDispatcher: OnBackPressedDispatcher
        get() = OnBackPressedDispatcher()
}

@Composable
fun DisableBackHandler(
    isDisabled: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        values = LocalOnBackPressedDispatcherOwner.current?.let { parentDispatcherOwner ->
            arrayOf(
                LocalOnBackPressedDispatcherOwner provides if (isDisabled) {
                    DummyOnBackPressedDispatcherOwner(parentDispatcherOwner.lifecycle)
                } else {
                    parentDispatcherOwner
                },
            )
        } ?: arrayOf(),
        content = content,
    )
}


fun ShowSnackbar(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    snackbarVisibleState: Boolean,
    user: FirebaseUser?
) {
    scope.launch {
        if (user == null) {
            println("Your account was not found")
            snackbarHostState.showSnackbar(
                message = "Your account was not found",
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Short
            )
        } else {
            println("Your account was found")
            snackbarHostState.showSnackbar(
                message = "Your account was found",
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Short
            )
        }
    }
}

//@Preview
//@Composable
//fun LoginScreenPreview() {
//    LoginScreen(rememberNavController())
//}