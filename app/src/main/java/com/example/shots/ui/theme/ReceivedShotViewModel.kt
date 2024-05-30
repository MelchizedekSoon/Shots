package com.example.shots.ui.theme

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.NetworkBoundResource
import com.example.shots.RoomModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.ReceivedShot
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReceivedShotViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase
) : ViewModel() {
    val userDao = RoomModule.provideUserDao(appDatabase)
    val receivedShotDao = RoomModule.provideReceiveShotDao(appDatabase)

    val scope = CoroutineScope(Dispatchers.IO)

    fun storeReceivedShotInRoom(
        shouldShowReceivedShotNotification: Boolean,
        context: Context,
        userId: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val receivedShotsList = getReceivedShotsFromFirebase(userId)

                try {
                    var receivedShot = receivedShotDao.findById(userId)
                    receivedShot = receivedShot.copy(
                        receivedShotId = userId,
                        receivedShots = receivedShotsList.toMutableList()
                    )
                    receivedShotDao.insert(receivedShot)
                    if (shouldShowReceivedShotNotification) {
                        NetworkBoundResource().requestForShotNotification(context, userId)
                    }
                } catch (npe: java.lang.NullPointerException) {
                    val receivedShot = ReceivedShot(userId, receivedShotsList.toMutableList())
                    try {
                        receivedShotDao.insert(receivedShot)
                        if (shouldShowReceivedShotNotification) {
                            NetworkBoundResource().requestForShotNotification(context, userId)
                        }
                    } catch(e: Exception) {
                        Log.d("ReceivedShotViewModel", "storeReceivedShotInRoom: $e")
                    }
                }
            }
        }
    }

    fun fetchReceivedShotFromRoom(receivedShotId: String): ReceivedShot {
        return try {
            val receivedShot = receivedShotDao.findById(receivedShotId)
            if (receivedShot != null) {
                receivedShot
            } else {
                ReceivedShot(receivedShotId, mutableListOf())
            }
        } catch (npe: NullPointerException) {
            ReceivedShot(receivedShotId, mutableListOf())
        }
    }

    fun saveReceivedShotToFirebase(
        shouldShowSendShotNotification: Boolean,
        receivedShotId: String,
        receivedShotData: MutableMap<String, Uri>,
        context: Context,
        receivedShotAdded: (Boolean) -> Unit,
        receivedShotNotAdded: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val success = firebaseRepository.writeReceivedShotToFirebase(
                    receivedShotId,
                    receivedShotData,
                    context
                )
                if (success) {
                    val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
                    receivedShotAdded(true)
                    Log.d("ReceivedShotViewModel", "receivedShot added!")
                    storeReceivedShotInRoom(false, context, yourUserId)
                    NetworkBoundResource().requestForShotNotification(context, receivedShotId)
                } else {
                    receivedShotNotAdded(true)
                    Log.d("ReceivedShotViewModel", "receivedShot failed to be added!")
                }
            }
        }
    }

    fun removeReceivedShotFromFirebase(
        context: Context,
        receivedShotId: String,
        receivedIsTrashed: (Boolean) -> Unit,
        receivedIsNotTrashed: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
                val success = firebaseRepository.deleteReceivedShotFromFirebase(receivedShotId)
                if (success) {
                    storeReceivedShotInRoom(false, context, yourUserId)
                    Log.d("ReceivedShotViewModel", "receivedShot-$receivedShotId deleted!")
                    receivedIsTrashed(true)
                } else {
                    storeReceivedShotInRoom(false, context, yourUserId)
                    Log.d("ReceivedShotViewModel", "receivedShot-$receivedShotId failed to be deleted!")
                    receivedIsNotTrashed(true)
                }
            }
        }
    }

    suspend fun getReceivedShotsFromFirebase(receivedShotId: String): List<String> {
        var receivedShotList = emptyList<String>()
        Log.d("ReceivedShotViewModel", "Inside getReceivedShotsFromFirebase")
        receivedShotList = firebaseRepository.getReceivedShotsFromFirebase(receivedShotId)
        Log.d(
            "ReceivedShotViewModel",
            "Inside receivedShotViewModel - the receivedShotList is $receivedShotList"
        )
        return receivedShotList
    }

}