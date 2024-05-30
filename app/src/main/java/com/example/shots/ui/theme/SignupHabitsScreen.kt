package com.example.shots.ui.theme

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavController
import com.example.shots.FirebaseModule
import com.example.shots.R
import com.example.shots.RoomModule
import com.example.shots.data.Drinking
import com.example.shots.data.Exercise
import com.example.shots.data.Marijuana
import com.example.shots.data.Smoking
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SignupHabitsScreen(
    navController: NavController, signupViewModel: SignupViewModel,
    usersViewModel: UsersViewModel,
    dataStore: DataStore<Preferences>
) {
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    val firestore = FirebaseModule.provideFirestore()
    val firebaseStorage = FirebaseModule.provideStorage()
    val firebaseRepository =
        FirebaseModule.provideFirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isFocused by remember { mutableStateOf(false) }
    val appDatabase = RoomModule.provideAppDatabase(LocalContext.current)
    var userDao = RoomModule.provideUserDao(appDatabase)
//    var hasBeenChanged by rememberSaveable { mutableStateOf(false) }
    var hasBeenChanged = false
    var context = LocalContext.current
    var isGoingBack by rememberSaveable { mutableStateOf(false) }
    var isEditingProfile by remember { mutableStateOf(false) }

    var user by remember { mutableStateOf(usersViewModel.getUser()) }

    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navController.navigate("signupFilter")
        }
    }

    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    DisposableEffect(backCallback) {
        onBackPressedDispatcher?.addCallback(backCallback)

        onDispose {
            backCallback.isEnabled = false
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = { ->
        SnackbarHost(
            hostState = snackbarHostState
        )
    }) { it ->
        if (isEditingProfile) {
            WaitScreen()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                LaunchedEffect(Unit) {
                    scope.launch {
                        dataStore.edit { preferences ->
                            preferences[intPreferencesKey("currentScreen")] = 12
                        }
                    }
                }
                IconButton(
                    onClick = { navController.navigate("signupFilter") },
                    modifier = Modifier.padding(it)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                IconButton(
                    onClick = {
                        val userData: MutableMap<String, Any> = mutableMapOf()
                        val mediaItems: MutableMap<String, Uri> = mutableMapOf()

                        val updatedExistingUser = user

                        if (updatedExistingUser != null) {
                            userData["exercise"] = "UNKNOWN"
                            userData["smoking"] = "UNKNOWN"
                            userData["drinking"] = "UNKNOWN"
                            userData["marijuana"] = "UNKNOWN"

                            val userId = user?.id ?: ""
                            usersViewModel.saveUserDataToFirebase(
                                userId ?: "", userData,
                                mediaItems, context
                            ) {
                                navController.navigate("users")
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(it)
                        .padding(0.dp, 0.dp, 8.dp, 0.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(text = "Skip")
                }
                Column(
                    modifier = Modifier.padding(32.dp, 48.dp, 32.dp, 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
//                Image(
//                    painterResource(R.drawable.shots_3_cropped),
//                    "Shots Logo",
//                    modifier = Modifier
//                        .height(360.dp)
//                        .aspectRatio(1f)
//                )
                    Text(
                        text = "Add Your Habits",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(0.dp, 48.dp, 0.dp, 0.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    var exerciseExpanded by remember { mutableStateOf(false) }
                    var exerciseStoredOption by
                    rememberSaveable {
                        mutableStateOf(
                            when (user?.exercise) {
                                Exercise.OFTEN -> "OFTEN"
                                Exercise.SOMETIMES -> "SOMETIMES"
                                Exercise.RARELY -> "RARELY"
                                Exercise.NEVER -> "NEVER"
                                else -> ""
                            }
                        )
                    }
                    var exerciseSelectedOption by rememberSaveable {
                        mutableStateOf(
                            when (exerciseStoredOption) {
                                "OFTEN" -> "Often"
                                "SOMETIMES" -> "Sometimes'"
                                "RARELY" -> "Rarely"
                                "NEVER" -> "Never"
                                else -> ""
                            }
                        )
                    }

                    var smokingExpanded by remember {
                        mutableStateOf(false)
                    }
                    var smokingStoredOption by rememberSaveable {
                        mutableStateOf(
                            when (user?.smoking) {
                                Smoking.YES -> "YES"
                                Smoking.ON_OCCASION -> "ON_OCCASION"
                                Smoking.NEVER_SMOKE -> "NEVER_SMOKE"
                                else -> ""
                            }
                        )
                    }
                    var smokingSelectedOption by rememberSaveable {
                        mutableStateOf(
                            when (smokingStoredOption) {
                                "YES" -> "Yes"
                                "ON_OCCASION" -> "On occasion"
                                "NEVER_SMOKE" -> "Never smoke"
                                else -> ""
                            }
                        )
                    }

                    var drinkingExpanded by remember {
                        mutableStateOf(false)
                    }
                    var drinkingStoredOption by rememberSaveable {
                        mutableStateOf(
                            when (user?.drinking) {
                                Drinking.YES -> "YES"
                                Drinking.ON_OCCASION -> "ON_OCCASION"
                                Drinking.NEVER_DRINK -> "NEVER_DRINK"
                                else -> ""
                            }
                        )
                    }
                    var drinkingSelectedOption by rememberSaveable {
                        mutableStateOf(
                            when (drinkingStoredOption) {
                                "YES" -> "Yes"
                                "ON_OCCASION" -> "On occasion"
                                "NEVER_DRINK" -> "Never drink"
                                else -> ""
                            }
                        )
                    }

                    var marijuanaExpanded by remember {
                        mutableStateOf(false)
                    }
                    var marijuanaStoredOption by rememberSaveable {
                        mutableStateOf(
                            when (user?.marijuana) {
                                Marijuana.YES -> "YES"
                                Marijuana.ON_OCCASION -> "ON_OCCASION"
                                Marijuana.NEVER -> "NEVER"
                                else -> ""
                            }
                        )
                    }
                    var marijuanaSelectedOption by rememberSaveable {
                        mutableStateOf(
                            when (marijuanaStoredOption) {
                                "YES" -> "Yes"
                                "ON_OCCASION" -> "On occasion"
                                "NEVER" -> "Never"
                                else -> ""
                            }
                        )
                    }

                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { exerciseExpanded = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(
                                    id = R.drawable.exercise_fill0_wght400_grad0_opsz24
                                ),
                                contentDescription = "Education Icon",
                                tint = Color(0xFFFF6F00)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (exerciseSelectedOption == "") {
                                exerciseStoredOption = "UNKNOWN"
                                Text(
                                    text = "Exercise", fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                Text(
                                    text = "Add", fontSize = 20.sp, color = Color(0xFFFF6F00)
                                )
                            } else {
                                Text(text = exerciseSelectedOption, fontSize = 20.sp)
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                IconButton(onClick = { exerciseSelectedOption = "" }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cancel_24px),
                                        contentDescription = "Cancel Button",
                                        modifier = Modifier,
                                        tint = Color(0xFFFF6F00)
                                    )
                                }
                            }
                            DropdownMenu(modifier = Modifier.background(Color.White),
                                expanded = exerciseExpanded,
                                onDismissRequest = { exerciseExpanded = false }) {
                                DropdownMenuItem(text = { Text("Often") }, onClick = {
                                    exerciseSelectedOption = "Often"
                                    exerciseStoredOption = "OFTEN"
                                    exerciseExpanded = false
                                })
                                DropdownMenuItem(text = { Text("Sometimes") }, onClick = {
                                    exerciseSelectedOption = "Sometimes"
                                    exerciseStoredOption = "SOMETIMES"
                                    exerciseExpanded = false
                                })
                                DropdownMenuItem(text = { Text("Rarely") }, onClick = {
                                    exerciseSelectedOption = "Rarely"
                                    exerciseStoredOption = "RARELY"
                                    exerciseExpanded = false
                                })
                                DropdownMenuItem(text = { Text("Never") }, onClick = {
                                    exerciseSelectedOption = "Never"
                                    exerciseStoredOption = "NEVER"
                                    exerciseExpanded = false
                                })
                            }
                            LaunchedEffect(exerciseStoredOption) {
                                if (exerciseSelectedOption != "") {
                                    hasBeenChanged = true
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                        }
                    }
                    HorizontalDivider(thickness = 1.dp)
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { smokingExpanded = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(
                                    id = R.drawable.smoking_cigar_svgrepo_com_2
                                ),
                                contentDescription = "Smoking Icon",
                                tint = Color(0xFFFF6F00)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (smokingSelectedOption == "") {
                                smokingStoredOption = "UNKNOWN"
                                Text(
                                    text = "Smoking", fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                Text(
                                    text = "Add", fontSize = 20.sp, color = Color(0xFFFF6F00)
                                )
                            } else {
                                Text(text = smokingSelectedOption, fontSize = 20.sp)
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                IconButton(onClick = { smokingSelectedOption = "" }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cancel_24px),
                                        contentDescription = "Cancel Button",
                                        modifier = Modifier,
                                        tint = Color(0xFFFF6F00)
                                    )
                                }
                            }
                            DropdownMenu(modifier = Modifier.background(Color.White),
                                expanded = smokingExpanded,
                                onDismissRequest = { smokingExpanded = false }) {
                                DropdownMenuItem(text = { Text("Yes") }, onClick = {
                                    smokingSelectedOption = "Yes"
                                    smokingStoredOption = "YES"
                                    smokingExpanded = false
                                })
                                DropdownMenuItem(text = { Text("On occasion") }, onClick = {
                                    smokingSelectedOption = "On occasion"
                                    smokingStoredOption = "ON_OCCASION"
                                    smokingExpanded = false
                                })
                                DropdownMenuItem(text = { Text("Never smoke") }, onClick = {
                                    smokingSelectedOption = "Never smoke"
                                    smokingStoredOption = "NEVER_SMOKE"
                                    smokingExpanded = false
                                })
                                DropdownMenuItem(text = { Text("Never") }, onClick = {
                                    smokingSelectedOption = "Never"
                                    smokingStoredOption = "NEVER"
                                    smokingExpanded = false
                                })
                            }
                            LaunchedEffect(smokingSelectedOption) {
                                if (exerciseSelectedOption != "") {
                                    hasBeenChanged = true
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                        }
                    }
                    HorizontalDivider(thickness = 1.dp)
                    Box(modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                        .clickable {
                            drinkingExpanded = true
                        }) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(
                                    id = R.drawable.drink_cocktail_svgrepo_com
                                ),
                                contentDescription = "Education Icon",
                                tint = Color(0xFFFF6F00)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (drinkingSelectedOption == "") {
                                drinkingStoredOption = "UNKNOWN"
                                Text(
                                    text = "Drinking", fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                Text(
                                    text = "Add", fontSize = 20.sp, color = Color(0xFFFF6F00)
                                )
                            } else {
                                Text(text = drinkingSelectedOption, fontSize = 20.sp)
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                IconButton(onClick = { drinkingSelectedOption = "" }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cancel_24px),
                                        contentDescription = "Cancel Button",
                                        modifier = Modifier,
                                        tint = Color(0xFFFF6F00)
                                    )
                                }
                            }
                            DropdownMenu(modifier = Modifier.background(Color.White),
                                expanded = drinkingExpanded,
                                onDismissRequest = { drinkingExpanded = false }) {
                                DropdownMenuItem(text = { Text("Yes") }, onClick = {
                                    drinkingSelectedOption = "Yes"
                                    drinkingStoredOption = "YES"
                                    drinkingExpanded = false
                                })
                                DropdownMenuItem(text = { Text("On occasion") }, onClick = {
                                    drinkingSelectedOption = "On occasion"
                                    drinkingStoredOption = "ON_OCCASION"
                                    drinkingExpanded = false
                                })
                                DropdownMenuItem(text = { Text("Never drink") }, onClick = {
                                    drinkingSelectedOption = "Never drink"
                                    drinkingStoredOption = "NEVER_DRINK"
                                    drinkingExpanded = false
                                })
                            }
                            LaunchedEffect(drinkingStoredOption) {
                                hasBeenChanged = true
                            }
                            Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                        }
                    }
                    HorizontalDivider(thickness = 1.dp)
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    marijuanaExpanded = true
                                }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(
                                    id = R.drawable.cannabis_marijuana_svgrepo_com
                                ),
                                contentDescription = "Education Icon",
                                tint = Color(0xFFFF6F00)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (marijuanaSelectedOption == "") {
                                marijuanaStoredOption = "UNKNOWN"
                                Text(
                                    text = "Marijuana", fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                Text(
                                    text = "Add", fontSize = 20.sp, color = Color(0xFFFF6F00)
                                )
                            } else {
                                Text(text = marijuanaSelectedOption, fontSize = 20.sp)
                                Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                IconButton(onClick = { marijuanaSelectedOption = "" }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cancel_24px),
                                        contentDescription = "Cancel Button",
                                        modifier = Modifier,
                                        tint = Color(0xFFFF6F00)
                                    )
                                }
                            }
                            DropdownMenu(modifier = Modifier.background(Color.White),
                                expanded = marijuanaExpanded,
                                onDismissRequest = { marijuanaExpanded = false }) {
                                DropdownMenuItem(text = { Text("Yes") }, onClick = {
                                    marijuanaSelectedOption = "Yes"
                                    marijuanaStoredOption = "YES"
                                    marijuanaExpanded = false
                                })
                                DropdownMenuItem(text = { Text("On occasion") }, onClick = {
                                    marijuanaSelectedOption = "On occasion"
                                    marijuanaStoredOption = "ON_OCCASION"
                                    marijuanaExpanded = false
                                })
                                DropdownMenuItem(text = { Text("Never") }, onClick = {
                                    marijuanaSelectedOption = "Never"
                                    marijuanaStoredOption = "NEVER"
                                    marijuanaExpanded = false
                                })
                            }
                            LaunchedEffect(marijuanaStoredOption) {
                                hasBeenChanged = true
                            }
                            Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                        }
                    }
                    HorizontalDivider(thickness = 1.dp)

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        shape = RoundedCornerShape(0.dp),
                        onClick = {
                            if (!isEditingProfile && (exerciseStoredOption != "UNKNOWN" || drinkingStoredOption != "UNKNOWN" ||
                                        smokingStoredOption != "UNKNOWN" || marijuanaStoredOption != "UNKNOWN")
                            ) {
//                                isEditingProfile = true

                                val existingUser = user

                                var updatedExistingUser = existingUser

                                updatedExistingUser =
                                    updatedExistingUser?.copy(
                                        exercise = when (exerciseStoredOption) {
                                            "OFTEN" -> Exercise.OFTEN
                                            "SOMETIMES" -> Exercise.SOMETIMES
                                            "RARELY" -> Exercise.RARELY
                                            "NEVER" -> Exercise.NEVER
                                            else -> Exercise.UNKNOWN
                                        }
                                    )
                                        ?.copy(
                                            smoking = when (smokingStoredOption) {
                                                "YES" -> Smoking.YES
                                                "ON_OCCASION" -> Smoking.ON_OCCASION
                                                "NEVER_SMOKE" -> Smoking.NEVER_SMOKE
                                                else -> Smoking.UNKNOWN
                                            }
                                        )
                                        ?.copy(
                                            drinking = when (drinkingStoredOption) {
                                                "YES" -> Drinking.YES
                                                "ON_OCCASION" -> Drinking.ON_OCCASION
                                                "NEVER_DRINK" -> Drinking.NEVER_DRINK
                                                else -> Drinking.UNKNOWN
                                            }
                                        )
                                        ?.copy(
                                            marijuana = when (marijuanaStoredOption) {
                                                "YES" -> Marijuana.YES
                                                "ON_OCCASION" -> Marijuana.ON_OCCASION
                                                "NEVER" -> Marijuana.NEVER
                                                else -> Marijuana.UNKNOWN
                                            }
                                        )

                                Log.d(
                                    TAG,
                                    "Finally, the user on age screen is - ${updatedExistingUser}"
                                )


                                val userData: MutableMap<String, Any> = mutableMapOf()
                                val mediaItems: MutableMap<String, Uri> = mutableMapOf()

                                if (updatedExistingUser != null) {
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

                                    val userId = user?.id ?: ""
                                    usersViewModel.saveUserDataToFirebase(
                                        userId ?: "", userData,
                                        mediaItems, context
                                    ) {
                                        navController.navigate("users")
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
                        Text("Add your habits", fontSize = 16.sp)
                    }
                }
            }
        }

    }
    if (isEditingProfile) {
        WaitScreen()
    }
}