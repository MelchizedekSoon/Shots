package com.example.shots.ui.theme

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.shots.CountdownScreen
import com.example.shots.R
import com.example.shots.videoPlayerForShot
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(UnstableApi::class)
@Composable
fun CameraScreen(
    navController: NavController, userId: String?,
    yourUserId: String?,
    receivedShotViewModel: ReceivedShotViewModel,
    ifSeenReceivedShotViewModel: IfSeenReceivedShotViewModel,
    sentShotViewModel: SentShotViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf("") }
    var scope = rememberCoroutineScope()
    val showSnackbar: () -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar(snackbarMessage)
        }
    }
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()

    var recording: Recording?

    val file = File(
        context.getExternalFilesDir(null),
        "${yourUserId}_video.mp4")

    var isFacingBack by remember {
        mutableStateOf(true)
    }

    var hasPressedRecord by remember {
        mutableStateOf(false)
    }

    var hasPressedStop by remember {
        mutableStateOf(false)
    }

    val stopCallback: () -> Unit = {
        hasPressedStop = true
        hasPressedRecord = false
    }

    var isReadyToPlay by remember {
        mutableStateOf(false)
    }

    var isFinalized by remember {
        mutableStateOf(false)
    }

    var permissionGranted by remember {
        mutableStateOf(false)
    }

    var isPlayingBack by remember {
        mutableStateOf(false)
    }

    val lensFacingBack = CameraSelector.LENS_FACING_BACK
    val lensFacingFront = CameraSelector.LENS_FACING_FRONT
    val lifecycleOwner = LocalLifecycleOwner.current
    val preview = Preview.Builder().build()
    val previewView = remember {
        PreviewView(context)
    }
    val cameraxSelectorBack = CameraSelector.Builder().requireLensFacing(lensFacingBack).build()
    val cameraxSelectorFront = CameraSelector.Builder().requireLensFacing(lensFacingFront).build()

    val imageCapture = remember {
        ImageCapture.Builder().build()
    }

    val qualitySelector = QualitySelector.fromOrderedList(
        listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
        FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
    )

    LaunchedEffect(Unit) {
        isFacingBack = !isFacingBack
    }

    LaunchedEffect(isFacingBack) {
        Log.d("VideoHelper", "Camera should start within isFacingBack")
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        if (!isFacingBack) {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraxSelectorBack,
                preview,
                imageCapture
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)
        } else {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraxSelectorFront,
                preview,
                imageCapture
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)
        }
    }

    LaunchedEffect(hasPressedRecord) {
        Log.d("VideoHelper", "hasPressedRecord = $hasPressedRecord")
        val cameraProvider = context.getCameraProvider()
        val backgroundCameraExecutor: ScheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor()
        val recorder = Recorder.Builder()
            .setExecutor(backgroundCameraExecutor).setQualitySelector(qualitySelector)
            .build()
        val videoCapture = VideoCapture.withOutput(recorder)

        cameraProvider.unbindAll()
        if (hasPressedRecord) {
            cameraProvider.bindToLifecycle(
                lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, videoCapture
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)
            val fileOutputOptions =
                FileOutputOptions.Builder(file).build() // Build the file output options
            val listenerExecutor: ScheduledExecutorService =
                Executors.newSingleThreadScheduledExecutor()


            permissionGranted = when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    true
                }

                else -> {
                    false
                }
            }

            if (permissionGranted) {


                val preparedRecording =
                    recorder.prepareRecording(context, fileOutputOptions).withAudioEnabled()


                val listener = androidx.core.util.Consumer<VideoRecordEvent> { event ->
                    // Handle the video record event
                    when (event) {

                        is VideoRecordEvent.Start -> {
                            Log.d("VideoHelper", "Recording started! - $file")
                            // Handle the start event
                        }

                        is VideoRecordEvent.Finalize -> {
                            // Handle the finalize event and any error information
                            Log.d("VideoHelper", "Recording done! - $file")
                            val error = event.error
                            if (error != null) {
                                Log.d("CameraScreen", "There's been an error! - $error")
                                // Handle the error
                            }
//                            isFinalized = true
                            isReadyToPlay = true
                        }
                        // Handle other video record events if needed
                    }
                }
                if (hasPressedRecord) {
                    recording = preparedRecording.start(listenerExecutor, listener)
                    Log.d("VideoHelper", "Recording started! - $file")
                    if (hasPressedStop) {
                        recording?.stop()
                        isReadyToPlay = true
                        Log.d("VideoHelper", "Recording stopped! - $file")
                        hasPressedRecord = false
                    }
                } else if (isFinalized) {
                }
            } else {
                coroutineScope {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar("You must accept permissions to send shots")
                    }
                }
            }
        }
    }




    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        if (isPlayingBack) {
            Icon(
                painter = painterResource(R.drawable.cancel_24px), "Cancel Icon",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .height(60.dp)
                    .width(60.dp)
                    .padding(16.dp, 16.dp, 0.dp, 0.dp)
                    .clickable {
                        file.delete()
                        hasPressedRecord = false
                        isFacingBack = true
                    }
            )
        }

        val deleteCallback: () -> Unit = {
            file.delete()
            hasPressedRecord = false
            isFacingBack = true
            navController.navigate("camera/$userId")
        }


        val saveCallback: () -> Unit = {

            val directoryName = "Shots"

            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
//                put(
//                    MediaStore.MediaColumns.DATA,
//                    "${Environment.getExternalStorageDirectory()}/$directoryName/${file.name}"
//                )
            }

            val resolver = context.contentResolver
            val collectionUri =
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val itemUri = resolver.insert(collectionUri, contentValues)

            if (itemUri != null) {
                val outputStream = resolver.openOutputStream(itemUri)
                val inputStream = FileInputStream(file)

                if (outputStream != null) {
                    inputStream.copyTo(outputStream)
                }
                inputStream.close()
                outputStream?.close()

                // Video file saved successfully
                Log.d("VideoHelper", "Video saved to gallery: $itemUri")
            } else {
                // Failed to save video file
                Log.e("VideoHelper", "Failed to save video to gallery")
            }


        }

        var sendWasClicked by remember { mutableStateOf(false) }

        var wasAddedToSent by remember { mutableStateOf(false) }

        var wasNotAddedToSent by remember { mutableStateOf(false) }

        var wasAddedToReceived by remember { mutableStateOf(false) }

        var wasNotAddedToReceived by remember { mutableStateOf(false) }

        val sendCallback: () -> Unit = {
            sendWasClicked = true
            val sentShotData: MutableMap<String, Uri> = mutableMapOf()
            val receivedShotData: MutableMap<String, Uri> = mutableMapOf()
            val ifSeenReceivedShotData: MutableMap<String, Boolean> = mutableMapOf()
            //lisa = userId
            //jamar = yourUserId
            sentShotData["sentShot-$userId"] = file.toUri()
            receivedShotData["receivedShot-$yourUserId"] = file.toUri()
            ifSeenReceivedShotData["ifSeenReceivedShot-$yourUserId"] = true

            if (userId != null) {
                receivedShotViewModel.saveReceivedShot(
                    userId,
                    receivedShotData,
                    context
                )
                ifSeenReceivedShotViewModel.saveIfSeenReceivedShot(
                    userId,
                    ifSeenReceivedShotData
                )
                sentShotViewModel.saveSentShot(
                    userId,
                    sentShotData,
                    context
                )
            }

        }


        if (!hasPressedRecord) {
            Icon(
                painter = painterResource(R.drawable.radio_button_checked_24px),
                "Recording Icon", tint = Color.Red,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(120.dp)
                    .width(120.dp)
                    .padding(0.dp, 0.dp, 0.dp, 32.dp)
                    .clickable {
                        hasPressedRecord = true
                    }
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.stop_24px),
                "Recording Icon", tint = Color.Red,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(120.dp)
                    .width(120.dp)
                    .padding(0.dp, 0.dp, 0.dp, 32.dp)
                    .clickable {
                        hasPressedStop = true
                        hasPressedRecord = false
                    }
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.flip_camera_android_24px),
            "Flip Camera Icon",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .height(48.dp)
                .width(48.dp)
                .padding(0.dp, 16.dp, 16.dp, 0.dp)
                .clickable {
                    isFacingBack = !isFacingBack
                }
        )
        if (hasPressedRecord) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomStart)
                    .padding(16.dp, 0.dp, 0.dp, 16.dp)
            ) {
                CountdownScreen(stopCallback)
            }
        } else if (isReadyToPlay) {
            Log.d("VideoHelper", "Should be showing video now! - ${file.absoluteFile}")
            isPlayingBack = true
            videoPlayerForShot(
                file.toUri(), deleteCallback,
                saveCallback, sendCallback
            )
            LaunchedEffect(sendWasClicked) {
                Log.d("CameraScreen", "sendWasClicked = $sendWasClicked")
                if (sendWasClicked) {
                    snackbarHostState
                        .showSnackbar("Your shot should be received soon!")
                    navController.popBackStack()
                    navController.popBackStack()
                }
            }
            LaunchedEffect(wasAddedToSent) {
                Log.d("CameraScreen", "wasAddedToSent = $wasAddedToSent")
                if (wasAddedToSent) {
                    snackbarHostState
                        .showSnackbar("Shot was added to your sent shots")
                }
            }
            LaunchedEffect(wasNotAddedToSent) {
                if (wasNotAddedToSent) {
                    snackbarHostState
                        .showSnackbar("Shot was not added to your sent shots")
                }
            }
            LaunchedEffect(wasAddedToReceived) {
                if (wasAddedToReceived) {
                    snackbarHostState
                        .showSnackbar("Shot was added to your received shots")
                }
            }
            LaunchedEffect(wasNotAddedToReceived) {
                if (wasNotAddedToReceived) {
                    snackbarHostState
                        .showSnackbar("Shot was not added to your received shots")
                }
            }
        }
    }
}

private fun requestCameraPermission(launcher: ActivityResultLauncher<String>) {
    val permission = Manifest.permission.CAMERA
    launcher.launch(permission)
}