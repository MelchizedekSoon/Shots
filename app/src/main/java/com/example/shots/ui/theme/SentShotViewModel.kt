package com.example.shots.ui.theme

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.shots.RemoveSentShotWorker
import com.example.shots.data.AppDatabase
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.SentShot
import com.example.shots.data.SentShotRepository
import com.example.shots.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SentShotUiState(
    val sentShots: List<String> = emptyList()
)

@HiltViewModel
class SentShotViewModel @Inject constructor(
    private val sentShotRepository: SentShotRepository,
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _sentShotUiState = MutableStateFlow(SentShotUiState())
    val sentShotUiState: StateFlow<SentShotUiState> = _sentShotUiState.asStateFlow()

    init {
        try {
            loadSentShots()
        } catch (npe: NullPointerException) {
            Log.d("SentShotViewModel", "loadSentShots: ${npe.message}")
        }
    }

    fun loadSentShots() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                sentShotRepository.fetchUpdatedSentShots().collect { sentShots ->
                    _sentShotUiState.value = SentShotUiState(sentShots)
                }
            } catch (npe: NullPointerException) {
                Log.d("SentShotViewModel", "loadSentShots: ${npe.message}")
            } catch(e: Exception) {
                Log.d("SentShotViewModel", "loadSentShots: ${e.message}")
            }
        }
    }

    fun saveSentShot(
        sentShotId: String,
        sentShotData: MutableMap<String, Uri>,
        context: Context
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                sentShotRepository.saveSentShot(sentShotId, sentShotData, context)

                val receivingUser = userRepository.getUser(sentShotId)

                val userData: MutableMap<String, Any> = mutableMapOf()
                val mediaItems: MutableMap<String, Uri> = mutableMapOf()

                receivingUser.newShotsCount = receivingUser.newShotsCount?.plus(1)

                Log.d("SentShotViewModel", "newShotsCount = ${receivingUser.newShotsCount}")

                userData["newShotsCount"] = receivingUser.newShotsCount ?: 0

                receivingUser.shotsCount = receivingUser.shotsCount?.plus(1)

                userData["shotsCount"] = receivingUser.shotsCount ?: 0

                userRepository.saveUserData(
                    receivingUser.id,
                    userData, mediaItems, context
                )

                loadSentShots()
            }
        }
    }

    fun removeSentShot(
        context: Context,
        sentShotId: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                sentShotRepository.removeSentShot(sentShotId)

                Log.d("SentShotViewModel", "sentShot deleted!")

                val receivingUser = userRepository.getUser(sentShotId)

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

                userRepository.saveUserData(
                    receivingUser.id,
                    userData, mediaItems, context
                )

                loadSentShots()

            }
        }
    }

    fun storeSentShot(sentShot: SentShot) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                sentShotRepository.storeSentShot(sentShot)
            }
        }
    }

//    fun storeSentShotInRoom(userId: String) {
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                val sentShotsList = getSentShotsFromFirebase(userId)
//                try {
//                    var sentShot = sentShotDao.findById(userId)
//                    sentShot = sentShot.copy(
//                        sentShotId = userId,
//                        sentShots = sentShotsList.toMutableList()
//                    )
//                    sentShotDao.upsert(sentShot)
//                } catch (npe: java.lang.NullPointerException) {
//                    val sentShot = SentShot(userId, sentShotsList.toMutableList())
//                    try {
//                        sentShotDao.upsert(sentShot)
//                    } catch (e: Exception) {
//                        Log.d("SentShotViewModel", "storeSentShotInRoom: ${e.message}")
//                    }
//                }
//            }
//        }
//    }
//
//    suspend fun fetchSentShotFromRoom(sentShotId: String): SentShot {
//        return withContext(Dispatchers.IO) {
//            try {
//                val sentShot = sentShotDao.findById(sentShotId)
//                Log.d("SentShotViewModel", "fetchSent Succeeded!")
//                sentShot
//            } catch (npe: NullPointerException) {
//                Log.d("SentShotViewModel", "fetchSent Failed!")
//                SentShot(sentShotId, mutableListOf())
//            }
//        }
//    }

    suspend fun getSentShotsFromFirebase(sentShotId: String): List<String> {
        return withContext(Dispatchers.IO) {
            Log.d("SentShotViewModel", "Inside getSentShotsFromFirebase")
            val sentShotList = firebaseRepository.getSentShotsFromFirebase(sentShotId)
            sentShotList
        }
    }

    suspend fun fetchUpdatedSentShots(): Flow<List<String>> {
        return sentShotRepository.fetchUpdatedSentShots()
    }

}