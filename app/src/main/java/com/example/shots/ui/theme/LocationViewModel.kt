package com.example.shots.ui.theme

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.data.AppDatabase
import com.example.shots.data.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase
) : ViewModel() {
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    fun saveLocationToFirebase(
        userId: String, latitude: Double, longitude: Double,
        context: Context,
        usersViewModel: UsersViewModel
    ) {
        viewModelScope.launch {
            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()
            userData["latitude"] = latitude
            userData["longitude"] = longitude
            val success =
                firebaseRepository.writeUserDataToFirebase(userId, userData, mediaItems, context)
            if (success) {
                Log.d(ContentValues.TAG, "location added!")
                usersViewModel.storeUserInRoom(userId)
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