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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.shots.FirebaseModule
import com.example.shots.R
import com.example.shots.RoomModule
import com.example.shots.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShotsScreen(
    navController: NavController, usersViewModel: UsersViewModel,
    sentShotViewModel: SentShotViewModel, receivedShotViewModel: ReceivedShotViewModel,
    locationViewModel: LocationViewModel
) {
    val appDatabase = RoomModule.provideAppDatabase(LocalContext.current)
    val userDao = RoomModule.provideUserDao(appDatabase)
    val receivedShotDao = RoomModule.provideReceiveShotDao(appDatabase)
    val sentShotDao = RoomModule.provideSentShotDao(appDatabase)
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    var user by remember { mutableStateOf<User?>(null) }
    val sentShotsCurrentUserIdList = mutableListOf<String>()
    val sentShotsCurrentUserShotList = mutableListOf<String>()
    var sentShotsList by remember { mutableStateOf<List<String>?>(null) }
    var sentShotsPairList by remember {
        mutableStateOf<MutableList<Pair<User?, String>>>(
            mutableListOf()
        )
    }
    var receivedShotsList by remember { mutableStateOf<List<String>?>(null) }
    var receivedShotsPairList by remember {
        mutableStateOf<MutableList<Pair<User?, String>>>(
            mutableListOf()
        )
    }
    var receivedShotMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var sentShotMap by remember { mutableStateOf<MutableMap<String, String>>(mutableMapOf()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isOnReceived by rememberSaveable {
        mutableStateOf(true)
    }
    val isPlayingReceivedList = remember { mutableStateListOf<Boolean>() }
    val isPlayingSentList = remember { mutableStateListOf<Boolean>() }

    var returnedShotsListValue by remember { mutableStateOf("") }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var currentUserId: String by remember { mutableStateOf("") }
    var currentUserShot: String by remember { mutableStateOf("") }
    val showSnackbar: () -> Unit = {
        scope.launch {
        }
    }



    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {

                val userData: MutableMap<String, Any> = mutableMapOf()
                val mediaItems: MutableMap<String, Uri> = mutableMapOf()

                userData["newShotsCount"] = 0

                usersViewModel.
                saveUserDataToFirebase(firebaseAuth.currentUser?.displayName ?: "",
                    userData,
                    mediaItems,
                    context) {

                }

                val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
                Log.d("ShotsScreen", "We're after userId")

                user = usersViewModel.getUser()


                /**
                 * if the below two lines of code are grayed out
                 * and issues arise with this screen
                 * uncomment code out
                 */

//                receivedShotViewModel.storeReceivedShotInRoom(context, userId)

//                sentShotViewModel.storeSentShotInRoom(userId)

                receivedShotsList =
                    receivedShotViewModel.fetchReceivedShotFromRoom(yourUserId).receivedShots
                        .filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()

                (receivedShotsList as MutableList<String>).forEach { returnedShotsListValue ->
                    currentUserId = returnedShotsListValue.substringBefore("-")
                    currentUserShot = returnedShotsListValue.substringAfter("-")
                    currentUser = usersViewModel.fetchUserFromRoom(currentUserId)
                    val pair: Pair<User?, String> = currentUser to currentUserShot
                    receivedShotsPairList.add(pair)
                    Log.d("ShotsScreen", "receivedShotsList = $receivedShotsList")
                    Log.d("ShotsScreen", "receivedShotsPairList = $receivedShotsPairList")
                }

                for (i in 0 until receivedShotsPairList.size) {
                    isPlayingReceivedList.add(i, false)
                }
//                receivedShotsList = try {
//                    receivedShotDao.findById(userId).receivedShots
//                } catch (npe: java.lang.NullPointerException) {
//                    mutableListOf()
//                }.filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()

                sentShotsList = sentShotViewModel.fetchSentShotFromRoom(yourUserId).sentShots
                    .filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()

                (sentShotsList as MutableList<String>).forEach { returnedShotsListValue ->
                    currentUserId = returnedShotsListValue.substringBefore("-")
                    currentUserShot = returnedShotsListValue.substringAfter("-")
                    currentUser = usersViewModel.fetchUserFromRoom(currentUserId)
                    val pair: Pair<User?, String> = currentUser to currentUserShot
                    sentShotsPairList.add(pair)
                    Log.d("ShotsScreen", "sentShotsList = $sentShotsList")
                    Log.d("ShotsScreen", "sentShotsPairList = $sentShotsPairList")
                }

                for (i in 0 until sentShotsPairList.size) {
                    isPlayingSentList.add(i, false)
                }

//                sentShotsList = try {
//                    sentShotDao.findById(userId).sentShots
//                } catch (npe: java.lang.NullPointerException) {
//                    mutableListOf()
//                }.filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()


//                receivedShotsList =
//                    receivedShotViewModel.fetchReceivedShotFromRoom(userId).receivedShots
//
//                Log.d("ShotsScreen", "The receivedShotsList is $receivedShotsList")
//
//                sentShotsList = sentShotViewModel.fetchSentShotFromRoom(userId).sentShots
//
//                Log.d("ShotsScreen", "The sentShotsList is $sentShotsList")
//
//
//                Log.d("ShotsScreen", "The sentShotsList is $sentShotsList")
//                Log.d("ShotsScreen", "The receivedShotsList is $receivedShotsList")

                var prevReceivedShotSize = 0

            } catch (npe: NullPointerException) {
                Log.d("ShotsScreen", "Something ended up null")
                Log.d("ShotsScreen", "The user on shotsScreen is $user")
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
                text = "Shots", fontSize = 24.sp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 0.dp, 0.dp, 16.dp), horizontalArrangement =
                Arrangement.Center
            ) {
                Text(
                    text = "Received",
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            isOnReceived = true
                        },
                    textAlign = TextAlign.Center,
                    color = if (isOnReceived) Color(0xFFFF6F00) else Color.Black
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Sent",
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            isOnReceived = false
                        },
                    textAlign = TextAlign.Center,
                    color = if (!isOnReceived) Color(0xFFFF6F00) else Color.Black
                )
                Spacer(Modifier.height(16.dp))
            }
            if (!isOnReceived) {

                if(sentShotsPairList.size < 1) {
                    return@Column
                }

                // grid for sent shots

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = rememberLazyGridState(),
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

                    items(sentShotsPairList.size) { index ->

                        if (index >= sentShotsPairList.size) {
                            // Index is out of bounds, handle the case as needed
                            return@items
                        }

                        val localCurrentUser = sentShotsPairList[index].first
                        val localCurrentUserShot = sentShotsPairList[index].second


                        Box(modifier = Modifier.clickable {
                            isPlayingSentList[index] = !isPlayingSentList[index]
                            //play video
                        }) {
                            if (index >= sentShotsPairList.size) {
                                // Index is out of bounds, handle the case as needed
                                return@items
                            }
                            Log.d(TAG, "isPlaying = ${isPlayingSentList[index]}")
                            if (isPlayingSentList[index]) {
                                val encodedUrl = URLEncoder.encode(
                                    localCurrentUserShot,
                                    StandardCharsets.UTF_8.toString()
                                )
                                val sent = "sent"
                                navController.navigate("playShot/${localCurrentUser?.id}/$encodedUrl/$sent")
                            } else {
                                Card(modifier = Modifier.height(240.dp)) {
                                    GlideImage(
                                        model = localCurrentUserShot.toUri(),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        contentDescription = "shot for shots user",
                                        colorFilter = ColorFilter.colorMatrix(
                                            ColorMatrix(
                                                colorMatrix
                                            )
                                        )
                                    )
                                }

                                GlideImage(
                                    model = localCurrentUser?.mediaOne,
                                    contentDescription = "User's profile photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .height(72.dp)
                                        .width(72.dp)
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                        .align(Alignment.TopStart)
                                        .clickable {
                                            navController.navigate("userProfile/${localCurrentUser?.id}")
                                        }

                                )

                                Column(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .align(Alignment.BottomEnd)
                                ) {

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {

                                        Text(
                                            modifier = Modifier.padding(bottom = 8.dp), // Add bottom padding to create space between the two text rows
                                            text = "${localCurrentUser?.displayName}, ${
                                                localCurrentUser?.birthday?.div(
                                                    31556952000
                                                )
                                            }",
                                            fontSize = 16.sp,
                                            style = TextStyle(
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = Color.White
                                        )
                                    }

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
                                        val thisLatitude = localCurrentUser?.latitude ?: 0.0
                                        val thisLongitude = localCurrentUser?.longitude ?: 0.0
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
            } else {

                if(receivedShotsPairList.size < 1) {
                    return@Column
                }

                // grid for received likes
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
                    items(receivedShotsPairList.size) { index ->

                        if (index >= receivedShotsPairList.size) {
                            // Index is out of bounds, handle the case as needed
                            return@items
                        }

                        val localCurrentUser = receivedShotsPairList[index].first
                        val localCurrentUserShot = receivedShotsPairList[index].second

                        Box(modifier = Modifier.clickable {
                            isPlayingReceivedList[index] = !isPlayingReceivedList[index]
                            Log.d(TAG, "isPlaying = ${isPlayingReceivedList[index]}")
                            //play video
                        }) {
                            Log.d(TAG, "isPlaying = ${isPlayingReceivedList[index]}")
                            if (isPlayingReceivedList[index]) {
                                val encodedUrl = URLEncoder.encode(
                                    localCurrentUserShot,
                                    StandardCharsets.UTF_8.toString()
                                )
                                val received = "received"
                                navController.navigate("playShot/${localCurrentUser?.id}/$encodedUrl/$received")
                            } else {
                                Card(modifier = Modifier.height(240.dp)) {
                                    GlideImage(
                                        model = localCurrentUserShot.toUri(),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        contentDescription = "shot for shots user",
                                        colorFilter = ColorFilter.colorMatrix(
                                            ColorMatrix(
                                                colorMatrix
                                            )
                                        )
                                    )
                                }

                                GlideImage(
                                    model = localCurrentUser?.mediaOne,
                                    contentDescription = "User's profile photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .height(72.dp)
                                        .width(72.dp)
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                        .align(Alignment.TopStart)
                                        .clickable {
                                            navController.navigate("userProfile/${localCurrentUser?.id}")
                                        }
                                )

                                Column(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .align(Alignment.BottomEnd)
                                ) {
                                    Text(
                                        modifier = Modifier.padding(bottom = 8.dp), // Add bottom padding to create space between the two text rows
                                        text = "${localCurrentUser?.displayName}, ${
                                            localCurrentUser?.birthday?.div(
                                                31556952000
                                            )
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
                                        val thisLatitude = localCurrentUser?.latitude ?: 0.0
                                        val thisLongitude = localCurrentUser?.longitude ?: 0.0
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
    }
}