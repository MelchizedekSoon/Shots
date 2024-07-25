package com.example.shots.ui.theme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.data.BlockedUserRepository
import com.example.shots.data.Gender
import com.example.shots.data.User
import com.example.shots.data.UserRepository
import com.example.shots.data.UserWhoBlockedYouRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val userViewModel: UserViewModel,
    private val userWhoBlockedYouRepository: UserWhoBlockedYouRepository,
    private val blockedUserRepository: BlockedUserRepository,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    // State whether the search is happening or not
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    // State the text typed by the user
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    // Original list of users
    private val _usersList = MutableStateFlow<List<User>>(emptyList())

    // Filtered list of users based on the search text
    private val _filteredUsersList = MutableStateFlow<List<User>>(emptyList())
    val filteredUsersList: StateFlow<List<User>> = _filteredUsersList

    init {
        try {
            loadUsers()
            fetchDataFromRoom(userViewModel)
        } catch (npe: NullPointerException) {
            Log.d("SearchViewModel", "NullPointerException")
        }
    }

    fun loadUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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

                userRepository.fetchUpdatedUsers().collect { updatedUsers ->
                    for (updatedUser in updatedUsers) {
                        if (updatedUser.id !in blockedUsersList && updatedUser.id !in usersWhoBlockedYouList) {
                            if (updatedUser.mediaOne?.isNotBlank() == true &&
                                updatedUser.displayName?.isNotBlank() == true &&
                                updatedUser.mediaProfileVideo?.isNotBlank() == true &&
                                updatedUser.gender != Gender.UNKNOWN
                            )
                                cards += updatedUser
                        }
                    }
                }

                _usersList.value = cards

                Log.d("UserViewModel", "cards - $cards")

//                _uiState.value = UsersUiState().copy(users = cards.toMutableList())
            } catch (e: Exception) {
//                _uiState.value = UsersUiState().copy(errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    private fun fetchDataFromRoom(userViewModel: UserViewModel) {
        viewModelScope.launch {
            try {

                val users = userViewModel.fetchAllNonBlockedUsersFromRoom()
                _usersList.value = users
            } catch (e: Exception) {
                // Handle any exceptions that occur during the database operation
            }
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
        filterUsersList(text)
    }

    private fun filterUsersList(query: String) {
        viewModelScope.launch {
            val filteredList = if (query.isNotBlank()) {
                _usersList.value.filter { user ->
                    user.userName?.contains(query, ignoreCase = true) == true ||
                            user.displayName?.contains(query, ignoreCase = true) == true
                }
            } else {
                emptyList()
            }
            _filteredUsersList.value = filteredList
        }
    }

    fun onToggleSearch() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) {
            onSearchTextChange("")
        }
    }
}