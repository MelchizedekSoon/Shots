package com.example.shots.ui.theme

import android.Manifest
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.shots.CameraPreviewScreen
import com.example.shots.FirebaseModule
import com.example.shots.RoomModule
import com.example.shots.ViewModelModule
import com.example.shots.cameraPermissionRequest
import com.example.shots.data.User

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CameraScreen(
    navController: NavController, userId: String?,
    receivedShotViewModel: ReceivedShotViewModel,
    sentShotViewModel: SentShotViewModel
) {
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    val firestore = FirebaseModule.provideFirestore()
    val firebaseStorage = FirebaseModule.provideStorage()
    val firebaseRepository =
        FirebaseModule.provideFirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    val editProfileViewModel =
        ViewModelModule.provideEditProfileViewModel(firebaseRepository, firebaseAuth)
    val appDatabase = RoomModule.provideAppDatabase(LocalContext.current)
    val userDao = RoomModule.provideUserDao(appDatabase)
    val bookmarkDao = RoomModule.provideBookmarkDao(appDatabase)
    val receivedLikeDao = RoomModule.provideReceiveLikeDao(appDatabase)
    val sentLikeDao = RoomModule.provideSentLikeDao(appDatabase)
    var yourUser by remember { mutableStateOf<User?>(null) }

    if (cameraPermissionRequest()) {
        val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
        CameraPreviewScreen(
            navController, userId,
            yourUserId,
            receivedShotViewModel,
            sentShotViewModel
        )
    }
}

private fun requestCameraPermission(launcher: ActivityResultLauncher<String>) {
    val permission = Manifest.permission.CAMERA
    launcher.launch(permission)
}