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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.example.shots.FirebaseModule
import com.example.shots.ViewModelModule
import com.example.shots.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
fun SignupAboutMeScreen(
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
    val editProfileViewModel =
        ViewModelModule.provideEditProfileViewModel(firebaseRepository, firebaseAuth)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isFocused by remember { mutableStateOf(false) }
    var isGoingBack by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    var mediaOneState by remember { mutableStateOf("") }
    var mediaTwoState by remember { mutableStateOf("") }
    var mediaThreeState by remember { mutableStateOf("") }
    var mediaFourState by remember { mutableStateOf("") }
    var mediaFiveState by remember { mutableStateOf("") }
    var mediaSixState by remember { mutableStateOf("") }
    var mediaSevenState by remember { mutableStateOf("") }
    var mediaEightState by remember { mutableStateOf("") }
    var mediaNineState by remember { mutableStateOf("") }

    var user by remember { mutableStateOf(usersViewModel.getUser()) }

    val showSnackbar: () -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar("Video duration exceeds the maximum allowed duration")
        }
    }

    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navController.navigate("signupProfileVideo")
        }
    }

    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    DisposableEffect(backCallback) {
        onBackPressedDispatcher?.addCallback(backCallback)

        onDispose {
            backCallback.isEnabled = false
        }
    }


    Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = { ->
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
                        preferences[intPreferencesKey("currentScreen")] = 6
                    }
                }
            }
            IconButton(
                onClick = { navController.navigate("signupProfileVideo") },
                modifier = Modifier.padding(it)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            IconButton(
                onClick = {
                    signupViewModel.updateSignUpUser { currentUser ->
                        currentUser.copy(aboutMe = "")
                    }
                    navController.navigate("signupPrompts")
                },
                modifier = Modifier
                    .padding(it)
                    .padding(0.dp, 0.dp, 8.dp, 0.dp)
                    .align(Alignment.TopEnd)
            ) {
                Text(text = "Skip")
            }
            Column(
                modifier = Modifier
                    .padding(32.dp, 48.dp, 32.dp, 0.dp)
                    .fillMaxWidth(),
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
                    text = "Add Your About Me",
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(0.dp, 48.dp, 0.dp, 0.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                val focusRequester = remember { FocusRequester() }
                val keyboardController = LocalSoftwareKeyboardController.current
                var aboutMeState by remember {
                    mutableStateOf(
                        user?.aboutMe ?: ""
                    )
                }
//                Text(
//                    text = "About Me", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
//                )
//                Spacer(Modifier.height(8.dp))
                Card {
                    // Define textValue variable outside the composable function

//                        var selectedOption by remember {
//                            mutableStateOf(
//                                if (retrievedUser.value?.lookingFor == null) {
//                                    ""
//                                } else {
//                                    retrievedUser.value?.lookingFor ?: ""
//                                }
//                            )
//                        }
                    TextField(value = aboutMeState,
                        onValueChange = { newValue ->
                            if (newValue.length <= 500) {
                                aboutMeState = newValue
                            }
                        },
                        label = { Text(text = "About Me") },
                        singleLine = false,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            focusRequester.freeFocus()
                        }),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(
                                0xFFFFD7B5
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .onFocusChanged { isFocused = it.isFocused }
                            .focusRequester(focusRequester))
                    if (!isFocused) {
                        DisposableEffect(Unit) {
                            focusRequester.freeFocus()
                            onDispose {
                                keyboardController?.hide()
                            }
                        }
                    }
                }


                Spacer(Modifier.height(32.dp))

                if (aboutMeState.isNotBlank()) {
                    Button(
                        shape = RoundedCornerShape(0.dp),
                        onClick = {
                            signupViewModel.updateSignUpUser { currentUser ->
                                currentUser.copy(aboutMe = aboutMeState)
                            }
                            Log.d(TAG, "AboutMeValue - ${signupViewModel.getSignUpUser().aboutMe}")
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val userId = firebaseAuth.currentUser?.displayName ?: ""
                                    val updatedExistingUser =
                                        user?.copy(
                                            aboutMe = aboutMeState
                                        )
                                    if (updatedExistingUser != null) {
                                        val userData: MutableMap<String, Any> = mutableMapOf()
                                        val mediaItems: MutableMap<String, Uri> = mutableMapOf()
                                        userData["aboutMe"] = updatedExistingUser.aboutMe ?: ""
                                        usersViewModel.saveUserDataToFirebase(
                                            userId ?: "", userData,
                                            mediaItems, context
                                        ) {

                                        }
                                    }
                                }
                            }
                            navController.navigate("signupPrompts")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            //padding is 376
                            .height(60.dp)
                            .padding(horizontal = 32.dp),
                    ) {
                        Text("Add your about me", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}