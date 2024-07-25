package com.example.shots.ui.theme

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.shots.ProfileMediaDisplay
import com.example.shots.R
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun PreviewScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    locationViewModel: LocationViewModel
) {
    userViewModel.loadYourUser()

    val user by userViewModel.user.collectAsState()

    var bookmarkWasClicked by remember { mutableStateOf(false) }
    var shotWasClicked by remember { mutableStateOf(false) }
    var likeWasClicked by remember { mutableStateOf(false) }
    var reportWasClicked by remember { mutableStateOf(false) }
    var blockWasClicked by remember { mutableStateOf(false) }
    var copyUserNameWasClicked by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
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
            val padding = if (showBottomSheet) {
                224.dp
            } else {
                32.dp
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp, 0.dp, 0.dp, padding)
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
    }) {
        Modifier.padding(it)
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

                                Spacer(Modifier.height(2.dp))

                                val acceptShotsValue = when (user?.acceptShots) {
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

                                //Location or how far away
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
                                        user?.latitude ?: 0.0,
                                        user?.longitude ?: 0.0,
                                        thisLatitude,
                                        thisLongitude
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
                                    Icon(painterResource(id = R.drawable.heart_alt_svgrepo_com),
                                        "Like Button",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .height(48.dp)
                                            .width(48.dp)
                                            .clickable {
                                                likeWasClicked = true
                                            })
                                    Text(text = "Like", fontSize = 16.sp)
                                }

                                LaunchedEffect(likeWasClicked) {
                                    if (likeWasClicked) {
                                        snackbarMessage = "Use this to send a like."
                                        snackbarHostState.showSnackbar(snackbarMessage)
                                        likeWasClicked = false
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
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .clickable {
                                                    shotWasClicked = true
                                                }
                                        )
                                    }
                                    Text(text = "Shoot", fontSize = 16.sp)
                                }

                                LaunchedEffect(shotWasClicked) {
                                    if (shotWasClicked) {
                                        snackbarMessage = "Use this to send a shot."
                                        snackbarHostState.showSnackbar(snackbarMessage)
                                        shotWasClicked = false
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    val painter =
                                        painterResource(id = R.drawable.bookmark_24px)

                                    Icon(painter,
                                        "Bookmark Button",
                                        tint = Color(0xFF007FFF),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .width(48.dp)
                                            .clickable {
                                                bookmarkWasClicked = true
                                            })
                                    Text(text = "Bookmark", fontSize = 16.sp)
                                }

                                LaunchedEffect(bookmarkWasClicked) {
                                    if (bookmarkWasClicked) {
                                        snackbarMessage = "Use this to bookmark a profile."
                                        snackbarHostState.showSnackbar(snackbarMessage)
                                        bookmarkWasClicked = false
                                    }
                                }

                            }
                        }
                    }
                }
            }

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
                                            Religion.HINDUISM -> "Hinduism"
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
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            reportWasClicked = true
                        }) {
                        Text(text = "Report", color = Color.Black)
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            blockWasClicked = true
                        }) {
                        Text(text = "Block", color = Color.Black)
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            copyUserNameWasClicked = true
                        }) {
                        Text(text = "Copy username", color = Color.Black)
                    }
//                    Text(text = "Copy profile URL", color = Color.Black)
//                    Spacer(Modifier.height(24.dp))
//                    Text(text = "Share this profile", color = Color.Black)
                    Spacer(Modifier.height(72.dp))
                }
                LaunchedEffect(reportWasClicked) {
                    if (reportWasClicked) {
                        snackbarMessage = "Use this to report a profile."
                        snackbarHostState.showSnackbar(snackbarMessage)
                        reportWasClicked = false
                        Log.d("PreviewScreen", "reportWasClicked = $reportWasClicked")
                    }
                }
                LaunchedEffect(blockWasClicked) {
                    if (blockWasClicked) {
                        snackbarMessage = "Use this to block a profile."
                        snackbarHostState.showSnackbar(snackbarMessage)
                        blockWasClicked = false
                    }
                }
                LaunchedEffect(copyUserNameWasClicked) {
                    if (copyUserNameWasClicked) {
//                        val clipboardManager =
//                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                        val clipData = ClipData.newPlainText("Username", user?.userName)
//                        clipboardManager.setPrimaryClip(clipData)
                        snackbarMessage =
                            "Use this to copy a user's username."
                        snackbarHostState.showSnackbar(snackbarMessage)
                        copyUserNameWasClicked = false
                        // Show a toast or snackbar to indicate that the username has been copied
//            Toast.makeText(
//                context,
//                "Username ${user?.userName} copied to clipboard",
//                Toast.LENGTH_SHORT
//            ).show()
                    }
                }
            }
        }

    }
}


@Preview
@Composable
fun PreviewScreenPreview() {
//    UserProfileScreen(navController = rememberNavController())
}
