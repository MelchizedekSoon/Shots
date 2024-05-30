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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SignupDisplayNameScreen(
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var displayNameState by rememberSaveable {
        mutableStateOf(
            ""
        )
    }

    var user by remember { mutableStateOf(usersViewModel.getUser()) }

    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navController.navigate("signupAge")
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
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            LaunchedEffect(Unit) {
                scope.launch {
                    dataStore.edit { preferences ->
                        preferences[intPreferencesKey("currentScreen")] = 3
                    }
                }
            }
            IconButton(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val existingUser = user
                            Log.d(
                                TAG,
                                "In age screen, the returned initial user is ${existingUser}"
                            )
                            val updatedExistingUser = existingUser?.copy(
                                displayName =
                                ""
                            )
                            Log.d(
                                TAG,
                                "Finally, the user on age screen is - ${updatedExistingUser}"
                            )
                            if (updatedExistingUser != null) {
                                usersViewModel.userDao.update(updatedExistingUser)
                            }
                        }
                    }
                    navController.navigate("signupAge")
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
                    text = "Add your display name", fontSize = 24.sp,
                    modifier = Modifier
                        .padding(0.dp, 48.dp, 0.dp, 0.dp)
                )
                Text(
                    text = "Your display name can be changed later.", fontSize = 12.sp,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                    value = displayNameState, // Use the value from usernameState
                    onValueChange = { textValue: String ->
                        displayNameState = textValue
                    },
                    label = {
                        Text(
                            "Display name",
                            style = Typography.bodyMedium
                        )
                    }
                )
                val context = LocalContext.current
                var validDisplayName by remember { mutableStateOf(false) }
                LaunchedEffect(displayNameState) {
                    if (displayNameState != "") {
                        validDisplayName = displayNameState.isNotEmpty()
                        Log.d(
                            TAG, "validUsername ended up being" +
                                    " $validDisplayName"
                        )
                    }
                }
                if (displayNameState.isNotEmpty()) {
                    if (!validDisplayName) {
                        Text(
                            text = "This display name is not allowed.",
                            color = Color(0xFFFF0000)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        scope.launch {
                            if (displayNameState.isEmpty()) {
                                validDisplayName = false
                            }
                            if (validDisplayName) {
                                signupViewModel.updateSignUpUser { currentUser ->
                                    currentUser.copy(displayName = displayNameState)
                                }

                                Log.d(
                                    TAG, "DisplayName Screen - User Values - " +
                                            "${signupViewModel.getSignUpUser()}"
                                )
                                signupViewModel.updateScreenStatus(Screen.DISPLAY_NAME)
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        val userId = user?.id ?: ""
                                        val userData: MutableMap<String, Any> = mutableMapOf()
                                        val mediaItems: MutableMap<String, Uri> = mutableMapOf()
                                        userData["displayName"] = displayNameState
                                        usersViewModel.saveUserDataToFirebase(
                                            userId ?: "", userData,
                                            mediaItems, context
                                        ) {}
                                    }
                                }
                                navController.navigate("signupMedia")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        //padding is 376
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                ) {
                    Text("Add display name", fontSize = 16.sp)
                }
            }
        }

    }
}