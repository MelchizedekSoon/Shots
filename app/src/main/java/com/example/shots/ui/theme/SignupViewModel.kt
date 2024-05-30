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
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.User
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale
import javax.inject.Inject

enum class Screen {
    AGE,
    ABOUT_ME,
    DETAILS,
    DISPLAY_NAME,
    ESSENTIALS,
    HABITS,
    LINK,
    MEDIA,
    PROFILE_VIDEO,
    PROMPTS,
    USER_NAME,
    SIGN_UP
}

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {
    private val _emailText = mutableStateOf(TextFieldValue())
    var emailText: State<TextFieldValue> = _emailText

    private val _passwordText = mutableStateOf(TextFieldValue())
    var passwordText: State<TextFieldValue> = _passwordText

    // Use MutableState to hold the user data for sign-up
    private val _signUpUser = mutableStateOf(com.example.shots.data.User())


//    Log-In
//    Sign-Up
//    Age
//    Username
//    Display
//    Media
//    Profile Video
//    AboutMe
//    Prompts
//    Link
//    Details
//    Essentials
//    Habits


    val SIGNUP_SCREEN_KEY = intPreferencesKey("signup_screen")
    val AGE_SCREEN_KEY = stringPreferencesKey("age_screen")
    val USERNAME_SCREEN_KEY = stringPreferencesKey("username_screen")
    val DISPLAY_NAME_SCREEN_KEY = stringPreferencesKey("display_name_screen")
    val MEDIA_SCREEN_KEY = stringPreferencesKey("media_screen")
    val PROFILE_VIDEO_SCREEN_KEY = stringPreferencesKey("profile_video_screen")
    val ABOUT_ME_SCREEN_KEY = stringPreferencesKey("about_me_screen")
    val PROMPTS_SCREEN_KEY = stringPreferencesKey("prompts_screen")
    val LINK_SCREEN_KEY = stringPreferencesKey("link_screen")
    val DETAILS_SCREEN_KEY = stringPreferencesKey("details_screen")
    val ESSENTIALS_SCREEN_KEY = stringPreferencesKey("essentials_screen")
    val HABITS_SCREEN_KEY = stringPreferencesKey("habits_screen")
    val USERS_SCREEN_KEY = stringPreferencesKey("users_screen")

    val SCREEN_LIST_KEY = stringSetPreferencesKey("screen_list_key")
    val LAST_KNOWN_SCREEN_KEY = stringPreferencesKey("last_known_screen")


    private val _hasPassedSignUpScreen = mutableStateOf(false)
    private val _hasPassedAgeScreen = mutableStateOf(false)
    private val _hasPassedUsernameScreen = mutableStateOf(false)
    private val _hasPassedDisplayNameScreen = mutableStateOf(false)
    private val _hasPassedMediaScreen = mutableStateOf(false)
    private val _hasPassedProfileVideoScreen = mutableStateOf(false)
    private val _hasPassedAboutMeScreen = mutableStateOf(false)
    private val _hasPassedPromptsScreen = mutableStateOf(false)
    private val _hasPassedLinkScreen = mutableStateOf(false)
    private val _hasPassedLookingForScreen = mutableStateOf(false)
    private val _hasPassedDetailsScreen = mutableStateOf(false)
    private val _hasPassedEssentialsScreen = mutableStateOf(false)
    private val _hasPassedHabitsScreen = mutableStateOf(false)

    fun updateScreenStatus(screen: Screen) {
        when (screen) {
            Screen.SIGN_UP -> _hasPassedSignUpScreen.value = !_hasPassedSignUpScreen.value
            Screen.AGE -> _hasPassedAgeScreen.value = !_hasPassedAgeScreen.value
            Screen.USER_NAME -> _hasPassedUsernameScreen.value = !_hasPassedUsernameScreen.value
            Screen.DISPLAY_NAME -> _hasPassedDisplayNameScreen.value =
                !_hasPassedDisplayNameScreen.value

            Screen.MEDIA -> _hasPassedMediaScreen.value = !_hasPassedMediaScreen.value
            Screen.PROFILE_VIDEO -> _hasPassedProfileVideoScreen.value =
                !_hasPassedProfileVideoScreen.value

            Screen.ABOUT_ME -> _hasPassedAboutMeScreen.value = !_hasPassedAboutMeScreen.value
            Screen.PROMPTS -> _hasPassedPromptsScreen.value = !_hasPassedPromptsScreen.value
            Screen.LINK -> _hasPassedLinkScreen.value = !_hasPassedLinkScreen.value
            Screen.DETAILS -> _hasPassedDetailsScreen.value = !_hasPassedDetailsScreen.value
            Screen.ESSENTIALS -> _hasPassedEssentialsScreen.value =
                !_hasPassedEssentialsScreen.value

            Screen.HABITS -> _hasPassedHabitsScreen.value = !_hasPassedHabitsScreen.value
        }
    }

    fun getScreenStatus(screen: Screen): Boolean {
        return when (screen) {
            Screen.SIGN_UP -> _hasPassedSignUpScreen.value
            Screen.AGE -> _hasPassedAgeScreen.value
            Screen.USER_NAME -> _hasPassedUsernameScreen.value
            Screen.DISPLAY_NAME -> _hasPassedDisplayNameScreen.value
            Screen.MEDIA -> _hasPassedMediaScreen.value
            Screen.PROFILE_VIDEO -> _hasPassedProfileVideoScreen.value
            Screen.ABOUT_ME -> _hasPassedAboutMeScreen.value
            Screen.PROMPTS -> _hasPassedPromptsScreen.value
            Screen.LINK -> _hasPassedLinkScreen.value
            Screen.DETAILS -> _hasPassedDetailsScreen.value
            Screen.ESSENTIALS -> _hasPassedEssentialsScreen.value
            Screen.HABITS -> _hasPassedHabitsScreen.value
        }
    }

    // Update the sign-up user data using a lambda expression
    fun updateSignUpUser(update: (com.example.shots.data.User) -> com.example.shots.data.User) {
        _signUpUser.value = update(_signUpUser.value)
    }

    // Get the sign-up user data
    fun getSignUpUser(): User {
        return _signUpUser.value
    }

    suspend fun createUser(email: String, password: String): FirebaseUser? {
        Log.d(TAG, "Inside create user")
        return authViewModel.createUserWithEmailAndPassword(email, password)
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