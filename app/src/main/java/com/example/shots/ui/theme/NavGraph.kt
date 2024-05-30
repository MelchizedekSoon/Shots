package com.example.shots.ui.theme

// Example: NavGraph.kt
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

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun ShotsNav(
    authViewModel: AuthViewModel,
    editProfileViewMode: EditProfileViewModel,
    loginViewModel: LoginViewModel,
    signupViewModel: SignupViewModel,
    usersViewModel: UsersViewModel,
    locationViewModel: LocationViewModel,
    bookmarkViewModel: BookmarkViewModel,
    receivedLikeViewModel: ReceivedLikeViewModel,
    sentLikeViewModel: SentLikeViewModel,
    likeViewModel: LikeViewModel,
    receivedShotViewModel: ReceivedShotViewModel,
    sentShotViewModel: SentShotViewModel,
    blockedUserViewModel: BlockedUserViewModel,
    userWhoBlockedYouViewModel: UserWhoBlockedYouViewModel,
    blockViewModel: BlockViewModel,
    playShotViewModel: PlayShotViewModel,
    searchViewModel: SearchViewModel,
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

    val userDao = RoomModule.provideUserDao(appDatabase)
    val bookmarkDao = RoomModule.provideBookmarkDao(appDatabase)
    val receivedLikeDao = RoomModule.provideReceiveLikeDao(appDatabase)
    val sentLikeDao = RoomModule.provideSentLikeDao(appDatabase)

    // Check if the user is already logged in
    var isLoggedIn by rememberSaveable {
        mutableStateOf(false)
    }

    var cards by remember{ mutableStateOf(usersViewModel.fetchAllNonBlockedUsersFromRoom().reversed()) }

    Log.d("NavGraph", "cardsSize - ${cards.size}")

    var hasCards by remember{ mutableStateOf(cards.size > 0) }

    // used to verify if the user has completed the signUp process by getting passed
    // what is currently the habits screen
    var hasSignedUp by rememberSaveable { mutableStateOf(false) }
    var isNewSession = true

    var startDestination by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(Unit) {

        dataStore.data.collect { preferences ->
            isLoggedIn = preferences[booleanPreferencesKey("isLoggedIn")] ?: false
            hasSignedUp = preferences[booleanPreferencesKey("hasSignedUp")] ?: false
            startDestination = preferences[intPreferencesKey("currentScreen")] ?: 0
        }

        if (isLoggedIn && hasSignedUp) {
            navController.navigate("users")
        }

    }


    NavHost(
        navController = navController,
        //set to user for now but usually will be this when function
        startDestination =
        when (startDestination) {
            0 -> if (hasSignedUp) {
                "users"
            } else {
                "login"
            }

            1 -> "signupUsername"
            2 -> "signupAge"
            3 -> "signupDisplayName"
            4 -> "signupMedia"
            5 -> "signupProfileVideo"
            6 -> "signupAboutMe"
            7 -> "signupPrompts"
            8 -> "signupLink"
            9 -> "signupDetails"
            10 -> "signupEssentials"
            11 -> "signupFilter"
            12 -> "signupHabits"
            13 -> "users"
            else -> "login"
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        composable("login") {
            LoginScreen(
                navController,
                signupViewModel,
                usersViewModel,
                bookmarkViewModel,
                receivedLikeViewModel,
                sentLikeViewModel,
                receivedShotViewModel,
                sentShotViewModel,
                dataStore
            )
        }
        composable("signup") {
            SignupScreen(
                navController, signupViewModel, usersViewModel, locationViewModel, dataStore
            )
        }
        composable("signupAge") {
            SignupAgeScreen(navController, signupViewModel, usersViewModel, dataStore)
        }
        composable("signupUsername") {
            SignupUsernameScreen(navController, signupViewModel, usersViewModel, dataStore)
        }
        composable("signupDisplayName") {
            SignupDisplayNameScreen(navController, signupViewModel, usersViewModel, dataStore)
        }
        composable("signupMedia") {
            SignupMediaScreen(navController, signupViewModel, usersViewModel, dataStore)
        }
        composable("signupProfileVideo") {
            SignupMediaProfileVideoScreen(navController, signupViewModel, usersViewModel, dataStore)
        }
        composable("signupAboutMe") {
            SignupAboutMeScreen(navController, signupViewModel, usersViewModel, dataStore)
        }
        composable("signupPrompts") {
            SignupPromptsScreen(navController, signupViewModel, usersViewModel, dataStore)
        }
        composable("signupLink") {
            SignupLinkScreen(navController, signupViewModel, usersViewModel, dataStore)
        }
        composable("signupDetails") {
            SignupDetailsScreen(navController, signupViewModel, usersViewModel, dataStore)
        }
        composable("signupEssentials") {
            SignupEssentialsScreen(navController, signupViewModel, usersViewModel, dataStore)
        }
        composable("signupHabits") {
            SignupHabitsScreen(navController, signupViewModel, usersViewModel, dataStore)
        }
        composable("search") {
            SearchScreen(navController, searchViewModel, usersViewModel)
        }
        composable("profile") {
            ProfileScreen(
                navController,
                usersViewModel,
                bookmarkViewModel,
                receivedLikeViewModel,
                sentLikeViewModel,
                receivedShotViewModel,
                sentShotViewModel,
                dataStore
            )
        }
        composable("users") {
            UsersScreen(
                navController,
                signupViewModel,
                usersViewModel,
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
            val arguments = requireNotNull(navBackStackEntry.arguments)
            val userId = arguments.getString("userId")


            // Pass the userId and ViewModel to the ProfileScreen
            UserProfileScreen(
                navController,
                userId ?: "",
                usersViewModel,
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
                usersViewModel,
                sentShotViewModel,
                receivedShotViewModel,
                locationViewModel
            )
        }
        composable("editProfile") {
            EditProfileScreen(navController, usersViewModel)
        }
        composable("editProfile/{suggestion}") { backStackEntry ->
            val suggestion = backStackEntry.arguments?.getString("suggestion")
            EditProfileScreen(navController, usersViewModel)
        }
        composable("genderSearch") {
            GenderSearchScreen(navController)
        }
        composable("preview") {
            PreviewScreen(navController, usersViewModel, locationViewModel)
        }
        composable("bookmark") {
            BookmarkScreen(navController, usersViewModel, bookmarkViewModel, locationViewModel)
        }
        composable("like") {
            LikeScreen(
                navController = navController,
                usersViewModel,
                receivedLikeViewModel,
                sentLikeViewModel,
                locationViewModel,
                dataStore
            )
        }
        composable(
            "camera/{userId}", listOf(navArgument("userId") { type = NavType.StringType })
        ) { navBackStackEntry ->
            val arguments = requireNotNull(navBackStackEntry.arguments)
            val userId = arguments.getString("userId")
            CameraScreen(
                navController = navController, userId, receivedShotViewModel, sentShotViewModel
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
                usersViewModel,
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
            val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
            val currentUserIdValue = arguments.getString("currentUserId")
            val currentUserShotValue = arguments.getString("currentUserShot")
            val shotTypeValue = arguments.getString("shotType")


            if (currentUserIdValue != null && currentUserShotValue != null && shotTypeValue != null) {
                PlayShotScreen(
                    navController, yourUserId, currentUserIdValue, currentUserShotValue,
                    shotTypeValue, receivedShotViewModel,
                    sentShotViewModel, playShotViewModel, usersViewModel
                )
            }
            // Pass the userId and ViewModel to the ProfileScreen

        }
        composable(
            route = "channels"
        ) {
            ChannelsScreen(navController = navController, usersViewModel)
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
            FilterScreen(navController = navController, usersViewModel = usersViewModel)
        }
        composable(
            route = "signupFilter"
        ) {
            SignupFilterScreen(navController, usersViewModel, dataStore)
        }
        composable(
            route = "blockedUsers"
        ) {
            BlockedUsersScreen(
                navController, usersViewModel, blockedUserViewModel,
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
//    ShotsNav(authViewModel, editProfileViewMode, loginViewModel, signupViewModel, usersViewModel)
//}
