package com.example.shots.ui.theme

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.NetworkBoundResource
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.ReceivedLike
import com.example.shots.data.ReceivedLikeRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import javax.inject.Inject

data class ReceivedLikeUiState(
    val receivedLikes: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReceivedLikeViewModel @Inject constructor(
    private val receivedLikeRepository: ReceivedLikeRepository,
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceivedLikeUiState())
    val uiState: StateFlow<ReceivedLikeUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = ReceivedLikeUiState(isLoading = true)
        try {
            loadReceivedLikes()
        } catch (npe: NullPointerException) {
            Log.d("ReceivedLikeViewModel", "receivedLike failed to be loaded!")
        }
        _uiState.value = ReceivedLikeUiState(isLoading = false)
    }

    fun loadReceivedLikes() {
        viewModelScope.launch(dispatcher) {
            try {
                receivedLikeRepository.fetchUpdatedReceivedLikes()
                    .collect { returnedReceivedLikes ->
                        _uiState.value = ReceivedLikeUiState(receivedLikes = returnedReceivedLikes)
                    }
            } catch (e: Exception) {
                _uiState.value =
                    ReceivedLikeUiState(errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    fun getYourUserId(): String {
        return receivedLikeRepository.getYourUserId()
    }

    suspend fun fetchUpdatedReceivedLikes(): Flow<List<String>> {
        return receivedLikeRepository.fetchUpdatedReceivedLikes()
    }

    suspend fun getReceivedLikes(): Flow<List<String>> {
        try {
            return receivedLikeRepository.getReceivedLikes()
        } catch (npe: NullPointerException) {
            return flowOf(emptyList())
        }
    }

    fun storeReceivedLikeInRoom(receivedLike: ReceivedLike) {
        viewModelScope.launch(dispatcher) {
            receivedLikeRepository.storeReceivedLike(receivedLike)

//                val receivedLikesList = getReceivedLikesFromFirebase(userId)
//
//                try {
//                    var receivedLike = receivedLikeDao.findById(userId)
//                    receivedLike = receivedLike.copy(
//                        receivedLikeId = userId,
//                        receivedLikes = receivedLikesList.toMutableList()
//                    )
//                    receivedLikeDao.update(receivedLike)
//                } catch (npe: java.lang.NullPointerException) {
//                    val receivedLike = ReceivedLike(userId, receivedLikesList.toMutableList())
//                    try {
//                        receivedLikeDao.insert(receivedLike)
//                    } catch(e: Exception) {
//                        Log.d("ReceivedLikeViewModel", "receivedLike failed to be added!")
//                    }
//                }
//            }

        }

//        fun fetchReceivedLikeFromRoom(receivedLikeId: String): ReceivedLike {
//            return try {
//                val receivedLike = receivedLikeDao.findById(receivedLikeId)
//                if (receivedLike != null) {
//                    receivedLike
//                } else {
//                    ReceivedLike(receivedLikeId, mutableListOf())
//                }
//            } catch (npe: NullPointerException) {
//                ReceivedLike(receivedLikeId, mutableListOf())
//            }
//        }

//        suspend fun saveReceivedLikeToFirebase(
//            receivedLikeId: String,
//            receivedLikeData: MutableMap<String, Any>
//        ) {
//            viewModelScope.launch {
//                val userId = firebaseAuth.currentUser?.displayName ?: ""
//                val success =
//                    firebaseRepository.writeReceivedLikeToFirebase(receivedLikeId, receivedLikeData)
//                if (success) {
//                    Log.d(ContentValues.TAG, "receivedLike added!")
//                    storeReceivedLikeInRoom(userId)
//                } else {
//                    Log.d(ContentValues.TAG, "receivedLike failed to be added!")
//                }
//            }
//        }

//        suspend fun removeReceivedLikeFromFirebase(
//            receivedLikeId: String
//        ) {
//            viewModelScope.launch {
//                val userId = firebaseAuth.currentUser?.displayName ?: ""
//                val success = firebaseRepository.deleteReceivedLikeFromFirebase(receivedLikeId)
//                if (success) {
//                    Log.d(ContentValues.TAG, "receivedLike deleted!")
//                    storeReceivedLikeInRoom(userId)
//                } else {
//                    Log.d(ContentValues.TAG, "receivedLike failed to be deleted!")
//                }
//            }
//        }

        suspend fun getReceivedLikesFromFirebase(receivedLikeId: String): List<String> {
            return firebaseRepository.getReceivedLikesFromFirebase(receivedLikeId)
        }


    }

    fun removeReceivedLike(receivedLikeId: String) {
        viewModelScope.launch(dispatcher) {
            receivedLikeRepository.removeReceivedLike(receivedLikeId)
            loadReceivedLikes()
        }
    }

    fun saveAndStoreReceivedLike(
        receivedLikeId: String,
        receivedLikeData: MutableMap<String, Any>,
        context: Context
    ) {
        viewModelScope.launch(dispatcher) {
            receivedLikeRepository.saveAndStoreReceivedLike(
                getYourUserId(),
                receivedLikeId,
                receivedLikeData
            )
            loadReceivedLikes()
            if (dispatcher !is TestDispatcher) { // Check if NOT a TestDispatcher
                NetworkBoundResource().requestForLikeNotification(context, receivedLikeId)
            }
        }
    }


}