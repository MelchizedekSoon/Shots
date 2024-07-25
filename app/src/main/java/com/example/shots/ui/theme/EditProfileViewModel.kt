package com.example.shots.ui.theme

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.data.Drinking
import com.example.shots.data.Education
import com.example.shots.data.Exercise
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.Gender
import com.example.shots.data.Kids
import com.example.shots.data.LookingFor
import com.example.shots.data.Marijuana
import com.example.shots.data.Pets
import com.example.shots.data.Religion
import com.example.shots.data.Smoking
import com.example.shots.data.User
import com.example.shots.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val mediaOne: String? = "",
    val mediaTwo: String? = "",
    val mediaThree: String? = "",
    val mediaFour: String? = "",
    val mediaFive: String? = "",
    val mediaSix: String? = "",
    val mediaSeven: String? = "",
    val mediaEight: String? = "",
    val mediaNine: String? = "",
    val mediaProfileVideo: String? = "",
    val userName: String? = "",
    var displayName: String? = "",
    val aboutMe: String? = "",
    var promptOneAnswer: String? = "",
    var promptTwoAnswer: String? = "",
    var promptThreeAnswer: String? = "",
    var promptOneQuestion: String? = "",
    var promptTwoQuestion: String? = "",
    var promptThreeQuestion: String? = "",
    val link: String? = "",
    val lookingFor: LookingFor = LookingFor.UNKNOWN,
    val gender: Gender = Gender.UNKNOWN,
    val height: String? = "",
    val work: String? = "",
    val education: Education = Education.UNKNOWN,
    val kids: Kids = Kids.UNKNOWN,
    val religion: Religion = Religion.UNKNOWN,
    val pets: Pets = Pets.UNKNOWN,
    val exercise: Exercise = Exercise.UNKNOWN,
    val smoking: Smoking = Smoking.UNKNOWN,
    val drinking: Drinking = Drinking.UNKNOWN,
    val marijuana: Marijuana = Marijuana.UNKNOWN
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    firebaseRepository: FirebaseRepository,
    private val userRepository: UserRepository,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val scope = CoroutineScope(dispatcher)


    var user: User? = null

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        try {
            viewModelScope.launch(dispatcher) {
                userRepository.getCurrentUser().collect { returnedUser ->
                    user = returnedUser
                }
                loadEditProfileOptions()
            }
        } catch (npe: NullPointerException) {
            Log.d("EditProfileViewModel", "This is the result of EditProfileViewModel initialization - NullPointerException: ${npe.message}")
        }
    }

    fun loadEditProfileOptions() {
        //Hello
        viewModelScope.launch(dispatcher) {
            try {
                userRepository.getCurrentUser().collect { returnedUser ->

                    user = returnedUser

                    Log.d("EditProfileViewModel", "userName: ${returnedUser.userName}")

                    _uiState.value = EditProfileUiState( // Update the MutableStateFlow
                        mediaOne = returnedUser.mediaOne,
                        mediaTwo = returnedUser.mediaTwo,
                        mediaThree = returnedUser.mediaThree,
                        mediaFour = returnedUser.mediaFour,
                        mediaFive = returnedUser.mediaFive,
                        mediaSix = returnedUser.mediaSix,
                        mediaSeven = returnedUser.mediaSeven,
                        mediaEight = returnedUser.mediaEight,
                        mediaNine = returnedUser.mediaNine,
                        mediaProfileVideo = returnedUser.mediaProfileVideo,
                        userName = returnedUser.userName,
                        displayName = returnedUser.displayName,
                        aboutMe = returnedUser.aboutMe,
                        promptOneAnswer = returnedUser.promptOneAnswer,
                        promptTwoAnswer = returnedUser.promptTwoAnswer,
                        promptThreeAnswer = returnedUser.promptThreeAnswer,
                        promptOneQuestion = returnedUser.promptOneQuestion,
                        promptTwoQuestion = returnedUser.promptTwoQuestion,
                        promptThreeQuestion = returnedUser.promptThreeQuestion,
                        link = returnedUser.link,
                        lookingFor = returnedUser.lookingFor ?: LookingFor.UNKNOWN,
                        gender = returnedUser.gender ?: Gender.UNKNOWN,
                        height = returnedUser.height,
                        work = returnedUser.work,
                        education = returnedUser.education ?: Education.UNKNOWN,
                        kids = returnedUser.kids ?: Kids.UNKNOWN,
                        religion = returnedUser.religion ?: Religion.UNKNOWN,
                        pets = returnedUser.pets ?: Pets.UNKNOWN,
                        exercise = returnedUser.exercise ?: Exercise.UNKNOWN,
                        smoking = returnedUser.smoking ?: Smoking.UNKNOWN,
                        drinking = returnedUser.drinking ?: Drinking.UNKNOWN,
                        marijuana = returnedUser.marijuana ?: Marijuana.UNKNOWN
                    )

                }
            } catch (npe: NullPointerException) {
                Log.d("EditProfileViewModel", "$npe")
            }


            Log.d("EditProfileViewModel", "User: ${_uiState.value}")

        }
    }

    fun resetYourEditProfileState() {
        viewModelScope.launch(dispatcher) {
            _uiState.value = EditProfileUiState()
        }
    }

    fun saveAndStoreFields(context: Context): Boolean {
        var wasSaved = false
        viewModelScope.launch(dispatcher) {

            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()

            Log.d("EditProfileViewModel", "${_uiState.value}")

            userData["displayName"] = _uiState.value.displayName ?: ""
            userData["aboutMe"] = _uiState.value.aboutMe ?: ""
            userData["promptOneAnswer"] = _uiState.value.promptOneAnswer ?: ""
            userData["promptTwoAnswer"] = _uiState.value.promptTwoAnswer ?: ""
            userData["promptThreeAnswer"] = _uiState.value.promptThreeAnswer ?: ""
            userData["promptOneQuestion"] = _uiState.value.promptOneQuestion ?: ""
            userData["promptTwoQuestion"] = _uiState.value.promptTwoQuestion ?: ""
            userData["promptThreeQuestion"] = _uiState.value.promptThreeQuestion ?: ""
            userData["link"] = _uiState.value.link ?: ""
            userData["lookingFor"] = _uiState.value.lookingFor
            userData["gender"] = _uiState.value.gender
            userData["height"] = _uiState.value.height ?: ""
            userData["work"] = _uiState.value.work ?: ""
            userData["education"] = _uiState.value.education
            userData["kids"] = _uiState.value.kids
            userData["religion"] = _uiState.value.religion
            userData["pets"] = _uiState.value.pets
            userData["exercise"] = _uiState.value.exercise
            userData["smoking"] = _uiState.value.smoking
            userData["drinking"] = _uiState.value.drinking
            userData["marijuana"] = _uiState.value.marijuana


            userRepository.saveUserData(
                user?.id ?: "",
                userData, mediaItems, context
            )

            wasSaved = true

            loadEditProfileOptions()
        }
        return wasSaved
    }

    fun saveAndStoreMedia(
        mediaIdentifier: String,
        media: String,
        context: Context,
        wasSaved: (Boolean) -> Unit
    ) {
        viewModelScope.launch(dispatcher) {
            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()
            mediaItems[mediaIdentifier] = media.toUri()
            userRepository.saveUserData(
                user?.id ?: "",
                userData,
                mediaItems,
                context
            )
            val mediaWasSaved = true
            if (mediaWasSaved) {
                loadEditProfileOptions()
                wasSaved(true)
            } else {
                wasSaved(false)
            }
        }
    }

    fun updateDisplayName(displayName: String) {
        if (displayName.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(displayName = displayName)
            Log.d("EditProfileViewModel", "${_uiState.value}")
        } else {
            _uiState.value = _uiState.value.copy(displayName = _uiState.value.userName)
        }
    }

    fun updateAboutMe(aboutMe: String) {
        _uiState.value = _uiState.value.copy(aboutMe = aboutMe)
    }

    fun updatePromptOneAnswer(promptOneAnswer: String) {
        _uiState.value = _uiState.value.copy(promptOneAnswer = promptOneAnswer)
    }

    fun updatePromptTwoAnswer(promptTwoAnswer: String) {
        _uiState.value = _uiState.value.copy(promptTwoAnswer = promptTwoAnswer)
    }

    fun updatePromptThreeAnswer(promptThreeAnswer: String) {
        _uiState.value = _uiState.value.copy(promptThreeAnswer = promptThreeAnswer)
    }

    fun updatePromptOneQuestion(promptOneQuestion: String) {
        _uiState.value = _uiState.value.copy(promptOneQuestion = promptOneQuestion)
    }

    fun updatePromptTwoQuestion(promptTwoQuestion: String) {
        _uiState.value = _uiState.value.copy(promptTwoQuestion = promptTwoQuestion)
    }

    fun updatePromptThreeQuestion(promptThreeQuestion: String) {
        _uiState.value = _uiState.value.copy(promptThreeQuestion = promptThreeQuestion)
    }

    fun updateLink(link: String) {
        _uiState.value = _uiState.value.copy(link = link)
    }

    fun updateLookingFor(lookingFor: LookingFor) {
        _uiState.value = _uiState.value.copy(lookingFor = lookingFor)
    }

    fun updateGender(gender: Gender) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateHeight(height: String) {
        _uiState.value = _uiState.value.copy(height = height)
    }

    fun updateWork(work: String) {
        _uiState.value = _uiState.value.copy(work = work)
    }

    fun updateEducation(education: Education) {
        _uiState.value = _uiState.value.copy(education = education)
    }

    fun updateKids(kids: Kids) {
        _uiState.value = _uiState.value.copy(kids = kids)
    }

    fun updateReligion(religion: Religion) {
        _uiState.value = _uiState.value.copy(religion = religion)
    }

    fun updatePets(pets: Pets) {
        _uiState.value = _uiState.value.copy(pets = pets)
    }

    fun updateExercise(exercise: Exercise) {
        _uiState.value = _uiState.value.copy(exercise = exercise)
    }

    fun updateSmoking(smoking: Smoking) {
        _uiState.value = _uiState.value.copy(smoking = smoking)
    }

    fun updateDrinking(drinking: Drinking) {
        _uiState.value = _uiState.value.copy(drinking = drinking)
    }

    fun updateMarijuana(marijuana: Marijuana) {
        _uiState.value = _uiState.value.copy(marijuana = marijuana)
    }


}