package com.example.shots.ui.theme

import android.net.Uri
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
fun SignupLinkScreen(
    navController: NavController,
    signupViewModel: SignupViewModel,
    userViewModel: UserViewModel,
    dataStore: DataStore<Preferences>
) {
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    val firestore = FirebaseModule.provideFirestore()
    val firebaseStorage = FirebaseModule.provideStorage()
    val firebaseRepository =
        FirebaseModule.provideFirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isGoingBack by rememberSaveable { mutableStateOf(false) }
    var context = LocalContext.current

    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navController.navigate("signupPrompts")
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
                        preferences[intPreferencesKey("currentScreen")] = 8
                    }
                }
            }
            var linkState by remember {
                mutableStateOf(
                    signupViewModel.getSignUpUser().link ?: ""
                )
            }
            IconButton(
                onClick = { navController.navigate("signupPrompts") },
                modifier = Modifier.padding(it)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            IconButton(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
//                            val existingUser = userViewModel.getUser()
//                            val updatedExistingUser =
//                                existingUser?.copy(link = "")
//                            if (updatedExistingUser != null) {
//                                userViewModel.userDao.update(updatedExistingUser)
//                            }
                        }
                    }
                    navController.navigate("signupDetails")
                },
                modifier = Modifier
                    .padding(it)
                    .padding(0.dp, 0.dp, 8.dp, 0.dp)
                    .align(Alignment.TopEnd)
            ) {
                Text(text = "Skip")
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
                    text = "Add your link", fontSize = 24.sp,
                    modifier = Modifier
                        .padding(0.dp, 48.dp, 0.dp, 0.dp)
                )
                Text(
                    text = "Your link can be changed later.", fontSize = 12.sp,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                    value = linkState, // Use the value from usernameState
                    onValueChange = { textValue: String ->
                        linkState = textValue
                    },
                    label = {
                        Text(
                            "Link",
                            style = Typography.bodyMedium
                        )
                    }
                )
                LaunchedEffect(linkState) {
                    signupViewModel.updateSignUpUser { currentUser ->
                        currentUser.copy(link = linkState)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        if (linkState.isNotEmpty()) {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val existingUser = userViewModel.getUser()
                                    val updatedExistingUser =
                                        existingUser?.copy(link = linkState)

                                    val userId = existingUser?.id ?: ""

                                    if (updatedExistingUser != null) {
                                        val userData: MutableMap<String, Any> = mutableMapOf()
                                        val mediaItems: MutableMap<String, Uri> = mutableMapOf()
                                        userData["link"] = updatedExistingUser.link ?: ""
                                        userViewModel.saveUserDataToFirebase(
                                            userId ?: "", userData,
                                            mediaItems, context
                                        ) {

                                        }
                                    }
                                }
                            }
                            navController.navigate("signupDetails")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        //padding is 376
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                ) {
                    Text("Add your link", fontSize = 16.sp)
                }
            }
        }

    }
}