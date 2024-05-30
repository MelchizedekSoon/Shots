package com.example.shots.ui.theme

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.shots.FirebaseModule
import com.example.shots.PromptsUtils
import com.example.shots.R
import com.example.shots.RoomModule
import com.example.shots.ViewModelModule
import com.example.shots.data.Drinking
import com.example.shots.data.Education
import com.example.shots.data.Exercise
import com.example.shots.data.Gender
import com.example.shots.data.Kids
import com.example.shots.data.LookingFor
import com.example.shots.data.Marijuana
import com.example.shots.data.Pets
import com.example.shots.data.Religion
import com.example.shots.data.Smoking
import com.example.shots.data.TypeOfMedia
import com.example.shots.data.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.Calendar

object MediaIdentifiers {
    const val MEDIA_ONE = "mediaOne"
    const val MEDIA_TWO = "mediaTwo"
    const val MEDIA_THREE = "mediaThree"
    const val MEDIA_FOUR = "mediaFour"
    const val MEDIA_FIVE = "mediaFive"
    const val MEDIA_SIX = "mediaSix"
    const val MEDIA_SEVEN = "mediaSeven"
    const val MEDIA_EIGHT = "mediaEight"
    const val MEDIA_NINE = "mediaNine"
}


@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    InternalCoroutinesApi::class,
    ExperimentalGlideComposeApi::class
)
@Composable
fun EditProfileScreen(navController: NavController, usersViewModel: UsersViewModel) {
    val context = LocalContext.current
    val contentResolver = remember(context) { context.contentResolver }
    var contentType by remember { mutableStateOf("") }


    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    val firestore = FirebaseModule.provideFirestore()
    val firebaseStorage = FirebaseModule.provideStorage()
    val firebaseRepository =
        FirebaseModule.provideFirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    val editProfileViewModel: EditProfileViewModel =
        ViewModelModule.provideEditProfileViewModel(firebaseRepository, firebaseAuth)
    val appDatabase = RoomModule.provideAppDatabase(context)
    val userDao = appDatabase.userDao()
    var user by remember { mutableStateOf<User?>(usersViewModel.getUser()) }
    var updatedUser by remember { mutableStateOf(usersViewModel.getUser()) }
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

    val scope = rememberCoroutineScope()

    val userId = firebaseAuth.currentUser?.displayName ?: ""

    var mediaProfileVideo by rememberSaveable {
        mutableStateOf(
            user?.mediaProfileVideo ?: ""
        )
    }


    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf("") }

    val userData: MutableMap<String, Any> = mutableMapOf()
    val mediaItems: MutableMap<String, Uri> = mutableMapOf()


    // Testing to see if the typeOfMediaOne will persist when made a mutableOf and
    // returned via boolean
    var typeOfMediaOneIsVideo by remember { mutableStateOf(false) }


    var focusRequester = remember { FocusRequester() }
    var keyboardController = LocalSoftwareKeyboardController.current

    var isFocused by remember { mutableStateOf(false) }

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

    val displayNameCallback: () -> Unit = {
        scope.launch {
            snackbarMessage = "Display name must be added before save."
            snackbarHostState.showSnackbar(
                snackbarMessage, duration = SnackbarDuration.Short
            )
        }
    }


    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            hasPressedBack = true
        }
    }

    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    DisposableEffect(backCallback) {
        onBackPressedDispatcher?.addCallback(backCallback)

        onDispose {
            backCallback.isEnabled = false
        }
    }

    val showSnackbar: () -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar("Video duration exceeds the maximum allowed duration")
        }
    }

    val wasSaved: (Boolean) -> Unit = { itWasSaved ->
        if (itWasSaved) {
            isEditingProfile = false
            isEditingMediaTwo = false
        }
    }


    Scaffold(Modifier.background(Color.White), snackbarHost = {
        SnackbarHost(hostState = snackbarHostState) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp, 0.dp, 0.dp, 32.dp)
            ) {
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


        }
    }, topBar = {
        TopAppBar(title = { Text(text = "Edit Profile") },
            actions = {
//                Text(text = "Save", fontSize = 16.sp,
//                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp))
//                IconButton(onClick = {
//                    if (!isEditingProfile && user?.displayName?.isBlank() != true ||
//                        userData["displayName"].toString().isNotBlank()
//                    ) {
//
////                        isEditingProfile = true
//                        val currentUser = firebaseAuth.currentUser
//                        val userId = firebaseAuth.currentUser?.displayName ?: ""
//                        Log.d("EditProfileScreen", "userId = ${user?.id ?: ""}")
//                        Log.d("EditProfileScreen", "currentUser = ${currentUser}")
//                        val wasSaved = usersViewModel.saveUserDataToFirebase(
//                            user?.id ?: "",
//                            userData, mediaItems, context
//                        ) { wasSaved ->
//                            if (wasSaved) {
////                                scope.launch {
////                                    snackbarMessage = "Any changes should be made soon!"
////                                    snackbarHostState.showSnackbar(
////                                        snackbarMessage, duration = SnackbarDuration.Short
////                                    )
////                                }
//                                navController.popBackStack()
//                            }
//                        }
////                        user?.let {
////                            usersViewModel.updateUser(
////                                it, usersViewModel, navController,
////                                context) {}
////                        }
//                        scope.launch {
//                            snackbarMessage = "Profile should update soon!"
//                            snackbarHostState.showSnackbar(
//                                snackbarMessage, duration = SnackbarDuration.Short
//                            )
//                        }
//                    } else {
//                        scope.launch {
//                            snackbarMessage = "Display name must be added before save."
//                            snackbarHostState.showSnackbar(
//                                snackbarMessage, duration = SnackbarDuration.Short
//                            )
//                        }
//                    }
//                }) {
//                    Text(
//                        text = "Save", fontSize = 16.sp,
//                        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp)
//                    )
//                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    hasPressedBack = true
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back Icon")
                }
            })
    }) {
        Modifier.padding(it)
        Box(
            modifier = Modifier
                .padding(it)
                .padding(16.dp, 16.dp, 16.dp, 0.dp)

        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    val idState by rememberSaveable {
                        mutableStateOf(
                            user?.id ?: ""
                        )
                    }
                    LaunchedEffect(idState) {
                        if (idState.isNotBlank()) {
                            usersViewModel.updateUserField { currentUser ->
                                currentUser.copy(id = idState)
                            }
                            userData["id"] = idState
                        }
                    }
                    val userNameState by rememberSaveable {
                        mutableStateOf(
                            user?.userName ?: ""
                        )
                    }
                    LaunchedEffect(userNameState) {
                        if (userNameState.isNotBlank()) {
                            usersViewModel.updateUserField { currentUser ->
                                currentUser.copy(userName = userNameState)
                            }
                            userData["userName"] = userNameState
                        }
                    }
                    val birthdayState: Long by rememberSaveable {
                        mutableLongStateOf(
                            user?.birthday
                                ?: Calendar.getInstance().timeInMillis
                        )
                    }
                    LaunchedEffect(Unit) {
                        // years = milliseconds ÷ 31,556,952,000
                        Log.d(TAG, "Birthday is $birthdayState")
                        usersViewModel.updateUserField { currentUser ->
                            currentUser.copy(birthday = birthdayState)
                        }
                        userData["birthday"] = birthdayState
                    }

                }
                item {
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
                    Text(
                        text = "Media (Slot One - Required)", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(8.dp))
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
                                    user = user?.copy(mediaOne = mediaOneState)
                                    mediaItems["mediaOne"] = mediaOneState.toUri()
                                    mediaOneWasClicked = false
                                    isEditingProfile = true
                                    isEditingMediaOne = true
                                    usersViewModel.saveUserDataToFirebase(
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
                                    user = user?.copy(mediaTwo = mediaTwoState)
                                    mediaItems["mediaTwo"] = mediaTwoState.toUri()
                                    mediaTwoWasClicked = false
                                    isEditingProfile = true
                                    isEditingMediaTwo = true
                                    usersViewModel.saveUserDataToFirebase(
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
                                    user = user?.copy(mediaThree = mediaThreeState)
                                    mediaItems["mediaThree"] = mediaThreeState.toUri()
                                    mediaThreeWasClicked = false
                                    isEditingProfile = true
                                    isEditingMediaThree = true
                                    usersViewModel.saveUserDataToFirebase(
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
                }

                item {
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
                                    user = user?.copy(mediaFour = mediaFourState)
                                    mediaItems["mediaFour"] = mediaFourState.toUri()
                                    mediaFourWasClicked = false
                                    isEditingProfile = true
                                    isEditingMediaFour = true
                                    usersViewModel.saveUserDataToFirebase(
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
                                    user = user?.copy(mediaFive = mediaFiveState)
                                    mediaItems["mediaFive"] = mediaFiveState.toUri()
                                    mediaFiveWasClicked = false
                                    isEditingProfile = true
                                    isEditingMediaFive = true
                                    usersViewModel.saveUserDataToFirebase(
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
                                    user = user?.copy(mediaSix = mediaSixState)
                                    mediaItems["mediaSix"] = mediaSixState.toUri()
                                    mediaSixWasClicked = false
                                    isEditingProfile = true
                                    isEditingMediaSix = true
                                    usersViewModel.saveUserDataToFirebase(
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
                }

                item {
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
                                    user = user?.copy(mediaSeven = mediaSevenState)
                                    mediaItems["mediaSeven"] = mediaSevenState.toUri()
                                    isEditingProfile = true
                                    isEditingMediaSeven = true
                                    mediaSevenWasClicked = false
                                    usersViewModel.saveUserDataToFirebase(
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
                                    user = user?.copy(mediaEight = mediaEightState)
                                    mediaItems["mediaEight"] = mediaEightState.toUri()
                                    mediaEightWasClicked = false
                                    isEditingMediaEight = true
                                    isEditingProfile = true
                                    usersViewModel.saveUserDataToFirebase(
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
                                    user = user?.copy(mediaNine = mediaNineState)
                                    mediaItems["mediaNine"] = mediaNineState.toUri()
                                    mediaNineWasClicked = false
                                    isEditingMediaNine = true
                                    isEditingProfile = true
                                    usersViewModel.saveUserDataToFirebase(
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
                }

                item {
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

                    Text(
                        text = "Profile Video (Required)", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(8.dp))
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
                                    TAG,
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
                                user = user?.copy(mediaProfileVideo = mediaProfileVideoState)
                                mediaItems["mediaProfileVideo"] = mediaProfileVideoState.toUri()
                                mediaProfileVideoWasClicked = false
                                isEditingMediaProfileVideo = true
                                usersViewModel.saveUserDataToFirebase(
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
                }

                item {
                    var displayNameState by rememberSaveable {
                        mutableStateOf(
                            user?.displayName ?: ""
                        )
                    }
                    Text(
                        text = "Display Name (Required)", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(8.dp))
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
                        TextField(value = displayNameState,
                            onValueChange = { newValue ->
                                if (newValue.length <= 500) {
                                    displayNameState = newValue
                                }
                            },
//                            label = { Text(text = //Display Name) },
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
                        LaunchedEffect(displayNameState) {
                            userData["displayName"] = displayNameState
                            user = user?.copy(displayName = displayNameState)
                        }
                    }
                }
                item {
                    var aboutMeState by rememberSaveable {
                        mutableStateOf(
                            user?.aboutMe ?: ""
                        )
                    }
                    Text(
                        text = "About Me", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(8.dp))
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
                    LaunchedEffect(aboutMeState) {
                        usersViewModel.updateUserField { currentUser ->
                            currentUser.copy(aboutMe = aboutMeState)
                        }
                        userData["aboutMe"] = aboutMeState
                        user = user?.copy(aboutMe = aboutMeState)
                    }
                }
                item {
                    Text(
                        text = "Prompts", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
                    )
                }
                item {
                    //prompts items - a work in progress
                    val prompts: List<PromptsUtils.Prompt> =
                        PromptsUtils.loadPromptsFromJson(context)
                    val focusRequester = remember { FocusRequester() }
                    val keyboardController = LocalSoftwareKeyboardController.current
                    var promptOneSelection by rememberSaveable {
                        mutableStateOf(
                            user?.promptOneQuestion ?: ""
                        )
                    }
                    var promptOneExpanded by remember { mutableStateOf(false) }
                    var promptOneAnswerState by rememberSaveable {
                        mutableStateOf(
                            user?.promptOneAnswer ?: ""
                        )
                    }
                    var promptTwoSelection by rememberSaveable {
                        mutableStateOf(user?.promptTwoQuestion ?: "")
                    }
                    var promptTwoExpanded by remember { mutableStateOf(false) }
                    var promptTwoAnswerState by rememberSaveable {
                        mutableStateOf(
                            user?.promptTwoAnswer ?: ""
                        )
                    }
                    var promptThreeSelection by rememberSaveable {
                        mutableStateOf(
                            user?.promptThreeQuestion ?: ""
                        )
                    }
                    var promptThreeExpanded by remember { mutableStateOf(false) }
                    var promptThreeAnswerState by rememberSaveable {
                        mutableStateOf(
                            user?.promptThreeAnswer ?: ""
                        )
                    }


                    //promptOne starts
                    //promptOne - question and answer

                    Box() {
                        Column {
                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                            Box(
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            promptOneExpanded = true
                                        }, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(
                                            id = R.drawable.right_quote_svgrepo_com
                                        ),
                                        contentDescription = "Quote Icon",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (promptOneSelection == "") {
                                        Text(
                                            text = "Pick a prompt", fontSize = 20.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            softWrap = false,
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        Text(
                                            text = "Add",
                                            fontSize = 20.sp,
                                            color = Color(0xFFFF6F00)
                                        )
                                    } else {
                                        Text(
                                            text = promptOneSelection,
                                            fontSize = 20.sp,

                                            )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        IconButton(onClick = { promptOneSelection = "" }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.cancel_24px),
                                                contentDescription = "Add Media Button",
                                                modifier = Modifier,
                                                tint = Color(0xFFFF6F00)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                    DropdownMenu(modifier = Modifier.background(Color.White),
                                        expanded = promptOneExpanded,
                                        onDismissRequest = { promptOneExpanded = false }) {
                                        for (prompt in prompts) {
                                            DropdownMenuItem(text = { Text(prompt.prompt) },
                                                onClick = {
                                                    promptOneSelection = prompt.prompt
                                                    promptOneExpanded = false
                                                })
                                        }
                                    }
                                }
                            }
                            LaunchedEffect(promptOneSelection) {
//                                usersViewModel.updateUserField { currentUser ->
//                                    currentUser.copy(promptOneQuestion = promptOneSelection)
//                                }
                                userData["promptOneQuestion"] =
                                    promptOneSelection
                                user = user?.copy(promptOneQuestion = promptOneSelection)
                            }
                        }
                    }

                    //Prompt answer for promptOne

                    Card {
                        TextField(value = promptOneAnswerState,
                            onValueChange = { newValue ->
                                if (newValue.length <= 120) {
                                    promptOneAnswerState = newValue
                                }
                            },
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
                            label = { Text(text = "Answer") },
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
                        LaunchedEffect(promptOneAnswerState) {
                            usersViewModel.updateUserField { currentUser ->
                                currentUser.copy(promptOneAnswer = promptOneAnswerState)
                            }
                            userData["promptOneAnswer"] =
                                promptOneAnswerState
                            user = user?.copy(promptOneAnswer = promptOneAnswerState)
                        }
                    }
                    //promptOne ends

                    //promptTwo starts
                    //promptTwo question

                    Box() {
                        Column {
                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                            Box(
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            promptTwoExpanded = true
                                        }, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(
                                            id = R.drawable.right_quote_svgrepo_com
                                        ),
                                        contentDescription = "Quote Icon",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (promptTwoSelection == "") {
                                        Text(
                                            text = "Pick a prompt", fontSize = 20.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            softWrap = false,
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        Text(
                                            text = "Add",
                                            fontSize = 20.sp,
                                            color = Color(0xFFFF6F00)
                                        )
                                    } else {
                                        Text(
                                            text = promptTwoSelection,
                                            fontSize = 20.sp,

                                            )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        IconButton(onClick = { promptTwoSelection = "" }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.cancel_24px),
                                                contentDescription = "Add Media Button",
                                                modifier = Modifier,
                                                tint = Color(0xFFFF6F00)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                    DropdownMenu(modifier = Modifier.background(Color.White),
                                        expanded = promptTwoExpanded,
                                        onDismissRequest = { promptTwoExpanded = false }) {
                                        for (prompt in prompts) {
                                            DropdownMenuItem(text = { Text(prompt.prompt) },
                                                onClick = {
                                                    promptTwoSelection = prompt.prompt
                                                    promptTwoExpanded = false
                                                })
                                        }
                                    }
                                }
                            }
                        }
                        LaunchedEffect(promptTwoSelection) {
                            usersViewModel.updateUserField { currentUser ->
                                currentUser.copy(promptTwoQuestion = promptTwoSelection)
                            }
                            userData["promptTwoQuestion"] =
                                promptTwoSelection
                            user = user?.copy(promptTwoQuestion = promptTwoSelection)
                        }
                    }

                    //Prompt answer for promptTwo

                    Card {
                        TextField(value = promptTwoAnswerState,
                            onValueChange = { newValue ->
                                if (newValue.length <= 120) {
                                    promptTwoAnswerState = newValue
                                }
                            },
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
                            label = { Text(text = "Answer") },
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
                        LaunchedEffect(promptTwoAnswerState) {
                            usersViewModel.updateUserField { currentUser ->
                                currentUser.copy(promptTwoAnswer = promptTwoAnswerState)
                            }
                            userData["promptTwoAnswer"] =
                                promptTwoAnswerState
                            user = user?.copy(promptTwoAnswer = promptTwoAnswerState)
                        }
                    }

                    //promptTwo ends

                    //promptThree starts
                    //promptThree question

                    Box() {
                        Column {
                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                            Box(
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            promptThreeExpanded = true
                                        }, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(
                                            id = R.drawable.right_quote_svgrepo_com
                                        ),
                                        contentDescription = "Quote Icon",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (promptThreeSelection == "") {
                                        Text(
                                            text = "Pick a prompt", fontSize = 20.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            softWrap = false,
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        Text(
                                            text = "Add",
                                            fontSize = 20.sp,
                                            color = Color(0xFFFF6F00)
                                        )
                                    } else {
                                        Text(
                                            text = promptThreeSelection,
                                            fontSize = 20.sp,

                                            )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        IconButton(onClick = { promptThreeSelection = "" }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.cancel_24px),
                                                contentDescription = "Cancel Button",
                                                modifier = Modifier,
                                                tint = Color(0xFFFF6F00)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                    DropdownMenu(modifier = Modifier.background(Color.White),
                                        expanded = promptThreeExpanded,
                                        onDismissRequest = { promptThreeExpanded = false }) {
                                        for (prompt in prompts) {
                                            DropdownMenuItem(text = { Text(prompt.prompt) },
                                                onClick = {
                                                    promptThreeSelection = prompt.prompt
                                                    promptThreeExpanded = false
                                                })
                                        }
                                    }
                                }
                            }
                        }
                        LaunchedEffect(promptThreeSelection) {
                            usersViewModel.updateUserField { currentUser ->
                                currentUser.copy(promptThreeQuestion = promptThreeSelection)
                            }
                            userData["promptThreeQuestion"] =
                                promptThreeSelection
                            user = user?.copy(promptThreeQuestion = promptThreeSelection)
                        }
                    }

                    //Prompt answer for promptThree

                    Card {
                        TextField(value = promptThreeAnswerState,
                            onValueChange = { newValue ->
                                if (newValue.length <= 120) {
                                    promptThreeAnswerState = newValue
                                }
                            },
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
                            label = { Text(text = "Answer") },
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
                        LaunchedEffect(promptThreeAnswerState) {
                            usersViewModel.updateUserField { currentUser ->
                                currentUser.copy(promptThreeAnswer = promptThreeAnswerState)
                            }
                            userData["promptThreeAnswer"] =
                                promptThreeAnswerState
                            user = user?.copy(promptThreeAnswer = promptThreeAnswerState)
                        }
                    }

                    //promptThree ends

//                    Box() {
//                        Column {
//                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
//                            Box(
//                                modifier = Modifier
//                                    .height(60.dp)
//                                    .fillMaxWidth()
//                            ) {
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxSize()
//                                        .clickable {
//                                            lookingForExpanded = true
//                                        }, verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Icon(
//                                        painterResource(
//                                            id = R.drawable.looking_binocular_svgrepo_com_2
//                                        ),
//                                        contentDescription = "Looking Icon",
//                                        tint = Color(0xFFFF6F00)
//                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    if (lookingForSelectedOption == "") {
//                                        Text(
//                                            text = "Looking for?", fontSize = 20.sp
//                                        )
//                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
//                                        Text(
//                                            text = "Add",
//                                            fontSize = 20.sp,
//                                            color = Color(0xFFFF6F00)
//                                        )
//                                    } else {
//                                        Text(
//                                            text = lookingForSelectedOption, fontSize = 20.sp
//                                        )
//                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
//                                        Text(
//                                            modifier = Modifier.clickable {
//                                                lookingForSelectedOption = ""
//                                            },
//                                            text = "Reset",
//                                            fontSize = 20.sp,
//                                            color = Color(0xFFFF6F00)
//                                        )
//                                    }
//                                    Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
//                                    DropdownMenu(modifier = Modifier.background(Color.White),
//                                        expanded = lookingForExpanded,
//                                        onDismissRequest = { lookingForExpanded = false }) {
//                                        DropdownMenuItem(text = { Text("Long-term") }, onClick = {
//                                            lookingForSelectedOption = "Long-term"
//                                            lookingForExpanded = false
//                                            userData["lookingFor"] = LookingFor.LONG_TERM
//                                        })
//                                        DropdownMenuItem(text = { Text("Short-term") }, onClick = {
//                                            lookingForSelectedOption = "Short-term"
//                                            lookingForExpanded = false
//                                            userData["lookingFor"] = LookingFor.SHORT_TERM
//                                        })
//                                        DropdownMenuItem(text = { Text("Long-term but open-minded") },
//                                            onClick = {
//                                                lookingForSelectedOption =
//                                                    "Long-term but open-minded"
//                                                lookingForExpanded = false
//                                                userData["lookingFor"] =
//                                                    LookingFor.LONG_TERM_BUT_OPEN_MINDED
//                                            })
//                                        DropdownMenuItem(text = { Text("Short-term but open-minded") },
//                                            onClick = {
//                                                lookingForSelectedOption =
//                                                    "Short-term but open-minded"
//                                                lookingForExpanded = false
//                                                userData["lookingFor"] =
//                                                    LookingFor.SHORT_TERM_BUT_OPEN_MINDED
//                                            })
//                                        DropdownMenuItem(text = { Text("Friends") }, onClick = {
//                                            lookingForSelectedOption = "Friends"
//                                            lookingForExpanded = false
//                                            userData["lookingFor"] = LookingFor.FRIENDS
//                                        })
//                                        DropdownMenuItem(text = { Text("Unsure") }, onClick = {
//                                            lookingForSelectedOption = "Unsure"
//                                            lookingForExpanded = false
//                                            userData["lookingFor"] = LookingFor.UNSURE
//                                        })
//                                    }
//                                }
//                            }
//                        }
//                    }


//                    Box() {
//                        Column {
//                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
//                            Box(
//                                modifier = Modifier
//                                    .height(60.dp)
//                                    .fillMaxWidth()
//                            ) {
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxSize()
//                                        .clickable {
//                                            lookingForExpanded = true
//                                        }, verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Icon(
//                                        painterResource(
//                                            id = R.drawable.looking_binocular_svgrepo_com_2
//                                        ),
//                                        contentDescription = "Looking Icon",
//                                        tint = Color(0xFFFF6F00)
//                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    if (lookingForSelectedOption == "") {
//                                        Text(
//                                            text = "Looking for?", fontSize = 20.sp
//                                        )
//                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
//                                        Text(
//                                            text = "Add",
//                                            fontSize = 20.sp,
//                                            color = Color(0xFFFF6F00)
//                                        )
//                                    } else {
//                                        Text(
//                                            text = lookingForSelectedOption, fontSize = 20.sp
//                                        )
//                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
//                                        Text(
//                                            modifier = Modifier.clickable {
//                                                lookingForSelectedOption = ""
//                                            },
//                                            text = "Reset",
//                                            fontSize = 20.sp,
//                                            color = Color(0xFFFF6F00)
//                                        )
//                                    }
//                                    Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
//                                    DropdownMenu(modifier = Modifier.background(Color.White),
//                                        expanded = lookingForExpanded,
//                                        onDismissRequest = { lookingForExpanded = false }) {
//                                        DropdownMenuItem(text = { Text("Long-term") }, onClick = {
//                                            lookingForSelectedOption = "Long-term"
//                                            lookingForExpanded = false
//                                            userData["lookingFor"] = LookingFor.LONG_TERM
//                                        })
//                                        DropdownMenuItem(text = { Text("Short-term") }, onClick = {
//                                            lookingForSelectedOption = "Short-term"
//                                            lookingForExpanded = false
//                                            userData["lookingFor"] = LookingFor.SHORT_TERM
//                                        })
//                                        DropdownMenuItem(text = { Text("Long-term but open-minded") },
//                                            onClick = {
//                                                lookingForSelectedOption =
//                                                    "Long-term but open-minded"
//                                                lookingForExpanded = false
//                                                userData["lookingFor"] =
//                                                    LookingFor.LONG_TERM_BUT_OPEN_MINDED
//                                            })
//                                        DropdownMenuItem(text = { Text("Short-term but open-minded") },
//                                            onClick = {
//                                                lookingForSelectedOption =
//                                                    "Short-term but open-minded"
//                                                lookingForExpanded = false
//                                                userData["lookingFor"] =
//                                                    LookingFor.SHORT_TERM_BUT_OPEN_MINDED
//                                            })
//                                        DropdownMenuItem(text = { Text("Friends") }, onClick = {
//                                            lookingForSelectedOption = "Friends"
//                                            lookingForExpanded = false
//                                            userData["lookingFor"] = LookingFor.FRIENDS
//                                        })
//                                        DropdownMenuItem(text = { Text("Unsure") }, onClick = {
//                                            lookingForSelectedOption = "Unsure"
//                                            lookingForExpanded = false
//                                            userData["lookingFor"] = LookingFor.UNSURE
//                                        })
//                                    }
//                                }
//                            }
//                        }
//                    }


                }
//                item {
//                    Text(
//                        text = "Gender", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
//                    )
//                    Spacer(Modifier.height(8.dp))
//                    var expanded by remember { mutableStateOf(false) }
//                    var selectedOption by remember {
//                        mutableStateOf(
//                            genderSelection ?: ""
//                        )
//                    } // Initialize with genderSelection value
//                    val options = listOf("Option 1", "Option 2", "Option 3")
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .wrapContentSize(Alignment.TopStart)
//                    ) {
//                        Text(text = selectedOption.ifEmpty { "Select an option" },
//                            modifier = Modifier
//                                .padding(horizontal = 16.dp, vertical = 8.dp)
//                                .clickable { expanded = true })
//                        DropdownMenu(modifier = Modifier
//                            .background(Color.White)
//                            .align(Alignment.Center),
//                            expanded = expanded,
//                            onDismissRequest = { expanded = false }) {
//                            DropdownMenuItem(text = { Text("Man") },
//                                onClick = { selectedOption = "Man" })
//                            DropdownMenuItem(text = { Text("Woman") },
//                                onClick = { selectedOption = "Woman" })
//                            DropdownMenuItem(text = {
//                                Text("Non-Binary")
//                            }, onClick = {
//                                selectedOption = "Non-Binary"
////                                navController.navigate("genderSearch")
////                                if (genderSelection != null) {
////                                    selectedOption = genderSelection
////                                }
//                            })
//                        }
//                    }
//                }
//                item {
//                    Text(
//                        text = "Looking For", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
//                    )
//                    Spacer(Modifier.height(8.dp))
//                    var expanded by remember { mutableStateOf(false) }
//                    var selectedOption by remember {
//                        mutableStateOf("")
//                    } // Initialize with genderSelection value
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .wrapContentSize(Alignment.TopStart)
//                    ) {
//                        Text(text = selectedOption.ifEmpty { "Select an option" },
//                            modifier = Modifier
//                                .padding(horizontal = 16.dp, vertical = 8.dp)
//                                .clickable { expanded = true })
//                        DropdownMenu(modifier = Modifier.background(Color.White),
//                            expanded = expanded,
//                            onDismissRequest = { expanded = false }) {
//                            DropdownMenuItem(text = { Text("Short-term but open-minded") },
//                                onClick = { selectedOption = "Short-term but open-minded" })
//                            DropdownMenuItem(text = { Text("Long-term but open-minded") },
//                                onClick = { selectedOption = "Long-term but open-minded" })
//                            DropdownMenuItem(text = { Text("Short-term") },
//                                onClick = { selectedOption = "Short-term" })
//                            DropdownMenuItem(text = { Text("Long-term") },
//                                onClick = { selectedOption = "Long-term" })
//                            DropdownMenuItem(text = { Text("Friends") },
//                                onClick = { selectedOption = "Friends" })
//                            DropdownMenuItem(text = { Text("Unsure") },
//                                onClick = { selectedOption = "Unsure" })
//                        }
//                    }
//                }
//                item {
//                    Text(
//                        text = "Sexual Orientation",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.ExtraBold
//                    )
//                    Spacer(Modifier.height(8.dp))
//                    var expanded by remember { mutableStateOf(false) }
//                    var selectedOption by remember {
//                        mutableStateOf(
//                            genderSelection ?: ""
//                        )
//                    } // Initialize with genderSelection value
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .wrapContentSize(Alignment.TopStart)
//                    ) {
//                        Text(text = selectedOption.ifEmpty { "Select an option" },
//                            modifier = Modifier
//                                .padding(horizontal = 16.dp, vertical = 8.dp)
//                                .clickable { expanded = true })
//                        DropdownMenu(modifier = Modifier.background(Color.White),
//                            expanded = expanded,
//                            onDismissRequest = { expanded = false }) {
//                            DropdownMenuItem(text = { Text("Straight") },
//                                onClick = { selectedOption = "Straight" })
//                            DropdownMenuItem(text = { Text("Gay") },
//                                onClick = { selectedOption = "Gay" })
//                            DropdownMenuItem(text = { Text("Lesbian") },
//                                onClick = { selectedOption = "Lesbian" })
//                            DropdownMenuItem(text = { Text("Bisexual") },
//                                onClick = { selectedOption = "Bisexual" })
//                            DropdownMenuItem(text = { Text("Asexual") },
//                                onClick = { selectedOption = "Asexual" })
//                            DropdownMenuItem(text = { Text("Demisexual") },
//                                onClick = { selectedOption = "Demisexual" })
//                            DropdownMenuItem(text = { Text("Pansexual") },
//                                onClick = { selectedOption = "Pansexual" })
//                            DropdownMenuItem(text = { Text("Queer") },
//                                onClick = { selectedOption = "Queer" })
//                        }
//                    }
//                }
                item {
                    focusRequester = remember { FocusRequester() }
                    keyboardController = LocalSoftwareKeyboardController.current
                    var linkState by rememberSaveable {
                        mutableStateOf(
                            user?.link ?: ""
                        )
                    }
                    Text(
                        text = "Link", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(8.dp))
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
                        TextField(value = linkState,
                            onValueChange = { newValue ->
                                if (newValue.length <= 500) {
                                    linkState = newValue
                                }
                            },
                            label = { Text(text = "Link") },
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
                        Log.d(TAG, "linkState right before saving is ${linkState}")
                        LaunchedEffect(linkState) {
                            if (linkState.isNotBlank()) {
                                usersViewModel.updateUserField { currentUser ->
                                    currentUser.copy(link = linkState)
                                }
                                userData["link"] = linkState
                                user = user?.copy(link = linkState)
                            }
                            Log.d(TAG, "linkState after possibly adding is - ${linkState}")
                        }
                    }
                }
                item {
                    Text(
                        text = "Looking For", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
                    )
                }
                item {
                    var lookingForExpanded by remember { mutableStateOf(false) }
                    var lookingForStoredOption by rememberSaveable {
                        mutableStateOf(
                            when (user?.lookingFor) {
                                LookingFor.LONG_TERM -> "LONG_TERM"
                                LookingFor.LONG_TERM_BUT_OPEN_MINDED -> "LONG_TERM_BUT_OPEN_MINDED"
                                LookingFor.SHORT_TERM -> "SHORT_TERM"
                                LookingFor.SHORT_TERM_BUT_OPEN_MINDED -> "SHORT_TERM_BUT_OPEN_MINDED"
                                LookingFor.FRIENDS -> "FRIENDS"
                                LookingFor.UNSURE -> "UNSURE"
                                else -> "UNKNOWN"
                            }
                        )
                    }
                    var lookingForSelectedOption by rememberSaveable {
                        mutableStateOf(
                            when (lookingForStoredOption) {
                                "LONG_TERM" -> "Long-term"
                                "LONG_TERM_BUT_OPEN_MINDED" -> "Long-term but open-minded"
                                "SHORT_TERM" -> "Short-term"
                                "SHORT_TERM_BUT_OPEN_MINDED" -> "Short-term but open-minded"
                                "FRIENDS" -> "Friends"
                                "UNSURE" -> "Unsure"
                                else -> ""
                            }
                        )
                    }
                    Box() {
                        Column {
                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                            Box(
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            lookingForExpanded = true
                                        }, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(
                                            id = R.drawable.looking_binocular_svgrepo_com_2
                                        ),
                                        contentDescription = "Looking Icon",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (lookingForSelectedOption == "") {
                                        lookingForStoredOption = "UNKNOWN"
                                        Text(
                                            text = "Looking for?", fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        Text(
                                            text = "Add",
                                            fontSize = 20.sp,
                                            color = Color(0xFFFF6F00)
                                        )
                                    } else {
                                        Text(
                                            text = lookingForSelectedOption, fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        IconButton(onClick = { lookingForSelectedOption = "" }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.cancel_24px),
                                                contentDescription = "Cancel Button",
                                                modifier = Modifier,
                                                tint = Color(0xFFFF6F00)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                    DropdownMenu(modifier = Modifier.background(Color.White),
                                        expanded = lookingForExpanded,
                                        onDismissRequest = { lookingForExpanded = false }) {
                                        DropdownMenuItem(text = { Text("Long-term") }, onClick = {
                                            lookingForSelectedOption = "Long-term"
                                            lookingForStoredOption = "LONG_TERM"
                                            lookingForExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Short-term") }, onClick = {
                                            lookingForSelectedOption = "Short-term"
                                            lookingForStoredOption = "SHORT_TERM"
                                            lookingForExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Long-term but open-minded") },
                                            onClick = {
                                                lookingForSelectedOption =
                                                    "Long-term but open-minded"
                                                lookingForStoredOption = "LONG_TERM_BUT_OPEN_MINDED"
                                                lookingForExpanded = false
                                            })
                                        DropdownMenuItem(text = { Text("Short-term but open-minded") },
                                            onClick = {
                                                lookingForSelectedOption =
                                                    "Short-term but open-minded"
                                                lookingForStoredOption =
                                                    "SHORT_TERM_BUT_OPEN_MINDED"
                                                lookingForExpanded = false
                                            })
                                        DropdownMenuItem(text = { Text("Friends") }, onClick = {
                                            lookingForSelectedOption = "Friends"
                                            lookingForStoredOption = "FRIENDS"
                                            lookingForExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Unsure") }, onClick = {
                                            lookingForSelectedOption = "Unsure"
                                            lookingForStoredOption = "UNSURE"
                                            lookingForExpanded = false
                                        })
                                    }
                                    LaunchedEffect(lookingForStoredOption) {
                                        usersViewModel.updateUserField { currentUser ->
                                            currentUser.copy(
                                                lookingFor = when (lookingForStoredOption) {
                                                    "LONG_TERM" -> LookingFor.LONG_TERM
                                                    "SHORT_TERM" -> LookingFor.SHORT_TERM
                                                    "LONG_TERM_BUT_OPEN_MINDED" -> LookingFor.LONG_TERM_BUT_OPEN_MINDED
                                                    "SHORT_TERM_BUT_OPEN_MINDED" -> LookingFor.SHORT_TERM_BUT_OPEN_MINDED
                                                    "FRIENDS" -> LookingFor.FRIENDS
                                                    "UNSURE" -> LookingFor.UNSURE
                                                    else -> LookingFor.UNKNOWN
                                                }
                                            )
                                        }
                                        userData["lookingFor"] =
                                            lookingForStoredOption

                                        user = user?.copy(
                                            lookingFor = when (lookingForStoredOption) {
                                                "LONG_TERM" -> LookingFor.LONG_TERM
                                                "SHORT_TERM" -> LookingFor.SHORT_TERM
                                                "LONG_TERM_BUT_OPEN_MINDED" -> LookingFor.LONG_TERM_BUT_OPEN_MINDED
                                                "SHORT_TERM_BUT_OPEN_MINDED" -> LookingFor.SHORT_TERM_BUT_OPEN_MINDED
                                                "FRIENDS" -> LookingFor.FRIENDS
                                                "UNSURE" -> LookingFor.UNSURE
                                                else -> LookingFor.UNKNOWN
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Text(
                        text = "Gender (Required)", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
                    )
                }
                item {
                    var genderExpanded by remember { mutableStateOf(false) }
                    var genderStoredOption by rememberSaveable {
                        mutableStateOf(
                            when (user?.gender) {
                                Gender.MAN -> "MAN"
                                Gender.WOMAN -> "WOMAN"
                                Gender.NON_BINARY -> "NON_BINARY"
                                else -> "UNKNOWN"
                            }
                        )
                    }
                    var genderSelectedOption by rememberSaveable {
                        mutableStateOf(
                            when (genderStoredOption) {
                                "MAN" -> "Man"
                                "WOMAN" -> "Woman"
                                "NON_BINARY" -> "Non-Binary"
                                else -> ""
                            }
                        )
                    }
                    Box() {
                        Column {
                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                            Box(
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            genderExpanded = true
                                        }, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(
                                            id = R.drawable.male_and_female_symbol_svgrepo_com
                                        ),
                                        contentDescription = "Education Icon",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (genderSelectedOption == "") {
                                        genderStoredOption = "UNKNOWN"
                                        Text(
                                            text = "Add your gender", fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        Text(
                                            text = "Add",
                                            fontSize = 20.sp,
                                            color = Color(0xFFFF6F00)
                                        )
                                    } else {
                                        Text(
                                            text = genderSelectedOption, fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        IconButton(onClick = { genderSelectedOption = "" }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.cancel_24px),
                                                contentDescription = "Cancel Button",
                                                modifier = Modifier,
                                                tint = Color(0xFFFF6F00)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                    DropdownMenu(modifier = Modifier.background(Color.White),
                                        expanded = genderExpanded,
                                        onDismissRequest = { genderExpanded = false }) {
                                        DropdownMenuItem(text = { Text("Man") }, onClick = {
                                            genderSelectedOption = "Man"
                                            genderStoredOption = "MAN"
                                            genderExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Woman") }, onClick = {
                                            genderSelectedOption = "Woman"
                                            genderStoredOption = "WOMAN"
                                            genderExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Non-Binary") }, onClick = {
                                            genderSelectedOption = "Non-Binary"
                                            genderStoredOption = "NON_BINARY"
                                            genderExpanded = false
                                        })
                                    }
                                    LaunchedEffect(genderStoredOption) {
                                        usersViewModel.updateUserField { currentUser ->
                                            currentUser.copy(
                                                gender = when (genderStoredOption) {
                                                    "MAN" -> Gender.MAN
                                                    "WOMAN" -> Gender.WOMAN
                                                    "NON_BINARY" -> Gender.NON_BINARY
                                                    else -> Gender.UNKNOWN
                                                }
                                            )
                                        }
                                        userData["gender"] = genderStoredOption
                                        user = user?.copy(
                                            gender = when (genderStoredOption) {
                                                "MAN" -> Gender.MAN
                                                "WOMAN" -> Gender.WOMAN
                                                "NON_BINARY" -> Gender.NON_BINARY
                                                else -> Gender.UNKNOWN
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                //Sexual Orientation - May add
//                item {
//                    Text(
//                        text = "Sexual Orientation",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.ExtraBold
//                    )
//                }
//                item {
//                    Box() {
//                        Column {
//                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
//                            Box(
//                                modifier = Modifier
//                                    .height(60.dp)
//                                    .fillMaxWidth()
//                            ) {
//                                Row(
//                                    modifier = Modifier.fillMaxSize(),
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
////                                    Icon(
////                                        painterResource(
////                                            id = R.drawable.work_fill0_wght400_grad0_opsz24
////                                        ),
////                                        contentDescription = "Work Icon",
////                                        tint = Color(0xFFFF6F00)
////                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    var expanded by remember { mutableStateOf(false) }
//                                    var selectedOption by remember {
//                                        mutableStateOf(
//                                            ""
//                                        )
//                                    }
//                                    Text(text = selectedOption.ifEmpty { "Select an option" },
//                                        fontSize = 20.sp,
//                                        modifier = Modifier.clickable { expanded = true })
//                                    DropdownMenu(modifier = Modifier.background(Color.White),
//                                        expanded = expanded,
//                                        onDismissRequest = { expanded = false }) {
//                                        DropdownMenuItem(text = { Text("Straight") }, onClick = {
//                                            selectedOption = "Straight"
//                                            expanded = false
//                                        })
//                                        DropdownMenuItem(text = { Text("Gay") }, onClick = {
//                                            selectedOption = "Gay"
//                                            expanded = false
//                                        })
//                                        DropdownMenuItem(text = { Text("Lesbian") }, onClick = {
//                                            selectedOption = "Lesbian"
//                                            expanded = false
//                                        })
//                                        DropdownMenuItem(text = { Text("Bisexual") }, onClick = {
//                                            selectedOption = "Straight"
//                                            expanded = false
//                                        })
//                                        DropdownMenuItem(text = { Text("Asexual") }, onClick = {
//                                            selectedOption = "Gay"
//                                            expanded = false
//                                        })
//                                        DropdownMenuItem(text = { Text("Demisexual") }, onClick = {
//                                            selectedOption = "Lesbian"
//                                            expanded = false
//                                        })
//                                        DropdownMenuItem(text = { Text("Pansexual") }, onClick = {
//                                            selectedOption = "Straight"
//                                            expanded = false
//                                        })
//                                        DropdownMenuItem(text = { Text("Queer") }, onClick = {
//                                            selectedOption = "Gay"
//                                            expanded = false
//                                        })
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
                //Living in - May add
//                item {
//                    Text(
//                        text = "Living in", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
//                    )
//                }
//                item {
//                    Box() {
//                        Column {
//                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
//                            Box(
//                                modifier = Modifier
//                                    .height(60.dp)
//                                    .fillMaxWidth()
//                            ) {
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxSize()
//                                        .align(Alignment.Center)
//                                ) {
//                                    Icon(
//                                        painterResource(id = R.drawable.location_marker_svgrepo_com),
//                                        contentDescription = "Work Icon",
//                                        tint = Color(0xFFFF6F00)
//                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    val sheetState = rememberModalBottomSheetState()
//                                    val scope = rememberCoroutineScope()
//                                    var showBottomSheet by remember { mutableStateOf(false) }
//                                    if (showBottomSheet) {
//                                        ModalBottomSheet(
//                                            modifier = Modifier.fillMaxWidth(),
//                                            onDismissRequest = {
//                                                showBottomSheet = false
//                                            },
//                                            sheetState = sheetState
//                                        ) {
//                                            var textFieldValue by remember {
//                                                mutableStateOf(
//                                                    TextFieldValue()
//                                                )
//                                            }
//                                            val suggestions by remember {
//                                                mutableStateOf(
//                                                    mutableListOf<String>()
//                                                )
//                                            }
//                                            val httpClient = remember { OkHttpClient() }
//                                            val apiKey =
//                                                "YOUR_PLACES_API_KEY" // Replace with your Places API key
//
//                                            LaunchedEffect(textFieldValue.text) {
//                                                if (textFieldValue.text.isNotEmpty()) {
//                                                    val fetchedSuggestions =
//                                                        withContext(Dispatchers.IO) {
//                                                            fetchCityAndStateSuggestions(
//                                                                textFieldValue.text
//                                                            )
//                                                        }
//                                                    suggestions.clear()
//                                                    suggestions.addAll(fetchedSuggestions)
//                                                }
//                                            }
//
//                                            TextField(
//                                                value = textFieldValue,
//                                                onValueChange = { newValue ->
//                                                    textFieldValue = newValue
//                                                }
//                                            )
//
//                                            LazyColumn {
//                                                items(suggestions) { suggestion ->
//                                                    Text(text = suggestion)
//                                                }
//                                            }
//                                        }
//                                    }
//                                    Text(
//                                        modifier = Modifier.clickable {
//                                            showBottomSheet = true
//                                        }, text = "Work", fontSize = 20.sp
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
                item {
                    Text(
                        text = "Height", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
                    )
                }
                item {
                    var heightState by rememberSaveable {
                        mutableStateOf(user?.height ?: "")
                    }
                    val heightAdded = rememberSaveable { mutableStateOf(false) }
                    Box() {
                        Column {
                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                            Box(
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            heightAdded.value = true
                                        }, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(id = R.drawable.ruler_2_svgrepo_com),
                                        contentDescription = "Ruler for Height Icon",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (heightState == "") {
                                        Text(
                                            text = "Add your height", fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        Text(
                                            text = "Add",
                                            fontSize = 20.sp,
                                            color = Color(0xFFFF6F00)
                                        )
                                    } else {
                                        Text(
                                            text = heightState, fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        IconButton(onClick = { heightState = "" }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.cancel_24px),
                                                contentDescription = "Cancel Button",
                                                modifier = Modifier,
                                                tint = Color(0xFFFF6F00)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    if (heightAdded.value) {
                                        var feetToInt = 3
                                        var inchesToInt = 0
                                        ModalBottomSheet(onDismissRequest = {
                                            heightAdded.value = false
                                        }) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally// Set the width to match the parent
                                            ) {
                                                Text(
                                                    text = "Add your height",
                                                    fontSize = 20.sp,
                                                    textAlign = TextAlign.Center,
                                                    color = Color.Black
                                                )
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp)
                                                ) {
                                                    Column() {
                                                        Row() {
                                                            Box(
                                                                modifier = Modifier.weight(
                                                                    1f
                                                                )
                                                            ) {
                                                                Column() {
                                                                    Text(
                                                                        text = "Feet",
                                                                        color = Color.Black
                                                                    )
                                                                    var feet by rememberSaveable {
                                                                        mutableStateOf(
                                                                            "3"
                                                                        )
                                                                    }
                                                                    TextField(
                                                                        value = feet,
                                                                        onValueChange = {
                                                                            feet = it
                                                                        },
                                                                        colors = TextFieldDefaults.textFieldColors(
                                                                            containerColor = Color(
                                                                                0xFFFFD7B5
                                                                            )
                                                                        ),
                                                                        keyboardOptions = KeyboardOptions(
                                                                            keyboardType = KeyboardType.Number
                                                                        )
                                                                    )
                                                                    try {
                                                                        feetToInt = feet.toInt()
                                                                    } catch (_: NumberFormatException) {
                                                                    }
                                                                }
                                                            }
                                                            Spacer(Modifier.width(32.dp))
                                                            Box(
                                                                modifier = Modifier.weight(
                                                                    1f
                                                                )
                                                            ) {
                                                                Column() {
                                                                    Text(
                                                                        text = "Inches",
                                                                        color = Color.Black
                                                                    )
                                                                    var inches by rememberSaveable {
                                                                        mutableStateOf(
                                                                            "0"
                                                                        )
                                                                    }
                                                                    TextField(
                                                                        value = inches,
                                                                        onValueChange = {
                                                                            inches = it
                                                                        },
                                                                        colors = TextFieldDefaults.textFieldColors(
                                                                            containerColor = Color(
                                                                                0xFFFFD7B5
                                                                            )
                                                                        )
                                                                    )
                                                                    try {
                                                                        inchesToInt = inches.toInt()
                                                                    } catch (_: NumberFormatException) {
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        if ((inchesToInt < 0 || inchesToInt > 11) || (feetToInt < 3 || feetToInt > 7)) {
                                                            Spacer(Modifier.height(8.dp))
                                                            Text(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                text = "Height must be 3 - 7 feet and 0 - 11 inches",
                                                                color = Color.Red,
                                                                textAlign = TextAlign.Center
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(Modifier.height(48.dp))
                                                Button(onClick = {
                                                    if (inchesToInt in 0..11 && feetToInt >= 3 && feetToInt <= 7) {
                                                        heightState = "$feetToInt'$inchesToInt"
                                                        heightAdded.value = false
//                                                        scope.launch {
//                                                            snackbarMessage = "Height added!"
//                                                            snackbarHostState.showSnackbar(
//                                                                snackbarMessage,
//                                                                duration = SnackbarDuration.Short
//                                                            )
//                                                        }
                                                        //add height to the DB
                                                    }
                                                }) {
                                                    Text(text = "Add", color = Color.White)
                                                }
                                                Spacer(Modifier.height(120.dp))
                                            }
                                        }
                                    }
                                    LaunchedEffect(heightState) {
                                        usersViewModel.updateUserField { currentUser ->
                                            currentUser.copy(height = heightState)
                                        }
                                        userData["height"] = heightState
                                        user = user?.copy(height = heightState)
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Text(
                        text = "Essentials", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
                    )
                }
                item {
                    focusRequester = remember { FocusRequester() }
                    keyboardController = LocalSoftwareKeyboardController.current
                    val workAdded = rememberSaveable { mutableStateOf(false) }
                    var workState by rememberSaveable {
                        mutableStateOf(
                            user?.work ?: ""
                        )
                    }
                    var educationExpanded by remember { mutableStateOf(false) }
                    var educationStoredOption by rememberSaveable {
                        mutableStateOf(
                            when (user?.education) {
                                Education.SOME_HIGH_SCHOOL -> "SOME_HIGH_SCHOOL"
                                Education.HIGH_SCHOOL -> "HIGH_SCHOOL"
                                Education.SOME_COLLEGE -> "SOME_COLLEGE"
                                Education.UNDERGRAD_DEGREE -> "UNDERGRAD_DEGREE"
                                Education.SOME_GRAD_SCHOOL -> "SOME_GRAD_SCHOOL"
                                Education.GRAD_DEGREE -> "GRAD_DEGREE"
                                Education.TECH_TRADE_SCHOOL -> "TECH_TRADE_SCHOOL"
                                else -> "UNKNOWN"
                            }
                        )
                    }
                    var educationSelectedOption by rememberSaveable {
                        mutableStateOf(
                            when (educationStoredOption) {
                                "SOME_HIGH_SCHOOL" -> "Some High School"
                                "HIGH_SCHOOL" -> "High School"
                                "SOME_COLLEGE" -> "Some College"
                                "UNDERGRAD_DEGREE" -> "Undergrad Degree"
                                "SOME_GRAD_SCHOOL" -> "Some Grad School"
                                "GRAD_DEGREE" -> "Grad Degree"
                                "TECH_TRADE_SCHOOL" -> "Tech/Trade School"
                                else -> ""
                            }
                        )
                    }

                    var kidsExpanded by remember { mutableStateOf(false) }
                    var kidsStoredOption by rememberSaveable {
                        mutableStateOf(
                            when (user?.kids) {
                                Kids.ONE_DAY -> "ONE_DAY"
                                Kids.DONT_WANT -> "DONT_WANT"
                                Kids.HAVE_AND_WANT_MORE -> "HAVE_AND_WANT_MORE"
                                Kids.HAVE_AND_DONT_WANT_MORE -> "HAVE_AND_DONT_WANT_MORE"
                                Kids.UNSURE -> "UNSURE"
                                else -> "UNKNOWN"
                            }
                        )
                    }
                    var kidsSelectedOption by rememberSaveable {
                        mutableStateOf(
                            when (kidsStoredOption) {
                                "ONE_DAY" -> "One day"
                                "DONT_WANT" -> "Don't want"
                                "HAVE_AND_WANT_MORE" -> "Have and want more"
                                "HAVE_AND_DONT_WANT_MORE" -> "Have and don't want more"
                                "UNSURE" -> "Unsure"
                                else -> ""
                            }
                        )
                    }
                    var religionExpanded by remember { mutableStateOf(false) }
                    var religionStoredOption by rememberSaveable {
                        mutableStateOf(
                            when (user?.religion) {
                                Religion.CHRISTIANITY -> "CHRISTIANITY"
                                Religion.ISLAM -> "ISLAM"
                                Religion.HINDUISM -> "HINDUISM"
                                Religion.BUDDHISM -> "BUDDHISM"
                                Religion.SIKHISM -> "SIKHISM"
                                Religion.BAHAI_FAITH -> "BAHAI_FAITH"
                                Religion.CONFUCIANISM -> "CONFUCIANISM"
                                Religion.JAINISM -> "JAINISM"
                                Religion.SHINTOISM -> "SHINTOISM"
                                else -> "UNKNOWN"
                            }
                        )
                    }
                    var religionSelectedOption by rememberSaveable {
                        mutableStateOf(
                            when (religionStoredOption) {
                                "CHRISTIANITY" -> "Christianity"
                                "ISLAM" -> "Islam"
                                "HINDUISM" -> "Hinduism"
                                "BUDDHISM" -> "Buddhism"
                                "SIKHISM" -> "Sikhism"
                                "BAHAI_FAITH" -> "Baháʼí Faith"
                                "CONFUCIANISM" -> "Confucianism"
                                "JAINISM" -> "Jainism"
                                "SHINTOISM" -> "Shintoism"
                                else -> ""
                            }
                        )
                    }
                    var petsExpanded by remember { mutableStateOf(false) }
                    var petsStoredOption by
                    rememberSaveable {
                        mutableStateOf(
                            when (user?.pets) {
                                Pets.DOG -> "DOG"
                                Pets.CAT -> "CAT"
                                Pets.FISH -> "FISH"
                                Pets.HAMSTER_OR_GUINEA_PIG -> "HAMSTER_OR_GUINEA_PIG"
                                Pets.BIRD -> "BIRD"
                                Pets.RABBIT -> "RABBIT"
                                Pets.REPTILE -> "REPTILE"
                                Pets.AMPHIBIAN -> "AMPHIBIAN"
                                else -> "UNKNOWN"
                            }
                        )
                    }
                    var petsSelectedOption by rememberSaveable {
                        mutableStateOf(
                            when (petsStoredOption) {
                                "DOG" -> "Dog"
                                "CAT" -> "Cat"
                                "FISH" -> "Fish"
                                "HAMSTER_OR_GUINEA_PIG" -> "Hamster/Guinea Pig"
                                "BIRD" -> "Bird"
                                "RABBIT" -> "Rabbit"
                                "REPTILE" -> "Reptile"
                                "AMPHIBIAN" -> "Amphibian"
                                else -> ""
                            }
                        )
                    }
                    Box() {
                        Column {
                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                            Box(
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            workAdded.value = true
                                        }, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(
                                            id = R.drawable.work_fill0_wght400_grad0_opsz24
                                        ),
                                        contentDescription = "Work Icon",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (workState == "") {
                                        Text(
                                            text = "Work", fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        Text(
                                            text = "Add",
                                            fontSize = 20.sp,
                                            color = Color(0xFFFF6F00)
                                        )
                                    } else {
                                        Text(
                                            text = workState, fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        IconButton(onClick = { workState = "" }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.cancel_24px),
                                                contentDescription = "Cancel Button",
                                                modifier = Modifier,
                                                tint = Color(0xFFFF6F00)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                    if (workAdded.value) {
                                        ModalBottomSheet(onDismissRequest = {
                                            workAdded.value = false
                                        }) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally// Set the width to match the parent
                                            ) {
                                                Text(
                                                    text = "What do you do for work?",
                                                    fontSize = 20.sp,
                                                    textAlign = TextAlign.Center,
                                                    color = Color.Black
                                                )
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp)
                                                ) {
                                                    Column() {
                                                        Row() {
                                                            TextField(value = workState,
                                                                onValueChange = { newValue: String ->
                                                                    if (newValue.length <= 35) {
                                                                        workState = newValue
                                                                    }
                                                                },
//                                                                label = { Text(text = "Work") },
                                                                singleLine = false,
                                                                keyboardOptions = KeyboardOptions(
                                                                    imeAction = ImeAction.Done
                                                                ),
                                                                keyboardActions = KeyboardActions(
                                                                    onDone = {
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
                                                                    .onFocusChanged {
                                                                        isFocused = it.isFocused
                                                                    }
                                                                    .focusRequester(
                                                                        focusRequester
                                                                    ))
                                                            if (!isFocused) {
                                                                DisposableEffect(Unit) {
                                                                    focusRequester.freeFocus()
                                                                    onDispose {
                                                                        keyboardController?.hide()
                                                                    }
                                                                }
                                                            }
                                                        }

                                                    }
                                                }
                                                Spacer(Modifier.height(48.dp))
                                                Button(onClick = {
                                                    workAdded.value = false
                                                }) {
                                                    Text(text = "Add", color = Color.White)
                                                }
                                                Spacer(Modifier.height(120.dp))
                                            }
                                        }
                                    }
                                    LaunchedEffect(workState) {
                                        usersViewModel.updateUserField { currentUser ->
                                            currentUser.copy(work = workState)
                                        }
                                        userData["work"] = workState
                                        user = user?.copy(work = workState)
                                    }
                                }
                            }
                            HorizontalDivider(thickness = 1.dp)
                            Box(
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            educationExpanded = true
                                        }, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(
                                            id = R.drawable.education_svgrepo_com
                                        ),
                                        contentDescription = "Education Icon",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (educationSelectedOption == "") {
                                        educationStoredOption = "UNKNOWN"
                                        Text(
                                            text = "Education", fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        Text(
                                            text = "Add",
                                            fontSize = 20.sp,
                                            color = Color(0xFFFF6F00)
                                        )
                                    } else {
                                        Text(
                                            text = educationSelectedOption, fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        IconButton(onClick = { educationSelectedOption = "" }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.cancel_24px),
                                                contentDescription = "Cancel Button",
                                                modifier = Modifier,
                                                tint = Color(0xFFFF6F00)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                    DropdownMenu(modifier = Modifier.background(Color.White),
                                        expanded = educationExpanded,
                                        onDismissRequest = { educationExpanded = false }) {
                                        DropdownMenuItem(text = { Text("Some High School") },
                                            onClick = {
                                                educationSelectedOption = "Some High School"
                                                educationStoredOption = "SOME_HIGH_SCHOOL"
                                                educationExpanded = false
                                            })
                                        DropdownMenuItem(text = { Text("High School") }, onClick = {
                                            educationSelectedOption = "High School"
                                            educationStoredOption = "HIGH_SCHOOL"
                                            educationExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Some College") },
                                            onClick = {
                                                educationSelectedOption = "Some College"
                                                educationStoredOption = "SOME_COLLEGE"
                                                educationExpanded = false
                                            })
                                        DropdownMenuItem(text = { Text("Undergrad Degree") },
                                            onClick = {
                                                educationSelectedOption = "Undergrad Degree"
                                                educationStoredOption = "UNDERGRAD_DEGREE"
                                                educationExpanded = false
                                            })
                                        DropdownMenuItem(text = { Text("Some Grad School") },
                                            onClick = {
                                                educationSelectedOption = "Some Grad School"
                                                educationStoredOption = "SOME_GRAD_SCHOOL"
                                                educationExpanded = false
                                            })
                                        DropdownMenuItem(text = { Text("Grad Degree") }, onClick = {
                                            educationSelectedOption = "Grad Degree"
                                            educationStoredOption = "GRAD_DEGREE"
                                            educationExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Technical/Trade School") },
                                            onClick = {
                                                educationSelectedOption = "Technical/Trade School"
                                                educationStoredOption = "TECH_TRADE_SCHOOL"
                                                educationExpanded =
                                                    false
                                            })
                                    }
                                    LaunchedEffect(educationStoredOption) {
                                        usersViewModel.updateUserField { currentUser ->
                                            currentUser.copy(
                                                education =
                                                when (educationStoredOption) {
                                                    "SOME_HIGH_SCHOOL" -> Education.SOME_HIGH_SCHOOL
                                                    "HIGH_SCHOOL" -> Education.HIGH_SCHOOL
                                                    "SOME_COLLEGE" -> Education.SOME_COLLEGE
                                                    "UNDERGRAD_DEGREE" -> Education.UNDERGRAD_DEGREE
                                                    "SOME_GRAD_SCHOOL" -> Education.SOME_GRAD_SCHOOL
                                                    "GRAD_DEGREE" -> Education.GRAD_DEGREE
                                                    "TECH_TRADE_SCHOOL" -> Education.TECH_TRADE_SCHOOL
                                                    else -> Education.UNKNOWN
                                                }
                                            )
                                        }
                                        userData["education"] = educationStoredOption
                                        user = user?.copy(
                                            education = when (educationStoredOption) {
                                                "SOME_HIGH_SCHOOL" -> Education.SOME_HIGH_SCHOOL
                                                "HIGH_SCHOOL" -> Education.HIGH_SCHOOL
                                                "SOME_COLLEGE" -> Education.SOME_COLLEGE
                                                "UNDERGRAD_DEGREE" -> Education.UNDERGRAD_DEGREE
                                                "SOME_GRAD_SCHOOL" -> Education.SOME_GRAD_SCHOOL
                                                "GRAD_DEGREE" -> Education.GRAD_DEGREE
                                                "TECH_TRADE_SCHOOL" -> Education.TECH_TRADE_SCHOOL
                                                else -> Education.UNKNOWN
                                            }
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(thickness = 1.dp)
                            Box(
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { kidsExpanded = true },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(
                                            id = R.drawable.children_svgrepo_com
                                        ),
                                        contentDescription = "Kids Icon",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp)) // Adjust the space as needed
                                    if (kidsSelectedOption == "") {
                                        kidsStoredOption = "UNKNOWN"
                                        Text(
                                            text = "Kids", fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        Text(
                                            text = "Add",
                                            fontSize = 20.sp,
                                            color = Color(0xFFFF6F00)
                                        )
                                    } else {
                                        Text(
                                            text = kidsSelectedOption, fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        IconButton(onClick = { kidsSelectedOption = "" }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.cancel_24px),
                                                contentDescription = "Cancel Button",
                                                modifier = Modifier,
                                                tint = Color(0xFFFF6F00)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                    DropdownMenu(modifier = Modifier.background(Color.White),
                                        expanded = kidsExpanded,
                                        onDismissRequest = { kidsExpanded = false }) {
                                        DropdownMenuItem(text = { Text("One day") }, onClick = {
                                            kidsSelectedOption = "One day"
                                            kidsStoredOption = "ONE_DAY"
                                            kidsExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Don't want") }, onClick = {
                                            kidsSelectedOption = "Don't want"
                                            kidsStoredOption = "DONT_WANT"
                                            kidsExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Have and want more") },
                                            onClick = {
                                                kidsSelectedOption = "Have and want more"
                                                kidsStoredOption = "HAVE_AND_WANT_MORE"
                                                kidsExpanded = false
                                            })
                                        DropdownMenuItem(text = { Text("Have and don't want more") },
                                            onClick = {
                                                kidsSelectedOption = "Have and don't want more"
                                                kidsStoredOption = "HAVE_AND_DONT_WANT_MORE"
                                                kidsExpanded = false
                                            })
                                        DropdownMenuItem(text = { Text("Unsure") }, onClick = {
                                            kidsSelectedOption = "Unsure"
                                            kidsStoredOption = "UNSURE"
                                            kidsExpanded = false
                                        })
                                    }
                                    LaunchedEffect(kidsSelectedOption) {
                                        usersViewModel.updateUserField { currentUser ->
                                            currentUser.copy(
                                                kids =
                                                when (kidsStoredOption) {
                                                    "ONE_DAY" -> Kids.ONE_DAY
                                                    "DONT_WANT" -> Kids.DONT_WANT
                                                    "HAVE_AND_WANT_MORE" -> Kids.HAVE_AND_WANT_MORE
                                                    "HAVE_AND_DONT_WANT_MORE" -> Kids.HAVE_AND_DONT_WANT_MORE
                                                    "UNSURE" -> Kids.UNSURE
                                                    else -> Kids.UNKNOWN
                                                }
                                            )
                                        }
                                        userData["kids"] = kidsStoredOption
                                        user = user?.copy(
                                            kids = when (kidsStoredOption) {
                                                "ONE_DAY" -> Kids.ONE_DAY
                                                "DONT_WANT" -> Kids.DONT_WANT
                                                "HAVE_AND_WANT_MORE" -> Kids.HAVE_AND_WANT_MORE
                                                "HAVE_AND_DONT_WANT_MORE" -> Kids.HAVE_AND_DONT_WANT_MORE
                                                "UNSURE" -> Kids.UNSURE
                                                else -> Kids.UNKNOWN
                                            }
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(thickness = 1.dp)
                            Box(
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { religionExpanded = true },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(
                                            id = R.drawable.praying_svgrepo_com
                                        ),
                                        contentDescription = "Education Icon",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (religionSelectedOption == "") {
                                        religionStoredOption = "UNKNOWN"
                                        Text(
                                            text = "Religion", fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        Text(
                                            text = "Add",
                                            fontSize = 20.sp,
                                            color = Color(0xFFFF6F00)
                                        )
                                    } else {
                                        Text(
                                            text = religionSelectedOption, fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        IconButton(onClick = { religionSelectedOption = "" }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.cancel_24px),
                                                contentDescription = "Cancel Button",
                                                modifier = Modifier,
                                                tint = Color(0xFFFF6F00)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                    DropdownMenu(modifier = Modifier.background(Color.White),
                                        expanded = religionExpanded,
                                        onDismissRequest = { religionExpanded = false }) {
                                        DropdownMenuItem(text = { Text("Christianity") },
                                            onClick = {
                                                religionSelectedOption = "Christianity"
                                                religionStoredOption = "CHRISTIANITY"
                                                religionExpanded = false
                                            })
                                        DropdownMenuItem(text = { Text("Islam") }, onClick = {
                                            religionSelectedOption = "Islam"
                                            religionStoredOption = "ISLAM"
                                            religionExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Hinduism") }, onClick = {
                                            religionSelectedOption = "Hinduism"
                                            religionStoredOption = "HINDUISM"
                                            religionExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Buddhism") }, onClick = {
                                            religionSelectedOption = "Buddhism"
                                            religionStoredOption = "BUDDHISM"
                                            religionExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Sikhism") }, onClick = {
                                            religionSelectedOption = "Sikhism"
                                            religionStoredOption = "SIKHISM"
                                            religionExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Judaism") }, onClick = {
                                            religionSelectedOption = "Judaism"
                                            religionStoredOption = "JUDAISM"
                                            religionExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Baháʼí Faith") },
                                            onClick = {
                                                religionSelectedOption = "Baháʼí Faith"
                                                religionStoredOption = "BAHAI_FAITH"
                                                religionExpanded = false
                                            })
                                        DropdownMenuItem(text = { Text("Confucianism") },
                                            onClick = {
                                                religionSelectedOption = "Confucianism"
                                                religionStoredOption = "CONFUCIANISM"
                                                religionExpanded = false
                                            })
                                        DropdownMenuItem(text = { Text("Jainism") }, onClick = {
                                            religionSelectedOption = "Jainism"
                                            religionStoredOption = "JAINISM"
                                            religionExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Shintoism") }, onClick = {
                                            religionSelectedOption = "Shintoism"
                                            religionStoredOption = "SHINTOISM"
                                            religionExpanded = false
                                        })
                                    }
                                    LaunchedEffect(religionStoredOption) {
                                        usersViewModel.updateUserField { currentUser ->
                                            currentUser.copy(
                                                religion =
                                                when (religionStoredOption) {
                                                    "CHRISTIANITY" -> Religion.CHRISTIANITY
                                                    "ISLAM" -> Religion.ISLAM
                                                    "HINDIUSM" -> Religion.HINDUISM
                                                    "BUDDHISM" -> Religion.BUDDHISM
                                                    "SIKHISM" -> Religion.SIKHISM
                                                    "JUDAISM" -> Religion.JUDAISM
                                                    "BAHA'I FAITH" -> Religion.BAHAI_FAITH
                                                    "CONFUCIANISM" -> Religion.CONFUCIANISM
                                                    "JAINISM" -> Religion.JAINISM
                                                    "SHINTOISM" -> Religion.SHINTOISM
                                                    else -> Religion.UNKNOWN
                                                }
                                            )
                                        }
                                        userData["religion"] = religionStoredOption
                                        user = user?.copy(
                                            religion = when (religionStoredOption) {
                                                "CHRISTIANITY" -> Religion.CHRISTIANITY
                                                "ISLAM" -> Religion.ISLAM
                                                "HINDIUSM" -> Religion.HINDUISM
                                                "BUDDHISM" -> Religion.BUDDHISM
                                                "SIKHISM" -> Religion.SIKHISM
                                                "JUDAISM" -> Religion.JUDAISM
                                                "BAHA'I FAITH" -> Religion.BAHAI_FAITH
                                                "CONFUCIANISM" -> Religion.CONFUCIANISM
                                                "JAINISM" -> Religion.JAINISM
                                                "SHINTOISM" -> Religion.SHINTOISM
                                                else -> Religion.UNKNOWN
                                            }
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(thickness = 1.dp)
                            Box(
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            petsExpanded = true
                                        }, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(
                                            id = R.drawable.pets_svgrepo_com
                                        ),
                                        contentDescription = "Pets Icon",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (petsSelectedOption == "") {
                                        petsStoredOption = "UNKNOWN"
                                        Text(
                                            text = "Pets", fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        Text(
                                            text = "Add",
                                            fontSize = 20.sp,
                                            color = Color(0xFFFF6F00)
                                        )
                                    } else {
                                        Text(text = petsSelectedOption, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                        IconButton(onClick = { petsSelectedOption = "" }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.cancel_24px),
                                                contentDescription = "Cancel Button",
                                                modifier = Modifier,
                                                tint = Color(0xFFFF6F00)
                                            )
                                        }
                                    }
                                    DropdownMenu(modifier = Modifier.background(Color.White),
                                        expanded = petsExpanded,
                                        onDismissRequest = { petsExpanded = false }) {
                                        DropdownMenuItem(text = { Text("Dog") }, onClick = {
                                            petsSelectedOption = "Dog"
                                            petsStoredOption = "DOG"
                                            petsExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Cat") }, onClick = {
                                            petsSelectedOption = "Cat"
                                            petsStoredOption = "CAT"
                                            petsExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Fish") }, onClick = {
                                            petsSelectedOption = "Fish"
                                            petsStoredOption = "FISH"
                                            petsExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Hamster/Guinea Pig") },
                                            onClick = {
                                                petsSelectedOption = "Hamster/Guinea Pig"
                                                petsStoredOption = "HAMSTER_OR_GUINEA_PIG"
                                                petsExpanded = false
                                            })
                                        DropdownMenuItem(text = { Text("Bird") }, onClick = {
                                            petsSelectedOption = "Bird"
                                            petsStoredOption = "BIRD"
                                            petsExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Rabbit") }, onClick = {
                                            petsSelectedOption = "Rabbit"
                                            petsStoredOption = "RABBIT"
                                            petsExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Reptile") }, onClick = {
                                            petsSelectedOption = "Reptile"
                                            petsStoredOption = "REPTILE"
                                            petsExpanded = false
                                        })
                                        DropdownMenuItem(text = { Text("Amphibian") }, onClick = {
                                            petsSelectedOption = "Amphibian"
                                            petsStoredOption = "AMPHIBIAN"
                                            petsExpanded = false
                                        })
                                    }
                                    Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                    LaunchedEffect(petsStoredOption) {
                                        usersViewModel.updateUserField { currentUser ->
                                            currentUser.copy(
                                                pets =
                                                when (petsStoredOption) {
                                                    "DOG" -> Pets.DOG
                                                    "CAT" -> Pets.CAT
                                                    "FISH" -> Pets.FISH
                                                    "HAMSTER_OR_GUINEA_PIG" -> Pets.HAMSTER_OR_GUINEA_PIG
                                                    "BIRD" -> Pets.BIRD
                                                    "RABBIT" -> Pets.RABBIT
                                                    "REPTILE" -> Pets.REPTILE
                                                    "AMPHIBIAN" -> Pets.AMPHIBIAN
                                                    else -> Pets.UNKNOWN
                                                }
                                            )
                                        }
                                        userData["pets"] = petsStoredOption
                                        user = user?.copy(
                                            pets = when (petsStoredOption) {
                                                "DOG" -> Pets.DOG
                                                "CAT" -> Pets.CAT
                                                "FISH" -> Pets.FISH
                                                "HAMSTER_OR_GUINEA_PIG" -> Pets.HAMSTER_OR_GUINEA_PIG
                                                "BIRD" -> Pets.BIRD
                                                "RABBIT" -> Pets.RABBIT
                                                "REPTILE" -> Pets.REPTILE
                                                "AMPHIBIAN" -> Pets.AMPHIBIAN
                                                else -> Pets.UNKNOWN
                                            }
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(thickness = 1.dp)
                        }
                    }
                }
                item {
                    Text(
                        text = "Habits", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
                    )
                }
                item {
                    var exerciseExpanded by remember { mutableStateOf(false) }
                    var exerciseStoredOption by
                    rememberSaveable {
                        mutableStateOf(
                            when (user?.exercise) {
                                Exercise.OFTEN -> "OFTEN"
                                Exercise.SOMETIMES -> "SOMETIMES"
                                Exercise.RARELY -> "RARELY"
                                Exercise.NEVER -> "NEVER"
                                else -> "UNKNOWN"
                            }
                        )
                    }
                    var exerciseSelectedOption by rememberSaveable {
                        mutableStateOf(
                            when (exerciseStoredOption) {
                                "OFTEN" -> "Often"
                                "SOMETIMES" -> "Sometimes'"
                                "RARELY" -> "Rarely"
                                "NEVER" -> "Never"
                                else -> ""
                            }
                        )
                    }
                    var smokingExpanded by remember {
                        mutableStateOf(false)
                    }
                    var smokingStoredOption by rememberSaveable {
                        mutableStateOf(
                            when (user?.smoking) {
                                Smoking.YES -> "YES"
                                Smoking.ON_OCCASION -> "ON_OCCASION"
                                Smoking.NEVER_SMOKE -> "NEVER_SMOKE"
                                else -> "UNKNOWN"
                            }
                        )
                    }
                    var smokingSelectedOption by rememberSaveable {
                        mutableStateOf(
                            when (smokingStoredOption) {
                                "YES" -> "Yes"
                                "ON_OCCASION" -> "On occasion"
                                "NEVER_SMOKE" -> "Never smoke"
                                else -> ""
                            }
                        )
                    }
                    var drinkingExpanded by remember {
                        mutableStateOf(false)
                    }
                    var drinkingStoredOption by rememberSaveable {
                        mutableStateOf(
                            when (user?.drinking) {
                                Drinking.YES -> "YES"
                                Drinking.ON_OCCASION -> "ON_OCCASION"
                                Drinking.NEVER_DRINK -> "NEVER_DRINK"
                                else -> "UNKNOWN"
                            }
                        )
                    }
                    var drinkingSelectedOption by rememberSaveable {
                        mutableStateOf(
                            when (drinkingStoredOption) {
                                "YES" -> "Yes"
                                "ON_OCCASION" -> "On occasion"
                                "NEVER_DRINK" -> "Never drink"
                                else -> ""
                            }
                        )
                    }
                    var marijuanaExpanded by remember {
                        mutableStateOf(false)
                    }
                    var marijuanaStoredOption by rememberSaveable {
                        mutableStateOf(
                            when (user?.marijuana) {
                                Marijuana.YES -> "YES"
                                Marijuana.ON_OCCASION -> "ON_OCCASION"
                                Marijuana.NEVER -> "NEVER"
                                else -> "UNKNOWN"
                            }
                        )
                    }
                    var marijuanaSelectedOption by rememberSaveable {
                        mutableStateOf(
                            when (marijuanaStoredOption) {
                                "YES" -> "Yes"
                                "ON_OCCASION" -> "On occasion"
                                "NEVER" -> "Never"
                                else -> ""
                            }
                        )
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { exerciseExpanded = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(
                                    id = R.drawable.exercise_fill0_wght400_grad0_opsz24
                                ), contentDescription = "Exercise Icon", tint = Color(0xFFFF6F00)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (exerciseSelectedOption == "") {
                                exerciseStoredOption = "UNKNOWN"
                                Text(
                                    text = "Exercise", fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                Text(
                                    text = "Add", fontSize = 20.sp, color = Color(0xFFFF6F00)
                                )
                            } else {
                                Text(text = exerciseSelectedOption, fontSize = 20.sp)
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                IconButton(onClick = { exerciseSelectedOption = "" }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cancel_24px),
                                        contentDescription = "Cancel Button",
                                        modifier = Modifier,
                                        tint = Color(0xFFFF6F00)
                                    )
                                }
                            }
                            DropdownMenu(modifier = Modifier.background(Color.White),
                                expanded = exerciseExpanded,
                                onDismissRequest = { exerciseExpanded = false }) {
                                DropdownMenuItem(text = { Text("Often") }, onClick = {
                                    exerciseSelectedOption = "Often"
                                    exerciseStoredOption = "OFTEN"
                                    exerciseExpanded = false
                                })
                                DropdownMenuItem(text = { Text("Sometimes") }, onClick = {
                                    exerciseSelectedOption = "Sometimes"
                                    exerciseStoredOption = "SOMETIMES"
                                    exerciseExpanded = false
                                })
                                DropdownMenuItem(text = { Text("Rarely") }, onClick = {
                                    exerciseSelectedOption = "Rarely"
                                    exerciseStoredOption = "RARELY"
                                    exerciseExpanded = false
                                })
                                DropdownMenuItem(text = { Text("Never") }, onClick = {
                                    exerciseSelectedOption = "Never"
                                    exerciseStoredOption = "NEVER"
                                    exerciseExpanded = false
                                })
                            }
                            Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                            LaunchedEffect(exerciseStoredOption) {
                                usersViewModel.updateUserField { currentUser ->
                                    currentUser.copy(
                                        exercise = when (exerciseStoredOption) {
                                            "OFTEN" -> Exercise.OFTEN
                                            "SOMETIMES" -> Exercise.SOMETIMES
                                            "RARELY" -> Exercise.RARELY
                                            "NEVER" -> Exercise.NEVER
                                            else -> Exercise.UNKNOWN
                                        }
                                    )
                                }
                                userData["exercise"] = exerciseStoredOption
                                user = user?.copy(
                                    exercise = when (exerciseStoredOption) {
                                        "OFTEN" -> Exercise.OFTEN
                                        "SOMETIMES" -> Exercise.SOMETIMES
                                        "RARELY" -> Exercise.RARELY
                                        "NEVER" -> Exercise.NEVER
                                        else -> Exercise.UNKNOWN
                                    }
                                )
                            }
                        }
                    }
                    HorizontalDivider(thickness = 1.dp)
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    smokingExpanded = true
                                }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(
                                    id = R.drawable.smoking_cigar_svgrepo_com_2
                                ), contentDescription = "Smoking Icon", tint = Color(0xFFFF6F00)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (smokingSelectedOption == "") {
                                smokingStoredOption = "UNKNOWN"
                                Text(
                                    text = "Smoking", fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                Text(
                                    text = "Add", fontSize = 20.sp, color = Color(0xFFFF6F00)
                                )
                            } else {
                                Text(text = smokingSelectedOption, fontSize = 20.sp)
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                IconButton(onClick = { smokingSelectedOption = "" }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cancel_24px),
                                        contentDescription = "Cancel Button",
                                        modifier = Modifier,
                                        tint = Color(0xFFFF6F00)
                                    )
                                }
                            }
                            DropdownMenu(modifier = Modifier.background(Color.White),
                                expanded = smokingExpanded,
                                onDismissRequest = { smokingExpanded = false }) {
                                DropdownMenuItem(text = { Text("Yes") }, onClick = {
                                    smokingSelectedOption = "Yes"
                                    smokingStoredOption = "YES"
                                    smokingExpanded = false
                                })
                                DropdownMenuItem(text = { Text("On occasion") }, onClick = {
                                    smokingSelectedOption = "On occasion"
                                    smokingStoredOption = "ON_OCCASION"
                                    smokingExpanded = false
                                })
                                DropdownMenuItem(text = { Text("Never smoke") }, onClick = {
                                    smokingSelectedOption = "Never smoke"
                                    smokingStoredOption = "NEVER_SMOKE"
                                    smokingExpanded = false
                                })
                            }
                            Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                            LaunchedEffect(smokingStoredOption) {
                                usersViewModel.updateUserField { currentUser ->
                                    currentUser.copy(
                                        smoking = when (smokingStoredOption) {
                                            "YES" -> Smoking.YES
                                            "ON_OCCASION" -> Smoking.ON_OCCASION
                                            "NEVER_SMOKE" -> Smoking.NEVER_SMOKE
                                            else -> Smoking.UNKNOWN
                                        }
                                    )
                                }
                                userData["smoking"] = smokingStoredOption
                                user = user?.copy(
                                    smoking = when (smokingStoredOption) {
                                        "YES" -> Smoking.YES
                                        "ON_OCCASION" -> Smoking.ON_OCCASION
                                        "NEVER_SMOKE" -> Smoking.NEVER_SMOKE
                                        else -> Smoking.UNKNOWN
                                    }
                                )
                            }
                        }
                    }
                    HorizontalDivider(thickness = 1.dp)
                    Box(modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                        .clickable {
                            drinkingExpanded = true
                        }) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(
                                    id = R.drawable.drink_cocktail_svgrepo_com
                                ), contentDescription = "Education Icon", tint = Color(0xFFFF6F00)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (drinkingSelectedOption == "") {
                                drinkingStoredOption = "UNKNOWN"
                                Text(
                                    text = "Drinking", fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                Text(
                                    text = "Add", fontSize = 20.sp, color = Color(0xFFFF6F00)
                                )
                            } else {
                                Text(text = drinkingSelectedOption, fontSize = 20.sp)
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                IconButton(onClick = { drinkingSelectedOption = "" }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cancel_24px),
                                        contentDescription = "Cancel Button",
                                        modifier = Modifier,
                                        tint = Color(0xFFFF6F00)
                                    )
                                }
                            }
                            DropdownMenu(modifier = Modifier.background(Color.White),
                                expanded = drinkingExpanded,
                                onDismissRequest = { drinkingExpanded = false }) {
                                DropdownMenuItem(text = { Text("Yes") }, onClick = {
                                    drinkingSelectedOption = "Yes"
                                    drinkingStoredOption = "YES"
                                    drinkingExpanded = false
                                })
                                DropdownMenuItem(text = { Text("On occasion") }, onClick = {
                                    drinkingSelectedOption = "On occasion"
                                    drinkingStoredOption = "ON_OCCASION"
                                    drinkingExpanded = false
                                })
                                DropdownMenuItem(text = { Text("Never drink") }, onClick = {
                                    drinkingSelectedOption = "Never drink"
                                    drinkingStoredOption = "NEVER_DRINK"
                                    drinkingExpanded = false
                                })
                            }
                            Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                            LaunchedEffect(drinkingStoredOption) {
//                                usersViewModel.updateUserField { currentUser ->
//                                    currentUser.copy(
//                                        drinking = when (drinkingStoredOption) {
//                                            "YES" -> Drinking.YES
//                                            "ON_OCCASION" -> Drinking.ON_OCCASION
//                                            "NEVER_DRINK" -> Drinking.NEVER_DRINK
//                                            else -> Drinking.UNKNOWN
//                                        }
//                                    )
//                                }
                                userData["drinking"] = drinkingStoredOption
                                user = user?.copy(
                                    drinking = when (drinkingStoredOption) {
                                        "YES" -> Drinking.YES
                                        "ON_OCCASION" -> Drinking.ON_OCCASION
                                        "NEVER_DRINK" -> Drinking.NEVER_DRINK
                                        else -> Drinking.UNKNOWN
                                    }
                                )
                            }
                        }
                    }
                    HorizontalDivider(thickness = 1.dp)
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    marijuanaExpanded = true
                                }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(
                                    id = R.drawable.cannabis_marijuana_svgrepo_com
                                ), contentDescription = "Education Icon", tint = Color(0xFFFF6F00)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (marijuanaSelectedOption == "") {
                                marijuanaStoredOption = "UNKNOWN"
                                Text(
                                    text = "Marijuana", fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                Text(
                                    text = "Add", fontSize = 20.sp, color = Color(0xFFFF6F00)
                                )
                            } else {
                                Text(text = marijuanaSelectedOption, fontSize = 20.sp)
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                IconButton(onClick = { marijuanaSelectedOption = "" }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cancel_24px),
                                        contentDescription = "Cancel Button",
                                        modifier = Modifier,
                                        tint = Color(0xFFFF6F00)
                                    )
                                }
                            }
                            DropdownMenu(modifier = Modifier.background(Color.White),
                                expanded = marijuanaExpanded,
                                onDismissRequest = { marijuanaExpanded = false }) {
                                DropdownMenuItem(text = { Text("Yes") }, onClick = {
                                    marijuanaSelectedOption = "Yes"
                                    marijuanaStoredOption = "YES"
                                    marijuanaExpanded = false
                                })
                                DropdownMenuItem(text = { Text("On occasion") }, onClick = {
                                    marijuanaSelectedOption = "On occasion"
                                    marijuanaStoredOption = "ON_OCCASION"
                                    marijuanaExpanded = false
                                })
                                DropdownMenuItem(text = { Text("Never") }, onClick = {
                                    marijuanaSelectedOption = "Never"
                                    marijuanaStoredOption = "NEVER"
                                    marijuanaExpanded = false
                                })
                            }
                            Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                            LaunchedEffect(marijuanaStoredOption) {
                                usersViewModel.updateUserField { currentUser ->
                                    currentUser.copy(
                                        marijuana = when (marijuanaStoredOption) {
                                            "YES" -> Marijuana.YES
                                            "ON_OCCASION" -> Marijuana.ON_OCCASION
                                            "NEVER" -> Marijuana.NEVER
                                            else -> Marijuana.UNKNOWN
                                        }
                                    )
                                }
                                userData["marijuana"] = marijuanaStoredOption
                                user = user?.copy(
                                    marijuana = when (marijuanaStoredOption) {
                                        "YES" -> Marijuana.YES
                                        "ON_OCCASION" -> Marijuana.ON_OCCASION
                                        "NEVER" -> Marijuana.NEVER
                                        else -> Marijuana.UNKNOWN
                                    }
                                )
                            }
                        }
                    }
                    HorizontalDivider(thickness = 1.dp)
                }
                item {
                    Spacer(Modifier.height(8.dp))
                }
            }
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

        if (hasPressedBack) {
            HandleBackPress(
                user, isEditingProfile, userData, firebaseAuth,
                mediaItems, usersViewModel, context, navController, scope,
                snackbarHostState, snackbarMessage, displayNameCallback
            )
            hasPressedBack = false
        }

    }

}


val apiKey = "AIzaSyCaT4JkdeJYxUrFhcPkpMstmAdCziplvOk"

//@Composable
//fun AutocompleteFragment(onPlaceSelected: (Place) -> Unit) {
//    val apiKey = "AIzaSyCaT4JkdeJYxUrFhcPkpMstmAdCziplvOk"
//    val context = LocalContext.current
//    val placesClient = remember(context) { Places.createClient(context) }
//
//    val autocompleteSessionToken = remember { AutocompleteSessionToken.newInstance() }
//    val autocompleteRequest = remember {
//        AutocompleteRequest.builder()
//            .setSessionToken(autocompleteSessionToken)
//            .build()
//    }
//
//    val autocompleteState by remember { mutableStateOf(AutocompleteState()) }
//
//    LaunchedEffect(apiKey) {
//        if (!Places.isInitialized()) {
//            Places.initialize(LocalContext.current.applicationContext, apiKey)
//        }
//    }
//
//    LaunchedEffect(autocompleteSessionToken) {
//        val response = placesClient.findAutocompletePredictions(autocompleteRequest)
//        if (response.isSuccess) {
//            val predictions = response.autocompletePredictions
//            if (predictions.isNotEmpty()) {
//                val placeId = predictions[0].placeId
//                val placeFields = listOf(Field.ID, Field.NAME)
//                val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()
//                val fetchPlaceResponse = placesClient.fetchPlace(fetchPlaceRequest)
//                if (fetchPlaceResponse.isSuccess) {
//                    val place = fetchPlaceResponse.place
//                    onPlaceSelected(place)
//                } else {
//                    // Handle fetch place failure
//                }
//            }
//        } else {
//            // Handle autocomplete failure
//        }
//    }
//}


//    private val httpClient = OkHttpClient()

//    private suspend fun fetchCityAndStateSuggestions(query: String): List<String> {
//        val apiKey = "YOUR_API_KEY"
//        val url = "https://places.googleapis.com/v1/places:autocomplete?key=$apiKey"
//
//        val requestBody = """
//        {
//            "input": "$query",
//            "types": ["(cities)"]
//        }
//    """.trimIndent()
//
//        val mediaType = "application/json".toTypeOfMedia
//        ()
//        val request =
//            Request.Builder().url(url).post(RequestBody.create(mediaType, requestBody)).build()
//
//        return withContext(Dispatchers.IO) {
//            val response = httpClient.newCall(request).execute()
//            val responseData = response.body?.string()
//
//            val suggestions = mutableListOf<String>()
//
//            if (responseData != null) {
//                try {
//                    val json = JSONObject(responseData)
//                    if (json.has("predictions")) {
//                        val predictions = json.getJSONArray("predictions")
//                        for (i in 0 until predictions.length()) {
//                            val prediction = predictions.getJSONObject(i)
//                            val description = prediction.getString("description")
//                            suggestions.add(description)
//                        }
//                    }
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                }
//            }
//
//            suggestions
//        }
//    }

// Perform autocomplete API request
fun performAutocompleteRequest(input: String) {
    val apiKey = "AIzaSyCaT4JkdeJYxUrFhcPkpMstmAdCziplvOk"
    val apiUrl = "https://maps.googleapis.com/maps/api/place/autocomplete/json"

    val url = "$apiUrl?input=$input&types=(cities)&key=$apiKey"

    // Make an HTTP request to the API endpoint using your preferred networking library (e.g., Retrofit, OkHttp, etc.)
    // Parse the API response and handle the suggestions
    // Filter the suggestions for cities
    // Update the UI with the city suggestions
}

// Handle text input changes
fun onTextInputChanged(input: String) {
    if (input.length >= 2) {
        performAutocompleteRequest(input)
    }
}

// Handle city selection
fun onCitySelected(city: String) {
    // Update the text input field with the selected city
    // Perform further actions based on the selected city
}

@Composable
fun LoadImageFromUri(uri: String) {
    val painter = // Apply transformations if needed
        rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current).data(data = uri)
                .apply<ImageRequest.Builder>(block = fun ImageRequest.Builder.() {
                    transformations(CircleCropTransformation()) // Apply transformations if needed
                }).build()
        )

    Image(
        painter = painter,
        contentDescription = null, // Provide an appropriate content description
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds // Adjust content scale as needed
    )
}

@Composable
fun HandleBackPress(
    user: User?,
    isEditingProfile: Boolean,
    userData: MutableMap<String, Any>,
    firebaseAuth: FirebaseAuth,
    mediaItems: MutableMap<String, Uri>,
    usersViewModel: UsersViewModel,
    context: Context,
    navController: NavController,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    snackbarMessage: String,
    displayNameCallback: () -> Unit
) {
    if (!isEditingProfile && user?.displayName?.isBlank() != true ||
        userData["displayName"].toString().isNotBlank()
    ) {

//                        isEditingProfile = true
        val currentUser = firebaseAuth.currentUser
        val userId = firebaseAuth.currentUser?.displayName ?: ""
        Log.d("EditProfileScreen", "userId = ${user?.id ?: ""}")
        Log.d("EditProfileScreen", "currentUser = ${currentUser}")
        mediaItems.clear()
        val wasSaved = usersViewModel.saveUserDataToFirebase(
            user?.id ?: "",
            userData, mediaItems, context
        ) { wasSaved ->
            if (wasSaved) {
//                                isEditingProfile = false
                                navController.popBackStack()
            }
        }
//                        user?.let {
//                            usersViewModel.updateUser(
//                                it, usersViewModel, navController,
//                                context) {}
//                        }
//        navController.popBackStack()

    } else {
        displayNameCallback()
    }
}


//@Preview
//@Composable
//fun EditProfileScreenPreview() {
//    EditProfileScreen(navController = rememberNavController())
//}


