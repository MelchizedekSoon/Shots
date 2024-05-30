package com.example.shots.ui.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.RoomModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.User
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val usersViewModel: UsersViewModel,
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
        fetchDataFromRoom(usersViewModel)
    }

    private fun fetchDataFromRoom(usersViewModel: UsersViewModel) {
        viewModelScope.launch {
            try {
                val users = usersViewModel.fetchAllNonBlockedUsersFromRoom()
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
                    user.userName?.contains(query, ignoreCase = true) == true
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