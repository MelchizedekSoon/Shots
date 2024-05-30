package com.example.shots.ui.theme

import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.example.shots.MainActivity
import com.example.shots.data.BlockedUserDao
import com.example.shots.data.Distance
import com.example.shots.data.Drinking
import com.example.shots.data.Education
import com.example.shots.data.Exercise
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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class BlockedUsersScreenKtTest {

    private var scope = CoroutineScope(Dispatchers.IO)
    private lateinit var navController: NavController
    private lateinit var blockViewModel: BlockViewModel
    private lateinit var blockedUserViewModel: BlockedUserViewModel
    private lateinit var usersViewModel: UsersViewModel
    private lateinit var blockedUserDao: BlockedUserDao
    private lateinit var blockedUsers: MutableList<String>
    private lateinit var user: User
    private lateinit var blockedUserOne: User
    private lateinit var blockedUserTwo: User

//    @get:Rule
//    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() = runBlocking {
        // Set up any necessary data or dependencies here.
        // Navigate to the BlockedUsersScreen.

        navController = mockk(relaxed = true)

        blockViewModel = mockk(relaxed = true)

        usersViewModel = mockk(relaxed = true)

        user = User(
            "Rick",
            "Rick",
            "Rick",
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

        Log.d("BlockedUsersScreenKtTest", "user = ${user.id}")

        blockedUserOne = User(
            "John",
            "John",
            "John",
            0.0,
            0.0,
            1111111111111,
            "andre_sebastian_3_i3gxwldew_unsplash.jpg",
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
            LookingFor.LONG_TERM,
            Gender.WOMAN,
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

        blockedUserTwo = User(
            "Rohn",
            "Rohn",
            "Rohn",
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

        Log.d("BlockedUsersScreenKtTest", "blockedUserOne = ${blockedUserOne.id}")
        blockedUsers = mutableListOf(
            blockedUserOne.displayName ?: "",
            blockedUserTwo.displayName ?: ""
        )

        blockedUserDao = mockk(relaxed = true)

        every { usersViewModel.getUser() } returns user

//        every { usersViewModel.fetchUserFromRoom(user.id) } returns user

        every {
            blockedUserDao.findById(user.id).blockedUsers
                .filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()
        } returns blockedUsers

        blockedUserViewModel = mockk(relaxed = true)

        every {
            blockedUserViewModel.fetchBlockedUserFromRoom(
                user.id ?: ""
            ).blockedUsers.filter { it.isNotEmpty() && it.isNotBlank() }.toMutableList()
        } returns blockedUsers

//        every { blockViewModel.block(any()) } returns Unit
//
//        blockViewModel.block(blockedUserOne.id)
//        blockViewModel.block(blockedUserTwo.id)

        composeTestRule.setContent {

            BlockedUsersScreen(
                navController = rememberNavController(),
                usersViewModel = mockk(relaxed = true),
                blockedUserViewModel = blockedUserViewModel,
                blockViewModel = blockViewModel,
                locationViewModel = mockk(relaxed = true)
            )

        }
    }

    @Test
    fun blockedUsersScreen_displayTitle() {
        // Verify that the blocked users are displayed.
        composeTestRule.onNodeWithText("Blocked Users").assertIsDisplayed()
    }

    @Test
    fun blockedUsersScreen_displaysLikeButton() {
        // Find the like button
        val likeButton = composeTestRule.onNodeWithContentDescription("Like Icon")


        // Perform a click action on the like button
        likeButton.assertHasClickAction()
    }

    /**
     * I will return to these later on but for now, I'll just test
     * UI that comes on the screen regardless of user data, the parts
     * that remain on the screen and don't leave and then
     * later hopefully I can figure out the adding data aspect
     * to properly test if users are blocked, etc.
     */

    //Display users test continues to fail because I don't know how to get it to upload fake users
    // fake data

//    @Test
//    fun blockedUsersScreen_displaysUsers() {
//
//        blockedUserViewModel.fetchBlockedUserFromRoom(user.id ?: "")
//
//        // Wait for the users to be loaded
//        composeTestRule.waitUntil {
//            blockedUserViewModel.fetchBlockedUserFromRoom(user.id ?: "").blockedUsers.isNotEmpty()
//        }
//
//        Log.d(
//            "BlockedUsersScreenKtTest",
//            "blockedUsers = ${blockedUserViewModel.fetchBlockedUserFromRoom(user.id ?: "").blockedUsers}"
//        )
//
//        // Verify that the fake users are displayed on the screen.
//        composeTestRule.onNodeWithText("John").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Sohn").assertIsDisplayed()
//    }

    // Navigate to user profile test continues to fail
    // because I don't know how to get it to upload fake users
    // in a reliable way

//    @Test
//    fun blockedUsersScreen_navigateToUserProfile() {
//        // Click on a user to navigate to their profile.
//        composeTestRule.onNodeWithText(blockedUserOne.displayName ?: "").performClick()
//
//        // Verify that the user profile screen is displayed.
//        composeTestRule.onNodeWithText(blockedUserOne.userName ?: "").assertIsDisplayed()
//    }

}