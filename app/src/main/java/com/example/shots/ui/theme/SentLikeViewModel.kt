package com.example.shots.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.SentLike
import com.example.shots.data.SentLikeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SentLikeUiState(
    val sentLikes: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SentLikeViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val sentLikeRepository: SentLikeRepository,
//    private val firebaseAuth: FirebaseAuth,
//    private val appDatabase: AppDatabase
) : ViewModel() {

//    val sentLikeDao = RoomModule.provideSentLikeDao(appDatabase)

    private val _uiState = MutableStateFlow(SentLikeUiState())
    val uiState: StateFlow<SentLikeUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = SentLikeUiState(isLoading = true)
        loadSentLikes()
        _uiState.value = SentLikeUiState(isLoading = false)
    }

     fun loadSentLikes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                sentLikeRepository.fetchUpdatedSentLikes().collect { returnedSentLikes ->
                    _uiState.value = SentLikeUiState(sentLikes = returnedSentLikes)
                }
            } catch (e: Exception) {
                _uiState.value =
                    SentLikeUiState(errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    fun removeSentLike(sentLikeId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                sentLikeRepository.removeSentLike(sentLikeId)
                loadSentLikes()
            }
        }
    }

    fun saveAndStoreSentLike(
        sentLikeId: String,
        sentLikeData: MutableMap<String, Any>
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                sentLikeRepository.saveAndStoreSentLike(sentLikeId, sentLikeData)
                loadSentLikes()
            }
        }
    }

    fun fetchSentLike(sentLikeId: String): SentLike {
        return sentLikeRepository.getSentLike(sentLikeId)
    }

    fun storeSentLike(sentLike: SentLike) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                sentLikeRepository.storeSentLike(sentLike)
            }
        }
    }

//    fun fetchSentLikeFromRoom(sentLikeId: String): SentLike {
//        return try {
//            val sentLike = sentLikeDao.findById(sentLikeId)
//            if (sentLike != null) {
//                sentLike
//            } else {
//                SentLike(sentLikeId, mutableListOf())
//            }
//        } catch (npe: NullPointerException) {
//            SentLike(sentLikeId, mutableListOf())
//        }
//    }

//    fun storeSentLikeInRoom(userId: String) {
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                val sentLikesList = getSentLikesFromFirebase(userId)
//                Log.d("SentLikeViewModel", "sentLikesList: $sentLikesList")
//                try {
//                    var sentLike = sentLikeDao.findById(userId)
//                    sentLike = sentLike.copy(
//                        sentLikeId = userId,
//                        sentLikes = sentLikesList.toMutableList()
//                    )
//                    sentLikeDao.insert(sentLike)
//                } catch (npe: java.lang.NullPointerException) {
//                    val sentLike = SentLike(userId, sentLikesList.toMutableList())
//                    Log.d("SentLikeViewModel", "sentLike was stored!")
//                    try {
//                        sentLikeDao.insert(sentLike)
//                    } catch (e: Exception) {
//                        Log.d("SentLikeViewModel", "sentLike failed to be stored!")
//                    }
//                }
//            }
//        }
//    }

//    suspend fun saveSentLikeToFirebase(
//        sentLikeId: String,
//        sentLikeData: MutableMap<String, Any>
//    ) {
//        viewModelScope.launch {
//            val userId = firebaseRepository.yourUserId
//            val success = firebaseRepository.writeSentLikeToFirebase(sentLikeId, sentLikeData)
//            if (success) {
//                Log.d("SentLikeViewModel", "sentLike saved!")
////                storeSentLikeInRoom(userId)
//            } else {
//                Log.d("SentLikeViewModel", "sentLike failed to be saved!")
//            }
//        }
//    }

//    suspend fun removeSentLikeFromFirebase(
//        sentLikeId: String
//    ) {
//        viewModelScope.launch {
//            val userId = firebaseAuth.currentUser?.displayName ?: ""
//            val success = firebaseRepository.deleteSentLikeFromFirebase(sentLikeId)
//            if (success) {
//                Log.d("SentLikeViewModel", "sentLike deleted!")
//                storeSentLikeInRoom(userId)
//            } else {
//                Log.d("SentLikeViewModel", "sentLike failed to be deleted!")
//            }
//        }
//    }

    suspend fun getSentLikesFromFirebase(sentLikeId: String): List<String> {
        return firebaseRepository.getSentLikesFromFirebase(sentLikeId)
    }

    suspend fun fetchUpdatedSentLikes(): Flow<List<String>> {
        return sentLikeRepository.fetchUpdatedSentLikes()
    }


}