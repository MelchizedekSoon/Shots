package com.example.shots.ui.theme

import android.content.ContentValues.TAG
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.shots.DialogUtils
import com.example.shots.FirebaseModule
import com.example.shots.R
import com.example.shots.RoomModule
import com.example.shots.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersScreen(
    navController: NavController, userViewModel: UserViewModel,
    blockedUserViewModel: BlockedUserViewModel,
    userWhoBlockedYouViewModel: UserWhoBlockedYouViewModel,
    blockViewModel: BlockViewModel,
    locationViewModel: LocationViewModel
) {
    val appDatabase = RoomModule.provideAppDatabase(LocalContext.current)
//    val userDao = RoomModule.provideUserDao(appDatabase)
    val blockedUserDao = RoomModule.provideBlockedUserDao(appDatabase)
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    var blockedUserList by remember { mutableStateOf<List<String>?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasBeenClicked by remember { mutableStateOf(false) }
    var isShowingblockedUserDialog by rememberSaveable { mutableStateOf(false) }
    val user: User? by remember { mutableStateOf(userViewModel.fetchUser()) }

    val blockedUsersUiState = blockedUserViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {

//            val userData: MutableMap<String, Any> = mutableMapOf()
//            val mediaItems: MutableMap<String, Uri> = mutableMapOf()
//
//            userData["timesblockedUseredCount"] = 0
//
//            userViewModel.saveUserDataToFirebase(
//                firebaseAuth.currentUser?.displayName ?: "",
//                userData,
//                mediaItems,
//                context
//            ) {
//
//            }

            val userId = user?.id
            try {
                if (userId != null) {
                    blockedUserViewModel.fetchUpdatedBlockedUsers()
                        .collect { returnedBlockedUserList ->
                            blockedUserList = returnedBlockedUserList
                                .filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()
                        }
//                    blockedUserList = blockedUserViewModel.fetchBlockedUserObject().blockedUsers
//                        .filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()
//                    blockedUserList = blockedUserViewModel.fetchBlockedUserFromRoom(userId).blockedUsers
//                       .filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()
//                    blockedUserList = blockedUserDao.findById(userId).blockedUsers
//                        .filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()
                }
                Log.d("BlockedUserScreen", "blockedUserList - $blockedUserList")
            } catch (npe: NullPointerException) {
                Log.d("BlockedUserScreen", "blockedUser isn't found")
            }
        }
    }

    Log.d(TAG, "The blockedUserList size - ${blockedUserList?.size} - ${blockedUserList}")

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
                text = "Blocked Users", fontSize = 24.sp,
            )

            Spacer(Modifier.height(16.dp))

            Log.d(
                "BlockedUsersScreen",
                "blockedUsers - ${blockedUsersUiState.value.blockedUsers.size}"
            )

            if (blockedUsersUiState.value.blockedUsers.isEmpty()) {
                Log.d(
                    "BlockedUsersScreen",
                    "blockedUsers - ${blockedUsersUiState.value.blockedUsers.size}"
                )

                Box(
                    modifier = Modifier
                        .padding(100.dp)
                        .fillMaxSize()
                ) {
                    Icon(
                        painterResource(id = R.drawable.block_24px),
                        "Block Icon",
                        modifier = Modifier
                            .height(240.dp)
                            .width(240.dp)
                            .align(Alignment.TopCenter)
                            .padding(0.dp, 40.dp, 0.dp, 0.dp)
                    )
                    //Please grant location access to see users."
                    Text(
                        "No Blocked Users",
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(0.dp, 40.dp, 0.dp, 0.dp)
                    )

                }


            } else {
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
                    items(blockedUserList?.size ?: 0) { index ->

                        var currentUser by remember {
                            mutableStateOf<User?>(
                                null
                            )
                        }
                        var isblockedUser by remember {
                            mutableStateOf(true)
                        }
                        val hasConfirmedRemoval by remember {
                            mutableStateOf(false)
                        }
                        if (!isblockedUser) {
                            Log.d("blockedUser", "blockedUser is now - $isblockedUser")

                            val isConfirmed = DialogUtils.blockedUserRemovalDialog { confirmed ->
                                if (!confirmed) {
                                    isblockedUser = !isblockedUser
                                } else {
                                    blockedUserViewModel.deleteBlockedUser(currentUser?.id ?: "")

                                    userWhoBlockedYouViewModel.deleteUserWhoBlockedYou(
                                        currentUser?.id ?: ""
                                    )

                                    userViewModel.loadUsers()
                                }

                                // Callback function called on dialog dismissal
                                isShowingblockedUserDialog = false

                                // Use the `confirmed` value if needed
                                Log.d(TAG, "Dialog dismissed, confirmed: $confirmed")
                            }
                            if (isConfirmed) {
                                Toast.makeText(context, "User unblocked", Toast.LENGTH_SHORT).show()
                                navController.navigate("blockedUsers")
                            }
                        }
                        LaunchedEffect(Unit) {
                            withContext(Dispatchers.IO) {
                                val currentUserId = blockedUserList?.get(index)
                                try {
                                    if (!currentUserId.isNullOrBlank()) {
                                        currentUser = userViewModel.fetchUserFromRoom(currentUserId)
//                                    if (currentUser != null) {
//                                        val yourblockedUsers =
//                                            blockedUserDao.findById(currentUser!!.id).blockedUsers
//                                        if (yourblockedUsers != null) {
//                                            isblockedUser =
//                                                yourblockedUsers.contains(currentUser!!.id) == true
//                                        }
//                                    }
                                    }
                                } catch (npe: java.lang.NullPointerException) {
                                    Log.d(TAG, "No blockedUsers yet")
                                }
//                            if (user?.id != blockedUserList?.get(index)) {
//                                blockedUserList?.removeAt(index)
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
                                        contentDescription = "mediaOne for blockedUser user",
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
                            val painter = if (!isblockedUser) {
                                if (hasConfirmedRemoval) {
                                    painterResource(id = R.drawable.block_24px)
                                } else {
                                    painterResource(id = R.drawable.block_24px)
                                }
                            } else {
                                painterResource(id = R.drawable.block_24px)
                            }
                            Icon(
                                painter = painter,
                                "blockedUser Icon",
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .height(36.dp)
                                    .width(36.dp)
                                    .clickable {
                                        isblockedUser = !isblockedUser
//                                scope.launch {
//                                    withContext(Dispatchers.IO) {
//                                        isblockedUsered = !isblockedUsered
//                                        val blockedUserData: MutableMap<String, Any> =
//                                            mutableMapOf()
//                                        blockedUserData["blockedUser-${currentUser?.id}"] =
//                                            currentUser?.id ?: ""
//                                        if (isblockedUsered) {
//                                            isShowingblockedUserDialog = true
//                                            currentUser?.let { it1 ->
//                                                blockedUserViewModel.saveblockedUserToFirebase(
//                                                    it1.id,
//                                                    blockedUserData
//                                                )
//                                            }
//                                        } else {
//                                            isShowingblockedUserDialog = true
//                                            Log.d(
//                                                TAG,
//                                                "isShowingblockedUserDialog - $isShowingblockedUserDialog"
//                                            )
////                                                if (hasConfirmedRemoval) {
////                                                    isblockedUsered = false
////                                                    blockedUserViewModel.removeblockedUserFromFirebase(
////                                                        currentUser?.id ?: ""
////                                                    )
////                                                    hasConfirmedRemoval = false
////                                                }
//                                        }
//                                        val retrievedblockedUsers: List<String> =
//                                            blockedUserViewModel.getblockedUsersFromFirebase(
//                                                firebaseAuth.currentUser?.uid ?: ""
//                                            )
//                                        Log.d(
//                                            TAG,
//                                            "The retrievedblockedUsers - $retrievedblockedUsers"
//                                        )
//                                        val userId = firebaseAuth.currentUser?.uid
//                                        if (!userId.isNullOrBlank()) {
//                                            var blockedUser = blockedUserDao.findById(userId)
//                                            if (blockedUser != null) {
//                                                blockedUser =
//                                                    blockedUser.copy(blockedUsers = retrievedblockedUsers.toMutableList())
//                                                blockedUserDao.update(blockedUser)
//                                            } else {
//                                                val newblockedUser =
//                                                    blockedUser(
//                                                        userId,
//                                                        retrievedblockedUsers.toMutableList()
//                                                    )
//                                                blockedUserDao.insert(newblockedUser)
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


}
