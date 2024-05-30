package com.example.shots.ui.theme

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.RoomModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.ReceivedShot
import com.example.shots.data.SentLike
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SentLikeViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase
) : ViewModel() {

    val sentLikeDao = RoomModule.provideSentLikeDao(appDatabase)

    fun fetchSentLikeFromRoom(sentLikeId: String): SentLike {
        return try {
            val sentLike = sentLikeDao.findById(sentLikeId)
            if (sentLike != null) {
                sentLike
            } else {
                SentLike(sentLikeId, mutableListOf())
            }
        } catch (npe: NullPointerException) {
            SentLike(sentLikeId, mutableListOf())
        }
    }

    fun storeSentLikeInRoom(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val sentLikesList = getSentLikesFromFirebase(userId)
                Log.d("SentLikeViewModel", "sentLikesList: $sentLikesList")
                try {
                    var sentLike = sentLikeDao.findById(userId)
                    sentLike = sentLike.copy(
                        sentLikeId = userId,
                        sentLikes = sentLikesList.toMutableList()
                    )
                    sentLikeDao.insert(sentLike)
                } catch (npe: java.lang.NullPointerException) {
                    val sentLike = SentLike(userId, sentLikesList.toMutableList())
                    Log.d("SentLikeViewModel", "sentLike was stored!")
                    try {
                        sentLikeDao.insert(sentLike)
                    } catch(e: Exception) {
                        Log.d("SentLikeViewModel", "sentLike failed to be stored!")
                    }
                }
            }
        }
    }

    suspend fun saveSentLikeToFirebase(
        sentLikeId: String,
        sentLikeData: MutableMap<String, Any>
    ) {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.displayName ?: ""
            val success = firebaseRepository.writeSentLikeToFirebase(sentLikeId, sentLikeData)
            if (success) {
                Log.d("SentLikeViewModel", "sentLike saved!")
                storeSentLikeInRoom(userId)
            } else {
                Log.d("SentLikeViewModel", "sentLike failed to be saved!")
            }
        }
    }

    suspend fun removeSentLikeFromFirebase(
        sentLikeId: String
    ) {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.displayName ?: ""
            val success = firebaseRepository.deleteSentLikeFromFirebase(sentLikeId)
            if (success) {
                Log.d("SentLikeViewModel", "sentLike deleted!")
                storeSentLikeInRoom(userId)
            } else {
                Log.d("SentLikeViewModel", "sentLike failed to be deleted!")
            }
        }
    }

    suspend fun getSentLikesFromFirebase(sentLikeId: String): List<String> {
        return firebaseRepository.getSentLikesFromFirebase(sentLikeId)
    }


}