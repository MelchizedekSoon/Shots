package com.example.shots.ui.theme

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.SnackbarDuration
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
fun SignupMediaScreen(
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

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var isFocused by remember { mutableStateOf(false) }
    var user by remember {
        mutableStateOf(userViewModel.getUser())
    }

    val userData: MutableMap<String, Any> = mutableMapOf()
    val mediaItems: MutableMap<String, Uri> = mutableMapOf()

    var mediaOneWasClicked by remember { mutableStateOf(false) }
    var mediaTwoWasClicked by remember { mutableStateOf(false) }
    var mediaThreeWasClicked by remember { mutableStateOf(false) }
    var mediaFourWasClicked by remember { mutableStateOf(false) }
    var mediaFiveWasClicked by remember { mutableStateOf(false) }
    var mediaSixWasClicked by remember { mutableStateOf(false) }
    var mediaSevenWasClicked by remember { mutableStateOf(false) }
    var mediaEightWasClicked by remember { mutableStateOf(false) }
    var mediaNineWasClicked by remember { mutableStateOf(false) }
    var mediaProfileVideoWasClicked by remember { mutableStateOf(false) }

    var isEditingProfile by rememberSaveable { mutableStateOf(false) }
    var hasPressedBack by rememberSaveable { mutableStateOf(false) }

    var isEditingMediaOne by rememberSaveable { mutableStateOf(false) }
    var isEditingMediaTwo by rememberSaveable { mutableStateOf(false) }
    var isEditingMediaThree by rememberSaveable { mutableStateOf(false) }
    var isEditingMediaFour by rememberSaveable { mutableStateOf(false) }
    var isEditingMediaFive by rememberSaveable { mutableStateOf(false) }
    var isEditingMediaSix by rememberSaveable { mutableStateOf(false) }
    var isEditingMediaSeven by rememberSaveable { mutableStateOf(false) }
    var isEditingMediaEight by rememberSaveable { mutableStateOf(false) }
    var isEditingMediaNine by rememberSaveable { mutableStateOf(false) }
    var isEditingMediaProfileVideo by rememberSaveable { mutableStateOf(false) }


    val showSnackbar: () -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar("Video duration exceeds the maximum allowed duration")
        }
    }
    var hasBeenAdded by rememberSaveable { mutableStateOf(false) }

    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navController.navigate("signupDisplayName")
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
                    user = userViewModel.getUser()
                    dataStore.edit { preferences ->
                        preferences[intPreferencesKey("currentScreen")] = 4
                    }
                }
            }
            IconButton(
                onClick = {
                    if (!isEditingProfile) {
                        navController.navigate("signupDisplayName")
                    }
                },
                modifier = Modifier.padding(it)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            IconButton(
                onClick = {
                    if (user?.mediaOne?.isNotBlank() == false && user?.mediaTwo?.isNotBlank() == false
                        && user?.mediaThree?.isNotBlank() == false && user?.mediaFour?.isNotBlank() == false
                        && user?.mediaFive?.isNotBlank() == false && user?.mediaSix?.isNotBlank() == false
                        && user?.mediaSeven?.isNotBlank() == false && user?.mediaEight?.isNotBlank() == false
                        && user?.mediaNine?.isNotBlank() == false
                    ) {
                        navController.navigate("signupProfileVideo")
                    }
                },
                modifier = Modifier
                    .padding(it)
                    .padding(0.dp, 0.dp, 8.dp, 0.dp)
                    .align(Alignment.TopEnd)
            ) {
                Text(text = "Skip")
            }
            LazyColumn(
                modifier = Modifier
                    .padding(32.dp, 48.dp, 32.dp, 0.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "Add Your Media",
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .padding(0.dp, 48.dp, 0.dp, 0.dp)
                            .fillMaxWidth()
                    )
                    Text(
                        text = "(Slot one is required later to interact with other users.)", fontSize = 12.sp,
                        modifier = Modifier
                            .padding(0.dp, 0.dp, 0.dp, 0.dp)
                    )
//                    Text(
//                        text = "Your media can be changed later.", fontSize = 12.sp,
//                        modifier = Modifier
//                            .padding(0.dp, 0.dp, 0.dp, 0.dp)
//                    )
//                    Text(
//                        text = "Slot one is required before interaction.", fontSize = 12.sp
//                    )
                    /** this was the original height, may change later
                    Spacer(modifier = Modifier.height(16.dp))
                    */

                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {

                    //mediaOne through mediaThree

                    //MediaOne
                    var mediaOneState by rememberSaveable {
                        mutableStateOf(
                            user?.mediaOne ?: ""
                        )
                    }
                    var typeOfMediaOneState by rememberSaveable { mutableStateOf(user?.typeOfMediaOne) }


                    var mediaTwoState by rememberSaveable {
                        mutableStateOf(
                            user?.mediaTwo ?: ""
                        )
                    }
                    var typeOfMediaTwoState
                            by rememberSaveable { mutableStateOf(user?.typeOfMediaTwo) }

                    var mediaThreeState by rememberSaveable {
                        mutableStateOf(
                            user?.mediaThree ?: ""
                        )
                    }
                    var typeOfMediaThreeState
                            by remember { mutableStateOf(user?.typeOfMediaThree) }

                    val pickMediaOne =
                        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                            if (uri != null) {
                                mediaOneState = uri.toString()
                            }
                        }
                    val pickMediaTwo =
                        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                            if (uri != null) {
                                mediaTwoState = uri.toString()
                            }
                        }
                    val pickMediaThree =
                        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                            if (uri != null) {
                                mediaThreeState = uri.toString()
                            }
                        }

                    Row {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(16.dp)
                                .height(180.dp)
                                .clickable {
                                    // Perform additional actions here if needed
                                    if (!isEditingProfile) {
                                        pickMediaOne.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                            )
                                        )
                                        mediaOneWasClicked = true
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
                            if (mediaOneState == "") {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(16.dp)
                                        .height(180.dp),
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
                                    if (isEditingMediaOne) {
                                        Loader()
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Log.d(
                                        TAG,
                                        "Glide Image - mediaOne Image - ${mediaOneState}"
                                    )
                                    GlideImage(
                                        model = mediaOneState,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {
                                            },
                                        contentScale = ContentScale.Crop,
                                        contentDescription = "mediaOne image"
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.cancel_svgrepo_com_2),
                                        "Cancel Icon",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                            .clickable {
                                                if (!isEditingProfile) {
                                                    mediaOneState = ""
                                                    typeOfMediaOneState = TypeOfMedia.UNKNOWN
                                                    mediaOneWasClicked = true
                                                }
                                            }
                                    )
                                    if (isEditingMediaOne) {
                                        Loader()
                                    }
                                }
                            }
                            LaunchedEffect(
                                mediaOneState
                            ) {
                                Log.d(
                                    "EditProfileScreen",
                                    "mediaOneWasClicked = $mediaOneWasClicked"
                                )
                                if (mediaOneWasClicked) {
//                                    user = user?.copy(mediaOne = mediaOneState)
                                    mediaItems["mediaOne"] = mediaOneState.toUri()
                                    mediaOneWasClicked = false
                                    isEditingProfile = true
                                    isEditingMediaOne = true
                                    userViewModel.saveUserDataToFirebase(
                                        user?.id ?: "",
                                        userData, mediaItems, context
                                    ) { wasSaved ->
                                        if (wasSaved) {
                                            isEditingProfile = false
                                            isEditingMediaOne = false
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(16.dp)
                                .height(180.dp)
                                .clickable {
                                    // Perform additional actions here if needed
                                    if (!isEditingProfile) {
                                        pickMediaTwo.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                            )
                                        )
                                        mediaTwoWasClicked = true
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
                            if (mediaTwoState == "") {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(16.dp)
                                        .height(180.dp),
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
                                        if (isEditingMediaTwo) {
                                            Loader()
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Log.d(
                                        TAG,
                                        "Glide Image - mediaTwo Image - ${mediaTwoState}"
                                    )
                                    GlideImage(
                                        model = mediaTwoState,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {

                                            },
                                        contentScale = ContentScale.Crop,
                                        contentDescription = "mediaTwo image"
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.cancel_svgrepo_com_2),
                                        "Cancel Icon",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                            .clickable {
                                                if (!isEditingProfile) {
                                                    mediaTwoState = ""
                                                    typeOfMediaTwoState = TypeOfMedia.UNKNOWN
                                                    mediaTwoWasClicked = true
                                                }
                                            }
                                    )
                                    if (isEditingMediaTwo) {
                                        Loader()
                                    }
                                }
                            }
                            LaunchedEffect(
                                mediaTwoState
                            ) {
                                Log.d(
                                    "EditProfileScreen",
                                    "mediaTwoWasClicked = $mediaTwoWasClicked"
                                )
                                if (mediaTwoWasClicked) {
                                    user = user.copy(mediaTwo = mediaTwoState)
                                    mediaItems["mediaTwo"] = mediaTwoState.toUri()
                                    mediaTwoWasClicked = false
                                    isEditingProfile = true
                                    isEditingMediaTwo = true
                                    userViewModel.saveUserDataToFirebase(
                                        user?.id ?: "",
                                        userData, mediaItems, context
                                    ) { wasSaved ->
                                        if (wasSaved) {
                                            isEditingProfile = false
                                            isEditingMediaTwo = false
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(16.dp)
                                .height(180.dp)
                                .clickable {
                                    // Perform additional actions here if needed
                                    if (!isEditingProfile) {
                                        pickMediaThree.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                            )
                                        )
                                        mediaThreeWasClicked = true
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
                            if (mediaThreeState == "") {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(16.dp)
                                        .height(180.dp),
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
                                        if (isEditingMediaThree) {
                                            Loader()
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Log.d(
                                        TAG,
                                        "Glide Image - mediaThree Image - ${mediaThreeState}"
                                    )
                                    GlideImage(
                                        model = mediaThreeState,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {

                                            },
                                        contentScale = ContentScale.Crop,
                                        contentDescription = "mediaThree image"
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.cancel_svgrepo_com_2),
                                        "Cancel Icon",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                            .clickable {
                                                if (!isEditingProfile) {
                                                    mediaThreeState = ""
                                                    typeOfMediaThreeState = TypeOfMedia.UNKNOWN
                                                    mediaThreeWasClicked = true
                                                }
                                            }
                                    )
                                    if (isEditingMediaThree) {
                                        Loader()
                                    }
                                }
                            }
                            LaunchedEffect(
                                mediaThreeState
                            ) {
                                if (mediaThreeWasClicked) {
                                    user = user.copy(mediaThree = mediaThreeState)
                                    mediaItems["mediaThree"] = mediaThreeState.toUri()
                                    mediaThreeWasClicked = false
                                    isEditingProfile = true
                                    isEditingMediaThree = true
                                    userViewModel.saveUserDataToFirebase(
                                        user?.id ?: "",
                                        userData, mediaItems, context
                                    ) { wasSaved ->
                                        if (wasSaved) {
                                            isEditingProfile = false
                                            isEditingMediaThree = false
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    //mediaFour through mediaSix
                    var mediaFourState by rememberSaveable {
                        mutableStateOf(
                            user?.mediaFour ?: ""
                        )
                    }
                    var typeOfMediaFourState
                            by rememberSaveable { mutableStateOf(user?.typeOfMediaFour) }

                    var mediaFiveState by rememberSaveable {
                        mutableStateOf(
                            user?.mediaFive ?: ""
                        )
                    }
                    var typeOfMediaFiveState
                            by rememberSaveable { mutableStateOf(user?.typeOfMediaFive) }

                    var mediaSixState by rememberSaveable {
                        mutableStateOf(
                            user?.mediaSix ?: ""
                        )
                    }
                    var typeOfMediaSixState
                            by rememberSaveable { mutableStateOf(user?.typeOfMediaSix) }


                    val pickMediaFour =
                        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                            if (uri != null) {
                                mediaFourState = uri.toString()
                            }
                        }
                    val pickMediaFive =
                        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                            if (uri != null) {
                                mediaFiveState = uri.toString()
                            }
                        }
                    val pickMediaSix =
                        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                            if (uri != null) {
                                mediaSixState = uri.toString()
                            }
                        }
                    Row {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(16.dp)
                                .height(180.dp)
                                .clickable {
                                    // Perform additional actions here if needed
                                    if (!isEditingProfile) {
                                        pickMediaFour.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                            )
                                        )
                                        mediaFourWasClicked = true
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
                            if (mediaFourState == "") {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(16.dp)
                                        .height(180.dp),
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
                                        if (isEditingMediaFour) {
                                            Loader()
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Log.d(
                                        TAG,
                                        "Glide Image - mediaFour Image - ${mediaFourState}"
                                    )
                                    GlideImage(
                                        model = mediaFourState,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {

                                            },
                                        contentScale = ContentScale.Crop,
                                        contentDescription = "mediaFour image"
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.cancel_svgrepo_com_2),
                                        "Cancel Icon",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                            .clickable {
                                                if (!isEditingProfile) {
                                                    mediaFourState = ""
                                                    typeOfMediaFourState = TypeOfMedia.UNKNOWN
                                                    mediaFourWasClicked = true
                                                }
                                            }
                                    )
                                    if (isEditingMediaFour) {
                                        Loader()
                                    }
                                }
                            }
                            LaunchedEffect(
                                mediaFourState
                            ) {
                                if (mediaFourWasClicked) {
                                    user = user.copy(mediaFour = mediaFourState)
                                    mediaItems["mediaFour"] = mediaFourState.toUri()
                                    mediaFourWasClicked = false
                                    isEditingProfile = true
                                    isEditingMediaFour = true
                                    userViewModel.saveUserDataToFirebase(
                                        user?.id ?: "",
                                        userData, mediaItems, context
                                    ) { wasSaved ->
                                        if (wasSaved) {
                                            isEditingProfile = false
                                            isEditingMediaFour = false
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(16.dp)
                                .height(180.dp)
                                .clickable {
                                    // Perform additional actions here if needed
                                    if (!isEditingProfile) {
                                        pickMediaFive.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                            )
                                        )
                                        mediaFiveWasClicked = true
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
                            if (mediaFiveState == "") {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(16.dp)
                                        .height(180.dp),
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
                                        if (isEditingMediaFive) {
                                            Loader()
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Log.d(
                                        TAG,
                                        "Glide Image - mediaFive Image - ${mediaFiveState}"
                                    )
                                    GlideImage(
                                        model = mediaFiveState,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {

                                            },
                                        contentScale = ContentScale.Crop,
                                        contentDescription = "mediaFive image"
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.cancel_svgrepo_com_2),
                                        "Cancel Icon",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                            .clickable {
                                                if (!isEditingProfile) {
                                                    mediaFiveState = ""
                                                    typeOfMediaFiveState = TypeOfMedia.UNKNOWN
                                                    mediaFiveWasClicked = true
                                                }
                                            }
                                    )
                                    if (isEditingMediaFive) {
                                        Loader()
                                    }
                                }
                            }
                            LaunchedEffect(
                                mediaFiveState
                            ) {
                                if (mediaFiveWasClicked) {
                                    user = user.copy(mediaFive = mediaFiveState)
                                    mediaItems["mediaFive"] = mediaFiveState.toUri()
                                    mediaFiveWasClicked = false
                                    isEditingProfile = true
                                    isEditingMediaFive = true
                                    userViewModel.saveUserDataToFirebase(
                                        user?.id ?: "",
                                        userData, mediaItems, context
                                    ) { wasSaved ->
                                        if (wasSaved) {
                                            isEditingProfile = false
                                            isEditingMediaFive = false
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(16.dp)
                                .height(180.dp)
                                .clickable {
                                    // Perform additional actions here if needed
                                    if (!isEditingProfile) {
                                        pickMediaSix.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                            )
                                        )
                                        mediaSixWasClicked = true
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
                            if (mediaSixState == "") {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(16.dp)
                                        .height(180.dp),
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
                                        if (isEditingMediaSix) {
                                            Loader()
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Log.d(
                                        TAG,
                                        "Glide Image - mediaSix Image - ${mediaSixState}"
                                    )
                                    GlideImage(
                                        model = mediaSixState,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {

                                            },
                                        contentScale = ContentScale.Crop,
                                        contentDescription = "mediaSix image"
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.cancel_svgrepo_com_2),
                                        "Cancel Icon",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                            .clickable {
                                                if (!isEditingProfile) {
                                                    mediaSixState = ""
                                                    typeOfMediaSixState = TypeOfMedia.UNKNOWN
                                                    mediaSixWasClicked = true
                                                }
                                            }
                                    )
                                    if (isEditingMediaSix) {
                                        Loader()
                                    }
                                }
                            }
                            LaunchedEffect(
                                mediaSixState
                            ) {
                                if (mediaSixWasClicked) {
                                    user = user.copy(mediaSix = mediaSixState)
                                    mediaItems["mediaSix"] = mediaSixState.toUri()
                                    mediaSixWasClicked = false
                                    isEditingProfile = true
                                    isEditingMediaSix = true
                                    userViewModel.saveUserDataToFirebase(
                                        user?.id ?: "",
                                        userData, mediaItems, context
                                    ) { wasSaved ->
                                        if (wasSaved) {
                                            isEditingProfile = false
                                            isEditingMediaSix = false
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    //mediaSeven through mediaNine
                    var mediaSevenState by rememberSaveable {
                        mutableStateOf(
                            user?.mediaSeven ?: ""
                        )
                    }
                    var typeOfMediaSevenState
                            by rememberSaveable {
                                mutableStateOf(
                                    user?.typeOfMediaSeven
                                )
                            }
                    var mediaEightState by rememberSaveable {
                        mutableStateOf(
                            user?.mediaEight ?: ""
                        )
                    }
                    var typeOfMediaEightState
                            by rememberSaveable {
                                mutableStateOf(
                                    user?.typeOfMediaEight
                                )
                            }
                    var mediaNineState by rememberSaveable {
                        mutableStateOf(
                            user?.mediaNine ?: ""
                        )
                    }
                    var typeOfMediaNineState
                            by rememberSaveable {
                                mutableStateOf(
                                    user?.typeOfMediaNine
                                )
                            }

                    val pickMediaSeven =
                        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                            if (uri != null) {
                                mediaSevenState = uri.toString()
                            }
                        }
                    val pickMediaEight =
                        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                            if (uri != null) {
                                mediaEightState = uri.toString()
                            }
                        }
                    val pickMediaNine =
                        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                            if (uri != null) {
                                mediaNineState = uri.toString()
                            }
                        }
                    Row {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(16.dp)
                                .height(180.dp)
                                .clickable {
                                    // Perform additional actions here if needed
                                    if (!isEditingProfile) {
                                        pickMediaSeven.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                            )
                                        )
                                        mediaSevenWasClicked = true
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
                            if (mediaSevenState == "") {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(16.dp)
                                        .height(180.dp),
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
                                        if (isEditingMediaSeven) {
                                            Loader()
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Log.d(
                                        TAG,
                                        "Glide Image - mediaSeven Image - ${mediaSevenState}"
                                    )
                                    GlideImage(
                                        model = mediaSevenState,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {

                                            },
                                        contentScale = ContentScale.Crop,
                                        contentDescription = "mediaSeven image"
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.cancel_svgrepo_com_2),
                                        "Cancel Icon",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                            .clickable {
                                                if (!isEditingProfile) {
                                                    mediaSevenState = ""
                                                    typeOfMediaSevenState = TypeOfMedia.UNKNOWN
                                                    mediaSevenWasClicked = true
                                                }
                                            }
                                    )
                                    if (isEditingMediaSeven) {
                                        Loader()
                                    }
                                }
                            }
                            LaunchedEffect(
                                mediaSevenState
                            ) {
                                if (mediaSevenWasClicked) {
                                    isEditingProfile = true
                                    user = user.copy(mediaSeven = mediaSevenState)
                                    mediaItems["mediaSeven"] = mediaSevenState.toUri()
                                    isEditingProfile = true
                                    isEditingMediaSeven = true
                                    mediaSevenWasClicked = false
                                    userViewModel.saveUserDataToFirebase(
                                        user?.id ?: "",
                                        userData, mediaItems, context
                                    ) { wasSaved ->
                                        if (wasSaved) {
                                            isEditingProfile = false
                                            isEditingMediaSeven = false
                                            mediaSevenWasClicked = false
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(16.dp)
                                .height(180.dp)
                                .clickable {
                                    // Perform additional actions here if needed
                                    if (!isEditingProfile) {
                                        pickMediaEight.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                            )
                                        )
                                        mediaEightWasClicked = true
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
                            if (mediaEightState == "") {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(16.dp)
                                        .height(180.dp),
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
                                        if (isEditingMediaEight) {
                                            Loader()
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Log.d(
                                        TAG,
                                        "Glide Image - mediaEight Image - ${mediaEightState}"
                                    )
                                    GlideImage(
                                        model = mediaEightState,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {

                                            },
                                        contentScale = ContentScale.Crop,
                                        contentDescription = "mediaEight image"
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.cancel_svgrepo_com_2),
                                        "Cancel Icon",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                            .clickable {
                                                if (!isEditingProfile) {
                                                    mediaEightState = ""
                                                    typeOfMediaEightState = TypeOfMedia.UNKNOWN
                                                    mediaEightWasClicked = true
                                                }
                                            }
                                    )
                                    if (isEditingMediaEight) {
                                        Loader()
                                    }
                                }
                            }
                            LaunchedEffect(
                                mediaEightState
                            ) {
                                if (mediaEightWasClicked) {
                                    user = user.copy(mediaEight = mediaEightState)
                                    mediaItems["mediaEight"] = mediaEightState.toUri()
                                    mediaEightWasClicked = false
                                    isEditingMediaEight = true
                                    isEditingProfile = true
                                    userViewModel.saveUserDataToFirebase(
                                        user?.id ?: "",
                                        userData, mediaItems, context
                                    ) { wasSaved ->
                                        if (wasSaved) {
                                            isEditingProfile = false
                                            isEditingMediaEight = false
                                            mediaEightWasClicked = false
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(16.dp)
                                .height(180.dp)
                                .clickable {
                                    // Perform additional actions here if needed
                                    if (!isEditingProfile) {
                                        pickMediaNine.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                            )
                                        )
                                        mediaNineWasClicked = true
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
                            if (mediaNineState == "") {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(16.dp)
                                        .height(180.dp),
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
                                        if (isEditingMediaNine) {
                                            Loader()
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Log.d(
                                        TAG,
                                        "Glide Image - mediaNine Image - ${mediaNineState}"
                                    )
                                    GlideImage(
                                        model = mediaNineState,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {

                                            },
                                        contentScale = ContentScale.Crop,
                                        contentDescription = "mediaNine image"
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.cancel_svgrepo_com_2),
                                        "Cancel Icon",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                            .clickable {
                                                if (!isEditingProfile) {
                                                    mediaNineState = ""
                                                    typeOfMediaNineState = TypeOfMedia.UNKNOWN
                                                    mediaNineWasClicked = true
                                                }
                                            }
                                    )
                                    if (isEditingMediaNine) {
                                        Loader()
                                    }
                                }
                            }
                            LaunchedEffect(
                                mediaNineState
                            ) {
                                if (mediaNineWasClicked) {
                                    isEditingProfile = true
                                    user = user.copy(mediaNine = mediaNineState)
                                    mediaItems["mediaNine"] = mediaNineState.toUri()
                                    mediaNineWasClicked = false
                                    isEditingMediaNine = true
                                    isEditingProfile = true
                                    userViewModel.saveUserDataToFirebase(
                                        user?.id ?: "",
                                        userData, mediaItems, context
                                    ) { wasSaved ->
                                        if (wasSaved) {
                                            isEditingProfile = false
                                            isEditingMediaNine = false
                                        }
                                    }
                                }
                            }
                        }
                    }



                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        shape = RoundedCornerShape(0.dp),
                        onClick = {
                            if (mediaOneState.isNotBlank() ||
                                mediaTwoState.isNotBlank() ||
                                mediaThreeState.isNotBlank() ||
                                mediaFourState.isNotBlank() ||
                                mediaFiveState.isNotBlank() ||
                                mediaSixState.isNotBlank() ||
                                mediaSevenState.isNotBlank() ||
                                mediaEightState.isNotBlank() ||
                                mediaNineState.isNotBlank()
                            ) {
                                Log.d(
                                    TAG, "ProfileVideo Screen - User Values - " +
                                            "${userViewModel.getUser()}"
                                )
//                                scope.launch {
//                                    withContext(Dispatchers.IO) {
//                                        val existingUser = userViewModel.getUser()
//                                        Log.d(
//                                            TAG,
//                                            "In age screen, the returned initial user is ${existingUser}"
//                                        )
//                                        val updatedExistingUser =
//                                            existingUser?.copy(mediaOne = mediaOneState)
//                                                ?.copy(mediaTwo = mediaTwoState)
//                                                ?.copy(mediaThree = mediaThreeState)
//                                                ?.copy(mediaFour = mediaFourState)
//                                                ?.copy(mediaFive = mediaFiveState)
//                                                ?.copy(mediaSix = mediaSixState)
//                                                ?.copy(mediaSeven = mediaSevenState)
//                                                ?.copy(mediaEight = mediaEightState)
//                                                ?.copy(mediaNine = mediaNineState)
//                                                ?.copy(typeOfMediaOne = TypeOfMedia.UNKNOWN)
//                                                ?.copy(typeOfMediaTwo = TypeOfMedia.UNKNOWN)
//                                                ?.copy(typeOfMediaThree = TypeOfMedia.UNKNOWN)
//                                                ?.copy(typeOfMediaFour = TypeOfMedia.UNKNOWN)
//                                                ?.copy(typeOfMediaFive = TypeOfMedia.UNKNOWN)
//                                                ?.copy(typeOfMediaSix = TypeOfMedia.UNKNOWN)
//                                                ?.copy(typeOfMediaSeven = TypeOfMedia.UNKNOWN)
//                                                ?.copy(typeOfMediaEight = TypeOfMedia.UNKNOWN)
//                                                ?.copy(typeOfMediaNine = TypeOfMedia.UNKNOWN)
//
//                                        Log.d(
//                                            TAG,
//                                            "Finally, the user on age screen is - ${updatedExistingUser}"
//                                        )
//                                        if (updatedExistingUser != null) {
//                                            userViewModel.userDao.update(updatedExistingUser)
//                                        }
//
//                                        mediaItems["mediaOne"]
//                                        userViewModel.saveUserDataToFirebase(
//                                            userId ?: "", userData,
//                                            mediaItems, context
//                                        ) {}
//                                    }
//                                }
                                if (!isEditingProfile) {
                                    navController.navigate("signupProfileVideo")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            //padding is 376
                            .height(60.dp)
                            .padding(horizontal = 32.dp),
                    ) {
                        Text("Add your media", fontSize = 16.sp)
                    }
                }
            }

//                Image(
//                    painterResource(R.drawable.shots_3_cropped),
//                    "Shots Logo",
//                    modifier = Modifier
//                        .height(360.dp)
//                        .aspectRatio(1f)
//                )


        }

        if (isEditingProfile) {
            snackbarMessage = "Please wait on this screen while we update your media."
            Box(modifier = Modifier.fillMaxSize()) {
                Button(modifier = Modifier.align(Alignment.BottomCenter), onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            snackbarMessage, duration = SnackbarDuration.Short
                        )
                    }
                }) {
                    Text(text = snackbarMessage)
                }
            }
//            Box(
//                Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.3f))
//                    .pointerInput(Unit) {}
//            ) {
//                Box(modifier = Modifier.align(Alignment.Center)) {
//                    Loader()
//                }
//                Text(
//                    text = "Updating, please give us a few moments!", fontSize = 24.sp,
//                    textAlign = TextAlign.Center,
//                    color = Color.White,
//                    modifier = Modifier
//                        .padding(0.dp, 240.dp, 0.dp, 0.dp)
//                        .align(Alignment.Center)
//                    //padding originally 120
//                )
//            }
        }
    }

}