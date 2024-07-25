package com.example.shots.ui.theme

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shots.FirebaseModule
import com.example.shots.RoomModule

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun ShotsNav(
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    filterViewModel: FilterViewModel,
    editProfileViewModel: EditProfileViewModel,
    loginViewModel: LoginViewModel,
    signupViewModel: SignupViewModel,
    userViewModel: UserViewModel,
    locationViewModel: LocationViewModel,
    bookmarkViewModel: BookmarkViewModel,
    receivedLikeViewModel: ReceivedLikeViewModel,
    sentLikeViewModel: SentLikeViewModel,
    likeViewModel: LikeViewModel,
    ifSeenReceivedShotViewModel: IfSeenReceivedShotViewModel,
    receivedShotViewModel: ReceivedShotViewModel,
    sentShotViewModel: SentShotViewModel,
    blockedUserViewModel: BlockedUserViewModel,
    userWhoBlockedYouViewModel: UserWhoBlockedYouViewModel,
    blockViewModel: BlockViewModel,
    playShotViewModel: PlayShotViewModel,
    searchViewModel: SearchViewModel,
    firebaseViewModel: FirebaseViewModel,
    dataStore: DataStore<Preferences>
) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    val firestore = FirebaseModule.provideFirestore()
    val firebaseStorage = FirebaseModule.provideStorage()
    val firebaseRepository =
        FirebaseModule.provideFirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    val appDatabase = RoomModule.provideAppDatabase(context)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    // Check if the user is already logged in
    var isLoggedIn by rememberSaveable {
        mutableStateOf(false)
    }

    var cards by remember {
        mutableStateOf(
            userViewModel.fetchAllNonBlockedUsersFromRoom().reversed()
        )
    }

    Log.d("NavGraph", "cardsSize - ${cards.size}")

    // used to verify if the user has completed the signUp process by getting passed
    // what is currently the habits screen
    var hasSignedUp by rememberSaveable { mutableStateOf(false) }

    var startDestination by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(Unit) {

        dataStore.data.collect { preferences ->
            isLoggedIn = preferences[booleanPreferencesKey("isLoggedIn")] ?: false
            hasSignedUp = preferences[booleanPreferencesKey("hasSignedUp")] ?: false
            startDestination = preferences[intPreferencesKey("currentScreen")] ?: 0
        }

        if (isLoggedIn) {
            navController.navigate("users")
        }

    }

    Log.d("NavGraph", "startDestination - $startDestination")
    Log.d("NavGraph", "hasSignedUp - $hasSignedUp")



    NavHost(
        navController = navController,
        //set to user for now but usually will be this when function
        startDestination =
        when (startDestination) {
            0 -> if (isLoggedIn) {
                "users"
            } else {
                "login"
            }

            1 -> "signUp"
//            2 -> "signupAge"
//            3 -> "signupDisplayName"
//            4 -> "signupMedia"
//            5 -> "signupProfileVideo"
//            6 -> "signupAboutMe"
//            7 -> "signupPrompts"
//            8 -> "signupLink"
//            9 -> "signupDetails"
//            10 -> "signupEssentials"
//            11 -> "signupFilter"
//            12 -> "signupHabits"
//            13 -> "users"
            else -> "login"
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        composable("login") {
            LoginScreen(
                navController,
                loginViewModel,
                authViewModel,
                signupViewModel,
                blockedUserViewModel,
                userWhoBlockedYouViewModel,
                editProfileViewModel,
                userViewModel,
                bookmarkViewModel,
                ifSeenReceivedShotViewModel,
                receivedLikeViewModel,
                sentLikeViewModel,
                receivedShotViewModel,
                sentShotViewModel,
                firebaseViewModel,
                dataStore
            )
        }
        composable("signup") {
            SignupScreen(
                navController,
                userViewModel,
                bookmarkViewModel,
                receivedLikeViewModel,
                sentLikeViewModel,
                receivedShotViewModel,
                sentShotViewModel,
                blockedUserViewModel,
                userWhoBlockedYouViewModel,
                editProfileViewModel,
                signupViewModel,
                firebaseViewModel,
                dataStore
            )
        }
        composable("signupAge") {
            SignupAgeScreen(navController, signupViewModel, userViewModel, dataStore)
        }
        composable("signupUsername") {
            SignupUsernameScreen(navController, signupViewModel, userViewModel, dataStore)
        }
        composable("signupDisplayName") {
            SignupDisplayNameScreen(navController, signupViewModel, userViewModel, dataStore)
        }
        composable("signupMedia") {
            SignupMediaScreen(navController, signupViewModel, userViewModel, dataStore)
        }
        composable("signupProfileVideo") {
            SignupMediaProfileVideoScreen(navController, signupViewModel, userViewModel, dataStore)
        }
        composable("signupAboutMe") {
            SignupAboutMeScreen(navController, signupViewModel, userViewModel, dataStore)
        }
        composable("signupPrompts") {
            SignupPromptsScreen(navController, signupViewModel, userViewModel, dataStore)
        }
        composable("signupLink") {
            SignupLinkScreen(navController, signupViewModel, userViewModel, dataStore)
        }
        composable("signupDetails") {
            SignupDetailsScreen(navController, signupViewModel, userViewModel, dataStore)
        }
        composable("signupEssentials") {
            SignupEssentialsScreen(navController, signupViewModel, userViewModel, dataStore)
        }
        composable("signupHabits") {
            SignupHabitsScreen(navController, signupViewModel, userViewModel, dataStore)
        }
        composable("search") {
            SearchScreen(navController, searchViewModel, userViewModel)
        }
        composable("profile") {
            ProfileScreen(
                navController,
                userViewModel,
                editProfileViewModel,
                profileViewModel,
                firebaseViewModel,
                dataStore
            )
        }
        composable("users") {
            UsersScreen(
                navController,
                signupViewModel,
                editProfileViewModel,
                userViewModel,
                locationViewModel,
                bookmarkViewModel,
                receivedLikeViewModel,
                sentLikeViewModel,
                receivedShotViewModel,
                sentShotViewModel,
                blockedUserViewModel,
                userWhoBlockedYouViewModel,
                dataStore
            )
        }
        composable(
            route = "userProfile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { navBackStackEntry ->
            var arguments = requireNotNull(navBackStackEntry.arguments)
            var userId = arguments.getString("userId")


            // Pass the userId and ViewModel to the ProfileScreen
            UserProfileScreen(
                navController,
                userId ?: "",
                userViewModel,
                bookmarkViewModel,
                sentLikeViewModel,
                receivedLikeViewModel,
                likeViewModel,
                locationViewModel,
                blockedUserViewModel,
                userWhoBlockedYouViewModel,
                blockViewModel,
                dataStore
            )
        }
        composable("shots") {
            ShotsScreen(
                navController,
                userViewModel,
                sentShotViewModel,
                ifSeenReceivedShotViewModel,
                receivedShotViewModel,
                locationViewModel
            )
        }
        composable("editProfile") {
            EditProfileScreen(navController, userViewModel, editProfileViewModel)
        }
        composable("editProfile/{suggestion}") { backStackEntry ->
            val suggestion = backStackEntry.arguments?.getString("suggestion")
            EditProfileScreen(navController, userViewModel, editProfileViewModel)
        }
        composable("genderSearch") {
            GenderSearchScreen(navController)
        }
        composable("preview") {
            PreviewScreen(navController, userViewModel, locationViewModel)
        }
        composable("bookmark") {
            BookmarkScreen(navController, userViewModel, bookmarkViewModel, locationViewModel)
        }
        composable("like") {
            LikeScreen(
                navController = navController,
                userViewModel,
                receivedLikeViewModel,
                sentLikeViewModel,
                locationViewModel
            )
        }
        composable(
            "camera/{userId}", listOf(navArgument("userId") { type = NavType.StringType })
        ) { navBackStackEntry ->
            val arguments = requireNotNull(navBackStackEntry.arguments)
            val userId = arguments.getString("userId")
            CameraScreen(
                navController = navController, userId, userViewModel.getYourUserId(),
                receivedShotViewModel, ifSeenReceivedShotViewModel, sentShotViewModel
            )
        }
        composable(
            route = "userProfile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { navBackStackEntry ->
            val arguments = requireNotNull(navBackStackEntry.arguments)
            val userId = arguments.getString("userId")


            // Pass the userId and ViewModel to the ProfileScreen
            UserProfileScreen(
                navController,
                userId ?: "",
                userViewModel,
                bookmarkViewModel,
                sentLikeViewModel,
                receivedLikeViewModel,
                likeViewModel,
                locationViewModel,
                blockedUserViewModel,
                userWhoBlockedYouViewModel,
                blockViewModel,
                dataStore
            )
        }
        composable(
            route = "playShot/{currentUserId}/{currentUserShot}/{shotType}",
            arguments = listOf(
                navArgument("currentUserId") { type = NavType.StringType },
                navArgument("currentUserShot") { type = NavType.StringType },
                navArgument("shotType") { type = NavType.StringType }
            )
        ) { navBackStackEntry ->
            val arguments = requireNotNull(navBackStackEntry.arguments)
            val currentUserIdValue = arguments.getString("currentUserId")
            val currentUserShotValue = arguments.getString("currentUserShot")
            val shotTypeValue = arguments.getString("shotType")


            if (currentUserIdValue != null && currentUserShotValue != null && shotTypeValue != null) {
                PlayShotScreen(
                    navController, currentUserIdValue, currentUserShotValue, shotTypeValue,
                    receivedShotViewModel, ifSeenReceivedShotViewModel, sentShotViewModel,
                    playShotViewModel, userViewModel, firebaseViewModel
                )
            }
            // Pass the userId and ViewModel to the ProfileScreen

        }
        composable(
            route = "channels"
        ) {
            ChannelsScreen(navController = navController, userViewModel, firebaseViewModel)
        }
        composable(
            route = "channel/{channelId}",
            arguments = listOf(navArgument("channelId") { type = NavType.StringType })
        ) { navBackStackEntry ->
            val arguments = navBackStackEntry.arguments
            val channelId = arguments?.getString("channelId")
            if (channelId != null) {
                ChannelScreen(navController, channelId)
            }
        }
        composable(
            route = "filter"
        ) {
            FilterScreen(
                navController = navController, userViewModel = userViewModel,
                filterViewModel = filterViewModel
            )
        }
        composable(
            route = "signupFilter"
        ) {
            SignupFilterScreen(navController, userViewModel, dataStore)
        }
        composable(
            route = "blockedUsers"
        ) {
            BlockedUsersScreen(
                navController, userViewModel, blockedUserViewModel,
                userWhoBlockedYouViewModel,
                blockViewModel,
                locationViewModel
            )
        }
        // Define other destinations here
    }
}


//    if (navController.currentDestination?.route !in listOf("login", "signup")) {
//        BottomNavigationBar(navController)
//    }


data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector,
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        val backStackEntry = navController.currentBackStackEntryAsState()

        val bottomNavItems = listOf(
            BottomNavItem(
                name = "Home",
                route = "home",
                icon = Icons.Rounded.Home,
            ), BottomNavItem(
                name = "Create",
                route = "add",
                icon = Icons.Rounded.AddCircle,
            ), BottomNavItem(
                name = "Settings",
                route = "settings",
                icon = Icons.Rounded.Settings,
            )
        )

        Scaffold(bottomBar = {
            Box(modifier = Modifier.navigationBarsPadding()) {
                androidx.compose.material3.NavigationBar(
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = item.route == backStackEntry.value?.destination?.route

                        NavigationBarItem(selected = selected,
                            onClick = { navController.navigate(item.route) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = "${item.name} Icon",
                                    tint = Color.White
                                )
                            })
                    }
                }
            }
        }, content = {
            // Your UI Content
            Text(text = "Your UI Content", modifier = Modifier.padding(it))
        })
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    val navController = rememberNavController()
    BottomNavigationBar(navController = navController)
}

//@Preview
//@Composable
//fun ShotsNavPreview() {
//    ShotsNav(authViewModel, editProfileViewMode, loginViewModel, signupViewModel, userViewModel)
//}
