package com.example.shots.ui.theme

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.RoomModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.ReceivedLike
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReceivedLikeViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase
) : ViewModel() {
    val userDao = RoomModule.provideUserDao(appDatabase)
    val receivedLikeDao = RoomModule.provideReceiveLikeDao(appDatabase)

    val scope = CoroutineScope(Dispatchers.IO)

    fun storeReceivedLikeInRoom(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val receivedLikesList = getReceivedLikesFromFirebase(userId)

                try {
                    var receivedLike = receivedLikeDao.findById(userId)
                    receivedLike = receivedLike.copy(
                        receivedLikeId = userId,
                        receivedLikes = receivedLikesList.toMutableList()
                    )
                    receivedLikeDao.update(receivedLike)
                } catch (npe: java.lang.NullPointerException) {
                    val receivedLike = ReceivedLike(userId, receivedLikesList.toMutableList())
                    try {
                        receivedLikeDao.insert(receivedLike)
                    } catch(e: Exception) {
                        Log.d("ReceivedLikeViewModel", "receivedLike failed to be added!")
                    }
                }
            }
        }
    }

    fun fetchReceivedLikeFromRoom(receivedLikeId: String): ReceivedLike {
        return try {
            val receivedLike = receivedLikeDao.findById(receivedLikeId)
            if (receivedLike != null) {
                receivedLike
            } else {
                ReceivedLike(receivedLikeId, mutableListOf())
            }
        } catch (npe: NullPointerException) {
            ReceivedLike(receivedLikeId, mutableListOf())
        }
    }

    suspend fun saveReceivedLikeToFirebase(
        receivedLikeId: String,
        receivedLikeData: MutableMap<String, Any>
    ) {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.displayName ?: ""
            val success =
                firebaseRepository.writeReceivedLikeToFirebase(receivedLikeId, receivedLikeData)
            if (success) {
                Log.d(ContentValues.TAG, "receivedLike added!")
                storeReceivedLikeInRoom(userId)
            } else {
                Log.d(ContentValues.TAG, "receivedLike failed to be added!")
            }
        }
    }

    suspend fun removeReceivedLikeFromFirebase(
        receivedLikeId: String
    ) {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.displayName ?: ""
            val success = firebaseRepository.deleteReceivedLikeFromFirebase(receivedLikeId)
            if (success) {
                Log.d(ContentValues.TAG, "receivedLike deleted!")
                storeReceivedLikeInRoom(userId)
            } else {
                Log.d(ContentValues.TAG, "receivedLike failed to be deleted!")
            }
        }
    }

    suspend fun getReceivedLikesFromFirebase(receivedLikeId: String): List<String> {
        return firebaseRepository.getReceivedLikesFromFirebase(receivedLikeId)
    }


}