package com.example.shots.ui.theme

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.RoomModule
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.LookingFor
import com.example.shots.data.User
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    var selectedOption by mutableStateOf<LookingFor?>(null)

//    var user = firebaseAuth.currentUser?.uid?.let {
//        com.example.shots.data.User(
//            it, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null, null, null, null, null, null
//        )
//    }
//
//    enum class UserField {
//        DISPLAY_NAME,
//        BIRTHDAY,
//        IMAGES,
//        VIDEOS,
//        PROFILE_VIDEO,
//        ABOUT_ME,
//        HEIGHT,
//        LOOKING_FOR,
//        GENDER,
//        SEXUAL_ORIENTATION,
//        WORK
//    }

//    @Composable
//    fun pickMedia() {
//        // Registers a photo picker activity launcher in single-select mode.
//        val pickMedia =
//            rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
//                // Callback is invoked after the user selects a media item or closes the
//                // photo picker.
//                if (uri != null) {
//                    Log.d("PhotoPicker", "Selected URI: $uri")
//                } else {
//                    Log.d("PhotoPicker", "No media selected")
//                }
//            }
//
//// Include only one of the following calls to launch(), depending on the types
//// of media that you want to let the user choose from.
//
//// Launch the photo picker and let the user choose images and videos.
//        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
//
//// Launch the photo picker and let the user choose only images.
//        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//
//// Launch the photo picker and let the user choose only videos.
//        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
//
//// Launch the photo picker and let the user choose only images/videos of a
//// specific MIME type, such as GIFs.
//        val mimeType = "image/gif"
//        pickMedia.launch(
//            PickVisualMediaRequest(
//                ActivityResultContracts.PickVisualMedia.SingleMimeType(
//                    mimeType
//                )
//            )
//        )
//    }


//    suspend fun getUserData(userId: String): User? {
////        Log.d(
////            TAG, "At the point of being in view model after being retrieved, here are the" +
////                    "values at that time, next is to go to the editProfileScreen where I will be used" +
////                    "by retrievedData - $userId"
////        )
//        return firebaseRepository.getUserData(userId)
//    }

//    fun saveUserDataToFirebase(
//        userId: String,
//        userData: Map<String, Any>,
//        mediaItems: Map<String, Uri>
//    ) {
//        viewModelScope.launch {
//            val success = firebaseRepository.writeUserDataToFirebase(userId, userData, mediaItems)
//            if (success) {
////                Log.d(
////                    TAG,
////                    "User data successfully added at the time the userData includes $userData" +
////                            "and the mediaItems include $mediaItems"
////                )
//                // Handle successful data save
//            } else {
////                Log.d(TAG, "User data not added")
//                // Handle data save error
//            }
//        }
//    }

//    fun deleteImageFromFirebase(mediaIdentifier: String) {
//        viewModelScope.launch {
//            val userId = firebaseAuth.currentUser?.uid
//            if (userId != null) {
//                firebaseRepository.deleteMediaFromFirebase(userId, mediaIdentifier)
////                Log.d(TAG, "If image was in DB at the $mediaIdentifier spot, it has been deleted")
//            } else {
////                Log.d(TAG, "userId is null so deleting the image is not possible")
//            }
//        }
//    }

//    suspend fun getMetadataFromRepo(uri: String, mediaIdentifier: String) : String {
//        return firebaseRepository.getMetadataFromStorage(mediaIdentifier)
//    }
//
//    @Composable
//    fun getUser() : User? {
//        val context = LocalContext.current
//        val appDatabase = RoomModule.provideAppDatabase(context)
//        val userDao = appDatabase.userDao()
//        val userId = firebaseAuth.currentUser?.uid ?: ""
//        LaunchedEffect(Unit) {
//            viewModelScope.launch(Dispatchers.IO) {
//                user = userDao.findById(userId)
//                val userList = userDao.getAll()
//                for(eachUser in userList) {
//                    Log.d(TAG, "Here's the data for each user - ${eachUser}")
//                }
//                Log.d(TAG, "This user returned ${user}")
//            }
//        }
//        return user
//    }


//    fun saveImageToStorage(imageUri: Uri, mediaIdentifier: String) {
//        viewModelScope.launch {
//            val success = firebaseRepository.uploadImageToStorage(imageUri, mediaIdentifier)
//            if (success) {
//                Log.d(TAG, "User image successfully added")
//                // Handle successful data save
//            } else {
//                Log.d(TAG, "User image not added")
//                // Handle data save error
//            }
//        }
//    }

//    fun saveVideoToStorage(videoUri: Uri, mediaIdentifier: String) {
//        viewModelScope.launch {
//            val success = firebaseRepository.uploadVideoToStorage(videoUri, mediaIdentifier)
//            if (success) {
//                Log.d(TAG, "User video successfully added")
//                // Handle successful data save
//            } else {
//                Log.d(TAG, "User video not added")
//                // Handle data save error
//            }
//        }
//    }

//    fun updateUserField(fieldName: UserField, value: Any?, userData: Map<String, Any>) {
//        user = when (fieldName) {
//            UserField.DISPLAY_NAME -> user?.copy(displayName = userData["displayName"] as String)
//            UserField.BIRTHDAY -> user?.copy(birthday = userData["birthday"] as Calendar)
//            UserField. -> user?.copy(mediaOne = userData["mediaOne"] as String)
//            UserField.IMAGES -> user?.copy(images = userData["images"] as List<String>)
//            UserField.VIDEOS -> user?.copy(videos = userData["videos"] as List<String>)
//            UserField.PROFILE_VIDEO -> user?.copy(profileVideo = userData["profileVideo"] as String)
//            UserField.ABOUT_ME -> user?.copy(aboutMe = userData["aboutMe"] as String)
//            UserField.HEIGHT -> user?.copy(height = userData["height"] as String)
//            UserField.LOOKING_FOR -> user?.copy(lookingFor = userData["lookingFor"] as LookingFor)
//            UserField.GENDER -> user?.copy(gender = userData["gender"] as Gender)
//            UserField.SEXUAL_ORIENTATION -> user?.copy(sexualOrientation = userData["sexualOrientation"] as SexualOrientation)
//            UserField.WORK -> user?.copy(work = userData["work"] as String)
//            else -> {}
//        }
//    }


//    @Composable
//    fun UpdateUserProfile(vararg changes: UserProfileChangeRequest.Builder) {
//        val user = firebaseAuth.currentUser
//        userProfileChangeRequest {
//            UserProfileChangeRequest.Builder()
//        }
//        val profileUpdates = userProfileChangeRequest {
//            changes.forEach { updateBuilder ->
//                updateBuilder.build()
//            }
//        }
//        LaunchedEffect(Unit) {
//            try {
//                user?.updateProfile(profileUpdates)
//                Log.d(TAG, "User profile updated.")
//            } catch (e: Exception) {
//                // Handle the exception and provide appropriate feedback to the user
//                Log.e(TAG, "Failed to update user profile: ${e.message}")
//            }
//        }
//    }
}