package com.example.shots.ui.theme

import android.net.Uri
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavController
import com.example.shots.FirebaseModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupAgeScreen(
    navController: NavController,
    signupViewModel: SignupViewModel,
    userViewModel: UserViewModel,
    dataStore: DataStore<Preferences>
) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    val firestore = FirebaseModule.provideFirestore()
    val firebaseStorage = FirebaseModule.provideStorage()
    val firebaseRepository =
        FirebaseModule.provideFirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val userId by remember { mutableStateOf(firebaseAuth.currentUser?.displayName ?: "") }

    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navController.navigate("signupUsername")
        }
    }

    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    DisposableEffect(backCallback) {
        onBackPressedDispatcher?.addCallback(backCallback)

        onDispose {
            backCallback.isEnabled = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        }) { it ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            LaunchedEffect(Unit) {
                scope.launch {
                    dataStore.edit { preferences ->
                        preferences[intPreferencesKey("currentScreen")] = 2
                    }
                }
            }

            IconButton(
                onClick = {
                    navController.navigate("signupUsername")
                },
                modifier = Modifier.padding(it)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Add your birthday", fontSize = 24.sp,
                    modifier = Modifier
                        .padding(0.dp, 48.dp, 0.dp, 0.dp)
                )
                Text(
                    text = "Your birthday cannot be changed later.", fontSize = 12.sp,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                )
                // First you need to remember a datePickerState.
// This state is where you get the user selection from
                val currentDate = LocalDate.now()
                val minBirthDate = currentDate.minusYears(18)
                val minBirthDateMillis =
                    minBirthDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = minBirthDateMillis
                )

// Second, you simply have to add the DatePicker component to your layout.
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.padding(32.dp)
                )

                val selectedDate = datePickerState.selectedDateMillis?.let {
                    Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate()
                }


                Text("Selected: ${selectedDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "No selection"}")

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        if (datePickerState.selectedDateMillis == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "No date" +
                                            " identified. Please add a valid birth date."
                                )
                            }
                        } else if (datePickerState.selectedDateMillis!! > minBirthDateMillis) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "You must be 18" +
                                            " or older to use an account."
                                )
                            }
                        } else {
                            val localDate: LocalDate = selectedDate ?: LocalDate.now()
                            if (localDate != LocalDate.now()) {
                                val calendar: Calendar = Calendar.getInstance()
                                calendar.set(
                                    localDate.year,
                                    localDate.monthValue - 1,
                                    localDate.dayOfMonth
                                )
                                val selectedDateInMillis =
                                    localDate.atStartOfDay().toInstant(ZoneOffset.UTC)
                                        .toEpochMilli()
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        val existingUser = userViewModel.getUser()
                                        val updatedExistingUser =
                                            existingUser?.copy(
                                                birthday = System.currentTimeMillis()
                                                        - selectedDateInMillis
                                            )
                                        if (updatedExistingUser != null) {
                                            val userData: MutableMap<String, Any> = mutableMapOf()
                                            val mediaItems: MutableMap<String, Uri> = mutableMapOf()
                                            val birthday = updatedExistingUser.birthday ?: 0
                                            userData["birthday"] = birthday
                                            userViewModel.saveUserDataToFirebase(
                                                userId, userData,
                                                mediaItems, context
                                            ) {
                                                navController.navigate("signupDisplayName")
                                            }
                                        }
                                    }
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please add a valid birth date.")
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        //padding is 376
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                ) {
                    Text("Add Birthday", fontSize = 16.sp)
                }
            }
        }
    }
}

//@Composable
//@Preview
//fun SignupAgeScreenPreview() {
//    SignupAgeScreen(
//        navController = rememberNavController(),
//        string = backStackEntry.arguments?.getString("userId")
//    )
//}