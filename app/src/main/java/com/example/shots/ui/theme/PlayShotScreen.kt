package com.example.shots.ui.theme

import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.shots.GetStreamClientModule
import com.example.shots.videoPlayerForReceivedShot
import com.example.shots.videoPlayerForSentShot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(UnstableApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PlayShotScreen(
    navController: NavController,
    yourUserId: String,
    currentUserId: String,
    currentUserShot: String,
    shotType: String,
    receivedShotViewModel: ReceivedShotViewModel,
    sentShotViewModel: SentShotViewModel,
    playShotViewModel: PlayShotViewModel,
    usersViewModel: UsersViewModel,
) {

    val context = LocalContext.current
    var scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember {
        mutableStateOf("")
    }

    /**
     * This code below is supposed to eliminate screenshots but doesn't seem to be working at the
     * moment
     */

//    val lifecycleOwner = LocalLifecycleOwner.current
//
//    DisposableEffect(Unit) {
//        val activity = context as? ComponentActivity
//        val window = activity?.window
//        window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
//
//        onDispose {
//            // Re-enable screenshots when the screen is no longer active
//            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
//        }
//    }

    val showSnackbar: () -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar("Video duration exceeds the maximum allowed duration")
        }
    }

    var wasSaved by remember { mutableStateOf(false) }
    var wasNotSaved by remember { mutableStateOf(false) }

    var receivedWasTrashed by remember { mutableStateOf(false) }
    var receivedWasNotTrashed by remember { mutableStateOf(false) }

    var sentWasTrashed by remember { mutableStateOf(false) }
    var sentWasNotTrashed by remember { mutableStateOf(false) }

    val backCallback: () -> Unit = {
        navController.popBackStack()
        navController.popBackStack()
    }

    val saveCallback: () -> Unit = {
        playShotViewModel.saveShot(currentUserShot, context,
            { videoSaved ->
                // Handle video saved callback
                // Additional logic after video saved
                wasSaved = videoSaved
            },
            { videoNotSaved ->
                // Handle video not saved callback
                // Additional logic after video not saved
                wasNotSaved = videoNotSaved
            }
        )
    }

    val trashCallbackForReceivedShot: () -> Unit = {
        receivedShotViewModel.removeReceivedShotFromFirebase(
            context,
            currentUserId,
            { receivedIsTrashed ->
                receivedWasTrashed = receivedIsTrashed
                Log.d("PlayShotScreen", "receivedWasTrashed: $receivedIsTrashed")
            },
            { receivedIsNotTrashed ->
                receivedWasNotTrashed = receivedIsNotTrashed
            })
    }


    val trashCallbackForSentShot: () -> Unit = {
        receivedShotViewModel.removeReceivedShotFromFirebase(
            context,
            currentUserId,
            { receivedIsTrashed ->
                receivedWasTrashed = receivedIsTrashed
                Log.d("PlayShotScreen", "receivedWasTrashed: $receivedIsTrashed")
            },
            { receivedIsNotTrashed ->
                receivedWasNotTrashed = receivedIsNotTrashed
            })
        sentShotViewModel.removeSentShotFromFirebase(context, currentUserId,
            { sentIsTrashed ->
                sentWasTrashed = sentIsTrashed
            },
            { sentIsNotTrashed ->
                sentWasNotTrashed = sentIsNotTrashed
            })
    }

    val checkCallback: () -> Unit = {
        val client = GetStreamClientModule.provideGetStreamClient(context, usersViewModel)
        client.createChannel(
            channelType = "messaging",
            channelId = "",
            memberIds = listOf(yourUserId, currentUserId),
            extraData = emptyMap()
        ).enqueue { result ->
            if (result.isSuccess) {
                Log.d("VideoHelper", "Channel succeeded!")
                navController.navigate("channels")
                val channel = result.getOrNull()
            } else {
                Log.d("VideoHelper", "Channel failed - ${result.errorOrNull()}")
                // Handle result.error()
            }
        }
        //This will be responsible for opening the messaging
        // between the two users so that they can communicate
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (shotType == "received") {
                videoPlayerForReceivedShot(
                    videoUri = currentUserShot.toUri(),
                    backCallback = backCallback,
                    trashCallback = trashCallbackForReceivedShot,
                    checkCallback = checkCallback,
                    navController
                )
            } else if (shotType == "sent") {
                videoPlayerForSentShot(
                    videoUri = currentUserShot.toUri(),
                    backCallback = backCallback,
                    saveCallback = saveCallback,
                    trashCallback = trashCallbackForSentShot,
                    navController
                )
            }

        }
    }

    LaunchedEffect(wasSaved) {
        if (wasSaved) {
            scope.launch {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Video has been saved!")
                    snackbarHostState.showSnackbar("Video has been saved!")
                    wasSaved = false
                }
            }
        }
    }

    LaunchedEffect(wasNotSaved) {
        if (wasNotSaved) {
            scope.launch {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Video has not been saved!")
                    snackbarHostState.showSnackbar("Video has not been saved!")
                    wasNotSaved = false
                }
            }
        }
    }

    LaunchedEffect(sentWasTrashed) {
        if (sentWasTrashed) {
            scope.launch {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Video has been deleted for you!")
                    snackbarHostState.showSnackbar("Video has been deleted for you.")
                    sentWasTrashed = false
                }
            }
        }
    }

    LaunchedEffect(sentWasNotTrashed) {
        if (sentWasNotTrashed) {
            scope.launch {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Video has NOT been deleted for you!")
                    snackbarHostState.showSnackbar("Video has failed to be deleted.")
                    sentWasNotTrashed = false
                }
            }
        }
    }

    LaunchedEffect(receivedWasTrashed) {
        if (receivedWasTrashed) {
            scope.launch {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Video has been deleted for them!")
                    snackbarHostState.showSnackbar("Video has been deleted for them!")
                    receivedWasTrashed = false
                }
            }
        }
    }

    LaunchedEffect(receivedWasNotTrashed) {
        if (receivedWasNotTrashed) {
            scope.launch {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Video failed to be deleted for them!")
                    snackbarHostState.showSnackbar("Video has failed to be deleted for them.")
                    receivedWasNotTrashed = false
                }
            }
        }
    }


}