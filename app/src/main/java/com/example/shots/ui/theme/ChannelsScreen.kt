package com.example.shots.ui.theme

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.shots.R
import com.example.shots.client
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.state.extensions.watchChannelAsState


@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChannelsScreen(
    navController: NavController,
    usersViewModel: UsersViewModel
) {
    val context = LocalContext.current

    val user = usersViewModel.getUser()

    LaunchedEffect(Unit) {
        if ((user?.newMessagesCount ?: 0) != 0) {
            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()

            userData["newMessagesCount"] = 0

            usersViewModel.saveUserDataToFirebase(user?.id ?: "", userData, mediaItems, context) {

            }
        }
    }

    if (client != null) {
        LaunchedEffect(Unit) {


// Watching a channel's state using the offline library
            client!!.watchChannelAsState(cid = "messaging:", messageLimit = 0)
                .collect { channelState ->
                    if (channelState != null) {
                        // StateFlow objects to observe
                        channelState.messages
                        channelState.reads
                        channelState.typing
                    } else {
                        // User not connected yet.
                    }
                }
        }
    }





    Scaffold(
        topBar = {
            TopAppBar(title = {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painterResource(R.drawable.shots_3_cropped),
                        "Shots Logo",
                        modifier = Modifier
                            .height(96.dp)
                            .aspectRatio(1f)
                    )
                }
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
                    GlideImage(
                        model = user?.mediaOne,
                        modifier = Modifier
                            .clip(CircleShape)
                            .height(48.dp)
                            .width(48.dp),
                        contentScale = ContentScale.Crop, // This sets the content scale for the loaded image
                        contentDescription = "mediaOne image/Profile Photo"
                    )
                    Spacer(Modifier.width(8.dp))
//                    IconButton(onClick = {}) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.tune_24px),
//                            contentDescription = "Filter Icon",
//                            modifier = Modifier
//                                .height(24.dp)
//                                .width(24.dp),
//                        )
//                    }
                })

        },
        bottomBar = {
            BottomBar(navController = navController, usersViewModel)
        }
    ) {
        Modifier.padding(it)
        // Observe the client connection state
        if (client != null) {
            val clientInitialisationState by client!!.clientState.initializationState.collectAsState()

            ChatTheme {
                when (clientInitialisationState) {
                    InitializationState.COMPLETE -> {
                        io.getstream.chat.android.compose.ui.channels.ChannelsScreen(
                            title = stringResource(id = R.string.app_name),
                            isShowingSearch = true,
                            onItemClick = { channel ->
                                val channelId = channel.cid
                                navController.navigate("channel/$channelId")
                            },
                            onBackPressed = { navController.popBackStack() }
                        )
                    }

                    InitializationState.INITIALIZING -> {
                        Text(text = "Initialising...")
                    }

                    InitializationState.NOT_INITIALIZED -> {
                        Text(text = "Not initialized...")
                    }
                }
            }
        }


    }


}