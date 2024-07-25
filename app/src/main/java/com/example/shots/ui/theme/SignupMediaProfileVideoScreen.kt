package com.example.shots.ui.theme

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.shots.FirebaseModule
import com.example.shots.R
import com.example.shots.ViewModelModule
import com.example.shots.data.TypeOfMedia
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
fun SignupMediaProfileVideoScreen(
    navController: NavController,
    signupViewModel: SignupViewModel,
    userViewModel: UserViewModel,
    dataStore: DataStore<Preferences>
) {
    val context = LocalContext.current
    val contentResolver = remember(context) { context.contentResolver }
    var contentType by remember { mutableStateOf("") }

    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    val firestore = FirebaseModule.provideFirestore()
    val firebaseStorage = FirebaseModule.provideStorage()
    val firebaseRepository =
        FirebaseModule.provideFirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    var isEditingProfile by rememberSaveable { mutableStateOf(false) }
    var mediaProfileVideoWasClicked by remember { mutableStateOf(false) }
    var isEditingMediaProfileVideo by rememberSaveable { mutableStateOf(false) }

    val user by remember { mutableStateOf(userViewModel.getUser()) }

    val userData: MutableMap<String, Any> = mutableMapOf()
    val mediaItems: MutableMap<String, Uri> = mutableMapOf()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isFocused by remember { mutableStateOf(false) }
    val showSnackbar: () -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar("Video duration exceeds the maximum allowed duration")
        }
    }

    var hasBeenAdded by rememberSaveable { mutableStateOf(false) }
    var isGoingBack by rememberSaveable { mutableStateOf(false) }

    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navController.navigate("signupMedia")
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
                        preferences[intPreferencesKey("currentScreen")] = 5
                    }
                }
            }
            IconButton(
                onClick = {
                    navController.navigate("signupMedia")
                },
                modifier = Modifier.padding(it)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            IconButton(
                onClick = {
                    if (user?.mediaProfileVideo.isNullOrBlank()) {
                        navController.navigate("signupAboutMe")
                    }
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
                    text = "Add Your Profile Video",
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(0.dp, 48.dp, 0.dp, 0.dp)
                        .fillMaxWidth()
                )
                Text(
                    text = "Introduce and verify yourself with a profile video!", fontSize = 12.sp,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                )
                Text(
                    text = "(Required later to interact with other users.)", fontSize = 12.sp,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))


                var mediaProfileVideoState by rememberSaveable {
                    mutableStateOf(
                        user?.mediaProfileVideo ?: ""
                    )
                }
                var typeOfMediaProfileVideoState by rememberSaveable {
                    mutableStateOf(
                        user?.typeOfMediaProfileVideo
                    )
                }
                var isPlayingProfileVideo by rememberSaveable { mutableStateOf(false) }

                /**For testing purpose right now I'll add a video
                but this will be limited to only recorded videos, not uploaded
                ones for the purpose of verification later on **/

                val makeProfileVideo =
                    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                        if (uri != null) {
                            mediaProfileVideoState = uri.toString()
                        }
                    }

                Card(
                    modifier = Modifier
                        .shadow(16.dp)
                        .height(360.dp)
                        .clickable {
                            // Perform additional actions here if needed
                            if (!isEditingProfile) {
                                makeProfileVideo.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                    )
                                )
                                mediaProfileVideoWasClicked = true
                            }
                        }, colors = CardColors(
                        contentColor = Color.Black,
                        containerColor = Color(
                            0xFFe5e4e2
                        ),
                        disabledContainerColor = Color.Red,
                        disabledContentColor = Color.Red,
                    )
                ) {
                    if (mediaProfileVideoState == "") {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(16.dp)
                                .height(360.dp),
                            colors = CardColors(
                                containerColor = Color.White,
                                disabledContentColor = Color.White,
                                disabledContainerColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    painter = painterResource(id = R.drawable.add_circle_24px),
                                    contentDescription = "Add Media Button",
                                    modifier = Modifier
                                        .height(48.dp)
                                        .width(48.dp)
                                        .align(Alignment.Center),
                                    tint = Color(0xFFFF6F00)
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Log.d(
                                ContentValues.TAG,
                                "Glide Image - mediaProfileVideo Image - ${mediaProfileVideoState}"
                            )
                            GlideImage(
                                model = mediaProfileVideoState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {

                                    },
                                contentScale = ContentScale.Crop,
                                contentDescription = "mediaProfileVideoState image"
                            )
                            Icon(
                                painter = painterResource(R.drawable.cancel_svgrepo_com_2),
                                "Cancel Icon",
                                tint = Color.Red,
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(40.dp)
                                    .align(Alignment.BottomEnd)
                                    .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                    .clickable {
                                        if (isEditingMediaProfileVideo || !isEditingProfile) {
                                            mediaProfileVideoState = ""
                                            typeOfMediaProfileVideoState = TypeOfMedia.UNKNOWN
                                            mediaProfileVideoWasClicked = true
                                        }
                                    }
                            )
                            if (isEditingMediaProfileVideo) {
                                Loader()
                            }
                        }
                    }

                    LaunchedEffect(
                        mediaProfileVideoState
                    ) {
                        if (mediaProfileVideoWasClicked) {
                            isEditingProfile = true
                            mediaItems["mediaProfileVideo"] = mediaProfileVideoState.toUri()
                            mediaProfileVideoWasClicked = false
                            isEditingMediaProfileVideo = true
                            userViewModel.saveUserDataToFirebase(
                                user?.id ?: "",
                                userData, mediaItems, context
                            ) { wasSaved ->
                                if (wasSaved) {
                                    isEditingProfile = false
                                    isEditingMediaProfileVideo = false
                                    mediaProfileVideoWasClicked = false
                                }
                            }
                        }
                    }
                }













                Spacer(Modifier.height(32.dp))

                Button(
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        if (mediaProfileVideoState.isNotBlank()) {
                            navController.navigate("signupAboutMe")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        //padding is 376
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                ) {
                    Text("Add your profile video", fontSize = 16.sp)
                }
            }
        }
    }
}