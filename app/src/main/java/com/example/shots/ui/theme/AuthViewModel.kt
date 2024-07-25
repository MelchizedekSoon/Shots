package com.example.shots.ui.theme

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.GetStreamClientModule
import com.example.shots.NetworkBoundResource
import com.example.shots.data.FirebaseRepository
import com.google.common.annotations.VisibleForTesting
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
) : ViewModel() {

//    private val _uiState = mutableStateOf<UiState>(UiState.Initial)
//    val uiState: State<UiState> = _uiState
//
//    sealed class UiState {
//        object Initial : UiState()
//        data class AuthSuccess(val user: FirebaseUser) : UiState()
//        object AuthFailed : UiState()
//    }

    private val _emailText = mutableStateOf(TextFieldValue())
    var emailText: State<TextFieldValue> = _emailText

    private val _passwordText = mutableStateOf(TextFieldValue())
    var passwordText: State<TextFieldValue> = _passwordText

    suspend fun signIn(email: String, password: String) : FirebaseUser? {
        Log.d("AuthViewModel", "signIn: $email")
        return signInWithEmailAndPassword(email, password)
    }

    fun onEmailTextChanged(newText: String) {
        _emailText.value = TextFieldValue(newText)
    }

    fun onPasswordTextChanged(newText: String) {
        _passwordText.value = TextFieldValue(newText)
    }

    // Function to handle authentication
    suspend fun signInWithEmailAndPassword(email: String, password: String): FirebaseUser? {
//        var user: FirebaseUser? = null
        return try {
            val authResult = firebaseRepository.signInWithEmailPassword(email, password)
            if (authResult != null) {
                Log.d("AuthViewModel", "User sign in successful: ${authResult.user}")
                authResult.user
            } else {
                Log.e("AuthViewModel", "User sign in failed: Result is null")
                null
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error signing user", e)
            null
        }
    }

    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String,
    ): FirebaseUser? {
        return try {
            val authResult = firebaseRepository.createUserWithEmailAndPassword(email, password)
            if (authResult != null) {
                Log.d("AuthViewModel", "User creation successful: ${authResult.user}")
                authResult.user
            } else {
                Log.e("AuthViewModel", "User creation failed: Result is null")
                null
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error creating user", e)
            null
        }
    }



}
