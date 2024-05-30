package com.example.shots.ui.theme

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.example.shots.FirebaseModule
import com.example.shots.GetStreamClientModule
import com.example.shots.NetworkBoundResource
import com.example.shots.ViewModelModule
import com.example.shots.data.FirebaseRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
) : ViewModel() {
    private val firestore = FirebaseModule.provideFirestore()
    private val authViewModel = ViewModelModule.
    provideAuthViewModel(firebaseRepository, firestore)
    private val _emailText = mutableStateOf(TextFieldValue())
    var emailText: State<TextFieldValue> = _emailText

    private val _passwordText = mutableStateOf(TextFieldValue())
    var passwordText: State<TextFieldValue> = _passwordText

    suspend fun signIn(email: String, password: String) : FirebaseUser? {
        Log.d("LoginViewModel", "signIn: $email")
        return authViewModel.signInWithEmail(email, password)
    }

    fun onEmailTextChanged(newText: String) {
        _emailText.value = TextFieldValue(newText)
    }

    fun onPasswordTextChanged(newText: String) {
        _passwordText.value = TextFieldValue(newText)
    }

}

