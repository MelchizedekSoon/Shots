package com.example.shots.ui.theme

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.shots.data.BlockedUserRepository
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
import com.example.shots.data.ShowMe
import com.example.shots.data.Smoking
import com.example.shots.data.TypeOfMedia
import com.example.shots.data.User
import com.example.shots.data.UserRepository
import com.example.shots.data.UserWhoBlockedYouRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

//sealed interface UsersUiState {
//    object Loading : UsersUiState
//    data class Success(val users: List<User>) : UsersUiState
//
////    data class Error(val errorMessage: ErrorMessage) : UsersUiState
//
//    data class Error(val errorMessage: String) : UsersUiState
//
//}

data class UsersUiState(
    var users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val userRepository: UserRepository,
    private val userWhoBlockedYouRepository: UserWhoBlockedYouRepository,
    private val blockedUserRepository: BlockedUserRepository,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    val locationViewModel = LocationViewModel(
        firebaseRepository,
        userRepository,
        dispatcher
    )

    // Use MutableState to hold the user data

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _uiState = MutableStateFlow<UsersUiState>(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()


    fun getYourUserId(): String {
        return userRepository.getYourUserId()
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                loadUsers()
                if (getYourUserId().isNotEmpty()) {
                    fetchUpdatedCurrentUser().collect { returnedUser ->
                        _user.value = returnedUser
                    }
                }
            } catch (npe: NullPointerException) {
                Log.d("UserViewModel", "Exception - $npe")
            }
        }
    }

    //    sealed class UiState {
//        object Initial : UiState()
//        data class AuthSuccess(val user: FirebaseUser) : UiState()
//        object AuthFailed : UiState()
//    }


    // Only to be used when building user in signup initially
    fun getInitialUser(): User {
        return User()
    }

    fun fetchUser(): User? {
        // Code to fetch the user data from a repository or data source
        _user.value = user.value
        return _user.value
    }

    private suspend fun fetchUpdatedUsers(): Flow<List<User>> {
        return userRepository.fetchUpdatedUsers()
    }

    suspend fun getCurrentUser(userId: String?): Flow<User> {
        return userRepository.getCurrentUser()
    }

    private suspend fun fetchUpdatedCurrentUser(): Flow<User> {
        return userRepository.fetchUpdatedCurrentUser()
    }


    // Fetch and set the user data directly
    fun getUser(): User {
        return userRepository.getUser(getYourUserId())

//        fun getUserSynchronously(): User? {
//            val completableFuture = CompletableFuture<User?>()
//            viewModelScope.launch(Dispatchers.IO) {
//                val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
//                val fetchedUser = fetchUserFromRoom(yourUserId)
//                completableFuture.complete(fetchedUser)
//            }
//
//            return completableFuture.get()
//        }
//
//        // Call the inner function for synchronous retrieval
//        return getUserSynchronously()
    }

    fun loadYourUser() {
        viewModelScope.launch(Dispatchers.IO) {
            getCurrentUser(getYourUserId()).collect { returnedUser ->
                Log.d("UserViewModel", "User - $returnedUser")
                _user.value = returnedUser
            }
        }
    }

    fun resetYourUser() {
        viewModelScope.launch(Dispatchers.IO) {
            _user.value = null
        }
    }

    fun loadUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val yourUser = fetchUser()

                val cards = mutableListOf<User>()

                var usersWhoBlockedYouList = mutableListOf<String>()

                var blockedUsersList = mutableListOf<String>()

                userWhoBlockedYouRepository.fetchUpdatedUsersWhoBlockedYou()
                    .collect { returnedUsersWhoBlockedYouList ->
                        usersWhoBlockedYouList = returnedUsersWhoBlockedYouList.toMutableList()
                    }

                blockedUserRepository.fetchUpdatedBlockedUsers()
                    .collect { returnedBlockedUsersList ->
                        blockedUsersList = returnedBlockedUsersList.toMutableList()
                    }

                fetchUpdatedUsers().collect { updatedUsers ->
                    for (updatedUser in updatedUsers) {
                        if (updatedUser.id !in blockedUsersList && updatedUser.id !in usersWhoBlockedYouList) {
                            if (updatedUser.mediaOne?.isNotBlank() == true &&
                                updatedUser.displayName?.isNotBlank() == true &&
                                updatedUser.mediaProfileVideo?.isNotBlank() == true &&
                                updatedUser.gender != Gender.UNKNOWN &&
                                updatedUser.age >= (yourUser?.ageMinToShow ?: 18) &&
                                updatedUser.age <= (yourUser?.ageMaxToShow ?: 35)
                            ) {

                                val distance = locationViewModel.calculateDistance(
                                    yourUser?.latitude ?: 0.0,
                                    yourUser?.longitude ?: 0.0,
                                    updatedUser.latitude ?: 0.0, updatedUser.longitude ?: 0.0
                                )

                                val distanceToShowUsers = when (yourUser?.showUsers) {
                                    Distance.TEN -> 10
                                    Distance.TWENTY -> 20
                                    Distance.THIRTY -> 30
                                    Distance.FORTY -> 40
                                    Distance.FIFTY -> 50
                                    Distance.SIXTY -> 60
                                    Distance.SEVENTY -> 70
                                    Distance.EIGHTY -> 80
                                    Distance.NINETY -> 90
                                    Distance.ONE_HUNDRED -> 100
                                    Distance.ANYWHERE -> Int.MAX_VALUE
                                    null -> 10
                                }

                                val distanceToAcceptShots = when (yourUser?.acceptShots) {
                                    Distance.TEN -> 10
                                    Distance.TWENTY -> 20
                                    Distance.THIRTY -> 30
                                    Distance.FORTY -> 40
                                    Distance.FIFTY -> 50
                                    Distance.SIXTY -> 60
                                    Distance.SEVENTY -> 70
                                    Distance.EIGHTY -> 80
                                    Distance.NINETY -> 90
                                    Distance.ONE_HUNDRED -> 100
                                    Distance.ANYWHERE -> Int.MAX_VALUE
                                    null -> 10
                                }

                                if (distance <= distanceToShowUsers && distance <= distanceToAcceptShots) {
                                    if ((yourUser?.showMe == ShowMe.MEN && updatedUser.gender == Gender.MAN) ||
                                        (yourUser?.showMe == ShowMe.WOMEN && updatedUser.gender == Gender.WOMAN) ||
                                        (yourUser?.showMe == ShowMe.ANYONE && (updatedUser.gender == Gender.WOMAN
                                                || updatedUser.gender == Gender.MAN ||
                                                updatedUser.gender == Gender.NON_BINARY))
                                    ) {
                                        cards += updatedUser
                                    }
                                }

                            }
                        }
                    }
                }

                Log.d("UserViewModel", "cards - $cards")

                _uiState.value = UsersUiState().copy(users = cards.toMutableList())
            } catch (e: Exception) {
                _uiState.value = UsersUiState().copy(errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.upsertUser(user)
        }
    }

    fun updateUsers(users: List<User>) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.upsertUsers(users)
        }
    }

    fun storeUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.storeUser(user)
        }
    }

    fun storeUsers(users: List<User>) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.storeUsers(users)
        }
    }

//    fun getUsersFromRepo(): List<User> {
//        return userRepository.getAllUsers()
////        return firebaseRepository.getUsers()
//    }

//    fun storeUserInRoom(user: User) {
//        viewModelScope.launch(Dispatchers.IO) {
//            withContext(Dispatchers.IO) {
//                userRepository.storeUser(user)
//            }
//        }
//
////        viewModelScope.launch(Dispatchers.IO) {
////            withContext(Dispatchers.IO) {
////                try {
////                    val user = getUserDataFromRepo(yourUserId)
////                    Log.d("UserViewModel", "The value for id - ${user?.id}")
////                    Log.d("UserViewModel", "The value for latitude - ${user?.latitude}")
////                    Log.d("UserViewModel", "The value for longitude - ${user?.longitude}")
////                    try {
////                        if (user != null && user.id.isNotBlank()) {
////                            userDao.update(user)
////                            Log.d("UserViewModel", "Updated successfully")
////                        }
////                    } catch (npe: NullPointerException) {
////                        if (user != null && user.id.isNotBlank()) {
////                            userDao.insert(user)
////                            Log.d("UserViewModel", "Insert failed - ${npe.message}")
////                        }
////                    }
////                } catch (e: Exception) {
////                    Log.d("UserViewModel", "Exception - $e")
////                }
////            }
////        }
//
//    }
//
//    fun storeUsersInRoom(users: List<User>) {
//        viewModelScope.launch(Dispatchers.IO) {
//            withContext(Dispatchers.IO) {
//                userRepository.storeUsers(users)
//
//            }
//        }
//
////        viewModelScope.launch(Dispatchers.IO) {
////            withContext(Dispatchers.IO) {
////                try {
////                    try {
////                        userDao.updateAll(users)
////                    } catch (npe: NullPointerException) {
////                        userDao.insertAll(users)
////                    }
////                } catch (e: Exception) {
////                    Log.d("UserViewModel", "Exception - $e")
////                }
////            }
////        }
//    }

    fun fetchUserFromRoom(userId: String): User {
        return userRepository.getUser(userId)
//        return withContext(Dispatchers.IO) {
//            userDao.findById(yourUserId)
//        }
    }

//    fun fetchAllUsersFromRoom(): List<User> {
//        return userRepository.getAllUsers()
////        return withContext(Dispatchers.IO) {
////            userDao.getAll()
////        }
//    }


    // Fetch and set the user data directly
    fun fetchAllNonBlockedUsersFromRoom(): List<User> {

        fun getAllNonBlockedUsersSynchronously(): List<User> {
            val completableFuture = CompletableFuture<List<User>>()
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val user = getUser()
                    val blockedUsers = blockedUserRepository.getBlockedUsers()
//                    val blockedUsers = RoomModule.provideBlockedUserDao(appDatabase)
//                        .findById(user.id ?: "").blockedUsers.toMutableList()
                    val usersWhoBlockedYou = userWhoBlockedYouRepository.getUsersWhoBlockedYou()
                    val users = userRepository.getAllUsers()
                    val filteredUsers = emptyList<User>().toMutableList()
                    val combinedFlow = combine(
                        users,
                        blockedUsers,
                        usersWhoBlockedYou
                    ) { usersList, blockedUsersList, usersWhoBlockedYouList ->
                        Triple(usersList, blockedUsersList, usersWhoBlockedYouList)
                    }
                    combinedFlow.collect { (usersList, blockedUsersList, usersWhoBlockedYouList) ->
                        for (eachUser in usersList) {
                            if (eachUser.id !in blockedUsersList && eachUser.id !in usersWhoBlockedYouList) {
                                filteredUsers += eachUser
                            }
                        }
                    }
                    Log.d("UserViewModel", "blockedUsers - $blockedUsers")
                    Log.d("UserViewModel", "usersWhoBlockedYou - $usersWhoBlockedYou")
                    Log.d("UserViewModel", "Users - $users")
                    Log.d("UserViewModel", "filteredUsers - $filteredUsers")
                    completableFuture.complete(filteredUsers.toList())
                } catch (npe: NullPointerException) {
                    completableFuture.complete(emptyList())
                } catch(e: Exception) {
                    completableFuture.complete(emptyList())
                }
            }

            return completableFuture.get()
        }

        // Call the inner function for synchronous retrieval
        return getAllNonBlockedUsersSynchronously()
    }


    // Fetch and set the user data directly
    fun fetchAllNonBlockedAndFilteredUsersFromRoom(): List<User> {

        fun getAllNonBlockedAndFilteredUsersSynchronously(): List<User> {
            val completableFuture = CompletableFuture<List<User>>()
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val user = getUser()
                    val yourShowUsersMiles = when (user?.showUsers) {
                        Distance.TEN -> 10
                        Distance.TWENTY -> 20
                        Distance.THIRTY -> 30
                        Distance.FORTY -> 40
                        Distance.FIFTY -> 50
                        Distance.SIXTY -> 60
                        Distance.SEVENTY -> 70
                        Distance.EIGHTY -> 80
                        Distance.NINETY -> 90
                        Distance.ONE_HUNDRED -> 100
                        Distance.ANYWHERE -> 15000
                        else -> 10
                    }


//                    val blockedUsers = RoomModule.provideBlockedUserDao(appDatabase)
//                        .findById(user?.id ?: "").blockedUsers.toMutableList()
//                    val usersWhoBlockedYou = RoomModule.provideUserWhoBlockedYouDao(appDatabase)
//                        .findById(user?.id ?: "").usersWhoBlockedYou.toMutableList()
//                    val users = userDao.getAll()

                    val blockedUsers = blockedUserRepository.getBlockedUsers()
                    val usersWhoBlockedYou = userWhoBlockedYouRepository.getUsersWhoBlockedYou()
                    val users = userRepository.getAllUsers()

                    val filteredUsers = emptyList<User>().toMutableList()

                    val combinedFlow = combine(
                        blockedUsers, usersWhoBlockedYou,
                        users
                    ) { blockedUsersList, usersWhoBlockedYouList, usersList ->
                        Triple(blockedUsersList, usersWhoBlockedYouList, usersList)
                    }

                    combinedFlow.collect { (usersWhoBlockedYouList, blockedUsersList, usersList) ->
                        for (eachUser in usersList) {

                            val theirAcceptShotsMiles = when (eachUser.acceptShots) {
                                Distance.TEN -> 10
                                Distance.TWENTY -> 20
                                Distance.THIRTY -> 30
                                Distance.FORTY -> 40
                                Distance.FIFTY -> 50
                                Distance.SIXTY -> 60
                                Distance.SEVENTY -> 70
                                Distance.EIGHTY -> 80
                                Distance.NINETY -> 90
                                Distance.ONE_HUNDRED -> 100
                                Distance.ANYWHERE -> 15000
                                else -> 10
                            }

                            if (eachUser.id !in blockedUsersList && eachUser.id !in usersWhoBlockedYouList) {

                                //this code filters cards based on what is being looked for
                                if (user.gender == Gender.MAN) {
                                    if (user.showMe == ShowMe.WOMEN) {
                                        if (eachUser.gender == Gender.WOMAN) {
                                            if (eachUser.showMe == ShowMe.MEN || eachUser.showMe == ShowMe.ANYONE) {
                                                val distance = locationViewModel.calculateDistance(
                                                    user.latitude ?: 0.0,
                                                    user.longitude ?: 0.0,
                                                    eachUser.latitude ?: 0.0,
                                                    eachUser.longitude ?: 0.0
                                                )
                                                if (distance <= yourShowUsersMiles &&
                                                    distance <= theirAcceptShotsMiles
                                                ) {
                                                    filteredUsers += eachUser
                                                }
                                            }
                                        }
                                    } else if (user.showMe == ShowMe.MEN) {
                                        if (eachUser.gender == Gender.MAN) {
                                            if (eachUser.showMe == ShowMe.MEN || eachUser.showMe == ShowMe.ANYONE) {
                                                val distance = locationViewModel.calculateDistance(
                                                    user.latitude ?: 0.0,
                                                    user.longitude ?: 0.0,
                                                    eachUser.latitude ?: 0.0,
                                                    eachUser.longitude ?: 0.0
                                                )
                                                if (distance <= yourShowUsersMiles &&
                                                    distance <= theirAcceptShotsMiles
                                                ) {
                                                    filteredUsers += eachUser
                                                }
                                            }
                                        }
                                    } else if (user.showMe == ShowMe.ANYONE) {
                                        if (eachUser.gender == Gender.MAN || eachUser.gender == Gender.WOMAN || eachUser.gender == Gender.NON_BINARY) {
                                            if (eachUser.showMe == ShowMe.MEN || eachUser.showMe == ShowMe.ANYONE) {
                                                val distance = locationViewModel.calculateDistance(
                                                    user.latitude ?: 0.0,
                                                    user.longitude ?: 0.0,
                                                    eachUser.latitude ?: 0.0,
                                                    eachUser.longitude ?: 0.0
                                                )
                                                if (distance <= yourShowUsersMiles &&
                                                    distance <= theirAcceptShotsMiles
                                                ) {
                                                    filteredUsers += eachUser
                                                }
                                            }
                                        }
                                    }
                                } else if (user?.gender == Gender.WOMAN) {
                                    if (user.showMe == ShowMe.MEN) {
                                        if (eachUser.gender == Gender.MAN) {
                                            if (eachUser.showMe == ShowMe.WOMEN || eachUser.showMe == ShowMe.ANYONE) {
                                                val distance = locationViewModel.calculateDistance(
                                                    user.latitude ?: 0.0,
                                                    user.longitude ?: 0.0,
                                                    eachUser.latitude ?: 0.0,
                                                    eachUser.longitude ?: 0.0
                                                )
                                                Log.d("UserViewModel", "distance - $distance")
                                                if (distance <= yourShowUsersMiles &&
                                                    distance <= theirAcceptShotsMiles
                                                ) {
                                                    filteredUsers += eachUser
                                                }
                                            }
                                        }
                                    } else if (user.showMe == ShowMe.WOMEN) {
                                        if (eachUser.gender == Gender.WOMAN) {
                                            if (eachUser.showMe == ShowMe.WOMEN || eachUser.showMe == ShowMe.ANYONE) {
                                                val distance = locationViewModel.calculateDistance(
                                                    user.latitude ?: 0.0,
                                                    user.longitude ?: 0.0,
                                                    eachUser.latitude ?: 0.0,
                                                    eachUser.longitude ?: 0.0
                                                )
                                                if (distance <= yourShowUsersMiles &&
                                                    distance <= theirAcceptShotsMiles
                                                ) {
                                                    filteredUsers += eachUser
                                                }
                                            }
                                        }
                                    } else if (user.showMe == ShowMe.ANYONE) {
                                        if (eachUser.gender == Gender.MAN || eachUser.gender == Gender.WOMAN || eachUser.gender == Gender.NON_BINARY) {
                                            if (eachUser.showMe == ShowMe.WOMEN || eachUser.showMe == ShowMe.ANYONE) {
                                                val distance = locationViewModel.calculateDistance(
                                                    user.latitude ?: 0.0,
                                                    user.longitude ?: 0.0,
                                                    eachUser.latitude ?: 0.0,
                                                    eachUser.longitude ?: 0.0
                                                )
                                                if (distance <= yourShowUsersMiles &&
                                                    distance <= theirAcceptShotsMiles
                                                ) {
                                                    filteredUsers += eachUser
                                                }
                                            }
                                        }
                                    }
                                } else if (user?.gender == Gender.NON_BINARY) {
                                    if (user.showMe == ShowMe.MEN) {
                                        if (eachUser.gender == Gender.MAN) {
                                            if (eachUser.showMe == ShowMe.ANYONE) {
                                                val distance = locationViewModel.calculateDistance(
                                                    user.latitude ?: 0.0,
                                                    user.longitude ?: 0.0,
                                                    eachUser.latitude ?: 0.0,
                                                    eachUser.longitude ?: 0.0
                                                )
                                                if (distance <= yourShowUsersMiles &&
                                                    distance <= theirAcceptShotsMiles
                                                ) {
                                                    filteredUsers += eachUser
                                                }
                                            }
                                        }
                                    } else if (user.showMe == ShowMe.WOMEN) {
                                        if (eachUser.gender == Gender.WOMAN) {
                                            if (eachUser.showMe == ShowMe.ANYONE) {
                                                val distance = locationViewModel.calculateDistance(
                                                    user.latitude ?: 0.0,
                                                    user.longitude ?: 0.0,
                                                    eachUser.latitude ?: 0.0,
                                                    eachUser.longitude ?: 0.0
                                                )
                                                if (distance <= yourShowUsersMiles &&
                                                    distance <= theirAcceptShotsMiles
                                                ) {
                                                    filteredUsers += eachUser
                                                }
                                            }
                                        }
                                    } else if (user.showMe == ShowMe.ANYONE) {
                                        if (eachUser.gender == Gender.MAN || eachUser.gender == Gender.WOMAN || eachUser.gender == Gender.NON_BINARY) {
                                            if (eachUser.showMe == ShowMe.ANYONE) {
                                                val distance = locationViewModel.calculateDistance(
                                                    user.latitude ?: 0.0,
                                                    user.longitude ?: 0.0,
                                                    eachUser.latitude ?: 0.0,
                                                    eachUser.longitude ?: 0.0
                                                )
                                                if (distance <= yourShowUsersMiles &&
                                                    distance <= theirAcceptShotsMiles
                                                ) {
                                                    filteredUsers += eachUser
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (user?.showMe == ShowMe.MEN) {
                                        if (eachUser.gender == Gender.MAN) {
                                            filteredUsers += eachUser
                                        }
                                    } else if (user?.showMe == ShowMe.WOMEN) {
                                        if (eachUser.gender == Gender.WOMAN) {
                                            filteredUsers += eachUser
                                        }
                                    } else if (user?.showMe == ShowMe.ANYONE) {
                                        if (eachUser.gender == Gender.MAN
                                            || eachUser.gender == Gender.WOMAN
                                            || eachUser.gender == Gender.NON_BINARY
                                        ) {
                                            filteredUsers += eachUser
                                        }
                                    }
                                }

                            }
                        }
                    }

                    Log.d("UserViewModel", "blockedUsers - $blockedUsers")
                    Log.d("UserViewModel", "usersWhoBlockedYou - $usersWhoBlockedYou")
                    Log.d("UserViewModel", "Users - $users")
                    Log.d("UserViewModel", "filteredUsers - $filteredUsers")
                    completableFuture.complete(filteredUsers.toList())
                } catch (npe: NullPointerException) {
                    completableFuture.complete(emptyList())
                }
            }

            return completableFuture.get()
        }

        // Call the inner function for synchronous retrieval
        return getAllNonBlockedAndFilteredUsersSynchronously()
    }


//    suspend fun fetchAllNonBlockedUsersFromRoom(): List<User> {
//        return withContext(Dispatchers.IO) {
//            val user = getUser()
//            try {
//                val blockedUsers = RoomModule.provideBlockedUserDao(appDatabase)
//                    .findById(user?.id ?: "").blockedUsers.toMutableList()
//                val usersWhoBlockedYou = RoomModule.provideUserWhoBlockedYouDao(appDatabase)
//                    .findById(user?.id ?: "").usersWhoBlockedYou.toMutableList()
//                val users = userDao.getAll()
//                val filteredUsers = emptyList<User>().toMutableList()
//                for (eachUser in users) {
//                    if (eachUser.id !in blockedUsers && eachUser.id !in usersWhoBlockedYou) {
//                        if (eachUser.displayName.isNullOrBlank() ||
//                            eachUser.mediaOne.isNullOrBlank() ||
//                            eachUser.mediaProfileVideo.isNullOrBlank() ||
//                            eachUser.gender == Gender.UNKNOWN
//                        ) {
//                            //not adding
//                        } else {
//                            filteredUsers += eachUser
//                        }
//                    }
//                }
//                Log.d("UserViewModel", "blockedUsers - $blockedUsers")
//                Log.d("UserViewModel", "usersWhoBlockedYou - $usersWhoBlockedYou")
//                Log.d("UserViewModel", "Users - $users")
//                filteredUsers.toList()
//            } catch (npe: NullPointerException) {
//                emptyList()
//            }
//        }
//    }


    var selectedOption by mutableStateOf<LookingFor?>(null)

//    var user = firebaseAuth.currentUser?.email?.let {
//        com.example.shots.data.User(
//            it, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null, null, null, null, null, null
//        )
//    }

    enum class UserField {
        DISPLAY_NAME,
        BIRTHDAY,
        IMAGES,
        VIDEOS,
        PROFILE_VIDEO,
        ABOUT_ME,
        HEIGHT,
        LOOKING_FOR,
        GENDER,
        SEXUAL_ORIENTATION,
        WORK
    }

    suspend fun getUserDataFromRepo(userId: String): User? {
        return firebaseRepository.getUserData(userId)
    }

    fun updateFilter(
        user: User?,
        userViewModel: UserViewModel,
        navController: NavHostController,
        context: Context,
        isDone: () -> Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()

            userData["showMe"] =
                when (user?.showMe) {
                    ShowMe.MEN -> "MEN"
                    ShowMe.WOMEN -> "WOMEN"
                    ShowMe.ANYONE -> "ANYONE"
                    else -> "UNKNOWN"
                }

            userData["showUsers"] =
                when (user?.showUsers ?: "") {
                    Distance.TEN -> "TEN"
                    Distance.TWENTY -> "TWENTY"
                    Distance.THIRTY -> "THIRTY"
                    Distance.FORTY -> "FORTY"
                    Distance.FIFTY -> "FIFTY"
                    Distance.SIXTY -> "SIXTY"
                    Distance.SEVENTY -> "SEVENTY"
                    Distance.EIGHTY -> "EIGHTY"
                    Distance.NINETY -> "NINETY"
                    Distance.ONE_HUNDRED -> "ONE_HUNDRED"
                    Distance.ANYWHERE -> "ANYWHERE"
                    else -> "TEN"
                }

            userData["acceptShots"] =
                when (user?.showUsers ?: "") {
                    Distance.TEN -> "TEN"
                    Distance.TWENTY -> "TWENTY"
                    Distance.THIRTY -> "THIRTY"
                    Distance.FORTY -> "FORTY"
                    Distance.FIFTY -> "FIFTY"
                    Distance.SIXTY -> "SIXTY"
                    Distance.SEVENTY -> "SEVENTY"
                    Distance.EIGHTY -> "EIGHTY"
                    Distance.NINETY -> "NINETY"
                    Distance.ONE_HUNDRED -> "ONE_HUNDRED"
                    Distance.ANYWHERE -> "ANYWHERE"
                    else -> "TEN"
                }

            userData["ageMinToShow"] = user?.ageMinToShow ?: "18"
            userData["ageMaxToShow"] = user?.ageMaxToShow ?: "35"

            saveUserDataToFirebase(
                getYourUserId(),
                userData, mediaItems, context
            ) { wasSaved ->
                if (wasSaved) {
                    isDone()
                }
            }

        }
    }

    fun updateUser(
        updatedExistingUser: User, userId: String, userViewModel: UserViewModel,
        navController: NavController, context: Context, isDone: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("UserViewModel", "User - $updatedExistingUser")
            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()
            userData["id"] = updatedExistingUser.id
            userData["displayName"] =
                updatedExistingUser.displayName ?: ""
            userData["userName"] = updatedExistingUser.userName ?: ""
            userData["latitude"] = updatedExistingUser.latitude ?: 0
            userData["longitude"] = updatedExistingUser.longitude ?: 0
            userData["birthday"] = updatedExistingUser.birthday ?: ""
            userData["aboutMe"] = updatedExistingUser.aboutMe ?: ""
            userData["promptOneQuestion"] =
                updatedExistingUser.promptOneQuestion ?: ""
            userData["promptOneAnswer"] =
                updatedExistingUser.promptOneAnswer ?: ""
            userData["promptTwoQuestion"] =
                updatedExistingUser.promptTwoQuestion ?: ""
            userData["promptTwoAnswer"] =
                updatedExistingUser.promptTwoAnswer ?: ""
            userData["promptThreeQuestion"] =
                updatedExistingUser.promptThreeQuestion ?: ""
            userData["promptThreeAnswer"] =
                updatedExistingUser.promptThreeAnswer ?: ""
            userData["link"] = updatedExistingUser.link ?: ""
            userData["lookingFor"] =
                when (updatedExistingUser.lookingFor) {
                    LookingFor.LONG_TERM -> "LONG_TERM"
                    LookingFor.SHORT_TERM -> "SHORT_TERM"
                    LookingFor.LONG_TERM_BUT_OPEN_MINDED -> "LONG_TERM_BUT_OPEN_MINDED"
                    LookingFor.SHORT_TERM_BUT_OPEN_MINDED -> "SHORT_TERM_BUT_OPEN_MINDED"
                    LookingFor.FRIENDS -> "FRIENDS"
                    LookingFor.UNSURE -> "UNSURE"
                    else -> "UNKNOWN"
                }
            userData["gender"] = when (updatedExistingUser.gender) {
                Gender.MAN -> "MAN"
                Gender.WOMAN -> "WOMAN"
                Gender.NON_BINARY -> "NON_BINARY"
                else -> "UNKNOWN"
            }
            userData["height"] = updatedExistingUser.height ?: ""
            userData["work"] = updatedExistingUser.work ?: ""
            userData["education"] =
                when (updatedExistingUser.education) {
                    Education.SOME_HIGH_SCHOOL -> "SOME_HIGH_SCHOOL"
                    Education.HIGH_SCHOOL -> "HIGH_SCHOOL"
                    Education.SOME_COLLEGE -> " SOME_COLLEGE"
                    Education.UNDERGRAD_DEGREE -> "UNDERGRAD_DEGREE"
                    Education.SOME_GRAD_SCHOOL -> "SOME_GRAD_SCHOOL"
                    Education.GRAD_DEGREE -> "GRAD_DEGREE"
                    Education.TECH_TRADE_SCHOOL -> "TECH_TRADE_SCHOOL"
                    else -> "UNKNOWN"
                }
            userData["kids"] = when (updatedExistingUser.kids) {
                Kids.ONE_DAY -> "ONE_DAY"
                Kids.DONT_WANT -> "DONT_WANT"
                Kids.HAVE_AND_WANT_MORE -> "HAVE_AND_WANT_MORE"
                Kids.HAVE_AND_DONT_WANT_MORE -> "HAVE_AND_DONT_WANT_MORE"
                Kids.UNSURE -> "UNSURE"
                else -> "UNKNOWN"
            }
            userData["religion"] = when (updatedExistingUser.religion) {
                Religion.CHRISTIANITY -> "CHRISTIANITY"
                Religion.ISLAM -> "ISLAM"
                Religion.HINDUISM -> "HINDUISM"
                Religion.BUDDHISM -> "BUDDHISM"
                Religion.SIKHISM -> "SIKHISM"
                Religion.JUDAISM -> "JUDAISM"
                Religion.BAHAI_FAITH -> "BAHAI_FAITH"
                Religion.CONFUCIANISM -> "CONFUCIANISM"
                Religion.JAINISM -> "JAINISM"
                Religion.SHINTOISM -> "SHINTOISM"
                else -> "UNKNOWN"
            }
            userData["pets"] = when (updatedExistingUser.pets) {
                Pets.DOG -> "DOG"
                Pets.CAT -> "CAT"
                Pets.FISH -> "FISH"
                Pets.HAMSTER_OR_GUINEA_PIG -> "HAMSTER_GUINEA_PIG"
                Pets.BIRD -> "BIRD"
                Pets.RABBIT -> "RABBIT"
                Pets.REPTILE -> "REPTILE"
                Pets.AMPHIBIAN -> "AMPHIBIAN"
                else -> "UNKNOWN"
            }
            userData["exercise"] = when (updatedExistingUser.exercise) {
                Exercise.OFTEN -> "OFTEN"
                Exercise.SOMETIMES -> "SOMETIMES"
                Exercise.RARELY -> "RARELY"
                Exercise.NEVER -> "NEVER"
                else -> "UNKNOWN"
            }
            userData["smoking"] = when (updatedExistingUser.smoking) {
                Smoking.YES -> "YES"
                Smoking.ON_OCCASION -> "ON_OCCASION"
                Smoking.NEVER_SMOKE -> "NEVER_SMOKE"
                else -> "UNKNOWN"
            }
            userData["drinking"] = when (updatedExistingUser.drinking) {
                Drinking.YES -> "YES"
                Drinking.ON_OCCASION -> "ON_OCCASION"
                Drinking.NEVER_DRINK -> "NEVER_DRINK"
                else -> "UNKNOWN"
            }
            userData["marijuana"] =
                when (updatedExistingUser.marijuana) {
                    Marijuana.YES -> "YES"
                    Marijuana.ON_OCCASION -> "ON_OCCASION"
                    Marijuana.NEVER -> "NEVER"
                    else -> "UNKNOWN"
                }

            val mediaOneUri = updatedExistingUser.mediaOne?.toUri()
            if (mediaOneUri != null) {
                mediaItems["mediaOne"] = mediaOneUri
            }
            userData["typeOfMediaOne"] =
                when (updatedExistingUser.typeOfMediaOne) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaTwoUri = updatedExistingUser.mediaTwo?.toUri()
            if (mediaTwoUri != null) {
                mediaItems["mediaTwo"] = mediaTwoUri
            }
            userData["typeOfMediaTwo"] =
                when (updatedExistingUser.typeOfMediaTwo) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaThreeUri = updatedExistingUser.mediaThree?.toUri()
            if (mediaThreeUri != null) {
                mediaItems["mediaThree"] = mediaThreeUri
            }
            userData["typeOfMediaThree"] =
                when (updatedExistingUser.typeOfMediaThree) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaFourUri = updatedExistingUser.mediaFour?.toUri()
            if (mediaFourUri != null) {
                mediaItems["mediaFour"] = mediaFourUri
            }
            userData["typeOfMediaFour"] =
                when (updatedExistingUser.typeOfMediaFour) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaFiveUri = updatedExistingUser.mediaFive?.toUri()
            if (mediaFiveUri != null) {
                mediaItems["mediaFive"] = mediaFiveUri
            }
            userData["typeOfMediaFive"] =
                when (updatedExistingUser.typeOfMediaFive) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaSixUri = updatedExistingUser.mediaSix?.toUri()
            if (mediaSixUri != null) {
                mediaItems["mediaSix"] = mediaSixUri
            }
            userData["typeOfMediaSix"] =
                when (updatedExistingUser.typeOfMediaSix) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaSevenUri = updatedExistingUser.mediaSeven?.toUri()
            if (mediaSevenUri != null) {
                mediaItems["mediaSeven"] = mediaSevenUri
            }
            userData["typeOfMediaSeven"] =
                when (updatedExistingUser.typeOfMediaSeven
                ) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaEightUri = updatedExistingUser.mediaEight?.toUri()
            if (mediaEightUri != null) {
                mediaItems["mediaEight"] = mediaEightUri
            }
            userData["typeOfMediaEight"] =
                when (updatedExistingUser.typeOfMediaEight) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaNineUri = updatedExistingUser.mediaNine?.toUri()
            if (mediaNineUri != null) {
                mediaItems["mediaNine"] = mediaNineUri
            }
            userData["typeOfMediaNine"] =
                when (updatedExistingUser.typeOfMediaNine) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val profileVideoUri =
                updatedExistingUser.mediaProfileVideo?.toUri()
            if (profileVideoUri != null) {
                mediaItems["mediaProfileVideo"] = profileVideoUri
            }

            userViewModel.saveUserDataToFirebase(
                userId,
                userData, mediaItems, context
            ) { wasSaved ->
                if (wasSaved) {
                    isDone()
                }
            }

        }
    }

    fun createUser(
        updatedExistingUser: User,
        userViewModel: UserViewModel,
        navController: NavController,
        context: Context,
        dataStore: DataStore<Preferences>,
        isDone: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()
            userData["id"] = updatedExistingUser.id
            userData["displayName"] =
                updatedExistingUser.displayName ?: ""
            userData["userName"] = updatedExistingUser.userName ?: ""
            userData["latitude"] = updatedExistingUser.latitude ?: 0
            userData["longitude"] = updatedExistingUser.longitude ?: 0
            userData["birthday"] = updatedExistingUser.birthday ?: ""
            userData["aboutMe"] = updatedExistingUser.aboutMe ?: ""
            userData["promptOneQuestion"] =
                updatedExistingUser.promptOneQuestion ?: ""
            userData["promptOneAnswer"] =
                updatedExistingUser.promptOneAnswer ?: ""
            userData["promptTwoQuestion"] =
                updatedExistingUser.promptTwoQuestion ?: ""
            userData["promptTwoAnswer"] =
                updatedExistingUser.promptTwoAnswer ?: ""
            userData["promptThreeQuestion"] =
                updatedExistingUser.promptThreeQuestion ?: ""
            userData["promptThreeAnswer"] =
                updatedExistingUser.promptThreeAnswer ?: ""
            userData["link"] = updatedExistingUser.link ?: ""
            userData["lookingFor"] =
                when (updatedExistingUser.lookingFor) {
                    LookingFor.LONG_TERM -> "LONG_TERM"
                    LookingFor.SHORT_TERM -> "SHORT_TERM"
                    LookingFor.LONG_TERM_BUT_OPEN_MINDED -> "LONG_TERM_BUT_OPEN_MINDED"
                    LookingFor.SHORT_TERM_BUT_OPEN_MINDED -> "SHORT_TERM_BUT_OPEN_MINDED"
                    LookingFor.FRIENDS -> "FRIENDS"
                    LookingFor.UNSURE -> "UNSURE"
                    else -> "UNKNOWN"
                }
            userData["gender"] = when (updatedExistingUser.gender) {
                Gender.MAN -> "MAN"
                Gender.WOMAN -> "WOMAN"
                Gender.NON_BINARY -> "NON_BINARY"
                else -> "UNKNOWN"
            }
            userData["height"] = updatedExistingUser.height ?: ""
            userData["work"] = updatedExistingUser.work ?: ""
            userData["education"] =
                when (updatedExistingUser.education) {
                    Education.SOME_HIGH_SCHOOL -> "SOME_HIGH_SCHOOL"
                    Education.HIGH_SCHOOL -> "HIGH_SCHOOL"
                    Education.SOME_COLLEGE -> " SOME_COLLEGE"
                    Education.UNDERGRAD_DEGREE -> "UNDERGRAD_DEGREE"
                    Education.SOME_GRAD_SCHOOL -> "SOME_GRAD_SCHOOL"
                    Education.GRAD_DEGREE -> "GRAD_DEGREE"
                    Education.TECH_TRADE_SCHOOL -> "TECH_TRADE_SCHOOL"
                    else -> "UNKNOWN"
                }
            userData["kids"] = when (updatedExistingUser.kids) {
                Kids.ONE_DAY -> "ONE_DAY"
                Kids.DONT_WANT -> "DONT_WANT"
                Kids.HAVE_AND_WANT_MORE -> "HAVE_AND_WANT_MORE"
                Kids.HAVE_AND_DONT_WANT_MORE -> "HAVE_AND_DONT_WANT_MORE"
                Kids.UNSURE -> "UNSURE"
                else -> "UNKNOWN"
            }
            userData["religion"] = when (updatedExistingUser.religion) {
                Religion.CHRISTIANITY -> "CHRISTIANITY"
                Religion.ISLAM -> "ISLAM"
                Religion.HINDUISM -> "HINDUISM"
                Religion.BUDDHISM -> "BUDDHISM"
                Religion.SIKHISM -> "SIKHISM"
                Religion.JUDAISM -> "JUDAISM"
                Religion.BAHAI_FAITH -> "BAHAI_FAITH"
                Religion.CONFUCIANISM -> "CONFUCIANISM"
                Religion.JAINISM -> "JAINISM"
                Religion.SHINTOISM -> "SHINTOISM"
                else -> "UNKNOWN"
            }
            userData["pets"] = when (updatedExistingUser.pets) {
                Pets.DOG -> "DOG"
                Pets.CAT -> "CAT"
                Pets.FISH -> "FISH"
                Pets.HAMSTER_OR_GUINEA_PIG -> "HAMSTER_GUINEA_PIG"
                Pets.BIRD -> "BIRD"
                Pets.RABBIT -> "RABBIT"
                Pets.REPTILE -> "REPTILE"
                Pets.AMPHIBIAN -> "AMPHIBIAN"
                else -> "UNKNOWN"
            }
            userData["exercise"] = when (updatedExistingUser.exercise) {
                Exercise.OFTEN -> "OFTEN"
                Exercise.SOMETIMES -> "SOMETIMES"
                Exercise.RARELY -> "RARELY"
                Exercise.NEVER -> "NEVER"
                else -> "UNKNOWN"
            }
            userData["smoking"] = when (updatedExistingUser.smoking) {
                Smoking.YES -> "YES"
                Smoking.ON_OCCASION -> "ON_OCCASION"
                Smoking.NEVER_SMOKE -> "NEVER_SMOKE"
                else -> "UNKNOWN"
            }
            userData["drinking"] = when (updatedExistingUser.drinking) {
                Drinking.YES -> "YES"
                Drinking.ON_OCCASION -> "ON_OCCASION"
                Drinking.NEVER_DRINK -> "NEVER_DRINK"
                else -> "UNKNOWN"
            }
            userData["marijuana"] =
                when (updatedExistingUser.marijuana) {
                    Marijuana.YES -> "YES"
                    Marijuana.ON_OCCASION -> "ON_OCCASION"
                    Marijuana.NEVER -> "NEVER"
                    else -> "UNKNOWN"
                }

            val mediaOneUri = updatedExistingUser.mediaOne?.toUri()
            if (mediaOneUri != null) {
                mediaItems["mediaOne"] = mediaOneUri
            }
            userData["typeOfMediaOne"] =
                when (updatedExistingUser.typeOfMediaOne) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaTwoUri = updatedExistingUser.mediaTwo?.toUri()
            if (mediaTwoUri != null) {
                mediaItems["mediaTwo"] = mediaTwoUri
            }
            userData["typeOfMediaTwo"] =
                when (updatedExistingUser.typeOfMediaTwo) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaThreeUri = updatedExistingUser.mediaThree?.toUri()
            if (mediaThreeUri != null) {
                mediaItems["mediaThree"] = mediaThreeUri
            }
            userData["typeOfMediaThree"] =
                when (updatedExistingUser.typeOfMediaThree) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaFourUri = updatedExistingUser.mediaFour?.toUri()
            if (mediaFourUri != null) {
                mediaItems["mediaFour"] = mediaFourUri
            }
            userData["typeOfMediaFour"] =
                when (updatedExistingUser.typeOfMediaFour) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaFiveUri = updatedExistingUser.mediaFive?.toUri()
            if (mediaFiveUri != null) {
                mediaItems["mediaFive"] = mediaFiveUri
            }
            userData["typeOfMediaFive"] =
                when (updatedExistingUser.typeOfMediaFive) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaSixUri = updatedExistingUser.mediaSix?.toUri()
            if (mediaSixUri != null) {
                mediaItems["mediaSix"] = mediaSixUri
            }
            userData["typeOfMediaSix"] =
                when (updatedExistingUser.typeOfMediaSix) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaSevenUri = updatedExistingUser.mediaSeven?.toUri()
            if (mediaSevenUri != null) {
                mediaItems["mediaSeven"] = mediaSevenUri
            }
            userData["typeOfMediaSeven"] =
                when (updatedExistingUser.typeOfMediaSeven
                ) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaEightUri = updatedExistingUser.mediaEight?.toUri()
            if (mediaEightUri != null) {
                mediaItems["mediaEight"] = mediaEightUri
            }
            userData["typeOfMediaEight"] =
                when (updatedExistingUser.typeOfMediaEight) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val mediaNineUri = updatedExistingUser.mediaNine?.toUri()
            if (mediaNineUri != null) {
                mediaItems["mediaNine"] = mediaNineUri
            }
            userData["typeOfMediaNine"] =
                when (updatedExistingUser.typeOfMediaNine) {
                    TypeOfMedia.VIDEO -> "VIDEO"
                    TypeOfMedia.IMAGE -> "IMAGE"
                    else -> "UNKNOWN"
                }

            val profileVideoUri =
                updatedExistingUser.mediaProfileVideo?.toUri()
            if (profileVideoUri != null) {
                mediaItems["mediaProfileVideo"] = profileVideoUri
            }

            userViewModel.saveUserDataToFirebase(
                getYourUserId(),
                userData, mediaItems, context
            ) { wasSaved ->
                if (wasSaved) {
                    isDone()
                    viewModelScope.launch(Dispatchers.IO) {
                        dataStore.edit { preferences ->
                            // this needs adjustment and logic figuring
                            preferences[intPreferencesKey("currentScreen")] = 12
                            preferences[booleanPreferencesKey("hasSignedUp")] = true
                        }
                    }
                }
            }
        }

    }

    fun saveUserDataToFirebase(
        userId: String,
        userData: MutableMap<String, Any>,
        mediaItems: MutableMap<String, Uri>,
        context: Context,
        wasSaved: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val success =
                firebaseRepository.writeUserDataToFirebase(
                    userId,
                    userData,
                    mediaItems,
                    context
                )
            if (success) {
                Log.d(
                    "UserViewModel",
                    "User data successfully added at the time the userData includes $userData" +
                            "and the mediaItems include $mediaItems"
                )
                storeAfterSavingUserDataToFirebase(userId)
                wasSaved(true) // Invoke the success callback
            } else {
                Log.d("UserViewModel", "User data not added")
                wasSaved(false) // Invoke the failure callback
            }
        }
    }


    private suspend fun storeAfterSavingUserDataToFirebase(yourUserId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var retrievedUser = getUserDataFromRepo(yourUserId)
            Log.d(TAG, "Value of returnedUser before adding to ROOM DB - $retrievedUser")
            if (retrievedUser != null) {
                userRepository.upsertUser(retrievedUser)
                loadUsers()
            }
        }
    }

    fun deleteImageFromFirebase(mediaIdentifier: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (getYourUserId() != null) {
                firebaseRepository.deleteMediaFromFirebase(getYourUserId(), mediaIdentifier)
//                Log.d(TAG, "If image was in DB at the $mediaIdentifier spot, it has been deleted")
            } else {
//                Log.d(TAG, "yourUserId is null so deleting the image is not possible")
            }
        }
    }

    suspend fun getMetadataFromRepo(uri: String, mediaIdentifier: String): String {
        return firebaseRepository.getMetadataFromStorage(mediaIdentifier)
    }

//    @Composable
//    fun getUser() : User? {
//        val context = LocalContext.current
//        val appDatabase = RoomModule.provideAppDatabase(context)
//        val userDao = appDatabase.userDao()
//        val yourUserId = firebaseAuth.currentUser?.email ?: ""
//        LaunchedEffect(Unit) {
//            viewModelScope.launch(Dispatchers.IO) {
//                user = userDao.findById(yourUserId)
//                val userList = userDao.getAll()
//                for(eachUser in userList) {
//                    Log.d(ContentValues.TAG, "Here's the data for each user - ${eachUser}")
//                }
//                Log.d(ContentValues.TAG, "This user returned ${user}")
//            }
//        }
//        return user
//    }

    fun saveAndStoreData(
        userId: String,
        userData: MutableMap<String, Any>,
        mediaItems: MutableMap<String, Uri>,
        context: Context,
        nextAction: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.saveUserData(userId, userData, mediaItems, context)
            loadUsers()
            loadYourUser()
            nextAction()
        }
    }

    fun saveAndStoreLocation(
        yourUserId: String, latitude: Double, longitude: Double,
        context: Context,
        userViewModel: UserViewModel
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()
            userData["latitude"] = latitude
            userData["longitude"] = longitude
            val success =
                firebaseRepository.writeUserDataToFirebase(
                    yourUserId,
                    userData,
                    mediaItems,
                    context
                )
            if (success) {
                Log.d(ContentValues.TAG, "location added!")
                val user = userRepository.getCurrentUser()
                user.collect { returnedUser ->
                    updateUser(returnedUser)
                }
            } else {
                Log.d(ContentValues.TAG, "Location failed to be added!")
            }
        }
    }

    fun calculateDistance(
        userLatitude: Double, userLongitude: Double,
        otherUserLatitude: Double, otherUserLongitude: Double
    ): Double {
        val earthRadius = 6371 // Radius of the Earth in kilometers

        val dLat = Math.toRadians(otherUserLatitude - userLatitude)
        val dLon = Math.toRadians(otherUserLongitude - userLongitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                Math.cos(Math.toRadians(userLatitude)) * cos(Math.toRadians(otherUserLatitude)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    fun updateUserField(any: Any) {

    }


}