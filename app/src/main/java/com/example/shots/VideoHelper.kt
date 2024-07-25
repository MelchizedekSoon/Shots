package com.example.shots

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.example.shots.ui.theme.IfSeenReceivedShotViewModel
import com.example.shots.ui.theme.ReceivedShotViewModel
import com.example.shots.ui.theme.SentShotViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Composable
fun cameraPermissionRequest(
): Boolean {
    val requiredPermissions: List<String> = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    var permissionResults = true

    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in requiredPermissions && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                permissionResults = false
            }
        }
    )

    DisposableEffect(Unit) {
        activityResultLauncher.launch(requiredPermissions.toTypedArray())

        onDispose {
            // Clean up any resources if needed
        }
    }

    return permissionResults
}


@Composable
fun requestRecordAudioPermission(): Boolean {
    var permissionGranted by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
    }

    DisposableEffect(Unit) {
        requestAudioPermission(launcher)
        onDispose { /* Cleanup logic if needed */ }
    }

    return permissionGranted
}

private fun requestAudioPermission(launcher: ActivityResultLauncher<String>) {
    val permission = Manifest.permission.RECORD_AUDIO
    launcher.launch(permission)
}

private fun requestCameraPermission(launcher: ActivityResultLauncher<String>) {
    val permission = Manifest.permission.CAMERA
    launcher.launch(permission)
}


@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(UnstableApi::class)
@Composable
fun CameraPreviewScreen(
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

    var recording: Recording? = null


    val file = File(
        context.getExternalFilesDir(null),
        "${yourUserId}_${System.currentTimeMillis()}.mp4"
    ) // Specify the file path for the recorded video
//    val videoFile = File(context.getExternalFilesDir(null), "my_video.mp4")

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

                scope.launch(Dispatchers.IO) {

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
                                    Log.d(TAG, "There's been an error! - $error")
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
                put(
                    MediaStore.MediaColumns.DATA,
                    "${Environment.getExternalStorageDirectory()}/$directoryName/${file.name}"
                )
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
            ifSeenReceivedShotData["ifSeenReceivedShot-$yourUserId"] = false

            if (userId != null) {

                val sendCallbackWorkRequest: WorkRequest =
                    OneTimeWorkRequestBuilder<SendCallbackWorker>()
                        .build()

                WorkManager
                    .getInstance(context)
                    .enqueue(sendCallbackWorkRequest)

                val myWorkRequest = OneTimeWorkRequest.from(SendCallbackWorker::class.java)


//                receivedShotViewModel.saveReceivedShot(
//                    userId,
//                    receivedShotData,
//                    context
//                )
//                ifSeenReceivedShotViewModel.saveIfSeenReceivedShot(
//                    userId,
//                    ifSeenReceivedShotData
//                )
//                sentShotViewModel.saveSentShot(
//                    userId,
//                    sentShotData,
//                    context
//                )

            }


//            if (yourUserId != null) {
//                receivedShotViewModel.storeReceivedShotInRoom(true, context, yourUserId)
//                sentShotViewModel.storeSentShotInRoom(yourUserId)
//            }

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
                Log.d(TAG, "sendWasClicked = $sendWasClicked")
                if (sendWasClicked) {
                    snackbarHostState
                        .showSnackbar("Your shot should be received soon!")
                    navController.popBackStack()
                    navController.popBackStack()
                }
            }
            LaunchedEffect(wasAddedToSent) {
                Log.d(TAG, "wasAddedToSent = $wasAddedToSent")
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


@Composable
fun CountdownScreen(stopCallback: () -> Unit) {
    var secondsRemaining by remember { mutableStateOf(61) }

    DisposableEffect(Unit) {
        val timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                secondsRemaining -= 1
                if (secondsRemaining == 0) {
                    timer.cancel()
                    stopCallback()
                }
            }
        }

        timer.schedule(timerTask, 0, 1000)

        onDispose {
            timer.cancel()
        }
    }

    Text(
        text = "$secondsRemaining",
        color = Color.White,
        fontSize = 48.sp
    )
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

private fun captureImage(imageCapture: ImageCapture, context: Context) {
    val name = "CameraxImage.jpeg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
        }
    }
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                println("Success")
            }

            override fun onError(exception: ImageCaptureException) {
                println("Failed $exception")
            }

        })
}

//@OptIn(UnstableApi::class)
//@Composable
//fun ShootYourShot() {
//
//    val context = LocalContext.current
//
//    val qualitySelector = QualitySelector.fromOrderedList(
//        listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
//        FallbackStrategy.lowerQualityOrHigherThan(Quality.SD))
//
//    val recorder = Recorder.Builder()
//        .setExecutor(cameraExecutor).setQualitySelector(qualitySelector)
//        .build()
//
//    val videoCapture = VideoCapture.withOutput(recorder)
//
//    try {
//        // Bind use cases to camera
//        cameraProvider.bindToLifecycle(
//            context, CameraSelector.DEFAULT_BACK_CAMERA, preview, videoCapture)
//    } catch(exc: Exception) {
//        Log.e(TAG, "Use case binding failed", exc)
//    }
//
//    // Create MediaStoreOutputOptions for our recorder
//    val name = "CameraX-recording-" +
//            SimpleDateFormat(FILENAME_FORMAT, Locale.US)
//                .format(System.currentTimeMillis()) + ".mp4"
//    val contentValues = ContentValues().apply {
//        put(MediaStore.Video.Media.DISPLAY_NAME, name)
//    }
//    val mediaStoreOutput = MediaStoreOutputOptions.Builder(context.contentResolver,
//        MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
//        .setContentValues(contentValues)
//        .build()
//
//// 2. Configure Recorder and Start recording to the mediaStoreOutput.
//    val recording = if (ActivityCompat.checkSelfPermission(
//            context,
//            Manifest.permission.RECORD_AUDIO
//        ) != PackageManager.PERMISSION_GRANTED
//    ) {
//        // TODO: Consider calling
//        //    ActivityCompat#requestPermissions
//        // here to request the missing permissions, and then overriding
//        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//        //                                          int[] grantResults)
//        // to handle the case where the user grants the permission. See the documentation
//        // for ActivityCompat#requestPermissions for more details.
//
//        return
//    } else {
//        videoCapture.output
//            .prepareRecording(context, mediaStoreOutput)
//            .withAudioEnabled()
//            .start(ContextCompat.getMainExecutor(context), captureListener)
//    }
//
//}

/** This function is for the videoPlayer that you see when you create a
shot but haven't yet sent it. Here, you can send it, delete it, save it, and mute/unmute it
 */

@OptIn(UnstableApi::class)
@Composable
fun videoPlayerForShot(
    videoUri: Uri,
    deleteCallback: () -> Unit,
    saveCallback: () -> Unit,
    sendCallback: () -> Unit
): Boolean {

    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    var thumbnailBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isVideoPlaying by remember { mutableStateOf(false) }

    // State to track whether the video is currently playing or paused
    var isTooLong by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by rememberSaveable { mutableStateOf(false) }
    var isHidden by rememberSaveable { mutableStateOf(true) }

    var scaffoldState = rememberScaffoldState()

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf("") }

    var scope = rememberCoroutineScope()

    var saveWasClicked by remember {
        mutableStateOf(false)
    }

    var deleteWasClicked by remember { mutableStateOf(false) }

    var sendWasClicked by remember { mutableStateOf(false) }

    var sendDialogWasClosed by remember {
        mutableStateOf(false)
    }



    Scaffold(
        modifier = Modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Snackbar(modifier = Modifier.padding(16.dp)) {
                        Text(text = snackbarMessage)
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .clickable {
                    isVideoPlaying = !isVideoPlaying
                    if (isVideoPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                }
        ) {
            DisposableEffect(exoPlayer) {
                onDispose {
                    exoPlayer.release()
                }
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(), // Aspect ratio 16:9
                factory = { contextOne ->
                    PlayerView(contextOne).apply {
                        player = exoPlayer

                        // Set the resize mode to Aspect Fill
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL

                        // Set the gravity of the controller to bottom
                        controllerShowTimeoutMs = 0
                        controllerAutoShow = false

                        // Set padding to prevent cutting off the video
                        contentDescription = "Video Player"

                        // Build the media item.
                        val mediaItem = MediaItem.fromUri(videoUri)

//                    exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT

                        // Set the media item to be played.
                        exoPlayer.setMediaItem(mediaItem)

                        // Prepare the player.
                        exoPlayer.prepare()

                        // Set to repeat (Just testing this out!)
                        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

                        // Hide controller bar
                        useController = false

                        // Mute video for thumbnail
                        exoPlayer.volume = 0.0f

                        // Play
                        exoPlayer.play()
                    }
                }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(0.dp, 16.dp, 16.dp, 0.dp),
                    verticalArrangement =
                    Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cancel_24px), "Cancel Icon",
                        tint = Color.White,
                        modifier = Modifier
                            .height(36.dp)
                            .width(36.dp)
                            .clickable {
                                deleteWasClicked = true
                            }
                    )
                    Spacer(modifier = Modifier.height(16.dp)) // Add a 16dp vertical space
                    Icon(
                        painter = painterResource(R.drawable.save_24px), "Save Icon",
                        tint = Color.White,
                        modifier = Modifier
                            .height(36.dp)
                            .width(36.dp)
                            .clickable {
                                saveWasClicked = true
                                saveCallback()
                            }
                    )
                    Spacer(modifier = Modifier.height(16.dp)) // Add a 16dp vertical space
                    if (isMuted) {
                        Icon(
                            painter = painterResource(R.drawable.volume_up_24px),
                            "Mute Icon", tint = Color.White,
                            modifier = Modifier
                                .height(36.dp)
                                .width(36.dp)
                                .clickable {
                                    Log.d("VideoHelper", "Should start playing now")
                                    exoPlayer.volume = 0.5f
                                    isMuted = false
                                }
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.volume_off_24px),
                            "Mute Icon", tint = Color.White,
                            modifier = Modifier
                                .height(36.dp)
                                .width(36.dp)
                                .clickable {
                                    exoPlayer.volume = 0.0f
                                    isMuted = true
                                }
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Icon(
                        painter = painterResource(R.drawable.send_24px),
                        "Send Icon", tint = Color.White,
                        modifier = Modifier
                            .height(36.dp)
                            .width(36.dp)
                            .clickable {
                                //This will send the shot (video) to the user
                                sendWasClicked = true
                            }
                    )
                }
            }

            val notDeleteCallback: () -> Unit = {
                deleteWasClicked = false
            }

            if (deleteWasClicked) {
                DeleteDialog(deleteCallback, notDeleteCallback)
            }

            val notSendCallback: () -> Unit = {
                sendWasClicked = false
            }


            if (sendWasClicked) {
                SendDialog(sendCallback, notSendCallback) { sendSnackbar ->
                    if (sendSnackbar) {
                        sendDialogWasClosed = sendWasClicked
                    }
                }
            }


            val currentPosition = remember { mutableStateOf(0L) }
            val maxPosition = remember { mutableStateOf(0L) }

            LaunchedEffect(Unit) {
                while (true) {
                    currentPosition.value = exoPlayer.currentPosition
                    maxPosition.value = exoPlayer.duration
                    delay(100)
                }
            }

            if (maxPosition.value >= 0) {
                Slider(
                    value = currentPosition.value.toFloat(),
                    onValueChange = { position ->
                        exoPlayer.seekTo(position.toLong())
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                    valueRange = 0f..maxPosition.value.toFloat(),
                    colors = SliderColors(
                        Color.White, Color.White, Color.Black, Color.Black, Color.White,
                        Color.White, Color.White, Color.White, Color.White, Color.White
                    ),
                    enabled = maxPosition.value > 0L
                )
            }


        }

        LaunchedEffect(sendDialogWasClosed) {
            if (sendDialogWasClosed) {
                snackbarMessage = "Your shot should be received soon!"
                snackbarHostState.showSnackbar(
                    snackbarMessage
                )
                sendWasClicked = false
                sendDialogWasClosed = false
            }
        }

        LaunchedEffect(saveWasClicked) {
            if (saveWasClicked) {
                snackbarMessage = "Saving to your device now!"
                snackbarHostState.showSnackbar(
                    snackbarMessage
                )
                saveWasClicked = false
            }
        }

    }




    LaunchedEffect(isMuted) {
        if (!isMuted) {
            exoPlayer.volume = 0.0f
        } else {
            exoPlayer.volume = 0.5f
        }
    }

    return isTooLong
}


@OptIn(UnstableApi::class)
@Composable
fun videoPlayerForReceivedShot(
    videoUri: Uri,
    backCallback: () -> Unit,
    trashCallback: () -> Unit,
    checkCallback: () -> Unit,
    navController: NavController
): Boolean {

    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.popBackStack()
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


    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    var thumbnailBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isVideoPlaying by remember { mutableStateOf(false) }

    // State to track whether the video is currently playing or paused
    var isTooLong by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by rememberSaveable { mutableStateOf(false) }
    var isHidden by rememberSaveable { mutableStateOf(true) }

    var scaffoldState = rememberScaffoldState()

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf("") }

    var scope = rememberCoroutineScope()

    var backWasClicked by remember {
        mutableStateOf(false)
    }

    var trashWasClicked by remember { mutableStateOf(false) }

    var trashDialogWasClosed by remember {
        mutableStateOf(false)
    }

    var checkWasClicked by remember { mutableStateOf(false) }

    var checkDialogWasClosed by remember {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Snackbar(modifier = Modifier.padding(16.dp)) {
                        Text(text = snackbarMessage)
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .clickable {
                    isVideoPlaying = !isVideoPlaying
                    if (isVideoPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                }
        ) {
            DisposableEffect(exoPlayer) {
                onDispose {
                    exoPlayer.release()
                }
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(), // Aspect ratio 16:9
                factory = { contextOne ->
                    PlayerView(contextOne).apply {
                        player = exoPlayer

                        // Set the resize mode to Aspect Fill
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL

                        // Set the gravity of the controller to bottom
                        controllerShowTimeoutMs = 0
                        controllerAutoShow = false

                        // Set padding to prevent cutting off the video
                        contentDescription = "Video Player"

                        // Build the media item.
                        val mediaItem = MediaItem.fromUri(videoUri)

//                    exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT

                        // Set the media item to be played.
                        exoPlayer.setMediaItem(mediaItem)

                        // Prepare the player.
                        exoPlayer.prepare()

                        // Set to repeat (Just testing this out!)
                        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

                        // Hide controller bar
                        useController = false

                        // Mute video for thumbnail
                        exoPlayer.volume = 0.0f

                        // Play
                        exoPlayer.play()
                    }
                }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(0.dp, 16.dp, 16.dp, 0.dp),
                    verticalArrangement =
                    Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back_24px), "Back Icon",
                        tint = Color.White,
                        modifier = Modifier
                            .height(36.dp)
                            .width(36.dp)
                            .clickable {
                                backCallback()
                            }
                    )
                    Spacer(modifier = Modifier.height(16.dp)) // Add a 16dp vertical space
                    Icon(
                        painter = painterResource(R.drawable.delete_24px), "Trash Icon",
                        tint = Color.White,
                        modifier = Modifier
                            .height(36.dp)
                            .width(36.dp)
                            .clickable {
                                trashWasClicked = true
                            }
                    )
                    Spacer(modifier = Modifier.height(16.dp)) // Add a 16dp vertical space
                    if (isMuted) {
                        Icon(
                            painter = painterResource(R.drawable.volume_up_24px),
                            "Mute Icon", tint = Color.White,
                            modifier = Modifier
                                .height(36.dp)
                                .width(36.dp)
                                .clickable {
                                    Log.d("VideoHelper", "Should start playing now")
                                    exoPlayer.volume = 0.5f
                                    isMuted = false
                                }
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.volume_off_24px),
                            "Mute Icon", tint = Color.White,
                            modifier = Modifier
                                .height(36.dp)
                                .width(36.dp)
                                .clickable {
                                    exoPlayer.volume = 0.0f
                                    isMuted = true
                                }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // Add a 16dp vertical space
                    Icon(
                        painter = painterResource(R.drawable.check_24px), "Check Icon",
                        tint = Color.White,
                        modifier = Modifier
                            .height(36.dp)
                            .width(36.dp)
                            .clickable {
                                checkWasClicked = true
                            }
                    )
                }
            }


            val notTrashCallback: () -> Unit = {
                trashWasClicked = false
            }

            if (trashWasClicked) {
                ReceivedTrashDialog(trashCallback, notTrashCallback) { trashSnackbar ->
                    if (trashSnackbar) {
                        trashDialogWasClosed = trashWasClicked
                    }
                }
            }

            val notCheckCallback: () -> Unit = {
                checkWasClicked = false
            }

            if (checkWasClicked) {
                CheckDialog(checkCallback, notCheckCallback) { checkSnackbar ->
                    if (checkSnackbar) {
                        checkDialogWasClosed = checkWasClicked
                    }
                }
            }


            val currentPosition = remember { mutableStateOf(0L) }
            val maxPosition = remember { mutableStateOf(0L) }

            LaunchedEffect(Unit) {
                while (true) {
                    currentPosition.value = exoPlayer.currentPosition
                    maxPosition.value = exoPlayer.duration
                    delay(100)
                }
            }

            if (maxPosition.value >= 0) {
                Slider(
                    value = currentPosition.value.toFloat(),
                    onValueChange = { position ->
                        exoPlayer.seekTo(position.toLong())
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                    valueRange = 0f..maxPosition.value.toFloat(),
                    colors = SliderColors(
                        Color.White, Color.White, Color.Black, Color.Black, Color.White,
                        Color.White, Color.White, Color.White, Color.White, Color.White
                    ),
                    enabled = maxPosition.value > 0L
                )
            }


        }

        LaunchedEffect(trashDialogWasClosed) {
            if (trashDialogWasClosed) {
                snackbarMessage = "This shot should be trashed soon!"
                snackbarHostState.showSnackbar(
                    snackbarMessage
                )
                trashWasClicked = false
                trashDialogWasClosed = false
            }
        }


    }




    LaunchedEffect(isMuted) {
        if (!isMuted) {
            exoPlayer.volume = 0.0f
        } else {
            exoPlayer.volume = 0.5f
        }
    }

    return isTooLong
}


@OptIn(UnstableApi::class)
@Composable
fun videoPlayerForSentShot(
    videoUri: Uri,
    backCallback: () -> Unit,
    saveCallback: () -> Unit,
    trashCallback: () -> Unit,
    navController: NavController
): Boolean {

    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.popBackStack()
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


    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    var thumbnailBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isVideoPlaying by remember { mutableStateOf(false) }

    // State to track whether the video is currently playing or paused
    var isTooLong by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by rememberSaveable { mutableStateOf(false) }
    var isHidden by rememberSaveable { mutableStateOf(true) }

    var scaffoldState = rememberScaffoldState()

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf("") }

    var scope = rememberCoroutineScope()

    var backWasClicked by remember {
        mutableStateOf(false)
    }

    var saveWasClicked by remember { mutableStateOf(false) }

    var trashWasClicked by remember { mutableStateOf(false) }

    var trashDialogWasClosed by remember {
        mutableStateOf(false)
    }



    Scaffold(
        modifier = Modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Snackbar(modifier = Modifier.padding(16.dp)) {
                        Text(text = snackbarMessage)
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .clickable {
                    isVideoPlaying = !isVideoPlaying
                    if (isVideoPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                }
        ) {
            DisposableEffect(exoPlayer) {
                onDispose {
                    exoPlayer.release()
                }
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(), // Aspect ratio 16:9
                factory = { contextOne ->
                    PlayerView(contextOne).apply {
                        player = exoPlayer

                        // Set the resize mode to Aspect Fill
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL

                        // Set the gravity of the controller to bottom
                        controllerShowTimeoutMs = 0
                        controllerAutoShow = false

                        // Set padding to prevent cutting off the video
                        contentDescription = "Video Player"

                        // Build the media item.
                        val mediaItem = MediaItem.fromUri(videoUri)

//                    exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT

                        // Set the media item to be played.
                        exoPlayer.setMediaItem(mediaItem)

                        // Prepare the player.
                        exoPlayer.prepare()

                        // Set to repeat (Just testing this out!)
                        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

                        // Hide controller bar
                        useController = false

                        // Mute video for thumbnail
                        exoPlayer.volume = 0.0f

                        // Play
                        exoPlayer.play()
                    }
                }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(0.dp, 16.dp, 16.dp, 0.dp),
                    verticalArrangement =
                    Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back_24px), "Back Icon",
                        tint = Color.White,
                        modifier = Modifier
                            .height(36.dp)
                            .width(36.dp)
                            .clickable {
                                backCallback()
                            }
                    )
                    Spacer(modifier = Modifier.height(16.dp)) // Add a 16dp vertical space
                    Icon(
                        painter = painterResource(R.drawable.save_24px), "Save Icon",
                        tint = Color.White,
                        modifier = Modifier
                            .height(36.dp)
                            .width(36.dp)
                            .clickable {
                                saveWasClicked = true
                            }
                    )
                    Spacer(modifier = Modifier.height(16.dp)) // Add a 16dp vertical space
                    Icon(
                        painter = painterResource(R.drawable.delete_24px), "Trash Icon",
                        tint = Color.White,
                        modifier = Modifier
                            .height(36.dp)
                            .width(36.dp)
                            .clickable {
                                trashWasClicked = true
                            }
                    )
                    Spacer(modifier = Modifier.height(16.dp)) // Add a 16dp vertical space
                    if (isMuted) {
                        Icon(
                            painter = painterResource(R.drawable.volume_up_24px),
                            "Mute Icon", tint = Color.White,
                            modifier = Modifier
                                .height(36.dp)
                                .width(36.dp)
                                .clickable {
                                    Log.d("VideoHelper", "Should start playing now")
                                    exoPlayer.volume = 0.5f
                                    isMuted = false
                                }
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.volume_off_24px),
                            "Mute Icon", tint = Color.White,
                            modifier = Modifier
                                .height(36.dp)
                                .width(36.dp)
                                .clickable {
                                    exoPlayer.volume = 0.0f
                                    isMuted = true
                                }
                        )
                    }
                }
            }

            val notSaveCallback: () -> Unit = {
                saveWasClicked = false
            }

            if (saveWasClicked) {
                saveWasClicked = false
                saveCallback()
            }

            val notTrashCallback: () -> Unit = {
                trashWasClicked = false
            }

            if (trashWasClicked) {
                SentTrashDialog(trashCallback, notTrashCallback) { trashSnackbar ->
                    if (trashSnackbar) {
                        trashDialogWasClosed = trashWasClicked
                    }
                }
            }


            val currentPosition = remember { mutableStateOf(0L) }
            val maxPosition = remember { mutableStateOf(0L) }

            LaunchedEffect(Unit) {
                while (true) {
                    currentPosition.value = exoPlayer.currentPosition
                    maxPosition.value = exoPlayer.duration
                    delay(100)
                }
            }

            if (maxPosition.value >= 0) {
                Slider(
                    value = currentPosition.value.toFloat(),
                    onValueChange = { position ->
                        exoPlayer.seekTo(position.toLong())
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                    valueRange = 0f..maxPosition.value.toFloat(),
                    colors = SliderColors(
                        Color.White, Color.White, Color.Black, Color.Black, Color.White,
                        Color.White, Color.White, Color.White, Color.White, Color.White
                    ),
                    enabled = maxPosition.value > 0L
                )
            }


        }

        LaunchedEffect(trashDialogWasClosed) {
            if (trashDialogWasClosed) {
                snackbarMessage = "Your shot should be trashed for you and who you sent it to soon!"
                snackbarHostState.showSnackbar(
                    snackbarMessage
                )
                trashWasClicked = false
                trashDialogWasClosed = false
            }
        }

        LaunchedEffect(saveWasClicked) {
            if (saveWasClicked) {
                snackbarMessage = "Saving to your device now!"
                snackbarHostState.showSnackbar(
                    snackbarMessage
                )
                saveWasClicked = false
            }
        }

    }




    LaunchedEffect(isMuted) {
        if (!isMuted) {
            exoPlayer.volume = 0.0f
        } else {
            exoPlayer.volume = 0.5f
        }
    }

    return isTooLong
}


@Composable
fun SentTrashDialog(
    onTrashConfirmed: () -> Unit, onTrashNotConfirmed: () -> Unit,
    trashSnackbar: (Boolean) -> Unit
) {
    var openDialog by remember { mutableStateOf(true) }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                openDialog = false
                onTrashNotConfirmed()
            },
            title = {
                Text(text = "Trash Shot")
            },
            text = {
                Text(text = "Mark this shot as trash and remove it for you and who you sent it to?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onTrashConfirmed()
                        trashSnackbar(true)
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onTrashNotConfirmed()
                        trashSnackbar(false)
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun ReceivedTrashDialog(
    onTrashConfirmed: () -> Unit, onTrashNotConfirmed: () -> Unit,
    trashSnackbar: (Boolean) -> Unit
) {
    var openDialog by remember { mutableStateOf(true) }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                openDialog = false
                onTrashNotConfirmed()
            },
            title = {
                Text(text = "Trash Shot")
            },
            text = {
                Text(text = "Mark this shot as trash and remove it?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onTrashConfirmed()
                        trashSnackbar(true)
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onTrashNotConfirmed()
                        trashSnackbar(false)
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}


@Composable
fun CheckDialog(
    onCheckConfirmed: () -> Unit, onCheckNotConfirmed: () -> Unit,
    checkSnackbar: (Boolean) -> Unit
) {
    var openDialog by remember { mutableStateOf(true) }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                openDialog = false
                onCheckNotConfirmed()
            },
            title = {
                Text(text = "Made Shot")
            },
            text = {
                Text(text = "Mark this shot as made?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onCheckConfirmed()
                        checkSnackbar(true)
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onCheckNotConfirmed()
                        checkSnackbar(false)
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}


@Composable
fun SendDialog(
    onSendConfirmed: () -> Unit, onSendNotConfirmed: () -> Unit,
    sendSnackbar: (Boolean) -> Unit
) {
    var openDialog by remember { mutableStateOf(true) }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                openDialog = false
                onSendNotConfirmed()
            },
            title = {
                Text(text = "Send")
            },
            text = {
                Text(text = "Are you sure you want to send this shot?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onSendConfirmed()
                        sendSnackbar(true)
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onSendNotConfirmed()
                        sendSnackbar(false)
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun DeleteDialog(onDeleteConfirmed: () -> Unit, onDeleteNotConfirmed: () -> Unit) {
    var openDialog by remember { mutableStateOf(true) }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                openDialog = false
                onDeleteNotConfirmed()
            },
            title = {
                Text(text = "Delete")
            },
            text = {
                Text(text = "Are you sure you want to delete this shot?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onDeleteConfirmed()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onDeleteNotConfirmed()
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}

/**
 * This is the video player for sent shots so once you've sent one,
 * you can go and view them here and if you delete it, it will also be deleted for the person
 * who sent it. This is not an option for received shots. Once you delete a received shot,
 * you won't be able to see it but the person who sent it will unless they also delete it.
 */


//@OptIn(UnstableApi::class)
//@Composable
//fun videoPlayerForReceivedShot(
//    videoUri: Uri,
//    backCallback: () -> Unit,
//    trashCallback: () -> Unit,
//    checkCallback: () -> Unit
//): Boolean {
//
//    val context = LocalContext.current
//
//    val exoPlayer = remember {
//        ExoPlayer.Builder(context).build()
//    }
//
//    var thumbnailBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
//    var isVideoPlaying by remember { mutableStateOf(false) }
//
//    // State to track whether the video is currently playing or paused
//    var isTooLong by remember { mutableStateOf(false) }
//    var isPaused by remember { mutableStateOf(false) }
//    var isMuted by rememberSaveable { mutableStateOf(false) }
//    var isHidden by rememberSaveable { mutableStateOf(true) }
//
//    var scaffoldState = rememberScaffoldState()
//
//    val snackbarHostState = remember { SnackbarHostState() }
//    var snackbarMessage by remember { mutableStateOf("") }
//
//    var scope = rememberCoroutineScope()
//
//    var deleteWasClicked by remember { mutableStateOf(false) }
//
//    var sendWasClicked by remember { mutableStateOf(false) }
//
//    var checkWasClicked by remember { mutableStateOf(false) }
//
//    var trashWasClicked by remember { mutableStateOf(false) }
//
//    var trashDialogWasClosed by remember {
//        mutableStateOf(false)
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .clickable {
//                isVideoPlaying = !isVideoPlaying
//                if (isVideoPlaying) {
//                    exoPlayer.pause()
//                } else {
//                    exoPlayer.play()
//                }
//            }
//    ) {
//        DisposableEffect(exoPlayer) {
//            onDispose {
//                exoPlayer.release()
//            }
//        }
//
//        AndroidView(
//            modifier = Modifier.fillMaxSize(), // Aspect ratio 16:9
//            factory = { contextOne ->
//                PlayerView(contextOne).apply {
//                    player = exoPlayer
//
//                    // Set the resize mode to Aspect Fill
//                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
//
//                    // Set the gravity of the controller to bottom
//                    controllerShowTimeoutMs = 0
//                    controllerAutoShow = false
//
//                    // Set padding to prevent cutting off the video
//                    contentDescription = "Video Player"
//
//                    // Build the media item.
//                    val mediaItem = MediaItem.fromUri(videoUri)
//
////                    exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
//
//                    // Set the media item to be played.
//                    exoPlayer.setMediaItem(mediaItem)
//
//                    // Prepare the player.
//                    exoPlayer.prepare()
//
//                    // Set to repeat (Just testing this out!)
//                    exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
//
//                    // Hide controller bar
//                    useController = false
//
//                    // Mute video for thumbnail
//                    exoPlayer.volume = 0.0f
//
//                    // Play
//                    exoPlayer.play()
//                }
//            }
//        )
//        Box(modifier = Modifier.fillMaxSize()) {
//            Column(
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .padding(0.dp, 16.dp, 16.dp, 0.dp),
//                verticalArrangement =
//                Arrangement.Center
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.arrow_back_24px), "Back Icon",
//                    tint = Color.White,
//                    modifier = Modifier
//                        .height(36.dp)
//                        .width(36.dp)
//                        .clickable {
//                            backCallback()
//                        }
//                )
////                Spacer(modifier = Modifier.height(16.dp)) // Add a 16dp vertical space
////                Icon(
////                    painter = painterResource(R.drawable.save_24px), "Save Icon",
////                    tint = Color.White,
////                    modifier = Modifier
////                        .height(36.dp)
////                        .width(36.dp)
////                        .clickable {
////                            saveCallback()
////                        }
////                )
//                Spacer(modifier = Modifier.height(16.dp)) // Add a 16dp vertical space
//                Icon(
//                    painter = painterResource(R.drawable.delete_24px), "Trash Icon",
//                    tint = Color.White,
//                    modifier = Modifier
//                        .height(36.dp)
//                        .width(36.dp)
//                        .clickable {
//                            trashCallback()
//                        }
//                )
//                Spacer(modifier = Modifier.height(16.dp)) // Add a 16dp vertical space
//                if (isMuted) {
//                    Icon(
//                        painter = painterResource(R.drawable.volume_up_24px),
//                        "Mute Icon", tint = Color.White,
//                        modifier = Modifier
//                            .height(36.dp)
//                            .width(36.dp)
//                            .clickable {
//                                Log.d("VideoHelper", "Should start playing now")
//                                exoPlayer.volume = 0.5f
//                                isMuted = false
//                            }
//                    )
//                } else {
//                    Icon(
//                        painter = painterResource(R.drawable.volume_off_24px),
//                        "Mute Icon", tint = Color.White,
//                        modifier = Modifier
//                            .height(36.dp)
//                            .width(36.dp)
//                            .clickable {
//                                exoPlayer.volume = 0.0f
//                                isMuted = true
//                            }
//                    )
//                }
//                Spacer(Modifier.height(16.dp))
//                Icon(
//                    painter = painterResource(R.drawable.check_24px),
//                    "Send Icon", tint = Color.White,
//                    modifier = Modifier
//                        .height(36.dp)
//                        .width(36.dp)
//                        .clickable {
//                            //This will send the shot (video) to the user
//                            checkWasClicked = true
//                        }
//                )
//            }
//        }
//
//        val notDeleteCallback: () -> Unit = {
//            deleteWasClicked = false
//        }
//
//        if (deleteWasClicked) {
////            DeleteDialog(deleteCallback, notDeleteCallback)
//        }
//
//        val notSendCallback: () -> Unit = {
//            sendWasClicked = false
//        }
//
//        if (sendWasClicked) {
////            SendDialog(sendCallback, notSendCallback)
//        }
//
//        if (checkWasClicked) {
////            CheckDialog(acceptedCallback, notAcceptedCallback) {
////
////            }
//        }
//
////        if (sendWasClicked) {
////            SendDialog(
////                sendCallback,
////                notSendCallback)
////        }
//
//        val notTrashCallback: () -> Unit = {
//            trashWasClicked = false
//        }
//
//
//        if (trashWasClicked) {
//            ReceivedTrashDialog(trashCallback, notTrashCallback) { trashSnackbar ->
//                if (trashSnackbar) {
//                    trashDialogWasClosed = trashWasClicked
//                }
//            }
//        }
//
//
//        val currentPosition = remember { mutableStateOf(0L) }
//        val maxPosition = remember { mutableStateOf(0L) }
//
//        LaunchedEffect(Unit) {
//            while (true) {
//                currentPosition.value = exoPlayer.currentPosition
//                maxPosition.value = exoPlayer.duration
//                delay(100)
//            }
//        }
//
//        if (maxPosition.value >= 0) {
//            Slider(
//                value = currentPosition.value.toFloat(),
//                onValueChange = { position ->
//                    exoPlayer.seekTo(position.toLong())
//                },
//                modifier = Modifier.align(Alignment.BottomCenter),
//                valueRange = 0f..maxPosition.value.toFloat(),
//                colors = SliderColors(
//                    Color.White, Color.White, Color.Black, Color.Black, Color.White,
//                    Color.White, Color.White, Color.White, Color.White, Color.White
//                ),
//                enabled = maxPosition.value > 0L
//            )
//        }
//
//        LaunchedEffect(trashDialogWasClosed) {
//            if (trashDialogWasClosed) {
//                snackbarMessage = "Your shot should be trashed soon!"
//                snackbarHostState.showSnackbar(
//                    snackbarMessage
//                )
//                trashWasClicked = false
//                trashDialogWasClosed = false
//            }
//        }
//
//
//    }
//    LaunchedEffect(isMuted) {
//        if (!isMuted) {
//            exoPlayer.volume = 0.0f
//        } else {
//            exoPlayer.volume = 0.5f
//        }
//    }
//
//    return isTooLong
//}


@OptIn(UnstableApi::class)
@Composable
fun videoPlayer(
    videoUri: Uri,
    maxDuration: Long,
    showSnackbar: () -> Unit
): Boolean {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    var thumbnailBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isVideoPlaying by remember { mutableStateOf(false) }

    // State to track whether the video is currently playing or paused
    var isTooLong by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by rememberSaveable { mutableStateOf(false) }
    var isHidden by rememberSaveable { mutableStateOf(true) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                isVideoPlaying = !isVideoPlaying
                if (isVideoPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
            }
    ) {
        DisposableEffect(exoPlayer) {
            onDispose {
                exoPlayer.release()
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(), // Aspect ratio 16:9
            factory = { contextOne ->
                PlayerView(contextOne).apply {

                    player = exoPlayer

                    // Set the resize mode to Aspect Fill
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

                    // Set the gravity of the controller to bottom
                    controllerShowTimeoutMs = 0
                    controllerAutoShow = false

                    // Set padding to prevent cutting off the video
                    contentDescription = "Video Player"

                    // Build the media item.
                    val mediaItem = MediaItem.fromUri(videoUri)

//                    exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT

                    // Set the media item to be played.
                    exoPlayer.setMediaItem(mediaItem)

                    // Prepare the player.
                    exoPlayer.prepare()

                    // Set to repeat (Just testing this out!)
                    exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

                    // Hide controller bar
                    useController = false

                    // Mute video for thumbnail
                    exoPlayer.volume = 0.0f

                    // Play
                    exoPlayer.play()
                }
            }
        )
        if (isMuted) {
            Icon(
                painter = painterResource(R.drawable.volume_up_24px),
                "Mute Icon", tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(0.dp, 8.dp, 8.dp, 0.dp)
                    .height(24.dp)
                    .width(24.dp)
                    .clickable {
                        Log.d("VideoHelper", "Should start playing now")
                        exoPlayer.volume = 0.5f
                        isMuted = false
                    }
            )
        }
        if (!isMuted) {
            Icon(
                painter = painterResource(R.drawable.volume_off_24px),
                "Mute Icon", tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(0.dp, 8.dp, 8.dp, 0.dp)
                    .height(24.dp)
                    .width(24.dp)
                    .clickable {
                        exoPlayer.volume = 0.0f
                        isMuted = true
                    }
            )
        }
//            if (!isPaused) {
//                Icon(
//                    painter = painterResource(R.drawable.pause_circle_24px),
//                    "Play Icon", tint = Color.White,
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .padding(0.dp, 0.dp, 0.dp, 16.dp)
//                        .clickable {
//                            isPaused = true
//                            exoPlayer.pause()
//                        }
//                )
//            }
//            if (isPaused) {
//                Icon(
//                    painter = painterResource(R.drawable.play_circle_24px),
//                    "Play Icon", tint = Color.White,
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .padding(0.dp, 0.dp, 0.dp, 16.dp)
//                        .clickable {
//                            isPaused = false
//                            exoPlayer.play()
//                        }
//                )
//            }
//            Icon(
//                painter = painterResource(R.drawable.fast_rewind_24px),
//                "Rewind Icon", tint = Color.White,
//                modifier = Modifier
//                    .align(Alignment.BottomStart)
//                    .padding(60.dp, 0.dp, 0.dp, 16.dp)
//                    .clickable {
//                        exoPlayer.seekTo(exoPlayer.currentPosition - 1000)
//                    }
//            )
//            Icon(
//                painter = painterResource(R.drawable.fast_forward_24px),
//                "Fast Forward Icon", tint = Color.White,
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .padding(0.dp, 0.dp, 60.dp, 16.dp)
//                    .clickable {
//                        exoPlayer.seekTo(exoPlayer.currentPosition + 1000)
//                    }
//            )

        val currentPosition = remember { mutableStateOf(0L) }
        val maxPosition = remember { mutableStateOf(0L) }

        LaunchedEffect(Unit) {
            while (true) {
                currentPosition.value = exoPlayer.currentPosition
                maxPosition.value = exoPlayer.duration
                delay(100)
            }
        }

        if (maxPosition.value >= 0) {
            Slider(
                value = currentPosition.value.toFloat(),
                onValueChange = { position ->
                    exoPlayer.seekTo(position.toLong())
                },
                modifier = Modifier.align(Alignment.BottomCenter),
                valueRange = 0f..maxPosition.value.toFloat(),
                colors = SliderColors(
                    Color.White, Color.White, Color.Black, Color.Black, Color.White,
                    Color.White, Color.White, Color.White, Color.White, Color.White
                ),
                enabled = maxPosition.value > 0L
            )
        }


    }
    LaunchedEffect(isMuted) {
        if (!isMuted) {
            exoPlayer.volume = 0.0f
        } else {
            exoPlayer.volume = 0.5f
        }
    }

    Log.d("VideoHelper", "isMuted = $isMuted")
    return isTooLong
}


@OptIn(UnstableApi::class)
@Composable
fun videoPlayerForUserCard(
    videoUri: Uri,
    maxDuration: Long,
    showSnackbar: () -> Unit,
    navToUserProfile: () -> Unit
): Boolean {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }


    var thumbnailBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isVideoPlaying by remember { mutableStateOf(false) }

    // State to track whether the video is currently playing or paused
    var isTooLong by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var isHidden by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                navToUserProfile()
            }
    ) {
        DisposableEffect(exoPlayer) {
            onDispose {
                exoPlayer.release()
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(), // Aspect ratio 16:9
            factory = { contextOne ->
                PlayerView(contextOne).apply {
                    player = exoPlayer
                    // Set the resize mode to Aspect Fill
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    // Set the gravity of the controller to bottom
                    controllerShowTimeoutMs = 0
                    controllerAutoShow = false

                    // Set padding to prevent cutting off the video
                    contentDescription = "Video Player"

                    // Build the media item.
                    val mediaItem = MediaItem.fromUri(videoUri)

                    exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT

                    // Set the media item to be played.
                    exoPlayer.setMediaItem(mediaItem)

                    // Prepare the player.
                    exoPlayer.prepare()

                    // Set to repeat (Just testing this out!)
                    exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

                    // Hide controller bar
                    useController = false

                    // Mute video for thumbnail
                    exoPlayer.volume = 0.0f

                    // Play
                    exoPlayer.play()
                }
            }
        )
        if (isHidden) {
            if (isMuted) {
                Icon(
                    painter = painterResource(R.drawable.volume_up_24px),
                    "Mute Icon", tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(0.dp, 8.dp, 8.dp, 0.dp)
                        .height(8.dp)
                        .width(8.dp)
                        .clickable {
                            isMuted = false
                            exoPlayer.volume = 0.5f
                        }
                )
            }
            if (!isMuted) {
                Icon(
                    painter = painterResource(R.drawable.volume_off_24px),
                    "Mute Icon", tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(0.dp, 8.dp, 8.dp, 0.dp)
                        .height(8.dp)
                        .width(8.dp)
                        .clickable {
                            isMuted = true
                            exoPlayer.volume = 0.0f
                        }
                )
            }
            if (!isPaused) {
                Icon(
                    painter = painterResource(R.drawable.pause_circle_24px),
                    "Play Icon", tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(0.dp, 0.dp, 0.dp, 16.dp)
                        .clickable {
                            isPaused = true
                            exoPlayer.pause()
                        }
                )
            }
            if (isPaused) {
                Icon(
                    painter = painterResource(R.drawable.play_circle_24px),
                    "Play Icon", tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(0.dp, 0.dp, 0.dp, 16.dp)
                        .clickable {
                            isPaused = false
                            exoPlayer.play()
                        }
                )
            }
            Icon(
                painter = painterResource(R.drawable.fast_rewind_24px),
                "Rewind Icon", tint = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(60.dp, 0.dp, 0.dp, 16.dp)
                    .clickable {
                        exoPlayer.seekTo(exoPlayer.currentPosition - 1000)
                    }
            )
            Icon(
                painter = painterResource(R.drawable.fast_forward_24px),
                "Fast Forward Icon", tint = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(0.dp, 0.dp, 60.dp, 16.dp)
                    .clickable {
                        exoPlayer.seekTo(exoPlayer.currentPosition + 1000)
                    }
            )
        }
    }
    return isTooLong
}


@OptIn(UnstableApi::class)
@Composable
fun videoPlayerForThumbnail(videoUri: Uri, maxDuration: Long, showSnackbar: () -> Unit): Boolean {
    val context = LocalContext.current
    val type = context.contentResolver.getType(videoUri)
    if (type == null || type.startsWith("image")) {
        return false
    }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    var thumbnailBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isVideoPlaying by remember { mutableStateOf(false) }

    // State to track whether the video is currently playing or paused
    var isTooLong by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var isHidden by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .clickable {
                isVideoPlaying = !isVideoPlaying
                if (isVideoPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
            }
    ) {
        DisposableEffect(exoPlayer) {
            onDispose {
                exoPlayer.release()
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(), // Aspect ratio 16:9
            factory = { contextOne ->
                PlayerView(contextOne).apply {
                    player = exoPlayer
                    // Set the resize mode to Aspect Fill
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    // Set the gravity of the controller to bottom
                    controllerShowTimeoutMs = 0
                    controllerAutoShow = false

                    // Set padding to prevent cutting off the video
                    contentDescription = "Video Player"

                    // Build the media item.
                    val mediaItem = MediaItem.fromUri(videoUri)

                    exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
//                    exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING

                    // Set the media item to be played.
                    exoPlayer.setMediaItem(mediaItem)

                    // Prepare the player.
                    exoPlayer.prepare()

                    // Set to repeat (Just testing this out!)
                    exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

                    // Hide controller bar
                    useController = false

                    // Mute video for thumbnail
                    exoPlayer.volume = 0.0f

                    // Play
                    Log.d(TAG, "We're playing the video now")
                    exoPlayer.play()
                }
            }
        )
//        if (isHidden) {
//            if (isMuted) {
//                Icon(
//                    painter = painterResource(R.drawable.volume_up_24px),
//                    "Mute Icon", tint = Color.White,
//                    modifier = Modifier
//                        .align(Alignment.TopEnd)
//                        .padding(0.dp, 8.dp, 8.dp, 0.dp)
//                        .height(8.dp)
//                        .width(8.dp)
//                        .clickable {
//                            isMuted = false
//                            exoPlayer.volume = 0.5f
//                        }
//                )
//            }
//            if (!isMuted) {
//                Icon(
//                    painter = painterResource(R.drawable.volume_off_24px),
//                    "Mute Icon", tint = Color.White,
//                    modifier = Modifier
//                        .align(Alignment.TopEnd)
//                        .padding(0.dp, 8.dp, 8.dp, 0.dp)
//                        .height(8.dp)
//                        .width(8.dp)
//                        .clickable {
//                            isMuted = true
//                            exoPlayer.volume = 0.0f
//                        }
//                )
//            }
//            if (!isPaused) {
//                Icon(
//                    painter = painterResource(R.drawable.pause_circle_24px),
//                    "Play Icon", tint = Color.White,
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .padding(0.dp, 0.dp, 0.dp, 16.dp)
//                        .clickable {
//                            isPaused = true
//                            exoPlayer.pause()
//                        }
//                )
//            }
//            if (isPaused) {
//                Icon(
//                    painter = painterResource(R.drawable.play_circle_24px),
//                    "Play Icon", tint = Color.White,
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .padding(0.dp, 0.dp, 0.dp, 16.dp)
//                        .clickable {
//                            isPaused = false
//                            exoPlayer.play()
//                        }
//                )
//            }
//            Icon(
//                painter = painterResource(R.drawable.fast_rewind_24px),
//                "Rewind Icon", tint = Color.White,
//                modifier = Modifier
//                    .align(Alignment.BottomStart)
//                    .padding(60.dp, 0.dp, 0.dp, 16.dp)
//                    .clickable {
//                        exoPlayer.seekTo(exoPlayer.currentPosition - 1000)
//                    }
//            )
//            Icon(
//                painter = painterResource(R.drawable.fast_forward_24px),
//                "Fast Forward Icon", tint = Color.White,
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .padding(0.dp, 0.dp, 60.dp, 16.dp)
//                    .clickable {
//                        exoPlayer.seekTo(exoPlayer.currentPosition + 1000)
//                    }
//            )
//        }
    }
    Log.d(
        TAG, "We're leaving the video player, so by now we should have " +
                "seen the logged account of it being played"
    )
    return isTooLong
}


/**
 * Composable function that displays an ExoPlayer to play a video using Jetpack Compose.
 *
 * @OptIn annotation to UnstableApi is used to indicate that the API is still experimental and may
 * undergo changes in the future.
 *
 * @see EXAMPLE_VIDEO_URI Replace with the actual URI of the video to be played.
 */
@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerView(uri: Uri) {
    Card(modifier = Modifier.fillMaxSize()) {
        // Get the current context
        val context = LocalContext.current

        // Initialize ExoPlayer
        val exoPlayer = ExoPlayer.Builder(context).build()

        // Create a MediaSource
        val mediaSource = remember(uri) {
            MediaItem.fromUri(uri)
        }

        // Set MediaSource to ExoPlayer
        LaunchedEffect(mediaSource) {
            exoPlayer.setMediaItem(mediaSource)
            exoPlayer.prepare()
        }

        // Manage lifecycle events
        DisposableEffect(Unit) {
            onDispose {
                exoPlayer.release()
            }
        }

        // Use AndroidView to embed an Android View (PlayerView) into Compose
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                }
            },
            modifier = Modifier
                .fillMaxSize()
        )
    }

}



