package com.example.shots.ui.theme

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.example.shots.FirebaseModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.Drinking
import com.example.shots.data.Education
import com.example.shots.data.Exercise
import com.example.shots.data.FakeFirebaseRepositoryImpl
import com.example.shots.data.FakeUserLocalDataSourceImpl
import com.example.shots.data.FakeUserRemoteDataSourceImpl
import com.example.shots.data.FakeUserRepositoryImpl
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.Gender
import com.example.shots.data.Kids
import com.example.shots.data.LookingFor
import com.example.shots.data.Marijuana
import com.example.shots.data.Pets
import com.example.shots.data.Religion
import com.example.shots.data.Smoking
import com.example.shots.data.UserDao
import com.example.shots.data.UserLocalDataSource
import com.example.shots.data.UserRemoteDataSource
import com.example.shots.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class EditProfileViewModelTest {

    private lateinit var roomDatabase: RoomDatabase
    private lateinit var context: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userDao: UserDao
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userRepository: UserRepository
    private lateinit var editProfileViewModel: EditProfileViewModel
    private lateinit var testDispatcher: TestDispatcher

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {

        context = InstrumentationRegistry.getInstrumentation().context

        roomDatabase = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java // Use your actual database class
        ).allowMainThreadQueries().build()

        firebaseAuth = FirebaseModule.provideFirebaseAuth()
        firebaseAuth.useEmulator("10.0.2.2", 9099)

        firebaseFirestore = FirebaseModule.provideFirestore()
        try {
            firebaseFirestore.useEmulator("10.0.2.2", 8080)
        } catch (_: IllegalStateException) {
        }
        val settings = FirebaseFirestoreSettings.Builder()
            .setHost("10.0.2.2:8080") // Use 10.0.2.2 for Android emulators
            .setSslEnabled(false).build()
        firebaseFirestore.firestoreSettings = settings

//        firebaseFirestore = FirebaseModule.provideFirestore()
//        try {
//            firebaseFirestore.useEmulator("192.168.1.102", 8080)
//        } catch (_: IllegalStateException) {
//        }
//        firebaseFirestore.firestoreSettings = firestoreSettings {}

        firebaseStorage = FirebaseModule.provideStorage()
        firebaseStorage.useEmulator("10.0.2.2", 9199)

        userDao = (roomDatabase as AppDatabase).userDao()

        userLocalDataSource = FakeUserLocalDataSourceImpl(firebaseAuth, userDao)
        userRemoteDataSource =
            FakeUserRemoteDataSourceImpl(firebaseAuth, firebaseFirestore, firebaseStorage)

        userRepository = FakeUserRepositoryImpl(userLocalDataSource, userRemoteDataSource)

        firebaseRepository =
            FakeFirebaseRepositoryImpl(firebaseAuth, firebaseFirestore, firebaseStorage)

        // 10.0.2.2 is the special IP address to connect to the 'localhost' of
// the host computer from an Android emulator.

        testDispatcher = StandardTestDispatcher()

        editProfileViewModel =
            EditProfileViewModel(firebaseRepository, userRepository, testDispatcher)

    }

    @Test
    fun getUser() {
    }

    @Test
    fun setUser() {
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getUiState() = runTest {
        val userData: MutableMap<String, Any> = mutableMapOf()
        val mediaItems: MutableMap<String, Uri> = mutableMapOf()

        userData["id"] = "John"
        userData["displayName"] = "John"
        userData["userName"] = "John"

        (userRepository as FakeUserRepositoryImpl).saveUserData(
            "John", userData, mediaItems, context
        )

        advanceUntilIdle()

        (userRepository as FakeUserRepositoryImpl).storeAfterSavingUserDataToFirebase("John")

        val user = (userRepository as FakeUserRepositoryImpl).getUser("John")

        Log.d("EditProfileViewModelTest", "User: $user")

        testDispatcher.scheduler.advanceUntilIdle()

        userDao.upsert(user)
        userRepository.getCurrentUser().collect { currentUser ->
            Log.d("EditProfileViewModelTest", "Current User: $currentUser")
        }

        testDispatcher.scheduler.advanceUntilIdle()

        editProfileViewModel.loadEditProfileOptions()

        testDispatcher.scheduler.advanceUntilIdle()

        Log.d("EditProfileViewModelTest", "User: ${editProfileViewModel.uiState.value}")

        Assert.assertTrue(editProfileViewModel.uiState.value.displayName == "John")
    }


    @Test
    fun loadEditProfileOptions() {
        val initialDisplayName = editProfileViewModel.uiState.value.displayName

        editProfileViewModel.updateDisplayName("John-John")

        testDispatcher.scheduler.advanceUntilIdle()

        val updatedDisplayName = editProfileViewModel.uiState.value.displayName

        Log.d("EditProfileViewModelTest", "Initial Display Name: $initialDisplayName")
        Log.d("EditProfileViewModelTest", "Updated Display Name: $updatedDisplayName")

        Assert.assertNotEquals(initialDisplayName, updatedDisplayName)
    }

    @Test
    fun resetYourEditProfileState() {

        editProfileViewModel.updateDisplayName("John")

        val initialDisplayName = editProfileViewModel.uiState.value.displayName

        Log.d(
            "EditProfileViewModelTest",
            "initialDisplayName inside of resetYourEditProfileState: $initialDisplayName"
        )

        testDispatcher.scheduler.advanceUntilIdle()

        editProfileViewModel.resetYourEditProfileState()

        testDispatcher.scheduler.advanceUntilIdle()

        val updatedDisplayName = editProfileViewModel.uiState.value.displayName

        Log.d(
            "EditProfileViewModelTest",
            "updatedDisplayName inside of resetYourEditProfileState: $updatedDisplayName"
        )

        Assert.assertTrue((updatedDisplayName?.isBlank() == true))
    }

    @Test
    fun saveAndStoreFields() {

        editProfileViewModel.updateDisplayName("Ron")

        editProfileViewModel.saveAndStoreFields(context)

        val displayName = editProfileViewModel.uiState.value.displayName

        Log.d("EditProfileViewModelTest", "displayName: $displayName")
    }

    @Test
    fun saveAndStoreMedia() {
    }

    @Test
    fun updateDisplayName() {
        editProfileViewModel.updateDisplayName("Ian")

        Assert.assertTrue(editProfileViewModel.uiState.value.displayName == "Ian")
    }

    @Test
    fun updateAboutMe() {
        editProfileViewModel.updateAboutMe("I am an android developer")

        Assert.assertTrue(editProfileViewModel.uiState.value.aboutMe == "I am an android developer")
    }

    @Test
    fun updatePromptOneAnswer() {
        editProfileViewModel.updatePromptOneAnswer("I am an android developer")

        Assert.assertTrue(editProfileViewModel.uiState.value.promptOneAnswer == "I am an android developer")
    }

    @Test
    fun updatePromptTwoAnswer() {
        editProfileViewModel.updatePromptTwoAnswer("I am an android developer")

        Assert.assertTrue(editProfileViewModel.uiState.value.promptTwoAnswer == "I am an android developer")
    }

    @Test
    fun updatePromptThreeAnswer() {
        editProfileViewModel.updatePromptThreeAnswer("I am an android developer")

        Assert.assertTrue(editProfileViewModel.uiState.value.promptThreeAnswer == "I am an android developer")
    }

    @Test
    fun updatePromptOneQuestion() {
        editProfileViewModel.updatePromptOneQuestion("I am an android developer")

        Assert.assertTrue(editProfileViewModel.uiState.value.promptOneQuestion == "I am an android developer")
    }

    @Test
    fun updatePromptTwoQuestion() {
        editProfileViewModel.updatePromptTwoQuestion("I am an android developer")

        Assert.assertTrue(editProfileViewModel.uiState.value.promptTwoQuestion == "I am an android developer")
    }

    @Test
    fun updatePromptThreeQuestion() {
        editProfileViewModel.updatePromptThreeQuestion("I am an android developer")

        Assert.assertTrue(editProfileViewModel.uiState.value.promptThreeQuestion == "I am an android developer")
    }

    @Test
    fun updateLink() {
        editProfileViewModel.updateLink("https://www.google.com")

        Assert.assertTrue(editProfileViewModel.uiState.value.link == "https://www.google.com")
    }

    @Test
    fun updateLookingFor() {
        editProfileViewModel.updateLookingFor(LookingFor.LONG_TERM)

        Assert.assertTrue(editProfileViewModel.uiState.value.lookingFor == LookingFor.LONG_TERM)
    }

    @Test
    fun updateGender() {
        editProfileViewModel.updateGender(Gender.MAN)

        Assert.assertTrue(editProfileViewModel.uiState.value.gender == Gender.MAN)
    }

    @Test
    fun updateHeight() {
        editProfileViewModel.updateHeight("5'8")

        Assert.assertTrue(editProfileViewModel.uiState.value.height == "5'8")
    }

    @Test
    fun updateWork() {
        editProfileViewModel.updateWork("Google")

        Assert.assertTrue(editProfileViewModel.uiState.value.work == "Google")
    }

    @Test
    fun updateEducation() {
        editProfileViewModel.updateEducation(Education.GRAD_DEGREE)

        Assert.assertTrue(editProfileViewModel.uiState.value.education == Education.GRAD_DEGREE)
    }

    @Test
    fun updateKids() {
        editProfileViewModel.updateKids(Kids.ONE_DAY)

        Assert.assertTrue(editProfileViewModel.uiState.value.kids == Kids.ONE_DAY)
    }

    @Test
    fun updateReligion() {
        editProfileViewModel.updateReligion(Religion.JUDAISM)

        Assert.assertTrue(editProfileViewModel.uiState.value.religion == Religion.JUDAISM)
    }

    @Test
    fun updatePets() {
        editProfileViewModel.updatePets(Pets.CAT)

        Assert.assertTrue(editProfileViewModel.uiState.value.pets == Pets.CAT)
    }

    @Test
    fun updateExercise() {
        editProfileViewModel.updateExercise(Exercise.NEVER)

        Assert.assertTrue(editProfileViewModel.uiState.value.exercise == Exercise.NEVER)
    }

    @Test
    fun updateSmoking() {
        editProfileViewModel.updateSmoking(Smoking.YES)

        Assert.assertTrue(editProfileViewModel.uiState.value.smoking == Smoking.YES)
    }

    @Test
    fun updateDrinking() {
        editProfileViewModel.updateDrinking(Drinking.NEVER_DRINK)

        Assert.assertTrue(editProfileViewModel.uiState.value.drinking == Drinking.NEVER_DRINK)
    }

    @Test
    fun updateMarijuana() {
        editProfileViewModel.updateMarijuana(Marijuana.ON_OCCASION)

        Assert.assertTrue(editProfileViewModel.uiState.value.marijuana == Marijuana.ON_OCCASION)
    }
}