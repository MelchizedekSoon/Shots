package com.example.shots.ui.theme

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.shots.DialogExamples
import com.example.shots.DialogUtils
import com.example.shots.FirebaseModule
import com.example.shots.R
import com.example.shots.RoomModule
import com.example.shots.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ShowDialog() {
    DialogExamples()
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    navController: NavController, usersViewModel: UsersViewModel,
    bookmarkViewModel: BookmarkViewModel,
    locationViewModel: LocationViewModel
) {
    val appDatabase = RoomModule.provideAppDatabase(LocalContext.current)
    val userDao = RoomModule.provideUserDao(appDatabase)
    val bookmarkDao = RoomModule.provideBookmarkDao(appDatabase)
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    var bookmarkList by remember { mutableStateOf<List<String>?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasBeenClicked by remember { mutableStateOf(false) }
    var isShowingBookmarkDialog by rememberSaveable { mutableStateOf(false) }
    var user: User? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {

            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()

            userData["timesBookmarkedCount"] = 0

            usersViewModel.saveUserDataToFirebase(
                firebaseAuth.currentUser?.displayName ?: "",
                userData,
                mediaItems,
                context
            ) {

            }

            user = usersViewModel.getUser()
            val userId = user?.id
            try {
                if (userId != null) {
                    bookmarkList = bookmarkDao.findById(userId).bookmarks
                        .filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()
                }
                Log.d("BookmarkScreen", "BookmarkList - $bookmarkList")
            } catch (npe: NullPointerException) {
                Log.d("BookmarkScreen", "Bookmark isn't found")
            }
        }
    }
    Log.d(TAG, "The bookmarkList size - ${bookmarkList?.size} - ${bookmarkList}")
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
                        contentDescription = "Filter Icon",
                        modifier = Modifier
                            .height(32.dp)
                            .width(32.dp)
                            .padding(0.dp, 0.dp, 8.dp, 0.dp)
                            .clickable {
                                navController.navigate("filter")
                            }
                    )
                })

        },
        bottomBar = {
            BottomBar(navController = navController, usersViewModel)
        }
    ) {
        Modifier.padding(it)
        Column(
            modifier = Modifier.padding(0.dp, 64.dp, 0.dp, 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bookmarks", fontSize = 24.sp,
            )
            Spacer(Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                val contrast = 0.8f // 0f..10f (1 should be default)
                val brightness = 0f // -255f..255f (0 should be default)
                val colorMatrix = floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness,
                    0f, contrast, 0f, 0f, brightness,
                    0f, 0f, contrast, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f
                )
                items(bookmarkList?.size ?: 0) { index ->

                    var currentUser by remember { mutableStateOf<User?>(null) }
                    var isBookmarked by remember {
                        mutableStateOf(true)
                    }
                    val hasConfirmedRemoval by remember {
                        mutableStateOf(false)
                    }
                    if (!isBookmarked) {
                        Log.d("Bookmark", "Bookmark is now - $isBookmarked")
                        val isConfirmed = DialogUtils.bookmarkRemovalDialog { confirmed ->
                            if (!confirmed) {
                                isBookmarked = !isBookmarked
                            }
                            // Callback function called on dialog dismissal
                            isShowingBookmarkDialog = false
                            // Use the `confirmed` value if needed
                            Log.d(TAG, "Dialog dismissed, confirmed: $confirmed")
                        }
                        if (isConfirmed) {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val yourUserId = user?.id ?: ""
                                    val theirUserId = currentUser?.id ?: ""
                                    val isRemovedFromDB =
                                        bookmarkViewModel.removeBookmarkFromFirebase(
                                            theirUserId,
                                            context
                                        )
                                    if (isRemovedFromDB) {
                                        var bookmark = bookmarkDao.findById(yourUserId)
                                        bookmarkList =
                                            bookmarkViewModel.getBookmarksFromFirebase(yourUserId)
                                        bookmark =
                                            try {
                                                bookmark.copy(bookmarks = bookmarkList!!.toMutableList())
                                            } catch(npe: java.lang.NullPointerException){
                                                bookmark
                                            }
                                        try {
                                            //may need to be update later but changing to insert
                                            bookmarkDao.insert(bookmark)
                                        } catch (npe: java.lang.NullPointerException) {
                                            bookmarkDao.insert(bookmark)
                                        }
                                    }
                                }
                                navController.navigate("bookmark")
                            }
                        }
                    }
                    LaunchedEffect(Unit) {
                        withContext(Dispatchers.IO) {
                            val currentUserId = bookmarkList?.get(index)
                            try {
                                if (!currentUserId.isNullOrBlank()) {
                                    currentUser = userDao.findById(currentUserId)
                                    if (currentUser != null) {
                                        val yourBookmarks =
                                            bookmarkDao.findById(currentUser!!.id).bookmarks
                                        if (yourBookmarks != null) {
//                                            isBookmarked =
//                                                yourBookmarks.contains(currentUser!!.id) == true
                                        }
                                    }
                                }
                            } catch (npe: java.lang.NullPointerException) {
                                Log.d(TAG, "No bookmarks yet")
                            }
//                            if (user?.id != bookmarkList?.get(index)) {
//                                bookmarkList?.removeAt(index)
//                            }
                        }
                    }
                    Box(modifier = Modifier.clickable {
                        navController.navigate("userProfile/${currentUser?.id}") {
//                            launchSingleTop = true
//                            popUpTo(navController.graph.startDestinationId) {
//                                saveState = true
//                            }
                        }
                    }) {
                        if (!currentUser?.mediaOne.isNullOrBlank()) {
                            Card(modifier = Modifier.height(240.dp)) {
                                Log.d(TAG, "The image is ${currentUser?.mediaOne}")
                                GlideImage(
                                    model = currentUser?.mediaOne,
                                    modifier = Modifier.fillMaxSize(),
                                    contentDescription = "mediaOne for bookmarked user",
                                    contentScale = ContentScale.Crop,
                                    colorFilter = ColorFilter.colorMatrix(ColorMatrix(colorMatrix))
                                )
                            }
                        } else {
                            Card(modifier = Modifier.height(240.dp)) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        painterResource(id = R.drawable.no_image_svgrepo_com),
                                        "No Image Icon",
                                        modifier = Modifier
                                            .height(80.dp)
                                            .width(80.dp)
                                            .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                    )
                                }
                            }
                        }
                        val painter = if (!isBookmarked) {
                            if (hasConfirmedRemoval) {
                                painterResource(id = R.drawable.bookmark_24px)
                            } else {
                                painterResource(id = R.drawable.baseline_bookmark_24)
                            }
                        } else {
                            painterResource(id = R.drawable.baseline_bookmark_24)
                        }
                        Icon(
                            painter = painter,
                            "Bookmark Icon",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .height(36.dp)
                                .width(36.dp)
                                .clickable {
                                    isBookmarked = !isBookmarked
//                                scope.launch {
//                                    withContext(Dispatchers.IO) {
//                                        isBookmarked = !isBookmarked
//                                        val bookmarkData: MutableMap<String, Any> =
//                                            mutableMapOf()
//                                        bookmarkData["bookmark-${currentUser?.id}"] =
//                                            currentUser?.id ?: ""
//                                        if (isBookmarked) {
//                                            isShowingBookmarkDialog = true
//                                            currentUser?.let { it1 ->
//                                                bookmarkViewModel.saveBookmarkToFirebase(
//                                                    it1.id,
//                                                    bookmarkData
//                                                )
//                                            }
//                                        } else {
//                                            isShowingBookmarkDialog = true
//                                            Log.d(
//                                                TAG,
//                                                "isShowingBookmarkDialog - $isShowingBookmarkDialog"
//                                            )
////                                                if (hasConfirmedRemoval) {
////                                                    isBookmarked = false
////                                                    bookmarkViewModel.removeBookmarkFromFirebase(
////                                                        currentUser?.id ?: ""
////                                                    )
////                                                    hasConfirmedRemoval = false
////                                                }
//                                        }
//                                        val retrievedBookmarks: List<String> =
//                                            bookmarkViewModel.getBookmarksFromFirebase(
//                                                firebaseAuth.currentUser?.uid ?: ""
//                                            )
//                                        Log.d(
//                                            TAG,
//                                            "The retrievedBookmarks - $retrievedBookmarks"
//                                        )
//                                        val userId = firebaseAuth.currentUser?.uid
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
//                                    }
//                                }
                                },
                            tint = Color.White
                        )

                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            Text(
                                modifier = Modifier.padding(bottom = 8.dp), // Add bottom padding to create space between the two text rows
                                text = "${currentUser?.displayName}, ${
                                    currentUser?.age
                                }",
                                fontSize = 16.sp,
                                style = TextStyle(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = "Location Icon",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(8.dp) // Set the size of the icon to match the font size of the text
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val yourLatitude = user?.latitude ?: 0.0
                                val yourLongitude = user?.longitude ?: 0.0
                                val thisLatitude = currentUser?.latitude ?: 0.0
                                val thisLongitude = currentUser?.longitude ?: 0.0
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


}
