package com.example.shots.ui.theme

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.shots.FirebaseModule
import com.example.shots.ProfileUserCardDisplay
import com.example.shots.R
import com.example.shots.RoomModule
import com.example.shots.ViewModelModule
import com.example.shots.data.Bookmark
import com.example.shots.data.TypeOfMedia
import com.example.shots.data.User
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    navController: NavHostController,
    signupViewModel: SignupViewModel,
    usersViewModel: UsersViewModel,
    locationViewModel: LocationViewModel,
    bookmarkViewModel: BookmarkViewModel,
    receivedLikeViewModel: ReceivedLikeViewModel,
    sentLikeViewModel: SentLikeViewModel,
    receivedShotViewModel: ReceivedShotViewModel,
    sentShotViewModel: SentShotViewModel,
    blockedUserViewModel: BlockedUserViewModel,
    userWhoBlockedYouViewModel: UserWhoBlockedYouViewModel,
    dataStore: DataStore<Preferences>
) {

    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    val firestore = FirebaseModule.provideFirestore()
    val firebaseStorage = FirebaseModule.provideStorage()
    val firebaseRepository =
        FirebaseModule.provideFirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    val appDatabase = RoomModule.provideAppDatabase(LocalContext.current)
    val retrievedUser = remember { mutableStateOf<User?>(null) }
    val scope = rememberCoroutineScope()
    var currentCard by rememberSaveable { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by remember {
        mutableStateOf("")
    }
    val showSnackbar: () -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar("Video duration exceeds the maximum allowed duration")
        }
    }

    val userId = firebaseAuth.currentUser?.displayName ?: ""
    val context = LocalContext.current

    var newLikesCount by remember { mutableStateOf(0) }
    var timesBookmarkedCount by remember { mutableStateOf(0) }

    var locationWasGranted by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationWasGranted = if (isGranted) {
            true
        } else {
            false
            // Permission denied, handle accordingly
        }
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }



    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {

            val user = usersViewModel.getUser()

            newLikesCount = user?.newLikesCount ?: 0

            timesBookmarkedCount = user?.timesBookmarkedCount ?: 0

            dataStore.edit { preferences ->
                // this needs adjustment and logic figuring
//                preferences[intPreferencesKey("currentCard")] = 0
                preferences[intPreferencesKey("currentScreen")] = 13
                preferences[booleanPreferencesKey("hasSignedUp")] = true
            }

            dataStore.data.collect { preferences ->
                currentCard = preferences[intPreferencesKey("currentCard")] ?: 0
            }
        }
    }

    Scaffold(
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
                    if (newLikesCount > 0) {
                        BadgedBox(badge = {
                            Badge(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ) { Text("$newLikesCount") }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.heart_alt_svgrepo_com),
                                contentDescription = "Like Icon",
                                modifier = Modifier
                                    .height(32.dp)
                                    .width(32.dp)
                                    .clickable {
                                        navController.navigate("like")
                                    }
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.heart_alt_svgrepo_com),
                            contentDescription = "Like Icon",
                            modifier = Modifier
                                .height(32.dp)
                                .width(32.dp)
                                .clickable {
                                    navController.navigate("like")
                                }
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.search_24px),
                        contentDescription = "Search Icon",
                        modifier = Modifier
                            .height(32.dp)
                            .width(32.dp)
                            .clickable {
                                navController.navigate("search")
                            }
                    )
                    Spacer(Modifier.width(16.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.tune_24px),
                        contentDescription = "Account Icon",
                        modifier = Modifier
                            .height(32.dp)
                            .width(32.dp)
                            .padding(0.dp, 0.dp, 8.dp, 0.dp)
                            .clickable {
                                navController.navigate("filter")
                            }
                    )
//                    IconButton(onClick = {}) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.tune_24px),
//                            contentDescription = "Account Icon",
//                            modifier = Modifier
//                                .height(24.dp)
//                                .width(24.dp),
//                        )
//                    }
                })

        },
        bottomBar = {
            BottomBar(navController = navController, usersViewModel)
        },
        content = {
            Modifier.padding(it)

            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    val user = usersViewModel.getUser()
                    if (user?.latitude == 0.0 && user.longitude == 0.0) {
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            object : CancellationToken() {
                                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                                    CancellationTokenSource().token

                                override fun isCancellationRequested() = false
                            })
                            .addOnSuccessListener { location: Location? ->
                                if (location == null)
                                    fusedLocationClient.lastLocation
                                        .addOnSuccessListener { location: Location? ->
                                            // Got last known location. In some rare situations this can be null.
                                            if (location != null) {
                                                val existingUser = usersViewModel.getUser()

                                                var updatedExistingUser =
                                                    existingUser?.copy(latitude = location.latitude)
                                                        ?.copy(longitude = location.longitude)

                                                val userData: MutableMap<String, Any> =
                                                    mutableMapOf()
                                                val mediaItems: MutableMap<String, Uri> =
                                                    mutableMapOf()

                                                if (updatedExistingUser != null) {
                                                    userData["latitude"] =
                                                        updatedExistingUser.latitude ?: 0
                                                    userData["longitude"] =
                                                        updatedExistingUser.longitude ?: 0
                                                    usersViewModel.saveUserDataToFirebase(
                                                        userId, userData,
                                                        mediaItems, context
                                                    ) {

                                                    }
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Cannot get location.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                else {
//                                        val lat = location.latitude
//                                        val lon = location.longitude

                                    locationViewModel.latitude = location.latitude
                                    locationViewModel.longitude = location.longitude
                                    locationViewModel.saveLocationToFirebase(
                                        userId, location.latitude,
                                        location.longitude, context, usersViewModel
                                    )
                                }
                            }

                    }

                    // You can use the API that requires the permission.
                    AnchoredDraggableBox(
                        navController,
                        usersViewModel,
                        locationViewModel,
                        bookmarkViewModel,
                        sentLikeViewModel,
                        receivedLikeViewModel,
                        sentShotViewModel,
                        receivedShotViewModel,
                        blockedUserViewModel,
                        userWhoBlockedYouViewModel,
                        dataStore,
                        showSnackbar
                    )
                }

//                ActivityCompat.shouldShowRequestPermissionRationale(
//                    context as AppCompatActivity, Manifest.permission.ACCESS_FINE_LOCATION
//                ) -> {
//                    // In an educational UI, explain to the user why your app requires this
//                    // permission for a specific feature to behave as expected, and what
//                    // features are disabled if it's declined. In this UI, include a
//                    // "cancel" or "no thanks" button that lets the user continue
//                    // using your app without granting the permission.
//                    LocationDialog { wasGranted ->
//                        if (wasGranted) {
//                            locationPermissionLauncher.launch(
//                                Manifest.permission.ACCESS_FINE_LOCATION
//                            )
//                        } else {
//                            locationWasGranted = wasGranted
//                        }
//                    }
//                    if (!locationWasGranted) {
//                        Card(modifier = Modifier.fillMaxSize()) {
//                            Text(text = "No cards can be displayed")
//                        }
//                    }
//                }

                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    locationPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    if (locationWasGranted) {
                        AnchoredDraggableBox(
                            navController,
                            usersViewModel,
                            locationViewModel,
                            bookmarkViewModel,
                            sentLikeViewModel,
                            receivedLikeViewModel,
                            sentShotViewModel,
                            receivedShotViewModel,
                            blockedUserViewModel,
                            userWhoBlockedYouViewModel,
                            dataStore,
                            showSnackbar
                        )
                    } else {
//                        Box(modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color.White)
//                            .clickable {
//                            }) {
//                            Card(
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .background(Color.White)
//                            ) {
//                                Text(
//                                    text = "No user cards displayed. " +
//                                            "Must grant location access first.",
//                                    textAlign = TextAlign.Center
//                                )
//                            }
//                        }

                        Box(
                            modifier = Modifier
                                .padding(150.dp)
                                .fillMaxSize()
                        ) {
                            Icon(
                                painterResource(id = R.drawable.location_marker_svgrepo_com),
                                "No Location Icon",
                                modifier = Modifier
                                    .height(280.dp)
                                    .width(280.dp)
                                    .align(Alignment.TopCenter)
                                    .padding(0.dp, 40.dp, 0.dp, 0.dp)
                            )

                            Text(
                                "Please grant location access to see users.",
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
        }
    )
}

@Composable
fun LocationDialog(
    onLocationConfirmed: (Boolean) -> Unit
) {
    var openDialog by remember { mutableStateOf(true) }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                openDialog = false
                onLocationConfirmed(false)
            },
            title = {
                Text(text = "Location")
            },
            text = {
                Text(
                    text = "Users cannot be shown without location access because they're shown " +
                            "based on distance."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onLocationConfirmed(true)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onLocationConfirmed(false)
                    }
                ) {
                    Text("No thanks")
                }
            }
        )
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(
    ExperimentalFoundationApi::class, ExperimentalPagerApi::class,
    ExperimentalGlideComposeApi::class
)
@Composable
fun AnchoredDraggableBox(
    navController: NavHostController,
    usersViewModel: UsersViewModel,
    locationViewModel: LocationViewModel,
    bookmarkViewModel: BookmarkViewModel,
    sentLikeViewModel: SentLikeViewModel,
    receivedLikeViewModel: ReceivedLikeViewModel,
    sentShotViewModel: SentShotViewModel,
    receivedShotViewModel: ReceivedShotViewModel,
    blockedUserViewModel: BlockedUserViewModel,
    userWhoBlockedYouViewModel: UserWhoBlockedYouViewModel,
    dataStore: DataStore<Preferences>,
    showSnackbar: () -> Unit
) {
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    val firestore = FirebaseModule.provideFirestore()
    val firebaseStorage = FirebaseModule.provideStorage()
    val firebaseRepository =
        FirebaseModule.provideFirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    val appDatabase = RoomModule.provideAppDatabase(LocalContext.current)
    val editProfileViewModel =
        ViewModelModule.provideEditProfileViewModel(firebaseRepository, firebaseAuth)
    val userDao = RoomModule.provideUserDao(appDatabase)
    val bookmarkDao = RoomModule.provideBookmarkDao(appDatabase)
    val sentLikeDao = RoomModule.provideSentLikeDao(appDatabase)
    val receivedLikeDao = RoomModule.provideReceiveLikeDao(appDatabase)
    val sentShotDao = RoomModule.provideSentShotDao(appDatabase)
    val receivedShotDao = RoomModule.provideReceiveShotDao(appDatabase)
    val context = LocalContext.current
    var currentCard by rememberSaveable { mutableIntStateOf(0) }
    var isBookmarked by remember { mutableStateOf(false) }
    var wasClicked by remember { mutableStateOf(false) }
    var user by remember { mutableStateOf<User?>(null) }
//    val cards: List<User> = listOf(
//        User(
//            "0",
//            "Rachel",
//            Calendar.getInstance()
//                .apply { set(1997, Calendar.NOVEMBER, 16) }, // Set birthday using Calendar
//            R.drawable.andre_sebastian_3_i3gxwldew_unsplash,
//            Uri.parse("android.resource://" + context.packageName + "/" + R.raw.oliviaprofilevideo),
//            "if you ain't got money, wut iz we tawkin 4?"
//
//        ),
//        User(
//            "1",
//            "Bethany",
//            Calendar.getInstance()
//                .apply { set(1993, Calendar.JANUARY, 16) }, // Set birthday using Calendar
//            R.drawable.rafaella_mendes_diniz_aol_mvxprmk_unsplash, null, "... be shopping"
//        ),
//        User(
//            "2",
//            "Thomasina",
//            Calendar.getInstance()
//                .apply { set(2000, Calendar.MAY, 15) }, // Set birthday using Calendar
//            R.drawable.ayo_ogunseinde_6w4f62sn_yi_unsplash, null, "Don't ask me"
//        )
//        // Add more users as needed
//    )
    var cards by remember { mutableStateOf(usersViewModel.fetchAllNonBlockedUsersFromRoom()) }
    val scope = rememberCoroutineScope()

    val shuffleCards: () -> Unit = {
        scope.launch {
            withContext(Dispatchers.Default) {
                val shuffledCards = cards.shuffled()
                withContext(Dispatchers.Main) {
                    cards = shuffledCards
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            withContext(Dispatchers.IO) {
                user = usersViewModel.getUser()


//                val currentId = firebaseAuth.currentUser?.displayName ?: ""
//                val blockedCards = blockedUserViewModel.getBlockedUsersFromFirebase(user?.id ?: "")
//                val usersWhoBlockedYouCards = userWhoBlockedYouViewModel.getUserWhoBlockedYouFromFirebase(user?.id ?: "")

//                cards = usersViewModel.fetchAllNonBlockedUsersFromRoom().reversed()

                //the below is to be used if calling fetchAllUsersFromRoom()
//                    .filter { user ->
//                        !blockedCards.contains(user.id ?: "")
//                    }
//                    .filter { user ->
//                        !usersWhoBlockedYouCards.contains(user.id ?: "")
//                    }


//                cards = cards.filter { card ->
//                    card.id != currentId
//                }
            }
        }
    }

    user = usersViewModel.getUser()

//    if (user?.displayName.isNullOrBlank() ||
//        user?.mediaOne.isNullOrBlank() ||
//        user?.mediaProfileVideo.isNullOrBlank() ||
//        user?.gender == Gender.UNKNOWN
//    ) {
//        Toast.makeText(context, "Please complete all required fields first.", Toast.LENGTH_SHORT)
//            .show()
//    }


//    if (cards.isEmpty()) {
//        // Content of the draggable component
//        // Content for each page
//        // This lambda will be called for each page index
//        // You can provide different content for each page based on the index
//
//        Card(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(8.dp)
//                .clickable {
//                },
////            elevation = CardDefaults.cardElevation(
////                defaultElevation = 8.dp
////            )
//        ) {
//            Box {
//                // Display card content
//                Box(modifier = Modifier
//                    .fillMaxSize()
//                    .clickable {
//                    }) {
//                    Icon(
//                        painterResource(id = R.drawable.groups_24px),
//                        "No Groups Icon",
//                        modifier = Modifier
//                            .height(280.dp)
//                            .width(280.dp)
//                            .align(Alignment.TopCenter)
//                            .padding(0.dp, 200.dp, 0.dp, 0.dp)
//                    )
//                    Text(
//                        "No user cards are currently available",
//                        fontSize = 24.sp,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier
//                            .align(Alignment.Center)
//                            .padding(8.dp, 80.dp, 8.dp, 0.dp)
//                    )
//                }
//            }
//        }
//
////        navController.navigate("users")
//
//
//    } else {
    // Define the number of pages
    val pageCount = 5

// Remember the pager state
    val pagerState = com.google.accompanist.pager.rememberPagerState(pageCount)
    val density = LocalDensity.current
    // Content to display on each page
    AnchoredDraggableState(
        initialValue = 0,
        positionalThreshold = { totalDistance: Float -> totalDistance * 0.5f },
        velocityThreshold = { with(density) { 100.dp.toPx() } },
        animationSpec = tween(),
    ).apply {
        updateAnchors(
            newAnchors = DraggableAnchors {
                with(density) {
                    0 at 0.dp.toPx()
                    100 at 100.dp.toPx()
                }
            }
        )
    }


    val state = rememberPagerState(currentCard) { cards.size }
    VerticalPager(
        state,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 56.dp, bottom = 56.dp)
    ) {
        var yourBookmarks: List<String> by remember { mutableStateOf(emptyList()) }
        val currentUser = cards[it]

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                yourBookmarks =
                    bookmarkViewModel.fetchBookmarkFromRoom(user?.id ?: "").bookmarks
            }
        }

//        LaunchedEffect(Unit) {
//            scope.launch {
//                withContext(Dispatchers.IO) {
//                    val TAG = "LoginScreen"
//                    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
//                    val userId = firebaseAuth.currentUser?.email ?: ""
//                    user = usersViewModel.getUserDataFromRepo(userId)
//                    usersViewModel.userDao.updateAll(usersViewModel.getUsersFromRepo())
//                    Log.d(TAG, "User - $user")
//                    Log.d(TAG, "UserId - $userId")
////                    if (user != null) {
////                        usersViewModel.userDao.update(user!!)
////                    } else {
////                        user = usersViewModel.getInitialUser()
////                        user = user!!.copy(id = userId)
////                        usersViewModel.userDao.update(user!!)
////                    }
////                    val bookmark = Bookmark(
////                        userId,
////                        bookmarkViewModel.getBookmarksFromFirebase(userId)
////                            .toMutableList()
////                    )
////                    bookmarkDao.update(bookmark)
////                    val sentLike = SentLike(
////                        userId,
////                        sentLikeViewModel.getSentLikesFromFirebase(userId)
////                            .toMutableList()
////                    )
////                    sentLikeDao.update(sentLike)
////                    val receivedLike = ReceivedLike(
////                        userId,
////                        receivedLikeViewModel.getReceivedLikesFromFirebase(userId)
////                            .toMutableList()
////                    )
////                    receivedLikeDao.update(receivedLike)
////                    val sentShot = SentShot(
////                        userId,
////                        sentShotViewModel.getSentShotsFromFirebase(userId)
////                            .toMutableList()
////                    )
////                    sentShotDao.update(sentShot)
////                    val receivedShot = ReceivedShot(
////                        userId,
////                        receivedShotViewModel.getReceivedShotsFromFirebase(userId)
////                            .toMutableList()
////                    )
////                    receivedShotDao.update(receivedShot)
//                }
//            }
//        }

//        LaunchedEffect(Unit) {
//            scope.launch {
//                withContext(Dispatchers.IO) {
        val userId = firebaseAuth.currentUser?.displayName ?: ""
//                    Log.d(TAG, "user id - $userId - ${firebaseAuth.currentUser?.email}")
//                    try {
//                        val allBookmarks = bookmarkDao.getAll()
//                        val retrievedBookmarks = bookmarkViewModel.getBookmarksFromFirebase(userId)
//                        Log.d(TAG, "Retrieved bookmarks - $retrievedBookmarks")
//
//                        val bookmark = Bookmark(userId, retrievedBookmarks.toMutableList())
//
//                        val bookmarkIdToCheck = bookmark.bookmarkId
//
//                        val isBookmarkIdContained =
//                            allBookmarks.any { it.bookmarkId == bookmarkIdToCheck }
//
//                        if (isBookmarkIdContained) {
//                            // Bookmark ID is already present, perform update
//                            bookmarkDao.update(bookmark)
//                        } else {
//                            // Bookmark ID is not present, perform insert
//                            bookmarkDao.insert(bookmark)
//                        }
//        yourBookmarks = bookmarkViewModel.fetchBookmarkFromRoom()
//                        Log.d(TAG, "Current bookmarks - $yourBookmarks")
//                    } catch (npe: NullPointerException) {
//                        Log.d(TAG, "No bookmarks yet")
//                    }
//
//                    try {
//                        val allSentLikes = sentLikeDao.getAll()
//                        val retrievedSentLikes = sentLikeViewModel.getSentLikesFromFirebase(userId)
//                        Log.d(TAG, "Retrieved bookmarks - $retrievedSentLikes")
//
//                        val sentLike = SentLike(userId, retrievedSentLikes.toMutableList())
//
//                        val sentLikeIdToCheck = sentLike.sentLikeId
//
//                        val isSentLikeIdContained =
//                            allSentLikes.any { it.sentLikeId == sentLikeIdToCheck }
//
//                        if (isSentLikeIdContained) {
//                            // sentLike ID is already present, perform update
//                            sentLikeDao.update(sentLike)
//                        } else {
//                            // sentLike ID is not present, perform insert
//                            sentLikeDao.insert(sentLike)
//                        }
////                        yourBookmarks = bookmarkDao.findById(userId).bookmarks
////                        Log.d(TAG, "Current bookmarks - $yourBookmarks")
//                    } catch (npe: NullPointerException) {
//                        Log.d(TAG, "No sentLikes yet")
//                    }
//
//                    try {
//                        val allReceivedLikes = receivedLikeDao.getAll()
//                        val retrievedReceivedLikes =
//                            receivedLikeViewModel.getReceivedLikesFromFirebase(userId)
//                        Log.d(TAG, "Retrieved receivedLikes - $retrievedReceivedLikes")
//
//                        val receivedLike =
//                            ReceivedLike(userId, retrievedReceivedLikes.toMutableList())
//
//                        val receivedLikeIdToCheck = receivedLike.receivedLikeId
//
//                        val isReceivedLikeIdContained =
//                            allReceivedLikes.any { it.receivedLikeId == receivedLikeIdToCheck }
//
//                        if (isReceivedLikeIdContained) {
//                            // receivedLike ID is already present, perform update
//                            receivedLikeDao.update(receivedLike)
//                        } else {
//                            // receivedLike ID is not present, perform insert
//                            receivedLikeDao.insert(receivedLike)
//                        }
////                        yourBookmarks = bookmarkDao.findById(userId).bookmarks
////                        Log.d(TAG, "Current bookmarks - $yourBookmarks")
//                    } catch (npe: NullPointerException) {
//                        Log.d(TAG, "No receivedLikes yet")
//                    }
//
//                }
//            }
////
//        }


        val navToUserProfile: () -> Unit = {
            scope.launch {
                withContext(Dispatchers.Main) {
                    navController.navigate("userProfile/${cards[it].id}") {
//                        launchSingleTop = true
//                        popUpTo(navController.graph.startDestinationId) {
//                            saveState = true
//                        }
                    }
                }
            }
        }

//        AnchoredDraggableState(
//            initialValue = 0,
//            positionalThreshold = { totalDistance: Float -> totalDistance * 0.5f },
//            velocityThreshold = { with(density) { 100.dp.toPx() } },
//            animationSpec = tween(),
//        ).apply {
//            updateAnchors(
//                newAnchors = DraggableAnchors {
//                    with(density) {
//                        0 at 0.dp.toPx()
//                        100 at 100.dp.toPx()
//                    }
//                }
//            )
//        }
        // Content of the draggable component
        // Content for each page
        // This lambda will be called for each page index
        // You can provide different content for each page based on the index
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clickable {
                    scope.launch {
                        withContext(Dispatchers.Main) {
                            navController.navigate("userProfile/${currentUser.id}") {
                                launchSingleTop = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        }
                    }
                },
//            elevation = CardDefaults.cardElevation(
//                defaultElevation = 8.dp
//            )
        ) {
            Box {
                // Display card content
                val contrast = 0.8f // 0f..10f (1 should be default)
                val brightness = 0f // -255f..255f (0 should be default)
                val colorMatrix = floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness,
                    0f, contrast, 0f, 0f, brightness,
                    0f, 0f, contrast, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f
                )
                if (currentUser.typeOfMediaOne == TypeOfMedia.VIDEO) {
                    ProfileUserCardDisplay(currentUser, showSnackbar, navToUserProfile)
                } else {
                    if (!currentUser.mediaOne.isNullOrBlank()) {
                        GlideImage(
                            currentUser.mediaOne,
                            "User's card for dating profile: ${currentUser.displayName}\"",
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    navToUserProfile()
                                },
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.colorMatrix(ColorMatrix(colorMatrix))
                        )
                    } else {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                navToUserProfile()
                            }) {
                            Icon(
                                painterResource(id = R.drawable.no_image_svgrepo_com),
                                "No Video Icon",
                                modifier = Modifier
                                    .height(280.dp)
                                    .width(280.dp)
                                    .align(Alignment.TopCenter)
                                    .padding(0.dp, 200.dp, 0.dp, 0.dp)
                            )
                            Text(
                                user?.displayName + " has no profile image. ",
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(8.dp, 80.dp, 8.dp, 0.dp)
                            )
                        }
                    }
                }

//                Image(
//                    painter = painterResource(),
//                    contentDescription = "User's card for dating profile: ${user.name}",
//                    modifier = Modifier
//                        .fillMaxSize(),// Adjust the aspect ratio as needed
//                    contentScale = ContentScale.Crop, // Ensure the image fills the available space without distorting its aspect ratio
//                    colorFilter = ColorFilter.colorMatrix(ColorMatrix(colorMatrix))
//                )
                Box(modifier = Modifier.fillMaxSize()) {
                    Icon(
                        painter = painterResource(id = R.drawable.shuffle_black_24dp),
                        "Shuffle Icon",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .height(40.dp)
                            .width(40.dp)
                            .clickable {
                                shuffleCards()
                            },
                        tint = Color.White
                    )
                    Icon(
                        painter = if (yourBookmarks.contains(currentUser.id)) {
                            painterResource(id = R.drawable.baseline_bookmark_24)
                        } else {
                            painterResource(id = R.drawable.bookmark_24px)
                        },
                        contentDescription = "Bookmark Icon",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .height(40.dp)
                            .width(40.dp)
                            .clickable {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        isBookmarked = !isBookmarked
                                        wasClicked = true
                                        val bookmarkData: MutableMap<String, Any> =
                                            mutableMapOf()
                                        Log.d("UsersScreen", "isBookmarked - $isBookmarked")
                                        Log.d("UsersScreen", "wasClicked - $wasClicked")
                                        bookmarkData["bookmark-${currentUser.id}"] =
                                            currentUser.id
                                        if (isBookmarked && wasClicked) {
                                            if (!yourBookmarks.contains(currentUser.id)) {
                                                if (bookmarkViewModel.saveBookmarkToFirebase(
                                                        currentUser.id,
                                                        bookmarkData, context
                                                    )
                                                ) {
                                                    val bookmarks =
                                                        bookmarkViewModel
                                                            .getBookmarksFromFirebase(
                                                                firebaseAuth.currentUser?.displayName
                                                                    ?: ""
                                                            )

                                                    val bookmark =
                                                        Bookmark(
                                                            firebaseAuth.currentUser?.displayName
                                                                ?: "",
                                                            bookmarks.toMutableList()
                                                        )

                                                    try {
                                                        bookmarkDao.insert(bookmark)
                                                    } catch (npe: java.lang.NullPointerException) {
                                                        bookmarkDao.insert(bookmark)
                                                    }
                                                    yourBookmarks = bookmark.bookmarks
                                                }
                                            }
                                            wasClicked = false
                                        } else if (!isBookmarked && wasClicked) {

                                            if (bookmarkViewModel.removeBookmarkFromFirebase(
                                                    currentUser.id, context
                                                )
                                            ) {
                                                val bookmarks =
                                                    bookmarkViewModel
                                                        .getBookmarksFromFirebase(
                                                            firebaseAuth.currentUser?.displayName
                                                                ?: ""
                                                        )

                                                val bookmark =
                                                    Bookmark(
                                                        firebaseAuth.currentUser?.displayName
                                                            ?: "",
                                                        bookmarks.toMutableList()
                                                    )

                                                try {
                                                    bookmarkDao.insert(bookmark)
                                                } catch (npe: java.lang.NullPointerException) {
                                                    bookmarkDao.insert(bookmark)
                                                }
                                                yourBookmarks = bookmark.bookmarks
                                            }

                                            wasClicked = false
                                        }
//                                        val retrievedBookmarks: List<String> =
//                                            bookmarkViewModel.getBookmarksFromFirebase(
//                                                firebaseAuth.currentUser?.displayName ?: ""
//                                            )
//                                        Log.d(TAG, "The retrievedBookmarks - $retrievedBookmarks")
//                                        val userId = firebaseAuth.currentUser?.displayName
//                                        if (!userId.isNullOrBlank()) {
//                                            var bookmark = bookmarkDao.findById(userId)
//                                            if (bookmark != null) {
//                                                bookmark =
//                                                    bookmark.copy(bookmarks = retrievedBookmarks.toMutableList())
//                                                bookmarkDao.update(bookmark)
//                                            } else {
//                                                val newBookmark =
//                                                    Bookmark(
//                                                        userId,
//                                                        retrievedBookmarks.toMutableList()
//                                                    )
//                                                bookmarkDao.insert(newBookmark)
//                                            }
//                                        }
                                    }
                                }
                            },
                        tint = Color.White
                    )
                    Column(
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomStart)
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp), // Add bottom padding to create space between the two text rows
                            text = "${currentUser.displayName}, ${
                                currentUser.age
                            }",
                            fontSize = 32.sp,
                            style = TextStyle(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "Location Icon",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.dp) // Set the size of the icon to match the font size of the text
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val yourLatitude = user?.latitude ?: 0.0
                            val yourLongitude = user?.longitude ?: 0.0
                            val thisLatitude = currentUser.latitude ?: 0.0
                            val thisLongitude = currentUser.longitude ?: 0.0
                            val distance = locationViewModel.calculateDistance(
                                yourLatitude,
                                yourLongitude,
                                thisLatitude,
                                thisLongitude
                            )
                            Text(
                                text = "${distance.toInt()} miles away",
                                fontSize = 16.sp,
                                style = TextStyle(fontWeight = FontWeight.Medium),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }


}


data class User(
    val id: String,
    val name: String,
    val birthday: Calendar,
    val imageResourceId: Int,
    val verificationVideoUri: Uri?,
    val aboutMe: String // Additional field for about me text
) {
    val age: Int
        get() {
            val today = Calendar.getInstance() // Get current date
            val dob = birthday // Get the user's birthday
            var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age-- // If the current date is before the user's birthday, subtract 1 from age
            }
            return age
        }
}


//@Preview
//@Composable
//fun UsersScreenPreview() {
//    val navController = rememberNavController()
//    UsersScreen(navController = navController)
//}