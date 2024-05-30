package com.example.shots.ui.theme

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavController
import com.example.shots.FirebaseModule
import com.example.shots.NetworkBoundResource
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun SignupUsernameScreen(
    navController: NavController,
    signupViewModel: SignupViewModel,
    usersViewModel: UsersViewModel,
    dataStore: DataStore<Preferences>
) {
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    val firestore = FirebaseModule.provideFirestore()
    val firebaseStorage = FirebaseModule.provideStorage()
    val firebaseRepository =
        FirebaseModule.provideFirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    Log.d(TAG, "The data ${signupViewModel.getSignUpUser()}")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val user by remember { mutableStateOf(usersViewModel.getUser()) }

    var usernameState by rememberSaveable {
        mutableStateOf(
            ""
        )
    }

    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navController.navigate("signup")
        }
    }

    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    DisposableEffect(backCallback) {
        onBackPressedDispatcher?.addCallback(backCallback)

        onDispose {
            backCallback.isEnabled = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                scope.launch {
                    dataStore.edit { preferences ->
                        preferences[intPreferencesKey("currentScreen")] = 1
                    }
                }
            }
            IconButton(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val userData: MutableMap<String, Any> = mutableMapOf()
                            val mediaItems: MutableMap<String, Uri> = mutableMapOf()

                            val existingUser = user

                            userData["userName"] = existingUser?.userName ?: ""

                            usersViewModel.saveUserDataToFirebase(
                                existingUser?.id ?: "",
                                userData,
                                mediaItems,
                                context
                            ) {}
                        }
                    }
                    navController.navigate("signup")
                },
                modifier = Modifier.padding(it)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(
                modifier = Modifier.padding(0.dp, 48.dp, 0.dp, 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//                Image(
//                    painterResource(R.drawable.shots_3_cropped),
//                    "Shots Logo",
//                    modifier = Modifier
//                        .height(360.dp)
//                        .aspectRatio(1f)
//                )
                Text(
                    text = "Add your username", fontSize = 24.sp,
                    modifier = Modifier
                        .padding(0.dp, 48.dp, 0.dp, 0.dp)
                )
                Text(
                    text = "Your username cannot be changed.", fontSize = 12.sp,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                    value = usernameState, // Use the value from usernameState
                    onValueChange = { textValue: String ->
                        usernameState = textValue
                    },
                    label = {
                        Text(
                            "Username",
                            style = Typography.bodyMedium
                        )
                    }
                )
                val context = LocalContext.current
                var validUsername by remember { mutableStateOf(false) }
                LaunchedEffect(usernameState) {
                    if (usernameState != "") {
                        validUsername = signupViewModel.checkForAvailability(
                            context,
                            usernameState.lowercase(Locale.ROOT)
                        )
                    }
                }
                if (usernameState.isNotEmpty()) {
                    if (!validUsername) {
                        Text(
                            text = "Username is taken, invalid, or banned!",
                            color = Color(0xFFFF0000)
                        )
                    } else {
                        Text(
                            text = "Username is valid!",
                            color = Color(0xFF00FF00)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        scope.launch {
                            if (usernameState.isEmpty()) {
                                validUsername = false
                            }
                            if (validUsername) {
                                signupViewModel.updateSignUpUser { currentUser ->
                                    currentUser.copy(userName = usernameState)
                                }

                                Log.d(
                                    TAG, "Username Screen - User Values - " +
                                            "${signupViewModel.getSignUpUser()}"
                                )
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        val userId = usernameState
                                        val userData: MutableMap<String, Any> = mutableMapOf()
                                        val mediaItems: MutableMap<String, Uri> = mutableMapOf()
                                        userData["id"] = userId
                                        userData["userName"] = usernameState
                                        firebaseAuth.currentUser?.updateProfile(
                                            UserProfileChangeRequest
                                                .Builder()
                                                .setDisplayName(usernameState)
                                                .build()
                                        )
                                        NetworkBoundResource().createUser(userId)
                                        usersViewModel.storeUserInRoom(userId)
                                        Log.d("SignupUsernameScreen", "Username: $userId")
                                        usersViewModel.saveUserDataToFirebase(
                                            userId, userData,
                                            mediaItems, context
                                        ) {
                                            navController.navigate("signupAge")
                                        }
                                    }
                                }
//                                navController.navigate("signupAge")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        //padding is 376
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                ) {
                    Text("Add username", fontSize = 16.sp)
                }
//                LaunchedEffect(usernameState) {
//                    usersViewModel.updateUserField { currentUser ->
//                        currentUser.copy(userName = usernameState)
//                    }
//                }
            }
        }

    }
}