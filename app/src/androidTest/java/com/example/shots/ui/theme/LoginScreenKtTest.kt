//package com.example.shots.ui.theme
//
//import androidx.compose.ui.test.isDisplayed
//import androidx.compose.ui.test.junit4.createAndroidComposeRule
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.compose.ui.test.onNodeWithText
//import androidx.compose.ui.test.performClick
//import androidx.compose.ui.test.performTouchInput
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.rememberNavController
//import com.example.shots.MainActivity
//import io.mockk.mockk
//import org.junit.Assert.*
//
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//
//class LoginScreenKtTest {
//
//    @get:Rule
//    val composeTestRule = createAndroidComposeRule<MainActivity>()
//
//
//    @Before
//    fun setUp() {
////        activityRule.setContent {
////            LoginScreen(
////                navController = rememberNavController(),
////                signupViewModel = mockk(relaxed=true),
////                usersViewModel = mockk(relaxed=true),
////                bookmarkViewModel = mockk(relaxed=true),
////                receivedLikeViewModel = mockk(relaxed = true),
////                sentLikeViewModel = mockk(relaxed = true),
////                receivedShotViewModel = mockk(relaxed = true),
////                sentShotViewModel = mockk(relaxed = true),
////                dataStore = mockk(relaxed = true)
////            )
////            UsersScreen(navController = rememberNavController(),
////                signupViewModel = mockk(relaxed = true),
////                usersViewModel = mockk(relaxed = true),
////                locationViewModel = mockk(relaxed = true),
////                bookmarkViewModel = mockk(relaxed = true),
////                receivedLikeViewModel = mockk(relaxed = true),
////                sentLikeViewModel = mockk(relaxed = true),
////                receivedShotViewModel = mockk(relaxed = true),
////                sentShotViewModel = mockk(relaxed = true),
////                blockedUserViewModel = mockk(relaxed = true),
////                userWhoBlockedYouViewModel = mockk(relaxed = true),
////                dataStore = mockk(relaxed = true))
////        }
////        composeTestRule.setContent {
////            LoginScreen(
////                navController = rememberNavController(),
////                signupViewModel = mockk(relaxed=true),
////                usersViewModel = mockk(relaxed=true),
////                bookmarkViewModel = mockk(relaxed=true),
////                receivedLikeViewModel = mockk(relaxed = true),
////                sentLikeViewModel = mockk(relaxed = true),
////                receivedShotViewModel = mockk(relaxed = true),
////                sentShotViewModel = mockk(relaxed = true),
////                dataStore = mockk(relaxed = true)
////            )
////            UsersScreen(navController = rememberNavController(),
////                signupViewModel = mockk(relaxed = true),
////                usersViewModel = mockk(relaxed = true),
////                locationViewModel = mockk(relaxed = true),
////                bookmarkViewModel = mockk(relaxed = true),
////                receivedLikeViewModel = mockk(relaxed = true),
////                sentLikeViewModel = mockk(relaxed = true),
////                receivedShotViewModel = mockk(relaxed = true),
////                sentShotViewModel = mockk(relaxed = true),
////                blockedUserViewModel = mockk(relaxed = true),
////                userWhoBlockedYouViewModel = mockk(relaxed = true),
////                dataStore = mockk(relaxed = true))
////        }
//
//
//    }
//
//    @Test
//    fun loginScreenIsDisplayed() {
//        composeTestRule.onNodeWithText("Log in with Email").isDisplayed()
//    }
//
//    @Test
//    fun loginScreenIsClicked() {
//        composeTestRule.onNodeWithText("Log in with Email").performClick()
//    }
//
//    @Test
//    fun loginScreen_signUpButtonIsDisplayed() {
//        composeTestRule.onNodeWithText("Sign up").isDisplayed()
//    }
//
//    @Test
//    fun loginScreen_signUpButtonIsClicked() {
//        composeTestRule.onNodeWithText("Sign up").performClick()
//    }
//
//}