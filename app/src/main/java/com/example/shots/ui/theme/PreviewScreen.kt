package com.example.shots.ui.theme

import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.example.shots.FirebaseModule
import com.example.shots.ProfileMediaDisplay
import com.example.shots.R
import com.example.shots.RoomModule
import com.example.shots.ViewModelModule
import com.example.shots.data.Distance
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
import com.example.shots.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalGlideComposeApi::class
)
@Composable
fun PreviewScreen(
    navController: NavController,
    usersViewModel: UsersViewModel,
    locationViewModel: LocationViewModel
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
    val bookmarkDao = RoomModule.provideBookmarkDao(appDatabase)

    var yourUser by remember { mutableStateOf<User?>(null) }
    var user by remember { mutableStateOf<User?>(null) }
    var userId by remember { mutableStateOf(firebaseAuth.currentUser?.displayName) }
    val yourLatitude = yourUser?.latitude ?: 0.0
    val yourLongitude = yourUser?.longitude ?: 0.0
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isShowingBookmarkDialog by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember {
        mutableStateOf("")
    }
    val showSnackbar: () -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar("Video duration exceeds the maximum allowed duration")
        }
    }


    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.popBackStack()
            }
        }
    }

    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    DisposableEffect(backCallback, onBackPressedDispatcher) {
        onBackPressedDispatcher?.addCallback(backCallback)
        onDispose {
            backCallback.remove()
        }
    }

    var isLiked by rememberSaveable {
        mutableStateOf(false)
    }

    var currentUser by remember { mutableStateOf<User?>(null) }
    var isBookmarked by remember {
        mutableStateOf(true)
    }
    var hasBeenClicked by remember {
        mutableStateOf(false)
    }
    var hasConfirmedRemoval by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        scope.launch {
            withContext(Dispatchers.IO) {
                user = userId.let { userDao.findById(it ?: "") }
                yourUser = usersViewModel.getUser()
                if (user != null) {
                    try {
                        val bookmarks =
                            bookmarkDao.findById(
                                firebaseAuth.currentUser?.displayName ?: ""
                            ).bookmarks
                        Log.d(TAG, "List of bookmarks - ${bookmarks} - userId is $userId")
                        isBookmarked = bookmarks.contains(userId)
                        //                    isLiked = yourUser?.sentLikes?.contains(user!!.id) == true
//                        isBookmarked = bookmarks?.contains(user!!.id) == true
                    } catch (npe: NullPointerException) {
                        Log.d(TAG, "No bookmarks yet")
                    }
                }
                Log.d(TAG, "Welcome to the page of ${user?.userName}")
                Log.d(TAG, "You're logged in as ${yourUser?.userName}")
            }
        }
    }


    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        TopAppBar(title = {
            Text(
                user?.userName ?: "Shots", style = Typography.bodyLarge
            )
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ), navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back Icon"
                )
            }
        }, actions = {
//                    IconButton(onClick = {}) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.more_vert_24px),
//                            contentDescription = "Account Icon",
//                            modifier = Modifier
//                                .height(24.dp)
//                                .width(24.dp),
//                        )
//                    }
        })

    }, snackbarHost = {
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
                    Text(text = "snackbarMessage")
                }
            }

        }
    }) {
        Modifier.padding(it)

        val context = LocalContext.current
//            val cards: List<User> = listOf(
//                User(
//                    "0",
//                    "Rachel",
//                    Calendar.getInstance()
//                        .apply { set(1997, Calendar.NOVEMBER, 16) }, // Set birthday using Calendar
//                    R.drawable.andre_sebastian_3_i3gxwldew_unsplash,
//                    Uri.parse("android.resource://" + context.packageName + "/" + R.raw.oliviaprofilevideo),
//                    "If you ain't spending no money on me, what we tawkin' 4? I can't deal with no broke a.. ninjas and " +
//                            "ion really b up hea lyke dat so add me on da gram. Im mo likely tu respond tu yu up der - RachelTheeBaddie - " +
//                            "My OF bussn too so if yu wanna no bout dat, den dont be scary " + "\uD83D\uDE1B"
//
//                ),
//                User(
//                    "1",
//                    "Bethany",
//                    Calendar.getInstance()
//                        .apply { set(1993, Calendar.JANUARY, 16) }, // Set birthday using Calendar
//                    R.drawable.rafaella_mendes_diniz_aol_mvxprmk_unsplash, null, "... be shopping"
//                ),
//                User(
//                    "2",
//                    "Thomasina",
//                    Calendar.getInstance()
//                        .apply { set(2000, Calendar.MAY, 15) }, // Set birthday using Calendar
//                    R.drawable.ayo_ogunseinde_6w4f62sn_yi_unsplash,
//                    Uri.parse("android.resource://" + context.packageName + "/" + R.raw.oliviaprofilevideo),
//                    "\"Wat's up yall, it's ya girl. I'm not looking for \" +\n" +
//                            "                    \"no broke boys so if you ain't got da bag, please do not apply. You gotta have motion to visit dis ocean.\" +\n" +
//                            "                    \"Independent woman, got my own so no I'm not looking for yours but if you broke, we can't relate.\" +\n" +
//                            "                    \"I'm just looking for my king and no we not linkin at yo crib for the first date, ask me out like a gentleman and yez I have a son so if dats an issue, \" +\n" +
//                            "                    \"please don't shoot your shot! Thank U, next!\""
//                )
//                // Add more users as needed
//            )
//            val user = cards[0]
        val scrollState: LazyListState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //profileVideo
            item {
                Card(
                    modifier = Modifier
                        .padding(0.dp, 48.dp, 0.dp, 0.dp)
                        .height(600.dp),
                    colors = CardColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContentColor = Color.Red,
                        disabledContainerColor = Color.Red
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Display user's verification video if available
//                            val file = File(filePath)
//                        val uri =
//                            Uri.parse("android.resource://" + context.packageName + "/" + R.raw.oliviaprofilevideo)
                        val uri = user?.mediaProfileVideo?.toUri()
                        if (!user?.mediaProfileVideo.isNullOrBlank()) {
                            if (uri != null) {
                                com.example.shots.videoPlayer(videoUri = uri, 60_000L, showSnackbar)
                            }
                        } else {
                            Icon(
                                painterResource(id = R.drawable.video_no_svgrepo_com),
                                "No Video Icon",
                                modifier = Modifier
                                    .height(280.dp)
                                    .width(280.dp)
                                    .align(Alignment.TopCenter)
                                    .padding(0.dp, 200.dp, 0.dp, 0.dp)
                            )
                            Text(
                                user?.displayName + " has no profile video so is unverified. ",
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(0.dp, 80.dp, 0.dp, 0.dp)
                            )
                        }
                    }
                }
            }

//            fun releaseVideoPlayer(completion: () -> Unit) {
//                // Release the video player resources
//                exoPlayer?.release()
//
//                // Call the completion callback when the release is complete
//                completion()
//            }


            //ProfileCard
            item() {
                Card(
                    modifier = Modifier.shadow(16.dp), colors = CardColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContentColor = Color.Red,
                        disabledContainerColor = Color.Red
                    )
                ) {
                    Box() {
                        Icon(painterResource(R.drawable.more_vert_24px),
                            "More Vert Icon",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(0.dp, 8.dp, 8.dp, 0.dp)
                                .clickable {
                                    showBottomSheet = true
                                })
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 40.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                //Display Name
                                val displayName = user?.displayName ?: ""
                                Text(
                                    text = displayName,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                //Username
                                Text(
                                    text = "@${user?.userName}", color = Color(0xFF808080)
                                )
                                //About Me
//                            Text(text = "About Me")

                                //Link
//                                val link = retrievedUser.value?.link ?: ""
//                                Text(
//                                    text = link,
//                                    color = Color(0xFF808080),
//                                    maxLines = 1,
//                                    softWrap = false
//                                )
                                if (!user?.link.isNullOrBlank()) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painterResource(id = R.drawable.link_24px),
                                            "Link Icon",
                                            tint = Color(0xFF808080)
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        val openUrl = rememberLauncherForActivityResult(
                                            ActivityResultContracts.StartActivityForResult()
                                        ) { }
                                        val link = user?.link ?: ""
                                        Text(text = link,
                                            color = Color(0xFF007FFF),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            softWrap = false,
                                            modifier = Modifier // Occupy remaining space in the row
                                                .clickable {
                                                    try {
                                                        val intent = Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse(user?.link ?: "")
                                                        )
                                                        openUrl.launch(intent)
                                                    } catch (e: ActivityNotFoundException) {
                                                        scope.launch {
                                                            withContext(Dispatchers.IO) {
                                                                snackbarMessage =
                                                                    "Link failed to open."
                                                                snackbarHostState.showSnackbar(
                                                                    snackbarMessage
                                                                )
                                                            }
                                                        }
                                                    }
                                                })
                                        // Other composables or icons can be added here
                                    }
                                }

                                if (!user?.link.isNullOrBlank()) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painterResource(id = R.drawable.link_24px),
                                            "Link Icon",
                                            tint = Color(0xFF808080)
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        val openUrl = rememberLauncherForActivityResult(
                                            ActivityResultContracts.StartActivityForResult()
                                        ) { }
                                        val link = user?.link ?: ""
                                        Text(text = link,
                                            color = Color(0xFF007FFF),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            softWrap = false,
                                            modifier = Modifier // Occupy remaining space in the row
                                                .clickable {
                                                    try {
                                                        val intent = Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse(user?.link ?: "")
                                                        )
                                                        openUrl.launch(intent)
                                                    } catch (e: ActivityNotFoundException) {
                                                        scope.launch {
                                                            withContext(Dispatchers.IO) {
                                                                snackbarMessage =
                                                                    "Link failed to open."
                                                                snackbarHostState.showSnackbar(
                                                                    snackbarMessage
                                                                )
                                                            }
                                                        }
                                                    }
                                                })
                                        // Other composables or icons can be added here
                                    }
                                }

                                Spacer(Modifier.height(2.dp))

                                val acceptShotsValue = when(user?.acceptShots) {
                                    Distance.TEN -> "within 10 miles"
                                    Distance.TWENTY -> "within 20 miles"
                                    Distance.THIRTY -> "within 30 miles"
                                    Distance.FORTY -> "within 40 miles"
                                    Distance.FIFTY -> "within 50 miles"
                                    Distance.SIXTY -> "within 60 miles"
                                    Distance.SEVENTY -> "within 70 miles"
                                    Distance.EIGHTY -> "within 80 miles"
                                    Distance.NINETY -> "within 90 miles"
                                    Distance.ONE_HUNDRED -> "within 100 miles"
                                    Distance.ANYWHERE -> "from anywhere"
                                    else -> "within 10 miles"
                                }

                                //Currently accepting shots - maybe put in settings
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Accepting shots $acceptShotsValue",
                                        color = Color(0xFF808080)
                                    )
                                }

//                                Row(
//                                    horizontalArrangement = Arrangement.Center,
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text(
//                                        text = "Within 100 miles",
//                                        fontWeight = FontWeight.Bold,
//                                        color = Color(0xFF808080)
//                                    )
//                                }

                                Spacer(Modifier.height(2.dp))

                                //Location or How far away
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painterResource(id = R.drawable.location_marker_svgrepo_com),
                                        "Location Icon",
                                        tint = Color(0xFF808080)
                                    )
                                    Spacer(Modifier.width(2.dp))
                                    val thisLatitude = user?.latitude ?: 0.0
                                    val thisLongitude = user?.longitude ?: 0.0
                                    val distance = locationViewModel.calculateDistance(
                                        yourLatitude, yourLongitude, thisLatitude, thisLongitude
                                    )
                                    Text(
                                        text = "${distance.toInt()} miles away",
                                        color = Color(0xFF808080)
                                    )
                                    // Other composables or icons can be added here
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (!isLiked) {
                                        Icon(painterResource(id = R.drawable.heart_alt_svgrepo_com),
                                            "Like Button",
                                            tint = Color.Red,
                                            modifier = Modifier
                                                .height(48.dp)
                                                .width(48.dp)
                                                .clickable {
                                                    isLiked = !isLiked
                                                })
                                        Text(text = "Like", fontSize = 16.sp)
                                    } else {
                                        Icon(painterResource(id = R.drawable.heart_svgrepo_com_2),
                                            "Like Button",
                                            tint = Color.Red,
                                            modifier = Modifier
                                                .height(48.dp)
                                                .width(48.dp)
                                                .clickable {
                                                    isLiked = !isLiked
                                                })
                                        Text(text = "Like", fontSize = 16.sp)
                                    }
                                }
                                LaunchedEffect(isLiked) {
                                    scope.launch(Dispatchers.IO) {
                                        val userDataForYou = mutableMapOf<String, Any>()
                                        val userDataForOtherUser = mutableMapOf<String, Any>()

                                        /** since this will be empty, mediaItems (below)
                                         * as it won't get mixed up anyway
                                         */

                                        val mediaItems: MutableMap<String, Uri> = mutableMapOf()
//                                        val sentLikes = yourUser?.sentLikes
//                                        Log.d(ContentValues.TAG, "$sentLikes")
//                                        var sentLikesList = sentLikes?.split(" ")
//                                        Log.d(ContentValues.TAG, "$sentLikesList")
//                                        val otherUserReceivedLikes = user?.receivedLikes
//                                        var otherUserReceivedLikesList =
//                                            otherUserReceivedLikes?.split(" ")

//                                        if (!isLiked) {
////                                            sentLikesList = sentLikesList?.toMutableList()
//                                            if (sentLikesList?.contains(user?.id) == true) {
//                                                sentLikesList.remove(user?.id)
//                                            }
//                                            otherUserReceivedLikesList =
//                                                otherUserReceivedLikesList?.toMutableList()
//                                            if (otherUserReceivedLikesList?.contains(yourUser?.id) == true) {
//                                                otherUserReceivedLikesList.remove(yourUser?.id)
//                                            }
//                                        } else {
//                                            sentLikesList = sentLikesList?.toMutableList()
//                                            if (!sentLikesList?.contains(user?.id)!!) {
//                                                sentLikesList.add(user?.id ?: "")
//                                            }
//                                            otherUserReceivedLikesList =
//                                                otherUserReceivedLikesList?.toMutableList()
//                                            if (yourUser != null && !otherUserReceivedLikesList?.contains(
//                                                    yourUser?.id
//                                                )!!
//                                            ) {
//                                                otherUserReceivedLikesList.add(
//                                                    yourUser?.id ?: ""
//                                                )
//                                            }
//                                        }

                                        /** storing the like you gave to someone else
                                         * as a String below
                                         */
//                                        val sentLikesAsString =
//                                            sentLikesList?.joinToString(" ")?.trim()
//                                        sentLikesList = null
//                                        if (sentLikesAsString != null) {
//                                            userDataForYou["sentLikes"] = sentLikesAsString
//                                        }

                                        // saving the sent like to firestore under your user data
                                        if (userId != null) {
                                            usersViewModel.saveUserDataToFirebase(
                                                userId!!, userDataForYou, mediaItems, context
                                            ) { wasSaved ->
                                                Log.d("PreviewScreen", "Was saved = $wasSaved")
                                            }
                                        }

                                        /** retrieving an instance of user (you) and getting
                                         * the data related from Firestore remotely and
                                         * then taking the udpated sent likes data
                                         * and adding that to your local Room DB
                                         */
//                                        if (userId != null) {
//                                            val returnedUser =
//                                                usersViewModel.getUserDataFromRepo(userId)
//                                            yourUser =
//                                                yourUser?.copy(sentLikes = returnedUser?.sentLikes)
//                                            if (yourUser != null) {
//                                                userDao.insert(yourUser!!)
//                                            }
//                                        }

                                        /** storing the received like to the user you sent
                                         * it to
                                         */
//                                        val otherUserReceivedLikesAsString =
//                                            otherUserReceivedLikesList?.joinToString(" ")?.trim()
//                                        otherUserReceivedLikesList = null
//                                        if (otherUserReceivedLikesAsString != null) {
//                                            userDataForOtherUser["receivedLikes"] =
//                                                otherUserReceivedLikesAsString
//                                        }


                                        /** saving the received like to firestore within
                                         * the other user's data
                                         */
                                        val otherUserId = user?.id ?: ""
                                        usersViewModel.saveUserDataToFirebase(
                                            otherUserId, userDataForOtherUser, mediaItems, context
                                        ) { wasSaved ->
                                            Log.d("PreviewScreen", "wasSaved = $wasSaved")
                                        }


                                        /** retrieving an instance of user (you) and getting
                                         * the data related from Firestore remotely and
                                         * then taking the udpated sent likes data
                                         * and adding that to your local Room DB
                                         */
//                                        if (user?.id != null) {
//                                            //user is the otherUser in this instance
//                                            val otherReturnedUser =
//                                                usersViewModel.getUserDataFromRepo(user?.id!!)
//                                            user =
//                                                user?.copy(receivedLikes = otherReturnedUser?.receivedLikes)
//                                            if (user != null) {
//                                                userDao.insert(user!!)
//                                            }
//                                        }


                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box() {
                                        Icon(
                                            painterResource(id = R.drawable.circle),
                                            "Orange Circle of Ball",
                                            tint = Color(0xFFFFA500),
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                        Icon(
                                            painterResource(id = R.drawable.sports_basketball_24px),
                                            "Shots Button",
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                    Text(text = "Shoot", fontSize = 16.sp)
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val painter = if (!isBookmarked) {
                                        painterResource(id = R.drawable.bookmark_24px)
                                    } else {
                                        painterResource(id = R.drawable.bookmark_24px)
                                        //This is just the preview screen
//                                        painterResource(id = R.drawable.baseline_bookmark_24)
                                    }

                                    Icon(painter,
                                        "Bookmark Button",
                                        tint = Color(0xFF007FFF),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .width(48.dp)
                                            .clickable {
                                                //Add a bookmark snackbar
                                            })
                                    Text(text = "Bookmark", fontSize = 16.sp)

//                                    if (!isBookmarked && hasBeenClicked) {
//                                        Log.d(TAG, "Bookmark is now - $isBookmarked")
//                                        val isConfirmed =
//                                            DialogUtils.bookmarkRemovalDialog { confirmed ->
//                                                // Callback function called on dialog dismissal
//                                                isShowingBookmarkDialog = false
//                                                // Use the `confirmed` value if needed
//                                                Log.d(
//                                                    TAG,
//                                                    "Dialog dismissed, confirmed: $confirmed"
//                                                )
//                                            }
//                                        LaunchedEffect(isConfirmed) {
//                                            if (isConfirmed) {
//                                                scope.launch {
//                                                    withContext(Dispatchers.IO) {
//                                                        val yourUserId =
//                                                            firebaseAuth.currentUser?.uid ?: ""
//                                                        Log.d(TAG, "theirUserId = $userId")
//                                                        val isRemovedFromDB =
//                                                            bookmarkViewModel.removeBookmarkFromFirebase(
//                                                                userId ?: ""
//                                                            )
//                                                        if (isRemovedFromDB) {
//                                                            Log.d(
//                                                                TAG,
//                                                                "Inside isRemovedFromDB - $isConfirmed"
//                                                            )
//                                                            var bookmark =
//                                                                bookmarkDao.findById(yourUserId)
//                                                            val bookmarkList =
//                                                                bookmarkViewModel.getBookmarksFromFirebase(
//                                                                    yourUserId
//                                                                )
//                                                            bookmark =
//                                                                bookmark.copy(bookmarks = bookmarkList.toMutableList())
//                                                            bookmarkDao.insert(bookmark)
//                                                            Log.d(
//                                                                TAG,
//                                                                "Bookmarks - ${bookmark.bookmarks}"
//                                                            )
//                                                        }
//
//                                                    }
//                                                }
//                                                hasConfirmedRemoval = true
//                                            }
//                                        }
//                                        hasBeenClicked = false
//                                    } else {
//                                        LaunchedEffect(isBookmarked) {
//                                            if (isBookmarked) {
//                                                scope.launch {
//                                                    withContext(Dispatchers.IO) {
//                                                        val bookmarkData =
//                                                            mutableMapOf<String, Any>()
//                                                        val yourUserId =
//                                                            firebaseAuth.currentUser?.uid ?: ""
//                                                        var bookmarkList =
//                                                            bookmarkViewModel.getBookmarksFromFirebase(
//                                                                yourUserId
//                                                            )
//                                                        if (!bookmarkList.contains(userId)) {
//                                                            bookmarkData["bookmark-$userId"] =
//                                                                bookmarkViewModel.saveBookmarkToFirebase(
//                                                                    yourUserId,
//                                                                    bookmarkData
//                                                                )
//                                                            bookmarkList =
//                                                                bookmarkViewModel.getBookmarksFromFirebase(
//                                                                    yourUserId
//                                                                )
//                                                            var bookmark =
//                                                                bookmarkDao.findById(yourUserId)
//                                                            bookmark =
//                                                                bookmark.copy(bookmarks = bookmarkList.toMutableList())
//                                                            bookmarkDao.insert(bookmark)
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }


                                }


                            }
                        }
                    }
                }
            }
//            item() {
//                Card(
//                    modifier = Modifier
//                        .shadow(16.dp),
//                    colors = CardColors(
//                        containerColor = Color.White,
//                        contentColor = Color.Black, disabledContentColor = Color.Red,
//                        disabledContainerColor = Color.Red
//                    )
//                ) {
//                    Box() {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .padding(16.dp)
//                        ) {
//                            Column(
//                                modifier = Modifier
//                                    .fillMaxSize(),
////                                verticalArrangement = Arrangement.spacedBy(8.dp), // Adjust the spacing here
////                                horizontalAlignment = Alignment.CenterHorizontally
//                            ) {
//                                Box(modifier = Modifier.fillMaxWidth()) {
//                                    Column() {
//                                        //Display Name
//                                        Text(
//                                            text = retrievedUser.value?.displayName ?: "",
////                                    fontSize = 28.sp,
//                                            fontWeight = FontWeight.Bold
//                                        )
//                                        //Username
//                                        val userName = retrievedUser.value?.userName ?: ""
//                                        if (userName != "") {
//                                            Text(
//                                                text = userName,
////                                        fontSize = 16.sp
//                                            )
//                                        }
//                                    }
//                                }
//
////                                Spacer(Modifier.height(8.dp))
//
//                                //Links
//                                Box(modifier = Modifier.fillMaxWidth()) {
//                                    Row() {
//                                        Icon(
//                                            painter = painterResource(R.drawable.link_svgrepo_com),
//                                            "Link Icon", tint = Color(0xFFFF6F00)
//                                        )
//                                        Spacer(modifier = Modifier.width(4.dp))
//                                        val openUrl = rememberLauncherForActivityResult(
//                                            ActivityResultContracts.StartActivityForResult()
//                                        ) { }
//                                        Text(
//                                            text = retrievedUser.value?.link ?: "",
//                                            maxLines = 1, // Set the maximum number of lines to 1
//                                            overflow = TextOverflow.Ellipsis, // Add an ellipsis (...) if the text overflows
//                                            modifier = Modifier.clickable {
//                                                val intent = Intent(
//                                                    Intent.ACTION_VIEW,
//                                                    Uri.parse(retrievedUser.value?.link)
//                                                )
//                                                openUrl.launch(intent)
//                                            }
//                                        )
//                                    }
//                                }
//
//
//                                //Location or How far away
//                                Box(
//                                    modifier = Modifier.fillMaxWidth(),
////                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Row() {
//                                        Icon(
//                                            painter = painterResource(R.drawable.location_marker_svgrepo_com),
//                                            "Location Icon", tint = Color(0xFFFF6F00)
//                                        )
//                                        Row(
////                                            verticalAlignment = Alignment.CenterVertically // Center the items vertically within the Row
//                                        ) {
//                                            Spacer(modifier = Modifier.width(4.dp))
//                                            Text(text = "45 miles away")
//                                        }
//                                    }
//                                }
////                                Spacer(Modifier.height(8.dp))
//                                Box(
//                                    modifier = Modifier.fillMaxWidth(),
////                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Row() {
//                                        Row(
////                                            verticalAlignment = Alignment.CenterVertically // Center the items vertically within the Row
//                                        ) {
//                                            Spacer(modifier = Modifier.width(4.dp))
//                                            Text(
//                                                text = "Acceptings shots from:",
//                                                fontWeight = FontWeight.Bold,
////                                                fontSize = 20.sp
//                                            )
//                                        }
//                                    }
//                                }
//
//
//                                Box(
//                                    modifier = Modifier.fillMaxWidth(),
////                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Row() {
//                                        Row(
////                                            verticalAlignment = Alignment.CenterVertically // Center the items vertically within the Row
//                                        ) {
//                                            // Currently accepting shots within:
//                                            Spacer(modifier = Modifier.width(4.dp))
//                                            Text(text = "Men within 50 miles")
//                                        }
//                                    }
//                                }
//                            }
//
//
//                            Spacer(Modifier.height(16.dp))
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.Center
//                            ) {
//                                Column(
//                                    modifier = Modifier.weight(1f),
//                                    verticalArrangement = Arrangement.Center,
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Icon(
//                                        painterResource(id = R.drawable.favorite_24px),
//                                        "Like Button",
//                                        tint = Color.Red
//                                    )
//                                    Text(text = "Like", fontSize = 16.sp)
//                                }
//                                Column(
//                                    modifier = Modifier.weight(1f),
//                                    verticalArrangement = Arrangement.Center,
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Box() {
//                                        Icon(
//                                            painterResource(id = R.drawable.circle),
//                                            "Orange Circle of Ball",
//                                            tint = Color(0xFFFFA500),
//                                            modifier = Modifier.align(Alignment.Center)
//                                        )
//                                        Icon(
//                                            painterResource(id = R.drawable.sports_basketball_24px),
//                                            "Shots Button",
//                                            modifier = Modifier.align(Alignment.Center)
//                                        )
//                                    }
//                                    Text(text = "Shoot", fontSize = 16.sp)
//                                }
//                                Column(
//                                    modifier = Modifier.weight(1f),
//                                    verticalArrangement = Arrangement.Center,
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Icon(
//                                        painterResource(id = R.drawable.bookmark_24px),
//                                        "Bookmark Button",
//                                        tint = Color.Blue,
//                                    )
//                                    Text(text = "Bookmark", fontSize = 16.sp)
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//                item() {
//                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                        Card(
//                            modifier = Modifier
//                                .height(90.dp)
//                                .shadow(16.dp)
//                                .weight(1f),
//                            colors = CardColors(
//                                containerColor = Color.White,
//                                contentColor = Color.Black, disabledContentColor = Color.Red,
//                                disabledContainerColor = Color.Red
//                            )
//                        ) {
//                            Box(modifier = Modifier.fillMaxSize()) {
//                                Column(
//                                    modifier = Modifier.fillMaxSize(),
//                                    verticalArrangement = Arrangement.Center,
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Icon(
//                                        painterResource(id = R.drawable.favorite_24px),
//                                        "Like Button",
//                                        tint = Color.Red
//                                    )
//                                    Text(text = "Like", fontSize = 16.sp)
//                                }
//                            }
//                        }
//                        Card(
//                            modifier = Modifier
//                                .height(90.dp)
//                                .shadow(16.dp)
//                                .weight(1f),
//                            colors = CardColors(
//                                containerColor = Color.White,
//                                contentColor = Color.Black, disabledContentColor = Color.Red,
//                                disabledContainerColor = Color.Red
//                            )
//                        ) {
//                            Box(modifier = Modifier.fillMaxSize()) {
//                                Column(
//                                    modifier = Modifier.fillMaxSize(),
//                                    verticalArrangement = Arrangement.Center,
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Box() {
//                                        Icon(
//                                            painterResource(id = R.drawable.circle),
//                                            "Orange Circle of Ball",
//                                            tint = Color(0xFFFFA500),
//                                            modifier = Modifier.align(Alignment.Center)
//                                        )
//                                        Icon(
//                                            painterResource(id = R.drawable.sports_basketball_24px),
//                                            "Shots Button",
//                                            modifier = Modifier.align(Alignment.Center)
//                                        )
//                                    }
//                                    Text(text = "Shoot", fontSize = 16.sp)
//                                }
//                            }
//                        }
//                        Card(
//                            modifier = Modifier
//                                .height(90.dp)
//                                .shadow(16.dp)
//                                .weight(1f),
//                            colors = CardColors(
//                                containerColor = Color.White,
//                                contentColor = Color.Black, disabledContentColor = Color.Red,
//                                disabledContainerColor = Color.Red
//                            )
//                        ) {
//                            Box(modifier = Modifier.fillMaxSize()) {
//                                Column(
//                                    modifier = Modifier.fillMaxSize(),
//                                    verticalArrangement = Arrangement.Center,
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Icon(
//                                        painterResource(id = R.drawable.bookmark_24px),
//                                        "Bookmark Button",
//                                        tint = Color.Blue,
//                                    )
//                                    Text(text = "Bookmark", fontSize = 16.sp)
//                                }
//                            }
//                        }
//                    }
//                }


            //mediaOne
            if (!user?.mediaOne.isNullOrBlank()) {
                item {
                    ProfileMediaDisplay(
                        user?.mediaOne, user?.typeOfMediaOne, showSnackbar
                    )
                }
            } else {
                item {

                }
            }

            //aboutMe section
            if (!user?.aboutMe.isNullOrBlank()) {
                item() {
                    Box() {
                        Card(
                            modifier = Modifier.shadow(16.dp), colors = CardColors(
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                disabledContentColor = Color.Red,
                                disabledContainerColor = Color.Red
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row() {
                                    Icon(
                                        painter = painterResource(id = R.drawable.message_text_svgrepo_com),
                                        "About Me",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        text = "About Me"
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    fontSize = Typography.bodyMedium.fontSize,
                                    text = user?.aboutMe ?: "",
                                )
                            }
                        }
                    }
                }
            }


            //mediaTwo
            if (!user?.mediaTwo.isNullOrBlank()) {
                item {
                    ProfileMediaDisplay(
                        user?.mediaTwo, user?.typeOfMediaTwo, showSnackbar
                    )
                }
            }

            //promptOne
            if (!user?.promptOneQuestion.isNullOrBlank() && !user?.promptOneAnswer.isNullOrBlank()) {
                item() {
                    Box() {
                        Card(
                            modifier = Modifier.shadow(16.dp), colors = CardColors(
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                disabledContentColor = Color.Red,
                                disabledContainerColor = Color.Red
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row() {
                                    Icon(
                                        painter = painterResource(id = R.drawable.right_quote_svgrepo_com),
                                        "Prompt",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        text = user?.promptOneQuestion ?: ""
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    fontSize = Typography.bodyMedium.fontSize,
                                    text = user?.promptOneAnswer ?: "",
                                )
                            }
                        }
                    }
                }
            }


            //mediaThree
            if (!user?.mediaThree.isNullOrBlank()) {
                item {
                    ProfileMediaDisplay(
                        user?.mediaThree, user?.typeOfMediaThree, showSnackbar
                    )
                }
            }


            //details
            //Checks if there are any details available and if so displays them
            if (user?.lookingFor != LookingFor.UNKNOWN || user?.gender != Gender.UNKNOWN || !user?.height.isNullOrBlank()) {
                item {
                    Box() {
                        Card(
                            modifier = Modifier.shadow(16.dp), colors = CardColors(
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                disabledContentColor = Color.Red,
                                disabledContainerColor = Color.Red
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row() {
                                    Icon(
                                        painter = painterResource(id = R.drawable.details_svgrepo_com),
                                        "Details",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        text = "Details"
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))
                                if (user?.lookingFor != LookingFor.UNKNOWN) {
                                    Row() {
                                        Icon(
                                            painter = painterResource(id = R.drawable.looking_binocular_svgrepo_com_2),
                                            "Looking Icon",
                                            tint = Color(0xFFFF6F00)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val lookingFor = when (user?.lookingFor) {
                                            LookingFor.LONG_TERM -> "Long-term"
                                            LookingFor.LONG_TERM_BUT_OPEN_MINDED -> "Long-term but open-minded"
                                            LookingFor.SHORT_TERM -> "Short-term"
                                            LookingFor.SHORT_TERM_BUT_OPEN_MINDED -> "Short-term but open-minded"
                                            LookingFor.UNSURE -> "Unsure"
                                            LookingFor.FRIENDS -> "Friends"
                                            else -> null
                                        }
                                        if (lookingFor != null) {
                                            Text(
                                                fontSize = Typography.bodyMedium.fontSize,
                                                text = lookingFor
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }
                                if (user?.gender != Gender.UNKNOWN) {
//                                    HorizontalDivider()
                                    Spacer(Modifier.height(8.dp))
                                    Row() {
                                        Icon(
                                            painter = painterResource(id = R.drawable.male_and_female_symbol_svgrepo_com),
                                            "Gender Icon",
                                            tint = Color(0xFFFF6F00)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val gender = when (user?.gender) {
                                            Gender.MAN -> "Man"
                                            Gender.WOMAN -> "Woman"
                                            Gender.NON_BINARY -> "Non-Binary"
                                            else -> null
                                        }
                                        if (gender != null) {
                                            Text(
                                                fontSize = Typography.bodyMedium.fontSize,
                                                text = gender
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }
                                if (!user?.height.isNullOrBlank()) {
//                                    HorizontalDivider()
                                    Spacer(Modifier.height(8.dp))
                                    Row() {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ruler_2_svgrepo_com),
                                            "Gender Icon",
                                            tint = Color(0xFFFF6F00)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            fontSize = Typography.bodyMedium.fontSize,
                                            text = user?.height ?: ""
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }


            //mediaFour
            if (!user?.mediaFour.isNullOrBlank()) {
                item {
                    ProfileMediaDisplay(
                        user?.mediaFour, user?.typeOfMediaFour, showSnackbar
                    )
                }
            }


            //promptTwo
            if (!user?.promptTwoQuestion.isNullOrBlank() && !user?.promptTwoAnswer.isNullOrBlank()) {
                item() {
                    Box() {
                        Card(
                            modifier = Modifier.shadow(16.dp), colors = CardColors(
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                disabledContentColor = Color.Red,
                                disabledContainerColor = Color.Red
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row() {
                                    Icon(
                                        painter = painterResource(id = R.drawable.right_quote_svgrepo_com),
                                        "Prompt",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        text = user?.promptTwoQuestion ?: ""
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    fontSize = Typography.bodyMedium.fontSize,
                                    text = user?.promptTwoAnswer ?: "",
                                )
                            }
                        }
                    }
                }
            }


            //mediaFive
            if (!user?.mediaFive.isNullOrBlank()) {
                item {
                    ProfileMediaDisplay(
                        media = user?.mediaFive, user?.typeOfMediaFive, showSnackbar
                    )
                }
            }


            //essentials
            //checks if there are any essentials available and if so, displays
            if (!user?.work.isNullOrBlank() || user?.education != Education.UNKNOWN || user?.kids != Kids.UNKNOWN || user?.religion != Religion.UNKNOWN || user?.pets != Pets.UNKNOWN) {
                item {
                    Box() {
                        Card(
                            modifier = Modifier.shadow(16.dp), colors = CardColors(
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                disabledContentColor = Color.Red,
                                disabledContainerColor = Color.Red
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row() {
                                    Icon(
                                        painter = painterResource(id = R.drawable.account_box_24px),
                                        "Essentials",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        text = "Essentials"
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider()
                                if (!user?.work.isNullOrBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Row() {
                                        Icon(
                                            painter = painterResource(id = R.drawable.work_fill0_wght400_grad0_opsz24),
                                            "Looking Icon",
                                            tint = Color(0xFFFF6F00)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val work = user?.work
                                        if (!work.isNullOrBlank()) {
                                            Text(
                                                fontSize = Typography.bodyMedium.fontSize,
                                                text = work
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }

                                if (user?.education != Education.UNKNOWN) {
//                                    HorizontalDivider()
                                    Spacer(Modifier.height(8.dp))
                                    Row() {
                                        Icon(
                                            painter = painterResource(id = R.drawable.education_svgrepo_com),
                                            "Education Icon",
                                            tint = Color(0xFFFF6F00)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val education = when (user?.education) {
                                            Education.SOME_HIGH_SCHOOL -> "Some High School"
                                            Education.HIGH_SCHOOL -> "High School"
                                            Education.SOME_COLLEGE -> "Some College"
                                            Education.UNDERGRAD_DEGREE -> "Undergrad Degree"
                                            Education.SOME_GRAD_SCHOOL -> "Some Grad School"
                                            Education.GRAD_DEGREE -> "Grad School"
                                            Education.TECH_TRADE_SCHOOL -> "Technical/Trade School"
                                            else -> null
                                        }
                                        if (education != null) {
                                            Text(
                                                fontSize = Typography.bodyMedium.fontSize,
                                                text = education
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }

                                if (user?.kids != Kids.UNKNOWN) {
//                                    HorizontalDivider()
                                    Spacer(Modifier.height(8.dp))
                                    Row() {
                                        Icon(
                                            painter = painterResource(id = R.drawable.children_svgrepo_com),
                                            "Kids Icon",
                                            tint = Color(0xFFFF6F00)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val kids = when (user?.kids) {
                                            Kids.ONE_DAY -> "One day"
                                            Kids.DONT_WANT -> "Don't want"
                                            Kids.HAVE_AND_WANT_MORE -> "Have and want more"
                                            Kids.HAVE_AND_DONT_WANT_MORE -> "Have and don't want more"
                                            Kids.UNSURE -> "Unsure"
                                            else -> null
                                        }
                                        if (kids != null) {
                                            Text(
                                                fontSize = Typography.bodyMedium.fontSize,
                                                text = kids
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }

                                if (user?.religion != Religion.UNKNOWN) {
//                                    HorizontalDivider()
                                    Spacer(Modifier.height(8.dp))
                                    Row() {
                                        Icon(
                                            painter = painterResource(id = R.drawable.praying_svgrepo_com),
                                            "Religion Icon",
                                            tint = Color(0xFFFF6F00)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val religion = when (user?.religion) {
                                            Religion.CHRISTIANITY -> "Christianity"
                                            Religion.ISLAM -> "Islam"
                                            Religion.HINDUISM -> "Hindiusm"
                                            Religion.BUDDHISM -> "Buddhism"
                                            Religion.SIKHISM -> "Sikhism"
                                            Religion.JUDAISM -> "Judaism"
                                            Religion.BAHAI_FAITH -> "Baháʼí Faith"
                                            Religion.CONFUCIANISM -> "Confucianism"
                                            Religion.JAINISM -> "Jainism"
                                            Religion.SHINTOISM -> "Shintoism"
                                            else -> null
                                        }
                                        if (religion != null) {
                                            Text(
                                                fontSize = Typography.bodyMedium.fontSize,
                                                text = religion
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }

                                if (user?.pets != Pets.UNKNOWN) {
//                                    HorizontalDivider()
                                    Spacer(Modifier.height(8.dp))
                                    Row() {
                                        Icon(
                                            painter = painterResource(id = R.drawable.pets_svgrepo_com),
                                            "Pets Icon",
                                            tint = Color(0xFFFF6F00)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val pets = when (user?.pets) {
                                            Pets.DOG -> "Dog"
                                            Pets.CAT -> "Cat"
                                            Pets.FISH -> "Fish"
                                            Pets.HAMSTER_OR_GUINEA_PIG -> "Hamster/Guinea Pig"
                                            Pets.BIRD -> "Bird"
                                            Pets.RABBIT -> "Rabbit"
                                            Pets.REPTILE -> "Reptile"
                                            Pets.AMPHIBIAN -> "Amphibian"
                                            else -> null
                                        }
                                        if (pets != null) {
                                            Text(
                                                fontSize = Typography.bodyMedium.fontSize,
                                                text = pets
                                            )
                                        }
                                    }
                                }


                            }
                        }
                    }
                }
            }


            //mediaSix
            if (!user?.mediaSix.isNullOrBlank()) {
                item {
                    ProfileMediaDisplay(
                        user?.mediaSix, user?.typeOfMediaSix, showSnackbar
                    )
                }
            }


            //promptThree
            if (!user?.promptThreeQuestion.isNullOrBlank() && !user?.promptThreeAnswer.isNullOrBlank()) {
                item() {
                    Box() {
                        Card(
                            modifier = Modifier.shadow(16.dp), colors = CardColors(
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                disabledContentColor = Color.Red,
                                disabledContainerColor = Color.Red
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row() {
                                    Icon(
                                        painter = painterResource(id = R.drawable.right_quote_svgrepo_com),
                                        "Prompt",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        text = user?.promptThreeQuestion ?: ""
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    fontSize = Typography.bodyMedium.fontSize,
                                    text = user?.promptThreeAnswer ?: "",
                                )
                            }
                        }
                    }
                }
            }

            //mediaSeven
            if (!user?.mediaSeven.isNullOrBlank()) {
                item {
                    ProfileMediaDisplay(
                        media = user?.mediaSeven, user?.typeOfMediaSeven, showSnackbar
                    )
//                    Card(
//                        modifier = Modifier
//                            .height(520.dp),
//                        colors = CardColors(
//                            containerColor = Color.White,
//                            contentColor = Color.Black, disabledContentColor = Color.Red,
//                            disabledContainerColor = Color.Red
//                        )
//                    ) {
//                        Box(modifier = Modifier.fillMaxSize()) {
//                            // Display user's verification video if available
//                            UserImageForPreview(user?.mediaSeven)
//                        }
//                    }
                }
            }


            //habits
            //Checks if there are any habits available and if so displays them
            if (user?.exercise != Exercise.UNKNOWN || user?.smoking != Smoking.UNKNOWN || user?.drinking != Drinking.UNKNOWN || user?.marijuana != Marijuana.UNKNOWN) {
                item {
                    Box() {
                        Card(
                            modifier = Modifier.shadow(16.dp), colors = CardColors(
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                disabledContentColor = Color.Red,
                                disabledContainerColor = Color.Red
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row() {
                                    Icon(
                                        painter = painterResource(id = R.drawable.workout_treadmill_svgrepo_com),
                                        "Habits",
                                        tint = Color(0xFFFF6F00)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        text = "Habits"
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))
                                if (user?.exercise != Exercise.UNKNOWN) {
                                    Row() {
                                        Icon(
                                            painter = painterResource(id = R.drawable.exercise_fill0_wght400_grad0_opsz24),
                                            "Exercise Icon",
                                            tint = Color(0xFFFF6F00)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val exercise = when (user?.exercise) {
                                            Exercise.OFTEN -> "Often"
                                            Exercise.SOMETIMES -> "Sometimes"
                                            Exercise.RARELY -> "Rarely"
                                            Exercise.NEVER -> "Never"
                                            else -> null
                                        }
                                        if (exercise != null) {
                                            Text(
                                                fontSize = Typography.bodyMedium.fontSize,
                                                text = exercise
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }
                                if (user?.smoking != Smoking.UNKNOWN) {
//                                    HorizontalDivider()
                                    Spacer(Modifier.height(8.dp))
                                    Row() {
                                        Icon(
                                            painter = painterResource(id = R.drawable.smoking_cigar_svgrepo_com_2),
                                            "Smoking Icon",
                                            tint = Color(0xFFFF6F00)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val smoking = when (user?.smoking) {
                                            Smoking.YES -> "Yes"
                                            Smoking.ON_OCCASION -> "On occasion"
                                            Smoking.NEVER_SMOKE -> "Never smoke"
                                            else -> null
                                        }
                                        if (smoking != null) {
                                            Text(
                                                fontSize = Typography.bodyMedium.fontSize,
                                                text = smoking
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }

                                if (user?.drinking != Drinking.UNKNOWN) {
//                                    HorizontalDivider()
                                    Spacer(Modifier.height(8.dp))
                                    Row() {
                                        Icon(
                                            painter = painterResource(id = R.drawable.drink_cocktail_svgrepo_com),
                                            "Drinking Icon",
                                            tint = Color(0xFFFF6F00)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val drinking = when (user?.drinking) {
                                            Drinking.YES -> "Yes"
                                            Drinking.ON_OCCASION -> "On occasion"
                                            Drinking.NEVER_DRINK -> "Never drink"
                                            else -> null
                                        }
                                        if (drinking != null) {
                                            Text(
                                                fontSize = Typography.bodyMedium.fontSize,
                                                text = drinking
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }

                                if (user?.marijuana != Marijuana.UNKNOWN) {
//                                    HorizontalDivider()
                                    Spacer(Modifier.height(8.dp))
                                    Row() {
                                        Icon(
                                            painter = painterResource(id = R.drawable.cannabis_marijuana_svgrepo_com),
                                            "Marijuana Icon",
                                            tint = Color(0xFFFF6F00)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val marijuana = when (user?.marijuana) {
                                            Marijuana.YES -> "Yes"
                                            Marijuana.ON_OCCASION -> "On occasion"
                                            Marijuana.NEVER -> "Never"
                                            else -> null
                                        }
                                        if (marijuana != null) {
                                            Text(
                                                fontSize = Typography.bodyMedium.fontSize,
                                                text = marijuana
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            //mediaEight
            if (!user?.mediaEight.isNullOrBlank()) {
                item {
                    ProfileMediaDisplay(
                        media = user?.mediaEight, user?.typeOfMediaEight, showSnackbar
                    )
                }
            }


            //mediaNine
            if (!user?.mediaNine.isNullOrBlank()) {
                item {
                    ProfileMediaDisplay(
                        user?.mediaNine, user?.typeOfMediaNine, showSnackbar
                    )
                }
            }

            //back to the top
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "To the top", modifier = Modifier.clickable {
                        scope.launch {
                            scrollState.scrollToItem(0)
                        }
                    })
                }
            }

        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                }, sheetState = sheetState
            ) {
                // Sheet content
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(text = "Report", color = Color.Black)
                    Spacer(Modifier.height(24.dp))
                    Text(text = "Block", color = Color.Black)
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                        }) {
                        Text(text = "Copy username", color = Color.Black)
                    }
//                    Text(text = "Copy profile URL", color = Color.Black)
//                    Spacer(Modifier.height(24.dp))
//                    Text(text = "Share this profile", color = Color.Black)
                    Spacer(Modifier.height(72.dp))
                }
            }
        }

    }
}

//fun hasDetails(retrievedUser: MutableState<User?>): Boolean {
//    return !(retrievedUser.value?.lookingFor == null && retrievedUser.value?.gender == null && retrievedUser.value?.height == null && retrievedUser.value?.height == "")
//}
//
//fun hasEssentials(retrievedUser: MutableState<User?>): Boolean {
//    return !((retrievedUser.value?.work == null || retrievedUser.value?.work == "") && retrievedUser.value?.education == null && retrievedUser.value?.kids == null && retrievedUser.value?.religion == null && retrievedUser.value?.pets == null)
//}
//
//fun hasHabits(retrievedUser: MutableState<User?>): Boolean {
//    return !(retrievedUser.value?.exercise == null && retrievedUser.value?.smoking == null && retrievedUser.value?.drinking == null && retrievedUser.value?.marijuana == null)
//}

//@Composable
//fun UserImage(imageResourceId: Int) {
//    Card(
//        modifier = Modifier.height(520.dp), colors = CardColors(
//            containerColor = Color.White,
//            contentColor = Color.Black,
//            disabledContentColor = Color.Red,
//            disabledContainerColor = Color.Red
//        )
//    ) {
//        Box(
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Image(
//                painter = painterResource(id = imageResourceId),
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.fillMaxSize()
//            )
//        }
//    }
//}

//@Composable
//fun VerificationVideo(videoUri: Uri?) {
//    videoUri?.let { uri ->
//        Box(
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            useExoPlayer(uri)
//        }
//    }
//}


//@Composable
//fun ExoVideoPlayer(file: File) {
//    val context = LocalContext.current
//    val exoPlayer = remember { getSimpleExoPlayer(context, file) }
//    AndroidView(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(bottom = 20.dp),
//        factory = { context1 ->
//            PlayerView(context1).apply {
//                player = exoPlayer
//            }
//        },
//    )
//}

//@androidx.annotation.OptIn(UnstableApi::class)
//@Composable
//private fun useExoPlayer(videoUri: Uri): ExoPlayer {
//
//
//    val context = LocalContext.current
//    val exoPlayer = remember {
//        ExoPlayer.Builder(context).build()
//    }
//
//    DisposableEffect(exoPlayer) {
//        onDispose {
//            exoPlayer.release()
//        }
//    }
//
//    AndroidView(
//        modifier = Modifier.fillMaxSize(),
//        factory = { contextOne ->
//            PlayerView(contextOne).apply {
//                player = exoPlayer
//                // Set the resize mode to Aspect Fill
//                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
//                // Set the gravity of the controller to bottom
//                controllerShowTimeoutMs = 0
//                controllerAutoShow = false
//                // Set padding to prevent cutting off the video
//                contentDescription = "Video Player"
//                setPadding(0, 0, 0, 0)
//                // Build the media item.
//                val mediaItem = MediaItem.fromUri(videoUri)
//                // Set the media item to be played.
//                exoPlayer.setMediaItem(mediaItem)
//                // Prepare the player.
//                exoPlayer.prepare()
//                exoPlayer.clock.currentTimeMillis()
//            }
//        },
//    )
//
//    return exoPlayer
//}
//
//
//@Composable
//fun AboutMeText(text: String) {
//    // Display user's about me text
//    // You can use Text composable here to display the text
//    Card(
//        modifier = Modifier.shadow(16.dp), colors = CardColors(
//            containerColor = Color.White,
//            contentColor = Color.Black,
//            disabledContentColor = Color.Red,
//            disabledContainerColor = Color.Red
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            Text(
//                fontWeight = FontWeight.Bold, fontSize = 16.sp, text = "About Me"
//            )
//            Text(
//                fontSize = Typography.bodyMedium.fontSize, text = text,
//            )
//        }
//    }
//}
//
////fun hasDetails(user: MutableState<User?>): Boolean {
////    return !(user.lookingFor == null && user.value?.gender == null &&
////            user.value?.height == null && retrievedUser.value?.height == "")
////}
////
////fun hasEssentials(user: MutableState<User?>): Boolean {
////    return !((retrievedUser.value?.work == null || retrievedUser.value?.work == "") &&
////            retrievedUser.value?.education == null && retrievedUser.value?.kids == null &&
////            retrievedUser.value?.religion == null && retrievedUser.value?.pets == null)
////}
////
////fun hasHabits(retrievedUser: MutableState<User?>): Boolean {
////    return !(retrievedUser.value?.exercise == null && retrievedUser.value?.smoking == null
////            && retrievedUser.value?.drinking == null && retrievedUser.value?.marijuana == null)
////}
//
//
//@OptIn(ExperimentalGlideComposeApi::class)
//@Composable
//fun UserImageForPreview(media: String?) {
//    Card(
//        modifier = Modifier.height(520.dp),
//        colors = CardColors(
//            containerColor = Color.White,
//            contentColor = Color.Black, disabledContentColor = Color.Red,
//            disabledContainerColor = Color.Red
//        )
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            GlideImage(
//                model = media?.toUri(),
//                contentDescription = "$media",
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.fillMaxSize()
//            )
//        }
//    }
//}
//
//@Composable
//fun VerificationVideoForPreview(videoUri: Uri?) {
//    videoUri?.let { uri ->
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            useExoPlayer(uri)
//        }
//    }
//}
//
//
//@Composable
//fun ExoVideoPlayerForPreview(file: File) {
////    val context = LocalContext.current
////    val exoPlayer = remember { getSimpleExoPlayer(context, file) }
////    AndroidView(
////        modifier = Modifier
////            .fillMaxSize()
////            .padding(bottom = 20.dp),
////        factory = { context1 ->
////            PlayerView(context1).apply {
////                player = exoPlayer
////            }
////        },
////    )
//}
//
//@androidx.annotation.OptIn(UnstableApi::class)
//@Composable
//private fun useExoPlayer(videoUri: Uri): ExoPlayer {
//    val context = LocalContext.current
//    val exoPlayer = remember {
//        ExoPlayer.Builder(context).build()
//    }
//
//    DisposableEffect(exoPlayer) {
//        onDispose {
//            exoPlayer.release()
//        }
//    }
//
//    AndroidView(
//        modifier = Modifier.fillMaxSize(),
//        factory = { context1 ->
//            PlayerView(context1).apply {
//                player = exoPlayer
//                // Set the resize mode to Aspect Fill
//                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
//                // Set the gravity of the controller to bottom
//                controllerShowTimeoutMs = 0
//                controllerAutoShow = false
//                // Set padding to prevent cutting off the video
//                contentDescription = "Video Player"
//                setPadding(0, 0, 0, 0)
//                // Build the media item.
//                val mediaItem = MediaItem.fromUri(videoUri)
//                // Set the media item to be played.
//                exoPlayer.setMediaItem(mediaItem)
//                // Prepare the player.
//                exoPlayer.prepare()
//            }
//        },
//    )
//
//    return exoPlayer
//}


//@Composable
//fun AboutMeTextForPreview(text: String) {
//    // Display user's about me text
//    // You can use Text composable here to display the text
//    Box() {
//        Card(
//            modifier = Modifier
//                .shadow(16.dp),
//            colors = CardColors(
//                containerColor = Color.White,
//                contentColor = Color.Black, disabledContentColor = Color.Red,
//                disabledContainerColor = Color.Red
//            )
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp)
//            ) {
//                Text(
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 16.sp, text = "About Me"
//                )
//                Spacer(Modifier.width(4.dp))
//                Icon(
//                    painter = painterResource(id = R.drawable.message_regular),
//                    "About Me", tint = Color(0xFFFF6F00)
//                )
//                Text(
//                    fontSize = Typography.bodyMedium.fontSize, text = text,
//                )
//            }
//        }
//    }
//}

@Preview
@Composable
fun PreviewScreenPreview() {
//    UserProfileScreen(navController = rememberNavController())
}
