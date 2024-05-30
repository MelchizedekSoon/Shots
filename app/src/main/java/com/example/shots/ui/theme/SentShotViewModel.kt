package com.example.shots.ui.theme

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.RoomModule
import com.example.shots.ViewModelModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.SentShot
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SentShotViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase
) : ViewModel() {
    private val usersViewModel =
        ViewModelModule.provideUsersViewModel(firebaseRepository, firebaseAuth, appDatabase)
    val userDao = RoomModule.provideUserDao(appDatabase)
    val sentShotDao = RoomModule.provideSentShotDao(appDatabase)


    fun storeSentShotInRoom(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val sentShotsList = getSentShotsFromFirebase(userId)
                try {
                    var sentShot = sentShotDao.findById(userId)
                    sentShot = sentShot.copy(
                        sentShotId = userId,
                        sentShots = sentShotsList.toMutableList()
                    )
                    sentShotDao.insert(sentShot)
                } catch (npe: java.lang.NullPointerException) {
                    val sentShot = SentShot(userId, sentShotsList.toMutableList())
                    try {
                        sentShotDao.insert(sentShot)
                    } catch(e: Exception) {
                        Log.d("SentShotViewModel", "storeSentShotInRoom: ${e.message}")
                    }
                }
            }
        }
    }

    suspend fun fetchSentShotFromRoom(sentShotId: String): SentShot {
        return withContext(Dispatchers.IO) {
            try {
                val sentShot = sentShotDao.findById(sentShotId)
                Log.d("SentShotViewModel", "fetchSent Succeeded!")
                sentShot
            } catch (npe: NullPointerException) {
                Log.d("SentShotViewModel", "fetchSent Failed!")
                SentShot(sentShotId, mutableListOf())
            }
        }
    }

    fun saveSentShotToFirebase(
        sentShotId: String,
        sentShotData: MutableMap<String, Uri>,
        context: Context,
        sentShotAdded: (Boolean) -> Unit,
        sentShotNotAdded: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var user = usersViewModel.getUser()
                val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
                val success = firebaseRepository.writeSentShotToFirebase(
                    sentShotId, sentShotData, context
                )
                if (success) {
                    Log.d("SentShotViewModel", "sentShot added!")

                    val receivingUser = usersViewModel.fetchUserFromRoom(sentShotId ?: "")

                    val userData: MutableMap<String, Any> = mutableMapOf()
                    val mediaItems: MutableMap<String, Uri> = mutableMapOf()

                    receivingUser.newShotsCount = receivingUser.newShotsCount?.plus(1)

                    Log.d("SentShotViewModel", "newShotsCount = ${receivingUser.newShotsCount}")

                    userData["newShotsCount"] = receivingUser.newShotsCount ?: 0

                    receivingUser.shotsCount = receivingUser.shotsCount?.plus(1)

                    userData["shotsCount"] = receivingUser.shotsCount ?: 0

                    usersViewModel.saveUserDataToFirebase(
                        receivingUser.id, userData, mediaItems,
                        context
                    ) {
                        viewModelScope.launch {
                            usersViewModel.storeUserInRoom(receivingUser.id)
                            storeSentShotInRoom(yourUserId)
                            sentShotAdded(true)
                        }
                    }

                } else {
                    Log.d("SentShotViewModel", "sentShot failed to be added!")
                    sentShotNotAdded(true)
                }
            }
        }
    }

    fun removeSentShotFromFirebase(
        context: Context,
        sentShotId: String,
        sentIsTrashed: (Boolean) -> Unit,
        sentIsNotTrashed: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
                val success = firebaseRepository.deleteSentShotFromFirebase(sentShotId)
                if (success) {
                    Log.d(ContentValues.TAG, "sentShot deleted!")

                    val receivingUser = usersViewModel.fetchUserFromRoom(sentShotId ?: "")

                    val userData: MutableMap<String, Any> = mutableMapOf()
                    val mediaItems: MutableMap<String, Uri> = mutableMapOf()

                    receivingUser.newShotsCount = receivingUser.newShotsCount?.minus(1)

                    if (receivingUser.newShotsCount!! < 0) {
                        receivingUser.newShotsCount = 0
                    }

                    userData["newShotsCount"] = receivingUser.newShotsCount ?: 0

                    receivingUser.shotsCount = receivingUser.shotsCount?.minus(1)

                    if (receivingUser.shotsCount!! < 0) {
                        receivingUser.shotsCount = 0
                    }

                    userData["shotsCount"] = receivingUser.shotsCount ?: 0

                    usersViewModel.saveUserDataToFirebase(
                        receivingUser.id, userData, mediaItems,
                        context
                    ) {
                        viewModelScope.launch {
                            usersViewModel.storeUserInRoom(receivingUser.id)
                            storeSentShotInRoom(yourUserId)
                            sentIsTrashed(true)
                        }
                    }


                } else {
                    Log.d(ContentValues.TAG, "sentShot failed to be deleted!")
                    sentIsNotTrashed(true)
                }
            }
        }
    }

    suspend fun getSentShotsFromFirebase(sentShotId: String): List<String> {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Inside getSentShotsFromFirebase")
            val sentShotList = firebaseRepository.getSentShotsFromFirebase(sentShotId)
            sentShotList
        }
    }

}