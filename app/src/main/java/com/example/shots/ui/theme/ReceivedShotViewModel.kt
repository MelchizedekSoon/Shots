package com.example.shots.ui.theme

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.NetworkBoundResource
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.ReceivedShot
import com.example.shots.data.ReceivedShotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ReceivedShotUiState(
    var receivedShots: List<String> = emptyList()
)

@HiltViewModel
class ReceivedShotViewModel @Inject constructor(
    private val receivedShotRepository: ReceivedShotRepository,
    private val firebaseRepository: FirebaseRepository,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _receivedShotUiState = MutableStateFlow(ReceivedShotUiState())
    val receivedShotUiState = _receivedShotUiState.asStateFlow()

    init {
        try {
            loadReceivedShots()
        } catch (npe: NullPointerException) {
            Log.d("ReceivedShotViewModel", "loadReceivedShots: $npe")
        }
    }


    fun loadReceivedShots() {
        viewModelScope.launch(dispatcher) {
            try {
                Log.d("ReceivedShotViewModel", "loadReceivedShots: ${receivedShotRepository.getYourUserId()}")
                if (receivedShotRepository.getYourUserId().isNotEmpty()) {
                    fetchUpdatedReceivedShots().collect { receivedShots ->
                        Log.d("ReceivedShotViewModel", "receivedShots: $receivedShots")
                        _receivedShotUiState.value = ReceivedShotUiState(receivedShots)
                    }
                }
            } catch (npe: NullPointerException) {
                Log.d("ReceivedShotViewModel", "loadReceivedShots: $npe")
            } catch (e: Exception) {
                Log.d("ReceivedShotViewModel", "loadReceivedShots: $e")
            }
        }
    }

    fun saveReceivedShot(
        receivedShotId: String,
        receivedShotData: MutableMap<String, Uri>,
        context: Context
    ) {
        viewModelScope.launch(dispatcher) {
                receivedShotRepository.saveReceivedShot(
                    receivedShotId, receivedShotData, context
                )
                loadReceivedShots()
            if (dispatcher !is TestDispatcher) { // Check if NOT a TestDispatcher
                NetworkBoundResource().requestForShotNotification(context, receivedShotId)
            }
        }
    }


    fun removeReceivedShot(
        receivedShotId: String
    ) {
        viewModelScope.launch(dispatcher) {
                receivedShotRepository.removeReceivedShot(receivedShotId)
                loadReceivedShots()
        }
    }

    fun removeTheirReceivedShot(
        receivedShotId: String
    ) {
        viewModelScope.launch(dispatcher) {
                receivedShotRepository.removeTheirReceivedShotFromFirebase(receivedShotId)
                loadReceivedShots()
        }
    }

     suspend fun fetchUpdatedReceivedShots(): Flow<List<String>> {
        return receivedShotRepository.fetchUpdatedReceivedShots().filterNotNull()
    }

    fun storeReceivedShot(receivedShot: ReceivedShot) {
        viewModelScope.launch(dispatcher) {
                receivedShotRepository.storeReceivedShot(receivedShot)
        }
    }

    fun storeReceivedShotInRoom(
        shouldShowReceivedShotNotification: Boolean,
        context: Context,
        userId: String
    ) {
        viewModelScope.launch(dispatcher) {
                val receivedShotsList = getReceivedShotsFromFirebase(userId)
                try {
                    var receivedShot = receivedShotRepository.getReceivedShot(userId)
                    receivedShot = receivedShot.copy(
                        receivedShotId = userId,
                        receivedShots = receivedShotsList.toMutableList()
                    )
                    receivedShotRepository.storeReceivedShot(receivedShot)
                    if (shouldShowReceivedShotNotification) {
                        NetworkBoundResource().requestForShotNotification(context, userId)
                    }
                } catch (npe: java.lang.NullPointerException) {
                    val receivedShot = ReceivedShot(userId, receivedShotsList.toMutableList())
                    try {
                        receivedShotRepository.storeReceivedShot(receivedShot)
                        if (shouldShowReceivedShotNotification) {
                            NetworkBoundResource().requestForShotNotification(context, userId)
                        }
                    } catch (e: Exception) {
                        Log.d("ReceivedShotViewModel", "storeReceivedShotInRoom: $e")
                    }
                }
        }
    }

    fun fetchReceivedShotFromRoom(receivedShotId: String): ReceivedShot {
        return try {
            val receivedShot = receivedShotRepository.getReceivedShot(receivedShotId)
            if (receivedShot != null) {
                receivedShot
            } else {
                ReceivedShot(receivedShotId, mutableListOf())
            }
        } catch (npe: NullPointerException) {
            ReceivedShot(receivedShotId, mutableListOf())
        }
    }

    private suspend fun getReceivedShotsFromFirebase(receivedShotId: String): List<String> {
        Log.d("ReceivedShotViewModel", "Inside getReceivedShotsFromFirebase")
        var receivedShotList = firebaseRepository.getReceivedShotsFromFirebase(receivedShotId)
        Log.d(
            "ReceivedShotViewModel",
            "Inside receivedShotViewModel - the receivedShotList is $receivedShotList"
        )
        return receivedShotList
    }


}