package com.example.shots.ui.theme

import com.example.shots.FirebaseModule
import com.example.shots.RoomModule
import com.example.shots.ViewModelModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.BlockedUser
import com.example.shots.data.BlockedUserDao
import com.example.shots.data.Distance
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
import com.example.shots.data.SexualOrientation
import com.example.shots.data.ShowMe
import com.example.shots.data.Smoking
import com.example.shots.data.TypeOfMedia
import com.example.shots.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.mockito.Mockito.mock

class BlockedUserViewModelTest {

    val user = User(
        "John",
        "John",
        "John",
        0.0,
        0.0,
        0,
        "",
        TypeOfMedia.UNKNOWN,
        "",
        TypeOfMedia.UNKNOWN,
        "",
        TypeOfMedia.UNKNOWN,
        "",
        TypeOfMedia.UNKNOWN,
        "",
        TypeOfMedia.UNKNOWN,
        "",
        TypeOfMedia.UNKNOWN,
        "",
        TypeOfMedia.UNKNOWN,
        "",
        TypeOfMedia.UNKNOWN,
        "",
        TypeOfMedia.UNKNOWN,
        "",
        TypeOfMedia.UNKNOWN,
        "",
        "",
        "",
        LookingFor.UNKNOWN,
        Gender.UNKNOWN,
        SexualOrientation.UNKNOWN,
        "",
        Education.UNKNOWN,
        Kids.UNKNOWN,
        Religion.UNKNOWN,
        Pets.UNKNOWN,
        Exercise.UNKNOWN,
        Smoking.UNKNOWN,
        Drinking.UNKNOWN,
        Marijuana.UNKNOWN,
        "",
        "",
        "",
        "",
        "",
        "",
        ShowMe.UNKNOWN,
        Distance.TWENTY,
        Distance.TWENTY,
        18,
        35,
        0,
        0,
        0,
        0,
        0,
        0
    )

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var appDatabase: AppDatabase
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var usersViewModel: UsersViewModel
    private lateinit var blockedUserViewModel: BlockedUserViewModel
    private lateinit var blockViewModel: BlockViewModel
    private lateinit var blockedUserDao: BlockedUserDao

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        firebaseAuth = mock(FirebaseAuth::class.java)
        appDatabase = mock(AppDatabase::class.java)
        firebaseStorage = mock(FirebaseStorage::class.java)
        firebaseFirestore = mock(FirebaseFirestore::class.java)
        firebaseRepository = FirebaseModule.provideFirebaseRepository(
            firebaseAuth,
            firebaseFirestore,
            firebaseStorage
        )
        usersViewModel =
            ViewModelModule.provideUsersViewModel(firebaseRepository, firebaseAuth, appDatabase)
        blockedUserViewModel = ViewModelModule.provideBlockedUserViewModel(
            firebaseRepository,
            firebaseAuth,
            appDatabase
        )
        blockViewModel =
            ViewModelModule.provideBlockViewModel(firebaseRepository, firebaseAuth, appDatabase)
        blockedUserDao = RoomModule.provideBlockedUserDao(appDatabase)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getBlockedUserDao_Returns_Correct_BlockedUserDao() {
        val expectedDao = RoomModule.provideBlockedUserDao(appDatabase)
        val actualDao = blockedUserViewModel.blockedUserDao

        Assertions.assertSame(expectedDao, actualDao)
    }

    @Test
    fun getUserWhoBlockedYouDao() {
        val expectedDao = RoomModule.provideUserWhoBlockedYouDao(appDatabase)
        val actualDao = blockedUserViewModel.userWhoBlockedYouDao

        Assertions.assertSame(expectedDao, actualDao)
    }

    @Test
    fun fetchBlockedUserFromRoom() {
        blockedUserViewModel.storeBlockedUserInRoom(user.id)

        val expectedBlockedUser = BlockedUser()
        expectedBlockedUser.blockedUserId = user.id

        // Call the fetchBlockedUserFromRoom() method
        val actualBlockedUser = blockedUserViewModel.fetchBlockedUserFromRoom(user.id)

        // Assert that the expected and actual BlockedUser objects are the same
        Assertions.assertSame(expectedBlockedUser.blockedUserId, actualBlockedUser.blockedUserId)
    }

    @Test
    fun storeBlockedUserInRoom() {
        // Arrange
        blockedUserViewModel.storeBlockedUserInRoom(user.id)

        // Act
        val blockedUser = blockedUserViewModel.fetchBlockedUserFromRoom(user.id)

        // Assert
        Assertions.assertNotNull(blockedUser)
    }

    @Test
    fun getBlockedUserDao() {
        val expectedDao = RoomModule.provideBlockedUserDao(appDatabase)

        val actualDao = blockedUserViewModel.blockedUserDao
        assertEquals(expectedDao, actualDao)
    }

//    @Test
//    fun saveBlockedUserToFirebase() = runBlocking {
//        val blockedUserId = user.id
//        val blockedUserData: MutableMap<String, Any> = mutableMapOf()
//        blockedUserData["blockedUser-${blockedUserId}"] = blockedUserId ?: ""
//        blockedUserViewModel.saveBlockedUserToFirebase(blockedUserId, blockedUserData)
//
//        val blockedUser = blockedUserViewModel.fetchBlockedUserFromRoom(blockedUserId)
//        Assertions.assertEquals(blockedUser.blockedUsers.size, 1)
//    }

//    @Test
//    fun `remove blocked user from firebase`() = runBlocking {
//        // Arrange
//        val blockedUser = mock(BlockedUser::class.java)
//        val blockedUserId = blockedUser.blockedUserId
//        val firebaseRepository = mock(FirebaseRepository::class.java)
//        val blockedUserViewModel = mock(BlockedUserViewModel::class.java)
//
//        // Mock the deleteBlockedUserFromFirebase() function
//        `when`(blockedUserViewModel.removeBlockedUserFromFirebase(blockedUserId)).thenReturn(Unit)
//
//        // Act
//        blockedUserViewModel.removeBlockedUserFromFirebase(blockedUserId)
//
//        // Assert
//        verify(blockedUserViewModel).removeBlockedUserFromFirebase(blockedUserId)
//    }

//    @Test
//    fun `get blocked users from firebase`() = runBlocking {
//        // Arrange
//        val blockedUser = mock(BlockedUser::class.java)
//        val blockedUsers = blockedUser.blockedUsers
//        val blockedUserId = blockedUser.blockedUserId
//        val firebaseRepository = mock(FirebaseRepository::class.java)
//        val blockedUserViewModel = mock(BlockedUserViewModel::class.java)
//
//        // Mock the deleteBlockedUserFromFirebase() function
//        `when`(blockedUserViewModel.getBlockedUsersFromFirebase(blockedUserId))
//            .thenReturn(blockedUsers)
//
//        // Act
//        blockedUserViewModel.removeBlockedUserFromFirebase(blockedUserId)
//
//        // Assert
//        verify(blockedUserViewModel).removeBlockedUserFromFirebase(blockedUserId)
//    }

    @Test
    fun getOneBlockedUserFromBlockedUsers() {

        blockedUserDao = RoomModule.provideBlockedUserDao(appDatabase)

        // Create a mock BlockedUser object
        val userOne = User(
            "Von",
            "Von",
            "Von",
            0.0,
            0.0,
            0,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            "",
            "",
            LookingFor.UNKNOWN,
            Gender.UNKNOWN,
            SexualOrientation.UNKNOWN,
            "",
            Education.UNKNOWN,
            Kids.UNKNOWN,
            Religion.UNKNOWN,
            Pets.UNKNOWN,
            Exercise.UNKNOWN,
            Smoking.UNKNOWN,
            Drinking.UNKNOWN,
            Marijuana.UNKNOWN,
            "",
            "",
            "",
            "",
            "",
            "",
            ShowMe.UNKNOWN,
            Distance.TWENTY,
            Distance.TWENTY,
            18,
            35,
            0,
            0,
            0,
            0,
            0,
            0
        )

        val userTwo = User(
            "Ron",
            "Ron",
            "Ron",
            0.0,
            0.0,
            0,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            "",
            "",
            LookingFor.UNKNOWN,
            Gender.UNKNOWN,
            SexualOrientation.UNKNOWN,
            "",
            Education.UNKNOWN,
            Kids.UNKNOWN,
            Religion.UNKNOWN,
            Pets.UNKNOWN,
            Exercise.UNKNOWN,
            Smoking.UNKNOWN,
            Drinking.UNKNOWN,
            Marijuana.UNKNOWN,
            "",
            "",
            "",
            "",
            "",
            "",
            ShowMe.UNKNOWN,
            Distance.TWENTY,
            Distance.TWENTY,
            18,
            35,
            0,
            0,
            0,
            0,
            0,
            0
        )

        var blockedUserOne = BlockedUser()
        blockedUserOne.blockedUserId = userOne.id

        var blockedUserTwo = BlockedUser()
        blockedUserTwo.blockedUserId = userTwo.id


        blockedUserViewModel.storeBlockedUserInRoom(user.id)

        var blockedUser = blockedUserViewModel.fetchBlockedUserFromRoom(user.id)
        blockedUser.blockedUsers.add(0, blockedUserOne.blockedUserId)

        // Assert that the first element of the blockedUsers list is equal to "John"
        Assertions.assertEquals(1, blockedUser.blockedUsers.size)
    }

    @Test
    fun getMultipleBlockedUsersFromBlockedUsers() {

        blockedUserDao = RoomModule.provideBlockedUserDao(appDatabase)

        // Create a mock BlockedUser object
        val userOne = User(
            "Von",
            "Von",
            "Von",
            0.0,
            0.0,
            0,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            "",
            "",
            LookingFor.UNKNOWN,
            Gender.UNKNOWN,
            SexualOrientation.UNKNOWN,
            "",
            Education.UNKNOWN,
            Kids.UNKNOWN,
            Religion.UNKNOWN,
            Pets.UNKNOWN,
            Exercise.UNKNOWN,
            Smoking.UNKNOWN,
            Drinking.UNKNOWN,
            Marijuana.UNKNOWN,
            "",
            "",
            "",
            "",
            "",
            "",
            ShowMe.UNKNOWN,
            Distance.TWENTY,
            Distance.TWENTY,
            18,
            35,
            0,
            0,
            0,
            0,
            0,
            0
        )

        val userTwo = User(
            "Ron",
            "Ron",
            "Ron",
            0.0,
            0.0,
            0,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            TypeOfMedia.UNKNOWN,
            "",
            "",
            "",
            LookingFor.UNKNOWN,
            Gender.UNKNOWN,
            SexualOrientation.UNKNOWN,
            "",
            Education.UNKNOWN,
            Kids.UNKNOWN,
            Religion.UNKNOWN,
            Pets.UNKNOWN,
            Exercise.UNKNOWN,
            Smoking.UNKNOWN,
            Drinking.UNKNOWN,
            Marijuana.UNKNOWN,
            "",
            "",
            "",
            "",
            "",
            "",
            ShowMe.UNKNOWN,
            Distance.TWENTY,
            Distance.TWENTY,
            18,
            35,
            0,
            0,
            0,
            0,
            0,
            0
        )

        var blockedUserOne = BlockedUser()
        blockedUserOne.blockedUserId = userOne.id

        var blockedUserTwo = BlockedUser()
        blockedUserTwo.blockedUserId = userTwo.id


        blockedUserViewModel.storeBlockedUserInRoom(user.id)

        var blockedUser = blockedUserViewModel.fetchBlockedUserFromRoom(user.id)
        blockedUser.blockedUsers.add(0, blockedUserOne.blockedUserId)
        blockedUser.blockedUsers.add(1, blockedUserTwo.blockedUserId)

        // Assert that the first element of the blockedUsers list is equal to "John"
        Assertions.assertEquals(2, blockedUser.blockedUsers.size)
    }
}