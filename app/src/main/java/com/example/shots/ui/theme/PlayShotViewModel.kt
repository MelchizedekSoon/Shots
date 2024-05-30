package com.example.shots.ui.theme

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.shots.RoomModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject

@HiltViewModel
class PlayShotViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase
) : ViewModel() {
    val userDao = RoomModule.provideUserDao(appDatabase)
    val receivedShotDao = RoomModule.provideReceiveShotDao(appDatabase)
    val sentShotDao = RoomModule.provideSentShotDao(appDatabase)


    @RequiresApi(Build.VERSION_CODES.Q)
    @OptIn(UnstableApi::class)
    fun saveShot(
        currentUserShot: String?,
        context: Context,
        isSaved: (Boolean) -> Unit,
        isNotSaved: (Boolean) -> Unit
    ): Unit {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val url = URL(currentUserShot)
                val fileName = "shot${
                    System.currentTimeMillis()
                }.mp4"
//                val directoryName = "Shots"

                val filePath = Paths.get(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        .toString(),
                    fileName
                )

                Files.createDirectories(filePath.parent)

                Files.copy(url.openStream(), filePath)

                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(
                        MediaStore.Video.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_DCIM
                    )
                }

                val resolver = context.contentResolver
                val collectionUri =
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = resolver.insert(collectionUri, contentValues)

                if (itemUri != null) {
                    val outputStream = resolver.openOutputStream(itemUri)

                    if (outputStream != null) {
                        Files.newInputStream(filePath).copyTo(outputStream)
                    }

                    outputStream?.close()

                    // Video file saved successfully
                    Log.d("PlayShotViewModel", "Should save your sent shot - true")
                    isSaved(true)
                } else {
                    // Failed to save video file
                    Log.d("PlayShotViewModel", "Should NOT save your sent shot - false")
                    isSaved(false)
                }
            }
        }
    }
}