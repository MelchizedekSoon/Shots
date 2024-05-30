package com.example.shots.ui.theme

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.shots.FirebaseModule
import com.example.shots.R
import com.example.shots.RoomModule
import com.example.shots.ViewModelModule
import com.example.shots.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun ProfileScreen(
    navController: NavHostController, usersViewModel: UsersViewModel,
    bookmarkViewModel: BookmarkViewModel,
    receivedLikeViewModel: ReceivedLikeViewModel,
    sentLikeViewModel: SentLikeViewModel,
    receivedShotViewModel: ReceivedShotViewModel,
    sentShotViewModel: SentShotViewModel,
    dataStore: DataStore<Preferences>
) {
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    val firestore = FirebaseModule.provideFirestore()
    val firebaseStorage = FirebaseModule.provideStorage()
    val firebaseRepository =
        FirebaseModule.provideFirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    val editProfileViewModel =
        ViewModelModule.provideEditProfileViewModel(firebaseRepository, firebaseAuth)
    val appDatabase = RoomModule.provideAppDatabase(LocalContext.current)
    val userDao = RoomModule.provideUserDao(appDatabase)
    val receivedLikeDao = RoomModule.provideReceiveLikeDao(appDatabase)
    val sentLikeDao = RoomModule.provideSentLikeDao(appDatabase)
    val receivedShotDao = RoomModule.provideReceiveShotDao(appDatabase)
    val sentShotDao = RoomModule.provideSentShotDao(appDatabase)
    var user: User? by remember { mutableStateOf(null) }
    val retrievedUser = remember { mutableStateOf<User?>(null) }
    var logOutWasClicked by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val logOutConfirmed: () -> Unit = {
        scope.launch {
            firebaseAuth.signOut()
            dataStore.edit { preferences ->
                preferences[intPreferencesKey("currentScreen")] = 0
                preferences[booleanPreferencesKey("isLoggedIn")] = false
            }
            navController.navigate("login")
        }
    }

    var mediaOneState by remember {
        mutableStateOf(
            ""
        )
    }
    var receivedLikesCount by remember { mutableStateOf(0) }
    var receivedShotsCount by remember { mutableStateOf(0) }
    var timesBookmarkedCount by remember { mutableStateOf(0) }


    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
//            val data = firebaseAuth.currentUser?.uid?.let {
//                editProfileViewModel.getUserData(it)
//            }
//            retrievedUser.value = data
//            mediaOneState = retrievedUser.value?.mediaOne ?: ""
            user = usersViewModel.getUser()
            val userList = userDao.getAll()
            if (user != null) {
                Log.d(TAG, "User after reaching Room DB is ${user}")
                Log.d(TAG, "All users here - $userList")
                mediaOneState = user?.mediaOne ?: ""
                Log.d(TAG, "In Launched Effect, the mediaOne value is ${mediaOneState}")

                // These have to be implemented but changed because as of now,
                // it is error prone. I need to check if the object exists first

//                receivedLikesCount = receivedLikeViewModel
//                    .fetchReceivedLikeFromRoom(user?.id ?: "")
//                    .receivedLikes.filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()
//                    .size

//                receivedShotsCount = receivedShotViewModel
//                    .fetchReceivedShotFromRoom(user?.id ?: "")
//                    .receivedShots.filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()
//                    .size

                receivedLikesCount = user?.likesCount ?: 0

                receivedShotsCount = user?.shotsCount ?: 0

                timesBookmarkedCount = user?.timesBookmarkedCount ?: 0

                //                receivedLikesCount =
//                    user?.id?.let {
//                        receivedLikeDao.findById(it).receivedLikes.filter {
//                            it.isNotEmpty()
//                                    || it.isNotBlank() || it.isNotBlank()
//                        }.size
//                    } ?: 0
//
//                receivedShotsCount =
//                    user?.id?.let {
//                        receivedShotDao.findById(it).receivedShots.filter {
//                            it.isNotEmpty()
//                                    || it.isNotBlank() || it.isNotBlank()
//                        }.size
//                    } ?: 0

            }
        }
    }

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = {
                Image(
                    painterResource(R.drawable.shots_3_cropped),
                    "Shots Logo",
                    modifier = Modifier
                        .height(96.dp)
                        .aspectRatio(1f)
                )
//                Text(
//                    //Add Shots back to this later
//                    "Shots",
//                    style = Typography.headlineMedium
//                )
            },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
//                navigationIcon = {
//                    IconButton(onClick = { /* Handle navigation icon click */ }) {
//                        Icon(
//                            Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Back Icon"
//                        )
//                    }
//                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(id = R.drawable.settings_24px),
                            contentDescription = "Account Icon",
                            modifier = Modifier
                                .height(24.dp)
                                .width(24.dp)
                                .clickable {
                                    showBottomSheet = true
                                }
                        )
                    }
                })

        },
        bottomBar = {
            BottomBar(navController = navController, usersViewModel)
        },
        content = {
            Modifier.padding(it)
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Card(
                        modifier = Modifier
                            .width(180.dp)
                            .height(260.dp)
                            .padding(0.dp, 72.dp, 0.dp, 0.dp),
                        colors = CardColors(
                            containerColor = Color.White,
                            contentColor = Color.Black, disabledContentColor = Color.Red,
                            disabledContainerColor = Color.Red
                        )
                    ) {
                        Log.d(
                            TAG, "Right before the GlideImage, mediaOne = ${user?.mediaOne}" +
                                    " but when you call mediaOneState it is " +
                                    mediaOneState
                        )
                        if (!user?.mediaOne.isNullOrBlank()) {
                            GlideImage(
                                model = user?.mediaOne,
                                modifier = Modifier.clip(CircleShape),
                                contentScale = ContentScale.Crop, // This sets the content scale for the loaded image
                                contentDescription = "mediaOne image/Profile Photo"
                            )
                        } else {
                            Icon(
                                painterResource(R.drawable.no_image_svgrepo_com),
                                "No Image Icon"
                                //might change to no account icon
                                , modifier = Modifier
                                    .height(160.dp)
                                    .width(160.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .padding(0.dp, 48.dp, 0.dp, 0.dp)
                            )
                        }


                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = user?.displayName ?: "", fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.Center) {
                        Text(text = "@", fontSize = 16.sp)
                        Text(
                            text = user?.userName ?: "", fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Row() {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clickable {
                                    navController.navigate("preview")
                                },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painterResource(id = R.drawable.account_box_24px),
                                "Preview Icon",
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(32.dp),
                                tint = Color(0xFFFF6F00)
                            )
                            Text(
                                text = "Preview", Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clickable { navController.navigate("editProfile") },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                "Edit Icon",
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(32.dp),
                                tint = Color(0xFFFF6F00)
                            )
                            Text(
                                text = "Edit", Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clickable {
                                    val clipboardManager =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipData = ClipData.newPlainText("Username", user?.userName)
                                    clipboardManager.setPrimaryClip(clipData)

                                    // Show a toast or snackbar to indicate that the username has been copied
//                                    Toast.makeText(
//                                        context,
//                                        "Username ${user?.userName} copied to clipboard",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.content_copy_24px),
                                "Copy Icon",
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(32.dp),
                                tint = Color(0xFFFF6F00)
                            )
                            Text(
                                text = "Copy", Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
//                        Column(
//                            modifier = Modifier
//                                .weight(1f)
//                                .height(64.dp)
//                                .clickable { },
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            Icon(
//                                imageVector = Icons.Outlined.Share,
//                                "Share Icon",
//                                modifier = Modifier
//                                    .width(32.dp)
//                                    .height(32.dp),
//                                tint = Color(0xFFFF6F00)
//                            )
//                            Text(
//                                text = "Share", Modifier.weight(1f),
//                                textAlign = TextAlign.Center
//                            )
//                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(Modifier.height(48.dp))
                    Column {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Your Stats",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        val likesReceivedText = buildAnnotatedString {
                            append("Likes received:")
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFFFF6F00),
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(" $receivedLikesCount")
                            }
                        }
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = likesReceivedText,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        val shotsReceivedText = buildAnnotatedString {
                            append("Shots received:")
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFFFF6F00),
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(" $receivedShotsCount")
                            }
                        }
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = shotsReceivedText,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        val timesBookmarkedText = buildAnnotatedString {
                            append("Times bookmarked:")
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFFFF6F00),
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(" $timesBookmarkedCount")
                            }
                        }
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = timesBookmarkedText,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
//                    Column(
//                        modifier = Modifier
//                            .padding(16.dp, 120.dp, 16.dp, 0.dp)
//                            .fillMaxWidth()
//                    ) {
//                        val motivationalQuotesList = listOf(
//                            "I can accept failure, everyone fails at something. But I can't accept not trying. - Michael Jordan",
//                            "I failed so therefore I succeed. - Michael Jordan",
//                            "Everything you've ever wanted is on the other side of fear. - George Addair"
//                        )
//
//
//                        // Display the motivational quote
//                        Text(
//                            text = motivationalQuotesList.random(),
//                            textAlign = TextAlign.Center,
//                            fontSize = 20.sp
//                        )
//                    }
                }
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                        },
                        sheetState = sheetState
                    ) {
                        // Sheet content
                        Column(
                            modifier = Modifier
                                .height(240.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Log Out", color = Color.Black,
                                modifier = Modifier.clickable {
                                    logOutWasClicked = true
//                                    scope.launch {
//                                        firebaseAuth.signOut()
//                                        dataStore.edit { preferences ->
//                                            preferences[intPreferencesKey("currentScreen")] = 0
//                                            preferences[booleanPreferencesKey("isLoggedIn")] = false
//                                        }
//                                        navController.navigate("login")
//                                    }
                                })
                            Spacer(Modifier.height(24.dp))
                            Text("Blocked Users", color = Color.Black,
                                modifier = Modifier.clickable {
                                   navController.navigate("blockedUsers")
                                })
                            Spacer(Modifier.height(24.dp))
                            Text("Delete Account", color = Color.Black,
                                modifier = Modifier.clickable {
//                                    logOutWasClicked = true
//                                    scope.launch {
//                                        firebaseAuth.signOut()
//                                        dataStore.edit { preferences ->
//                                            preferences[intPreferencesKey("currentScreen")] = 0
//                                            preferences[booleanPreferencesKey("isLoggedIn")] = false
//                                        }
//                                        navController.navigate("login")
//                                    }
                                })
                        }
                    }
                }

            }

            if (logOutWasClicked) {
                LogOutDialog(onLogOutConfirmed = logOutConfirmed) {
                    logOutWasClicked = false
                }
            }
//            Box(modifier = Modifier.fillMaxSize()) {
//                Card(
//                    modifier = Modifier
//                        .align(Alignment.TopCenter)
//                        .width(180.dp)
//                        .height(260.dp)
//                        .padding(0.dp, 72.dp, 0.dp, 0.dp),
//                    colors = CardColors(
//                        containerColor = Color.White,
//                        contentColor = Color.Black, disabledContentColor = Color.Red,
//                        disabledContainerColor = Color.Red
//                    )
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .align(Alignment.CenterHorizontally)
//                    ) {
//                        Image(
//                            modifier = Modifier.clip(CircleShape),
//                            painter = painterResource(id = R.drawable.rafaella_mendes_diniz_aol_mvxprmk_unsplash),
//                            contentDescription = "Profile Photo",
//                            contentScale = ContentScale.Crop,
//                        )
//                    }
//                }
//                Card(
//                    modifier = Modifier
//                        .align(Alignment.TopCenter)
//                        .padding(0.dp, 280.dp, 0.dp, 0.dp),
//                    colors = CardColors(
//                        containerColor = Color.White,
//                        contentColor = Color.Black, disabledContentColor = Color.Red,
//                        disabledContainerColor = Color.Red
//                    )
//                ) {
//                    Text(text = "Rachel", fontSize = 24.sp)
//                }
//                Row(
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .padding(0.dp, 0.dp, 0.dp, 104.dp)
//                ) {
//                    IconButton(onClick = { navController.navigate("editProfile") }) {
//                        Icon(
//                            imageVector = Icons.Default.Edit, contentDescription = "Edit Button",
//                            modifier = Modifier
//                                .height(48.dp)
//                                .width(48.dp)
//                        )
//                    }
//                    Spacer(Modifier.width(32.dp))
//                    Icon(
//                        painter = painterResource(id = R.drawable.visibility_24px),
//                        contentDescription = "View Profile Button",
//                        modifier = Modifier
//                            .height(48.dp)
//                            .width(48.dp)
//                    )
//                }
//            }
        }
    )
}


@Composable
fun LogOutDialog(
    onLogOutConfirmed: () -> Unit,
    onLogOutNotConfirmed: () -> Unit
) {
    var openDialog by remember { mutableStateOf(true) }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                openDialog = false
            },
            title = {
                Text(text = "Log Out")
            },
            text = {
                Text(text = "Do you want to log out?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onLogOutConfirmed()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onLogOutNotConfirmed()
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun motivationalQuotes(): String {
    val motivationalQuotesList = listOf(
        "I can accept failure, everyone fails at something. But I can't accept not trying. - Michael Jordan",
        "I failed so therefore I succeed. - Michael Jordan",
        "Everything you've ever wanted is on the other side of fear. - George Addair"
    )
    return motivationalQuotesList.random()
}

//@Preview
//@Composable
//fun ProfileScreenPreview() {
//    val navController = rememberNavController()
//    ProfileScreen(navController = navController)
//}
