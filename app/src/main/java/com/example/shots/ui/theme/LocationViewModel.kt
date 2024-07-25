package com.example.shots.ui.theme

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.User
import com.example.shots.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LocationUiState(
    var userId: String,
    val latitude: Double,
    val longitude: Double
)

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val userRepository: UserRepository,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    fun saveAndStoreLocation(
        userId: String, latitude: Double, longitude: Double,
        context: Context,
        userViewModel: UserViewModel
    ) {
        viewModelScope.launch(dispatcher) {
            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()
            userData["latitude"] = latitude
            userData["longitude"] = longitude
            val success =
                firebaseRepository.writeUserDataToFirebase(userId, userData, mediaItems, context)
            if (success) {
                Log.d("LocationViewModel", "location added!")
                var user: User? = null
                userRepository.getCurrentUser().collect { returnedUser ->
                    user = returnedUser
                }
                if (user != null) {
                    userViewModel.updateUser(user!!)
                }
            } else {
                Log.d(ContentValues.TAG, "Location failed to be added!")
            }
        }
    }

    fun calculateDistance(
        userLatitude: Double, userLongitude: Double,
        otherUserLatitude: Double, otherUserLongitude: Double
    ): Double {
        val earthRadius = 6371 // Radius of the Earth in kilometers

        val dLat = Math.toRadians(otherUserLatitude - userLatitude)
        val dLon = Math.toRadians(otherUserLongitude - userLongitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                Math.cos(Math.toRadians(userLatitude)) * cos(Math.toRadians(otherUserLatitude)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

}