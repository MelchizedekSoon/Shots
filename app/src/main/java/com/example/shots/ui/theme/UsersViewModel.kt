package com.example.shots.ui.theme

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
import com.example.shots.RoomModule
import com.example.shots.data.AppDatabase
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
import com.example.shots.data.UserDao
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase
) : ViewModel() {

    val userDao = RoomModule.provideUserDao(appDatabase)
    val blockedUserDao = RoomModule.provideBlockedUserDao(appDatabase)
    val userWhoBlockedYouDao = RoomModule.provideUserWhoBlockedYouDao(appDatabase)

    val locationViewModel = LocationViewModel(firebaseRepository, firebaseAuth, appDatabase)

    // Use MutableState to hold the user data
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    // Only to be used when building user in signup initially
    fun getInitialUser(): User {
        return User()
    }

    // Fetch and set the user data directly
    fun getUser(): User? {

        fun getUserSynchronously(): User? {
            val completableFuture = CompletableFuture<User?>()
            viewModelScope.launch(Dispatchers.IO) {
                val userId = firebaseAuth.currentUser?.displayName ?: ""
                val fetchedUser = fetchUserFromRoom(userId)
                completableFuture.complete(fetchedUser)
            }

            return completableFuture.get()
        }

        // Call the inner function for synchronous retrieval
        return getUserSynchronously()
    }

    // Function to update specific fields of the user
    fun updateUserField(updateAction: (User) -> User) {
        _user.value = updateAction(_user.value ?: User())
    }


    suspend fun getUsersFromRepo(): List<User> {
        return firebaseRepository.getUsers()
    }

    fun storeUserInRoom(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val user = getUserDataFromRepo(userId)
                    Log.d("UsersViewModel", "The value for id - ${user?.id}")
                    Log.d("UsersViewModel", "The value for latitude - ${user?.latitude}")
                    Log.d("UsersViewModel", "The value for longitude - ${user?.longitude}")
                    try {
                        if (user != null && user.id.isNotBlank()) {
                            userDao.update(user)
                            Log.d("UsersViewModel", "Updated successfully")
                        }
                    } catch (npe: NullPointerException) {
                        if (user != null && user.id.isNotBlank()) {
                            userDao.insert(user)
                            Log.d("UsersViewModel", "Insert failed - ${npe.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.d("UsersViewModel", "Exception - $e")
                }
            }
        }
    }

    fun storeUsersInRoom(users: List<User>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    try {
                        userDao.updateAll(users)
                    } catch (npe: NullPointerException) {
                        userDao.insertAll(users)
                    }
                } catch (e: Exception) {
                    Log.d("UsersViewModel", "Exception - $e")
                }
            }
        }
    }

    suspend fun fetchUserFromRoom(userId: String): User {
        return withContext(Dispatchers.IO) {
            userDao.findById(userId)
        }
    }

    suspend fun fetchAllUsersFromRoom(): List<User> {
        return withContext(Dispatchers.IO) {
            userDao.getAll()
        }
    }


    // Fetch and set the user data directly
    fun fetchAllNonBlockedUsersFromRoom(): List<User> {

        fun getAllNonBlockedUsersSynchronously(): List<User> {
            val completableFuture = CompletableFuture<List<User>>()
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val user = getUser()

                    val blockedUsers = RoomModule.provideBlockedUserDao(appDatabase)
                        .findById(user?.id ?: "").blockedUsers.toMutableList()
                    val usersWhoBlockedYou = RoomModule.provideUserWhoBlockedYouDao(appDatabase)
                        .findById(user?.id ?: "").usersWhoBlockedYou.toMutableList()
                    val users = userDao.getAll()
                    val filteredUsers = emptyList<User>().toMutableList()
                    for (eachUser in users) {
                        if (eachUser.id !in blockedUsers && eachUser.id !in usersWhoBlockedYou) {
                            filteredUsers += eachUser
                        }
                    }
                    Log.d("UsersViewModel", "blockedUsers - $blockedUsers")
                    Log.d("UsersViewModel", "usersWhoBlockedYou - $usersWhoBlockedYou")
                    Log.d("UsersViewModel", "Users - $users")
                    Log.d("UsersViewModel", "filteredUsers - $filteredUsers")
                    completableFuture.complete(filteredUsers.toList())
                } catch (npe: NullPointerException) {
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


                    val blockedUsers = RoomModule.provideBlockedUserDao(appDatabase)
                        .findById(user?.id ?: "").blockedUsers.toMutableList()
                    val usersWhoBlockedYou = RoomModule.provideUserWhoBlockedYouDao(appDatabase)
                        .findById(user?.id ?: "").usersWhoBlockedYou.toMutableList()
                    val users = userDao.getAll()
                    val filteredUsers = emptyList<User>().toMutableList()
                    for (eachUser in users) {

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

                        if (eachUser.id !in blockedUsers && eachUser.id !in usersWhoBlockedYou) {

                            //this code filters cards based on what is being looked for
                            if (user?.gender == Gender.MAN) {
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
                                            Log.d("UsersViewModel", "distance - $distance")
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
                    Log.d("UsersViewModel", "blockedUsers - $blockedUsers")
                    Log.d("UsersViewModel", "usersWhoBlockedYou - $usersWhoBlockedYou")
                    Log.d("UsersViewModel", "Users - $users")
                    Log.d("UsersViewModel", "filteredUsers - $filteredUsers")
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
//                Log.d("UsersViewModel", "blockedUsers - $blockedUsers")
//                Log.d("UsersViewModel", "usersWhoBlockedYou - $usersWhoBlockedYou")
//                Log.d("UsersViewModel", "Users - $users")
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
        usersViewModel: UsersViewModel,
        navController: NavHostController,
        context: Context,
        isDone: () -> Boolean
    ) {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.displayName ?: ""
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
                userId,
                userData, mediaItems, context
            ) { wasSaved ->
                if (wasSaved) {
                    isDone()
                }
            }

        }
    }

    fun updateUser(
        updatedExistingUser: User, usersViewModel: UsersViewModel,
        navController: NavController, context: Context, isDone: () -> Unit
    ) {
        viewModelScope.launch {
            Log.d("UsersViewModel", "User - $updatedExistingUser")
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

            val userId = firebaseAuth.currentUser?.displayName ?: ""

            usersViewModel.saveUserDataToFirebase(
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
        usersViewModel: UsersViewModel,
        navController: NavController,
        context: Context,
        dataStore: DataStore<Preferences>,
        isDone: () -> Unit
    ) {
        viewModelScope.launch {
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

            val userId = firebaseAuth.currentUser?.displayName ?: ""

            usersViewModel.saveUserDataToFirebase(
                userId,
                userData, mediaItems, context
            ) { wasSaved ->
                if (wasSaved) {
                    isDone()
                    viewModelScope.launch {
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
        viewModelScope.launch {
            val success =
                firebaseRepository.writeUserDataToFirebase(userId, userData, mediaItems, context)
            if (success) {
                Log.d(
                    "UsersViewModel",
                    "User data successfully added at the time the userData includes $userData" +
                            "and the mediaItems include $mediaItems"
                )
                initializeAfterSavingUserDataToFirebase(userId)
                wasSaved(true) // Invoke the success callback
            } else {
                Log.d("UsersViewModel", "User data not added")
                wasSaved(false) // Invoke the failure callback
            }
        }
    }


    suspend fun initializeAfterSavingUserDataToFirebase(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var retrievedUser = getUserDataFromRepo(userId)
                Log.d(TAG, "Value of returnedUser before adding to ROOM DB - $retrievedUser")

                if (retrievedUser != null) {
                    try {
                        userDao.update(retrievedUser)
                        var returnedUser = fetchUserFromRoom(retrievedUser.id)
                        if (returnedUser == null) {
                            userDao.insert(retrievedUser)
                        }
                        Log.d(TAG, "Updated user in Room DB: $returnedUser")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating user in Room DB: ${e.message}", e)
                        try {
                            userDao.insert(retrievedUser)
                            Log.d(TAG, "Inserted user in Room DB: $retrievedUser")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error inserting user in Room DB: ${e.message}", e)
                        }
                    }
                }

                Log.d(TAG, "Returned user - ${getUser()}")
            }
        }
    }

    fun deleteImageFromFirebase(mediaIdentifier: String) {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.displayName ?: ""
            if (userId != null) {
                firebaseRepository.deleteMediaFromFirebase(userId, mediaIdentifier)
//                Log.d(TAG, "If image was in DB at the $mediaIdentifier spot, it has been deleted")
            } else {
//                Log.d(TAG, "userId is null so deleting the image is not possible")
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
//        val userId = firebaseAuth.currentUser?.email ?: ""
//        LaunchedEffect(Unit) {
//            viewModelScope.launch(Dispatchers.IO) {
//                user = userDao.findById(userId)
//                val userList = userDao.getAll()
//                for(eachUser in userList) {
//                    Log.d(ContentValues.TAG, "Here's the data for each user - ${eachUser}")
//                }
//                Log.d(ContentValues.TAG, "This user returned ${user}")
//            }
//        }
//        return user
//    }


}