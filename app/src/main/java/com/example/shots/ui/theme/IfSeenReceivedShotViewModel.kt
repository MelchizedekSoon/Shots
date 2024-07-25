package com.example.shots.ui.theme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.IfSeenReceivedShot
import com.example.shots.data.IfSeenReceivedShotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class IfSeenReceivedShotUiState(
    var ifSeenReceivedShots: List<String> = emptyList()
)

@HiltViewModel
class IfSeenReceivedShotViewModel @Inject constructor(
    private val ifSeenReceivedShotRepository: IfSeenReceivedShotRepository,
    private val firebaseRepository: FirebaseRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _ifSeenReceivedShotUiState = MutableStateFlow(IfSeenReceivedShotUiState())
    val ifSeenReceivedShotUiState = _ifSeenReceivedShotUiState.asStateFlow()

    init {
        try {
            loadIfSeenReceivedShots()
        } catch (npe: NullPointerException) {
            Log.d("IfSeenReceivedShotViewModel", "loadReceivedShots: $npe")
        }
    }

    fun getYourUserId(): String {
        return firebaseRepository.getYourUserId()
    }

    fun loadIfSeenReceivedShots() {
        viewModelScope.launch(dispatcher) {
//            Log.d("IfSeenReceivedShotViewModel", "yourUserId = ${getYourUserId()}")
            try {
//                if (getYourUserId().isNotEmpty()) {
                fetchUpdatedIfSeenReceivedShots().collect { ifSeenReceivedShots ->
                    Log.d(
                        "IfSeenReceivedShotViewModel",
                        "ifSeenReceivedShots: $ifSeenReceivedShots"
                    )
                    Log.d(
                        "IfSeenReceivedShotViewModel",
                        "ifSeenReceivedShots: ${ifSeenReceivedShots.size}"
                    )
                    _ifSeenReceivedShotUiState.value =
                        IfSeenReceivedShotUiState(ifSeenReceivedShots)
                }
//                }
            } catch (npe: NullPointerException) {
                Log.d("IfSeenReceivedShotViewModel", "loadIfSeenReceivedShots: $npe")
            } catch (e: Exception) {
                Log.d("IfSeenReceivedShotViewModel", "loadIfSeenReceivedShots: $e")
            }
        }
    }

    fun saveIfSeenReceivedShot(
        ifSeenReceivedShotId: String,
        ifSeenReceivedShotData: MutableMap<String, Boolean>
    ) {
        viewModelScope.launch(dispatcher) {
            Log.d("IfSeenReceivedShotViewModel", "ifSeenReceivedShotId = $ifSeenReceivedShotId")
            Log.d("IfSeenReceivedShotViewModel", "ifSeenReceivedShotData = $ifSeenReceivedShotData")
            ifSeenReceivedShotRepository.saveIfSeenReceivedShot(
                ifSeenReceivedShotId, ifSeenReceivedShotData
            )
            loadIfSeenReceivedShots()
        }
    }


    fun removeIfSeenReceivedShot(
        ifSeenReceivedShotId: String
    ) {
        viewModelScope.launch(dispatcher) {
                ifSeenReceivedShotRepository.removeIfSeenReceivedShot(ifSeenReceivedShotId)
                loadIfSeenReceivedShots()
        }
    }

    suspend fun fetchUpdatedIfSeenReceivedShots(): Flow<List<String>> {
        return ifSeenReceivedShotRepository.fetchUpdatedIfSeenReceivedShots().filterNotNull()
    }

    fun storeReceivedShot(ifSeenReceivedShot: IfSeenReceivedShot) {
        viewModelScope.launch(dispatcher){
                ifSeenReceivedShotRepository.storeIfSeenReceivedShot(ifSeenReceivedShot)

        }
    }

    fun storeIfSeenReceivedShotInRoom(
        userId: String
    ) {
        viewModelScope.launch(dispatcher) {
                val ifSeenReceivedShotsList = getIfSeenReceivedShotsFromFirebase(userId)
                try {
                    var ifSeenReceivedShot =
                        ifSeenReceivedShotRepository.getIfSeenReceivedShot(userId)
                    ifSeenReceivedShot = ifSeenReceivedShot.copy(
                        ifSeenReceivedShotId = userId,
                        ifSeenReceivedShots = ifSeenReceivedShotsList.toMutableList()
                    )
                    ifSeenReceivedShotRepository.storeIfSeenReceivedShot(ifSeenReceivedShot)
                } catch (npe: java.lang.NullPointerException) {
                    val ifSeenReceivedShot =
                        IfSeenReceivedShot(userId, ifSeenReceivedShotsList.toMutableList())
                    try {
                        ifSeenReceivedShotRepository.storeIfSeenReceivedShot(ifSeenReceivedShot)
                    } catch (e: Exception) {
                        Log.d("IfSeenReceivedShotViewModel", "storeIfSeenReceivedShotInRoom: $e")
                    }
            }
        }
    }

    fun fetchIfSeenReceivedShotFromRoom(ifSeenReceivedShotId: String): IfSeenReceivedShot {
        return try {
            val ifSeenReceivedShot =
                ifSeenReceivedShotRepository.getIfSeenReceivedShot(ifSeenReceivedShotId)
            if (ifSeenReceivedShot != null) {
                ifSeenReceivedShot
            } else {
                IfSeenReceivedShot(ifSeenReceivedShotId, mutableListOf())
            }
        } catch (npe: NullPointerException) {
            IfSeenReceivedShot(ifSeenReceivedShotId, mutableListOf())
        }
    }

    private fun getIfSeenReceivedShotsFromFirebase(ifSeenReceivedShotId: String): List<String> {
        Log.d("IfSeenReceivedShotViewModel", "Inside getIfSeenReceivedShotsFromFirebase")
        val ifSeenReceivedShotList =
            ifSeenReceivedShotRepository.getIfSeenReceivedShot(ifSeenReceivedShotId).ifSeenReceivedShots
        Log.d(
            "IfSeenReceivedShotViewModel",
            "Inside ifSeenReceivedShotViewModel - the ifSeenReceivedShotList is $ifSeenReceivedShotList"
        )
        return ifSeenReceivedShotList
    }


}