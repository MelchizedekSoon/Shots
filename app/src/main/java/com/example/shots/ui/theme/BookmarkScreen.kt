package com.example.shots.ui.theme

import android.content.ContentValues.TAG
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.shots.DialogExamples
import com.example.shots.DialogUtils
import com.example.shots.R
import com.example.shots.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ShowDialog() {
    DialogExamples()
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    navController: NavController, userViewModel: UserViewModel,
    bookmarkViewModel: BookmarkViewModel,
    locationViewModel: LocationViewModel
) {

    var hasBeenClicked by remember { mutableStateOf(false) }
    var isShowingBookmarkDialog by rememberSaveable { mutableStateOf(false) }

    val user by userViewModel.user.collectAsState()

    val bookmarkUiState by bookmarkViewModel.uiState.collectAsState()

    var bookmarks by remember { mutableStateOf(bookmarkUiState.bookmarks) }

    Log.d("BookmarkScreen", "bookmarks = $bookmarks")

//    LaunchedEffect(bookmarkUiState, Unit) {
//        withContext(Dispatchers.IO) {
//
//            val userData: MutableMap<String, Any> = mutableMapOf()
//            val mediaItems: MutableMap<String, Uri> = mutableMapOf()
//
//            userData["timesBookmarkedCount"] = 0
//
//            userViewModel.saveAndStoreData(user?.id ?: "",
//                userData, mediaItems, context) {
//            }
//
//        }
//    }


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
            BottomBar(navController = navController, userViewModel)
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

            if (bookmarkUiState.isLoading) {
                Loader()
            } else if (bookmarkUiState.errorMessage?.isNotEmpty() == true) {

            } else if (bookmarkUiState.bookmarks.isNotEmpty()) {

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp, 0.dp, 8.dp, 64.dp)
                ) {

                    val contrast = 0.8f // 0f..10f (1 should be default)
                    val brightness = 0f // -255f..255f (0 should be default)
                    val colorMatrix = floatArrayOf(
                        contrast, 0f, 0f, 0f, brightness,
                        0f, contrast, 0f, 0f, brightness,
                        0f, 0f, contrast, 0f, brightness,
                        0f, 0f, 0f, 1f, 0f
                    )

                    items(bookmarks.size) { index ->
                        var currentUser by remember { mutableStateOf<User?>(null) }
                        var isBookmarked by remember {
                            mutableStateOf(true)
                        }
                        val hasConfirmedRemoval by remember {
                            mutableStateOf(false)
                        }
                        if (!isBookmarked) {
                            Log.d("BookmarkScreen", "Bookmark is now - $isBookmarked")
                            val isConfirmed = DialogUtils.bookmarkRemovalDialog { confirmed ->
                                if (!confirmed) {
                                    isBookmarked = !isBookmarked
                                }
                                // Callback function called on dialog dismissal
                                isShowingBookmarkDialog = false
                                // Use the `confirmed` value if needed
                                Log.d("BookmarkScreen", "Dialog dismissed, confirmed: $confirmed")
                            }
                            if (isConfirmed) {
                                bookmarkViewModel.removeBookmark(currentUser?.id ?: "")
                                LaunchedEffect(bookmarkUiState.bookmarks) {
                                    bookmarks = bookmarkUiState.bookmarks
                                    navController.navigate("bookmark")
                                }
                            }
                        }

                        LaunchedEffect(Unit) {
                            withContext(Dispatchers.IO) {
                                val currentUserId = bookmarks[index]
                                try {
                                    if (currentUserId.isNotBlank()) {
                                        currentUser =
                                            userViewModel.fetchUserFromRoom(currentUserId)
                                    }
                                } catch (npe: java.lang.NullPointerException) {
                                    Log.d("BookmarkScreen", "No bookmarks yet")
                                } catch (ioe: IndexOutOfBoundsException) {
                                    Log.d("BookmarkScreen", "Index out of bounds")
                                }
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
                                        colorFilter = ColorFilter.colorMatrix(
                                            ColorMatrix(
                                                colorMatrix
                                            )
                                        )
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

                            val painter = painterResource(id = R.drawable.baseline_bookmark_24)

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
                                            .size(8.dp)
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


            } else {


                Box(
                    modifier = Modifier
                        .padding(100.dp)
                        .fillMaxSize()
                ) {
                    Icon(
                        painterResource(id = R.drawable.bookmark_24px),
                        "Bookmark Icon",
                        modifier = Modifier
                            .height(240.dp)
                            .width(240.dp)
                            .align(Alignment.TopCenter)
                            .padding(0.dp, 40.dp, 0.dp, 0.dp)
                    )

                    Text(
                        "No Bookmarks",
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(0.dp, 40.dp, 0.dp, 0.dp)
                    )

                }


            }


        }
    }


}
