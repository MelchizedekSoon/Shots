package com.example.shots.ui.theme

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.example.shots.data.AuthRepository
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _emailText = mutableStateOf(TextFieldValue())
    var emailText: State<TextFieldValue> = _emailText

    private val _passwordText = mutableStateOf(TextFieldValue())
    var passwordText: State<TextFieldValue> = _passwordText

    fun onEmailTextChanged(newText: String) {
        _emailText.value = TextFieldValue(newText)
    }

    fun onPasswordTextChanged(newText: String) {
        _passwordText.value = TextFieldValue(newText)
    }

    suspend fun signInWithEmail(email: String, password: String): FirebaseUser? {
        return try {
            val authResult = authRepository.signInWithEmailPassword(email, password)
            if (authResult != null) {
                Log.d(TAG, "User sign in successful: ${authResult.user}")
                authResult.user
            } else {
                Log.e(TAG, "User sign in failed: Result is null")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error signing user", e)
            null
        }
    }

    suspend fun signInWithEmailPassword(email: String, password: String): AuthResult? {
        return authRepository.signInWithEmailPassword(email, password)
    }

}

