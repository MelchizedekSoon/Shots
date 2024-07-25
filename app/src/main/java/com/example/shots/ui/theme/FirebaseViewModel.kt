package com.example.shots.ui.theme

import androidx.lifecycle.ViewModel
import com.example.shots.data.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FirebaseViewModel @Inject constructor(
    val firebaseRepository: FirebaseRepository
) : ViewModel() {

    fun logOut() {
        firebaseRepository.logOut()
    }

    fun getYourUserId(): String {
        return firebaseRepository.getYourUserId()
    }

}