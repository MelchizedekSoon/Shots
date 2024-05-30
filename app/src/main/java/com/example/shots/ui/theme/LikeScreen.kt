package com.example.shots.ui.theme

import android.content.ContentValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.shots.FirebaseModule
import com.example.shots.R
import com.example.shots.RoomModule
import com.example.shots.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LikeScreen(
    navController: NavController, usersViewModel: UsersViewModel,
    receivedLikeViewModel: ReceivedLikeViewModel, sentLikeViewModel: SentLikeViewModel,
    locationViewModel: LocationViewModel,
    dataStore: DataStore<Preferences>
) {
    val appDatabase = RoomModule.provideAppDatabase(LocalContext.current)
    val userDao = RoomModule.provideUserDao(appDatabase)
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    var user by remember { mutableStateOf<User?>(null) }
    var sentLikesList by remember { mutableStateOf<List<String>?>(null) }
    var receivedLikesList by remember { mutableStateOf<List<String>?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLiked by rememberSaveable {
        mutableStateOf(false)
    }
    var isOnReceived by rememberSaveable {
        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {

            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()

            userData["newLikesCount"] = 0

            usersViewModel.
            saveUserDataToFirebase(firebaseAuth.currentUser?.displayName ?: "",
                userData,
                mediaItems,
                context) {

            }

            dataStore.edit { preferences ->
                preferences[intPreferencesKey("newLikesCount")] = 0
            }

            user = usersViewModel.getUser()

            val userId = firebaseAuth.currentUser?.displayName ?: ""

            sentLikesList = sentLikeViewModel.fetchSentLikeFromRoom(userId).sentLikes
                .filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()

            receivedLikesList = receivedLikeViewModel.fetchReceivedLikeFromRoom(userId).receivedLikes
                .filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()

            Log.d("LikeScreen", "The user on likedScreen is $user")
            Log.d("LikeScreen", "The sentLikesList is $sentLikesList")
            Log.d("LikeScreen", "The receivedLikesList is $receivedLikesList")
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
                            },
                        tint = Color(0xFFFF6F00)
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
                text = "Likes", fontSize = 24.sp,
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
                // grid for sent likes
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
                    items(sentLikesList?.size ?: 0) { index ->
                        var currentUser by remember { mutableStateOf<User?>(null) }
                        LaunchedEffect(Unit) {
                            withContext(Dispatchers.IO) {
                                currentUser = sentLikesList?.get(index)?.trim()
                                    ?.let { it1 -> userDao.findById(it1) }
                            }
                        }
                        Box(modifier = Modifier.clickable {
                            navController.navigate("userProfile/${currentUser?.id}") {
                                launchSingleTop = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        }) {
                            if(!currentUser?.mediaOne.isNullOrBlank()) {
                                Card(modifier = Modifier.height(240.dp)) {
                                    GlideImage(
                                        model = currentUser?.mediaOne,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        contentDescription = "mediaOne for liked user",
                                        colorFilter = ColorFilter.colorMatrix(
                                            ColorMatrix(
                                                colorMatrix
                                            )
                                        )
                                    )
                                }
                            } else {
                                Card(modifier = Modifier.height(240.dp)) {
                                    Column(modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally) {
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
//                            LaunchedEffect(
//                                isLiked
//                            ) {
//                                scope.launch(Dispatchers.IO) {
//                                    val userId = firebaseAuth.uid
//                                    val userData = mutableMapOf<String, Any>()
//                                    val mediaItems: MutableMap<String, Uri> = mutableMapOf()
//                                    var originalUser = usersViewModel.getUser()
//                                    val sentLikes = originalUser?.sentLikes
//                                    Log.d(ContentValues.TAG, "The sent likes are - $sentLikes")
//                                    sentLikesList = sentLikes?.split(" ") as MutableList<String>?
//                                    Log.d(
//                                        ContentValues.TAG,
//                                        "Likes before addition - $sentLikesList"
//                                    )
//
//                                    if (!isLiked
//                                    ) {
//                                        sentLikesList = sentLikesList?.toMutableList()
//                                        if (sentLikesList?.contains(user?.id) == true) {
//                                            Log.d(
//                                                ContentValues.TAG,
//                                                "Removing like to like list which is a String"
//                                            )
//                                            sentLikesList!!.remove(user?.id)
//                                            Log.d(
//                                                ContentValues.TAG,
//                                                "likes post-removal - $sentLikesList"
//                                            )
//                                        }
//                                    } else {
//                                        if (user != null) {
//                                            sentLikesList = sentLikesList?.toMutableList()
//                                            if (user != null && !sentLikesList?.contains(user?.id)!!) {
//                                                val currentId = user?.id
//                                                if (currentId != null) {
//                                                    sentLikesList!!.add(currentId)
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                    val sentLikesAsString = sentLikesList?.joinToString(" ")?.trim()
//                                    sentLikesList = null
//
//                                    if (sentLikesAsString != null) {
//                                        userData["sentLikes"] = sentLikesAsString
//                                    }
//
//                                    if (userId != null) {
//                                        usersViewModel.saveUserDataToFirebase(
//                                            userId,
//                                            userData,
//                                            mediaItems,
//                                            context
//                                        )
//                                    }
//
//                                    if (userId != null) {
//                                        originalUser = usersViewModel.getUser()
//                                        val returnedUser =
//                                            usersViewModel.getUserDataFromRepo(userId)
//                                        originalUser =
//                                            originalUser?.copy(sentLikes = returnedUser?.sentLikes)
//                                        if (originalUser != null) {
//                                            userDao.insert(originalUser)
//                                        }
//                                    }
//                                }
//                            }
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Text(
                                    modifier = Modifier.padding(bottom = 8.dp), // Add bottom padding to create space between the two text rows
                                    text = "${currentUser?.displayName}, ${
                                        currentUser?.birthday?.div(
                                            31556952000
                                        )
                                    }",
                                    fontSize = 16.sp,
                                    style = androidx.compose.ui.text.TextStyle(
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
                                        style = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Medium),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
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
                    items(receivedLikesList?.size ?: 0) { index ->
                        var currentUser by remember { mutableStateOf<User?>(null) }
                        LaunchedEffect(Unit) {
                            withContext(Dispatchers.IO) {
                                currentUser = receivedLikesList?.get(index)?.trim()
                                    ?.let { it1 -> userDao.findById(it1) }
                            }
                        }
//                        LaunchedEffect(Unit) {
//                            withContext(Dispatchers.IO) {
//                                val mutableReceivedLikesList = receivedLikesList?.toMutableList()
//                                val userId = mutableReceivedLikesList?.get(index)
//                                user = userDao.findById(userId ?: "")
//                                if (user?.id != userId) {
//                                    mutableReceivedLikesList?.removeAt(index)
//                                    sentLikesList = mutableReceivedLikesList?.toList()
//                                }
//                                if (user != null) {
//                                    isLiked = user?.receivedLikes?.contains(user!!.id) == true
//                                }
//                            }
//                        }
                        Box(modifier = Modifier.clickable {
                            navController.navigate("userProfile/${currentUser?.id}") {
//                                launchSingleTop = true
//                                popUpTo(navController.graph.startDestinationId) {
//                                    saveState = true
//                                }
                            }
                        }) {
                            if(!currentUser?.mediaOne.isNullOrBlank()) {
                                Card(modifier = Modifier.height(240.dp)) {
                                    Log.d(TAG, "The image is ${currentUser?.mediaOne}")
                                    GlideImage(
                                        model = currentUser?.mediaOne,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        contentDescription = "mediaOne for liked user",
                                        colorFilter = ColorFilter.colorMatrix(
                                            ColorMatrix(
                                                colorMatrix
                                            )
                                        )
                                    )
                                }
                            } else {
                                Card(modifier = Modifier.height(240.dp)) {
                                    Column(modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally) {
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
                            //this code is for removing a like which you cannot do for received likes
//                            LaunchedEffect(
//                                isLiked
//                            ) {
//                                scope.launch(Dispatchers.IO) {
//                                    val userId = firebaseAuth.uid
//                                    val userData = mutableMapOf<String, Any>()
//                                    val mediaItems: MutableMap<String, Uri> = mutableMapOf()
//                                    var originalUser = usersViewModel.getUser()
//                                    val sentLikes = originalUser?.sentLikes
//                                    Log.d(ContentValues.TAG, "$sentLikes")
//                                    sentLikesList = sentLikes?.split(" ") as MutableList<String>?
//                                    Log.d(
//                                        ContentValues.TAG,
//                                        "Likes before addition - $sentLikesList"
//                                    )
//
//                                    if (!isLiked
//                                    ) {
//                                        sentLikesList = sentLikesList?.toMutableList()
//                                        if (sentLikesList?.contains(user?.id) == true) {
//                                            Log.d(
//                                                ContentValues.TAG,
//                                                "Removing like to like list which is a String"
//                                            )
//                                            sentLikesList!!.remove(user?.id)
//                                            Log.d(
//                                                ContentValues.TAG,
//                                                "likes post-removal - $sentLikesList"
//                                            )
//                                        }
//                                    } else {
//                                        if (user != null) {
//                                            sentLikesList = sentLikesList?.toMutableList()
//                                            if (!sentLikesList?.contains(user?.id)!!) {
//                                                val currentId = user?.id
//                                                if (currentId != null) {
//                                                    sentLikesList!!.add(currentId)
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                    val sentLikesAsString = sentLikesList?.joinToString(" ")?.trim()
//                                    sentLikesList = null
//
//                                    if (sentLikesAsString != null) {
//                                        userData["sentLikes"] = sentLikesAsString
//                                    }
//
//                                    if (userId != null) {
//                                        usersViewModel.saveUserDataToFirebase(
//                                            userId,
//                                            userData,
//                                            mediaItems,
//                                            context
//                                        )
//                                    }
//
//                                    if (userId != null) {
//                                        originalUser = usersViewModel.getUser()
//                                        val returnedUser = usersViewModel.getUserDataFromRepo(userId)
//                                        originalUser =
//                                            originalUser?.copy(sentLikes = returnedUser?.sentLikes)
//                                        if (originalUser != null) {
//                                            userDao.insert(originalUser)
//                                        }
//                                    }
//                                }
//                            }
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Text(
                                    modifier = Modifier.padding(bottom = 8.dp), // Add bottom padding to create space between the two text rows
                                    text = "${currentUser?.displayName}, ${
                                        currentUser?.birthday?.div(
                                            31556952000
                                        )
                                    }",
                                    fontSize = 16.sp,
                                    style = androidx.compose.ui.text.TextStyle(
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
                                        style = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Medium),
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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LikeRow(user: User) {
    Box(modifier = Modifier
        .height(80.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .clickable { }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Card(
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .padding(0.dp, 0.dp, 0.dp, 0.dp),
                colors = CardColors(
                    containerColor = Color.White,
                    contentColor = Color.Black, disabledContentColor = Color.Red,
                    disabledContainerColor = Color.Red
                )
            ) {
                GlideImage(
                    model = user.mediaOne,
                    modifier = Modifier.clip(CircleShape),
                    contentScale = ContentScale.Crop, // This sets the content scale for the loaded image
                    contentDescription = "mediaOne image/Profile Photo"
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = buildAnnotatedString {
                    append("You gave ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(user.displayName)
                    }
                    append(" a like!")
                },
                fontSize = 16.sp
            )
        }

    }
}