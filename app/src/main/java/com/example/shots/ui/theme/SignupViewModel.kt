package com.example.shots.ui.theme

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import com.example.shots.R
import com.example.shots.data.AuthRepository
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.User
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository,
) : ViewModel() {

    private val _emailText = mutableStateOf(TextFieldValue())
    var emailText: State<TextFieldValue> = _emailText

    private val _passwordText = mutableStateOf(TextFieldValue())
    var passwordText: State<TextFieldValue> = _passwordText

    private val _usernameText = mutableStateOf(TextFieldValue())
    var usernameText: State<TextFieldValue> = _usernameText

    private val _birthdayText = mutableStateOf(TextFieldValue())
    var birthdayText: State<TextFieldValue> = _birthdayText

    suspend fun createUser(email: String, password: String): FirebaseUser? {
        Log.d(TAG, "Inside create user")
        return authRepository.createUserWithEmailAndPassword(email, password)
    }

    // Use MutableState to hold the user data for sign-up
    private val _signUpUser = mutableStateOf(com.example.shots.data.User())

    // Update the sign-up user data using a lambda expression
    fun updateSignUpUser(update: (com.example.shots.data.User) -> com.example.shots.data.User) {
        _signUpUser.value = update(_signUpUser.value)
    }

    // Get the sign-up user data
    fun getSignUpUser(): User {
        return _signUpUser.value
    }


    fun onEmailTextChanged(newText: String) {
        _emailText.value = TextFieldValue(newText)
    }

    fun onPasswordTextChanged(newText: String) {
        _passwordText.value = TextFieldValue(newText)
    }

    object BannedWords {

        fun getBannedWords(context: Context): HashSet<String> {
            val swearWordsSet = HashSet<String>()
            val inputStream: InputStream = context.resources.openRawResource(R.raw.bannedwords)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = reader.readLine()
            while (line != null) {
                swearWordsSet.add(line.lowercase(Locale.ROOT))
                line = reader.readLine()
            }
            reader.close()
            return swearWordsSet
        }
    }

    suspend fun checkUsernames(input: String): Boolean {
        val users = firebaseRepository.getUsers()
        users.find { user ->
            return user.userName == input
        }
        return false
    }

    suspend fun checkForBannedWords(context: Context, input: String): Boolean {
        val bannedWords = BannedWords.getBannedWords(context)
        val regex = bannedWords.joinToString("|").toRegex(RegexOption.IGNORE_CASE)
        // Username contains banned words
        Log.d(TAG, "Checking for banned words")
        return regex.containsMatchIn(input)
    }

    suspend fun checkForAvailability(context: Context, input: String): Boolean {
//        val bannedWords = BannedWords.getBannedWords(context)
//        val regex = bannedWords.joinToString("|").toRegex(RegexOption.IGNORE_CASE)
//        if (regex.containsMatchIn(input)) {
//            return false // Username contains banned words
//        }
        if (input.contains(" ")) {
            return false // Username contains white space
        }
        if (input.isEmpty() || input.length > 30) {
            return false
        }
        val allowedCharactersRegex = """^[a-zA-Z0-9]+$""".toRegex()
//        val allowedCharactersRegex =
//            """^[a-zA-Z0-9!\\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~]+$""".toRegex()
        if (!allowedCharactersRegex.matches(input)) {
            return false // Username contains disallowed characters
        }
        // Check for username availability if no banned words, no white space, and allowed characters
        return !checkUsernames(input)
    }

}