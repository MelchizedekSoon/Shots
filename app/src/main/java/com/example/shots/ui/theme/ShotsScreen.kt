package com.example.shots.ui.theme

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.shots.R
import com.example.shots.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShotsScreen(
    navController: NavController, userViewModel: UserViewModel,
    sentShotViewModel: SentShotViewModel, ifSeenReceivedShotViewModel: IfSeenReceivedShotViewModel,
    receivedShotViewModel: ReceivedShotViewModel,
    locationViewModel: LocationViewModel
) {

    sentShotViewModel.loadSentShots()
    receivedShotViewModel.loadReceivedShots()
    ifSeenReceivedShotViewModel.loadIfSeenReceivedShots()

    val sentShotUiState by sentShotViewModel.sentShotUiState.collectAsStateWithLifecycle()
    val receivedShotUiState by receivedShotViewModel.receivedShotUiState.collectAsStateWithLifecycle()
    val ifSeenReceivedShotUiState by ifSeenReceivedShotViewModel.ifSeenReceivedShotUiState.collectAsStateWithLifecycle()

    val user by userViewModel.user.collectAsState()

//    Log.d("ShotsScreen", "user = ${user?.id}")

    val sentShotsPairList by remember {
        mutableStateOf<MutableList<Pair<User?, String>>>(
            mutableListOf()
        )
    }

    val receivedShotsPairList by remember {
        mutableStateOf<MutableList<Pair<User?, String>>>(
            mutableListOf()
        )
    }

    val ifSeenReceivedShotData: MutableMap<String, Boolean> = mutableMapOf()
    val userData: MutableMap<String, Any> = mutableMapOf()
    val mediaItems: MutableMap<String, Uri> = mutableMapOf()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isOnReceived by rememberSaveable {
        mutableStateOf(true)
    }
    val isPlayingReceivedList = remember { mutableStateListOf<Boolean>() }
    val isPlayingSentList = remember { mutableStateListOf<Boolean>() }

    var returnedShotsListValue by remember { mutableStateOf("") }
    val showSnackbar: () -> Unit = {
        scope.launch {
        }
    }

    LaunchedEffect(receivedShotUiState, sentShotUiState) {
        withContext(Dispatchers.IO) {
            try {

                Log.d(
                    "ShotsScreen", "receivedShots = " +
                            "${receivedShotUiState} - ${receivedShotUiState.receivedShots.size}"
                )

                Log.d("ShotsScreen", "sentShots = ${sentShotUiState.sentShots.size}")

                sentShotsPairList.clear()
                receivedShotsPairList.clear()

                userData["newShotsCount"] = 0

                userViewModel.saveAndStoreData(
                    user?.id ?: "", userData, mediaItems,
                    context
                ) {}

                /**
                 * if the below two lines of code are grayed out
                 * and issues arise with this screen
                 * uncomment code out
                 */

                receivedShotUiState.receivedShots.forEach { receivedShotsValue ->
                    if (receivedShotsValue.isNotEmpty()) {
                        Log.d("ShotsScreen", "receivedShotsValue = $receivedShotsValue")
                        val currentUserId = receivedShotsValue.substringBefore("-")
                        val currentUserShot = receivedShotsValue.substringAfter("-")

                        // Fetch user, handling potential null
                        val currentUser = userViewModel.fetchUserFromRoom(currentUserId)

                        // Only add pair if both user and shot are non-null
                        if (currentUserShot.isNotBlank()) {
                            val pair = currentUser to currentUserShot
                            receivedShotsPairList.add(pair)
                        }
                    }
                }

                for (i in 0 until receivedShotsPairList.size) {
                    isPlayingReceivedList.add(i, false)
                }

                sentShotUiState.sentShots.forEach { sentShotsValue ->
                    if (sentShotsValue.isNotEmpty()) {
                        val currentUserId = sentShotsValue.substringBefore("-")
                        val currentUserShot = sentShotsValue.substringAfter("-")

                        // Fetch user, handling potential null
                        val currentUser = userViewModel.fetchUserFromRoom(currentUserId)

                        // Only add pair if both user and shot are non-null
                        if (currentUserShot.isNotBlank()) {
                            val pair = currentUser to currentUserShot
                            sentShotsPairList.add(pair)
                        }
                    }
                }

                for (i in 0 until sentShotsPairList.size) {
                    isPlayingSentList.add(i, false)
                }

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
            BottomBar(navController = navController, userViewModel)
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

                if (sentShotsPairList.size == 0) {

                    Box(
                        modifier = Modifier
                            .padding(88.dp)
                            .fillMaxSize()
                    ) {
                        Icon(
                            painterResource(id = R.drawable.sports_basketball_24px),
                            "Shot Icon",
                            modifier = Modifier
                                .height(240.dp)
                                .width(240.dp)
                                .align(Alignment.TopCenter)
                                .padding(0.dp, 40.dp, 0.dp, 0.dp)
                        )

                        Text(
                            "No Sent Shots",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(0.dp, 40.dp, 0.dp, 0.dp)
                        )

                    }

                } else {

                    // grid for sent shots

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = rememberLazyGridState(),
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

                }

            } else {

                if (receivedShotsPairList.size == 0) {

                    Box(
                        modifier = Modifier
                            .padding(88.dp)
                            .fillMaxSize()
                    ) {
                        Icon(
                            painterResource(id = R.drawable.sports_basketball_24px),
                            "Shot Icon",
                            modifier = Modifier
                                .height(240.dp)
                                .width(240.dp)
                                .align(Alignment.TopCenter)
                                .padding(0.dp, 40.dp, 0.dp, 0.dp)
                        )

                        Text(
                            "No Received Shots",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(0.dp, 40.dp, 0.dp, 0.dp)
                        )

                    }

                } else {

                    // grid for received likes
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
                        items(receivedShotsPairList.size) { index ->

                            if (index >= receivedShotsPairList.size) {
                                // Index is out of bounds, handle the case as needed
                                return@items
                            }

                            val localCurrentUser = receivedShotsPairList[index].first
                            val localCurrentUserShot = receivedShotsPairList[index].second

                            var ifSeen by remember { mutableStateOf("") }

                            for (ifSeenReceivedShot in ifSeenReceivedShotUiState.ifSeenReceivedShots) {

                                Log.d("ShotsScreen", "ifSeenReceivedShot = $ifSeenReceivedShot")

                                if (localCurrentUser?.id == ifSeenReceivedShot.substringBefore("-")) {
                                    ifSeen = ifSeenReceivedShot.substringAfter("-")
                                }

                                Log.d("ShotsScreen", "ifSeen = $ifSeen")
                            }

                            Box(modifier = Modifier.clickable {
                                isPlayingReceivedList[index] = !isPlayingReceivedList[index]
                                Log.d(TAG, "isPlaying = ${isPlayingReceivedList[index]}")
                                //play video
                            }) {
                                Log.d(TAG, "isPlaying = ${isPlayingReceivedList[index]}")
                                if (isPlayingReceivedList[index]) {

                                    ifSeenReceivedShotData["ifSeenReceivedShot-${
                                        localCurrentUser?.id
                                    }"] = false

                                    ifSeenReceivedShotViewModel.saveIfSeenReceivedShot(
                                        userViewModel.getYourUserId(),
                                        ifSeenReceivedShotData
                                    )

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
                                            .background(Color.LightGray) // Add background color here
                                            .align(Alignment.TopStart)
                                            .clickable {
                                                navController.navigate("userProfile/${localCurrentUser?.id}")
                                            }
                                    )

                                    if (ifSeen == "true") {
                                        Icon(
                                            //This is for messages, I'm still uncertain the exact icon but
                                            // this is for now
                                            painter = painterResource(id = R.drawable.new_star_solid_svgrepo_com),
                                            contentDescription = "Chat Icon",
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .height(72.dp)
                                                .width(72.dp)
                                                .padding(8.dp)
                                                .size(28.dp),
                                            tint = Color.Red
                                        )
                                    }

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
}