package com.example.shots.ui.theme

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
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavController
import com.example.shots.FirebaseModule
import com.example.shots.R
import com.example.shots.data.Distance
import com.example.shots.data.ShowMe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupFilterScreen(
    navController: NavController, userViewModel: UserViewModel,
    dataStore: DataStore<Preferences>
) {
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    val firestore = FirebaseModule.provideFirestore()
    val firebaseStorage = FirebaseModule.provideStorage()
    val firebaseRepository =
        FirebaseModule.provideFirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf(userViewModel.getUser()) }
    val context = LocalContext.current

    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navController.navigate("signupEssentials")
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
            ->
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
                        preferences[intPreferencesKey("currentScreen")] = 11
                    }
                }
            }
            IconButton(
                onClick = {
                    navController.navigate("signupEssentials")
                },
                modifier = Modifier.padding(it)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            IconButton(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val userData: MutableMap<String, Any> = mutableMapOf()
                            val mediaItems: MutableMap<String, Uri> = mutableMapOf()
                            val existingUser = user
                            //show me
                            //distance
                            // age
                            userData["showMe"] = ShowMe.WOMEN
                            userData["usersShow"] = Distance.ANYWHERE
                            userData["acceptShots"] = Distance.ANYWHERE
                            userData["ageMinToShow"] = 18
                            userData["ageMaxToShow"] = 35

                            if(existingUser?.id != null) {
                                userViewModel.saveUserDataToFirebase(
                                    existingUser.id,
                                    userData, mediaItems, context
                                ) {

                                }
                            }
                        }
                    }
                    navController.navigate("signupHabits")
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
                    text = "Add Your Filters",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(0.dp, 48.dp, 0.dp, 0.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))

                val showMeValue = when (user?.showMe) {
                    ShowMe.MEN -> "Men"
                    ShowMe.WOMEN -> "Women"
                    ShowMe.ANYONE -> "Anyone"
                    else -> "Women"
                }

                var showMeSelectedOption by remember { mutableStateOf(showMeValue) }

                Text(
                    text = "Show Me",
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 16.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Column {
                        RadioButton(
                            selected = showMeSelectedOption == "Men",
                            onClick = { showMeSelectedOption = "Men" }
                        )
                    }
                    Column {
                        Text("Men")
                    }

                    Column {
                        RadioButton(
                            selected = showMeSelectedOption == "Women",
                            onClick = { showMeSelectedOption = "Women" }
                        )
                    }
                    Column {
                        Text("Women")
                    }

                    Column {
                        RadioButton(
                            selected = showMeSelectedOption == "Anyone",
                            onClick = { showMeSelectedOption = "Anyone" }
                        )
                    }
                    Column {
                        Text("Anyone")
                    }
                }

                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Show Users",
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 16.sp
                )

                var showUsersExpanded by remember { mutableStateOf(false) }
                var showUsersStoredOption by rememberSaveable {
                    mutableStateOf(
                        when (user?.showUsers) {
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
                            else -> "UNKNOWN"
                        }
                    )
                }

                var showUsersSelectedOption by rememberSaveable {
                    mutableStateOf(
                        when (showUsersStoredOption) {
                            "TEN" -> "Within 10 miles"
                            "TWENTY" -> "Within 20 miles"
                            "THIRTY" -> "Within 30 miles"
                            "FORTY" -> "Within 40 miles"
                            "FIFTY" -> "Within 50 miles"
                            "SIXTY" -> "Within 60 miles"
                            "SEVENTY" -> "Within 70 miles"
                            "EIGHTY" -> "Within 80 miles"
                            "NINETY" -> "Within 90 miles"
                            "ONE_HUNDRED" -> "Within 100 miles"
                            "ANYWHERE" -> "Anywhere"
                            else -> "Within 10 miles"
                        }
                    )
                }
                Box() {
                    Column {
                        Box(
                            modifier = Modifier
                                .height(60.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        showUsersExpanded = true
                                    }, verticalAlignment = Alignment.CenterVertically
                            ) {
//                            Icon(
//                                painterResource(
//                                    id = R.drawable.male_and_female_symbol_svgrepo_com
//                                ),
//                                contentDescription = "Education Icon",
//                                tint = Color(0xFFFF6F00)
//                            )

                                Spacer(modifier = Modifier.width(8.dp))

                                if (showUsersSelectedOption == "") {
                                    showUsersStoredOption = "UNKNOWN"
                                    Text(
                                        text = "Add your distance", fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        text = "Add",
                                        fontSize = 20.sp,
                                        color = Color(0xFFFF6F00)
                                    )
                                } else {
                                    Text(
                                        text = showUsersSelectedOption, fontSize = 20.sp
                                    )

                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space

                                    IconButton(onClick = { showUsersSelectedOption = "" }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.cancel_24px),
                                            contentDescription = "Cancel Button",
                                            modifier = Modifier,
                                            tint = Color(0xFFFF6F00)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed

                                DropdownMenu(modifier = Modifier.background(Color.White),
                                    expanded = showUsersExpanded,
                                    onDismissRequest = { showUsersExpanded = false }) {
                                    DropdownMenuItem(text = { Text("Within 10 Miles") }, onClick = {
                                        showUsersSelectedOption = "Within 10 Miles"
                                        showUsersStoredOption = "TEN"
                                        showUsersExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 20 miles") }, onClick = {
                                        showUsersSelectedOption = "Within 20 Miles"
                                        showUsersStoredOption = "TWENTY"
                                        showUsersExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 30 Miles") }, onClick = {
                                        showUsersSelectedOption = "Within 30 Miles"
                                        showUsersStoredOption = "THIRTY"
                                        showUsersExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 40 Miles") }, onClick = {
                                        showUsersSelectedOption = "Within 40 Miles"
                                        showUsersStoredOption = "FORTY"
                                        showUsersExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 50 miles") }, onClick = {
                                        showUsersSelectedOption = "Within 50 Miles"
                                        showUsersStoredOption = "FIFTY"
                                        showUsersExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 60 Miles") }, onClick = {
                                        showUsersSelectedOption = "Within 60 Miles"
                                        showUsersStoredOption = "SIXTY"
                                        showUsersExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 70 Miles") }, onClick = {
                                        showUsersSelectedOption = "Within 70 Miles"
                                        showUsersStoredOption = "SEVENTY"
                                        showUsersExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 80 miles") }, onClick = {
                                        showUsersSelectedOption = "Within 80 Miles"
                                        showUsersStoredOption = "EIGHTY"
                                        showUsersExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 90 Miles") }, onClick = {
                                        showUsersSelectedOption = "Within 90 Miles"
                                        showUsersStoredOption = "NINETY"
                                        showUsersExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 100 Miles") }, onClick = {
                                        showUsersSelectedOption = "Within 100 Miles"
                                        showUsersStoredOption = "ONE_HUNDRED"
                                        showUsersExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Anywhere") }, onClick = {
                                        showUsersSelectedOption = "Anywhere"
                                        showUsersStoredOption = "ANYWHERE"
                                        showUsersExpanded = false
                                    })
                                }

                                LaunchedEffect(showUsersStoredOption) {
//                                    userViewModel.updateUserField { currentUser ->
//                                        currentUser.copy(
//                                            showUsers = when (showUsersStoredOption) {
//                                                "TEN" -> Distance.TEN
//                                                "TWENTY" -> Distance.TWENTY
//                                                "THIRTY" -> Distance.THIRTY
//                                                "FORTY" -> Distance.FORTY
//                                                "FIFTY" -> Distance.FIFTY
//                                                "SIXTY" -> Distance.SIXTY
//                                                "SEVENTY" -> Distance.SEVENTY
//                                                "EIGHTY" -> Distance.EIGHTY
//                                                "NINETY" -> Distance.NINETY
//                                                "ONE_HUNDRED" -> Distance.ONE_HUNDRED
//                                                "ANYWHERE" -> Distance.ANYWHERE
//                                                else -> Distance.TEN
//                                            }
//                                        )
//                                    }
//
//                                    user = user.copy(
//                                        showUsers = when (showUsersStoredOption) {
//                                            "TEN" -> Distance.TEN
//                                            "TWENTY" -> Distance.TWENTY
//                                            "THIRTY" -> Distance.THIRTY
//                                            "FORTY" -> Distance.FORTY
//                                            "FIFTY" -> Distance.FIFTY
//                                            "SIXTY" -> Distance.SIXTY
//                                            "SEVENTY" -> Distance.SEVENTY
//                                            "EIGHTY" -> Distance.EIGHTY
//                                            "NINETY" -> Distance.NINETY
//                                            "ONE_HUNDRED" -> Distance.ONE_HUNDRED
//                                            "ANYWHERE" -> Distance.ANYWHERE
//                                            else -> Distance.TEN
//                                        }
//                                    )
                                }

                            }
                        }
                    }
                }


                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))


                Text(
                    text = "Accept Shots",
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 16.sp
                )

                var acceptShotsExpanded by remember { mutableStateOf(false) }
                var acceptShotsStoredOption by rememberSaveable {
                    mutableStateOf(
                        when (user?.showUsers) {
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
                            else -> "UNKNOWN"
                        }
                    )
                }

                var acceptShotsSelectedOption by rememberSaveable {
                    mutableStateOf(
                        when (acceptShotsStoredOption) {
                            "TEN" -> "Within 10 miles"
                            "TWENTY" -> "Within 20 miles"
                            "THIRTY" -> "Within 30 miles"
                            "FORTY" -> "Within 40 miles"
                            "FIFTY" -> "Within 50 miles"
                            "SIXTY" -> "Within 60 miles"
                            "SEVENTY" -> "Within 70 miles"
                            "EIGHTY" -> "Within 80 miles"
                            "NINETY" -> "Within 90 miles"
                            "ONE_HUNDRED" -> "Within 100 miles"
                            "ANYWHERE" -> "Anywhere"
                            else -> "Within 10 miles"
                        }
                    )
                }
                Box() {
                    Column {
                        Box(
                            modifier = Modifier
                                .height(60.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        acceptShotsExpanded = true
                                    }, verticalAlignment = Alignment.CenterVertically
                            ) {
//                            Icon(
//                                painterResource(
//                                    id = R.drawable.male_and_female_symbol_svgrepo_com
//                                ),
//                                contentDescription = "Education Icon",
//                                tint = Color(0xFFFF6F00)
//                            )

                                Spacer(modifier = Modifier.width(8.dp))

                                if (acceptShotsSelectedOption == "") {
                                    acceptShotsStoredOption = "UNKNOWN"
                                    Text(
                                        text = "Add your distance", fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        text = "Add",
                                        fontSize = 20.sp,
                                        color = Color(0xFFFF6F00)
                                    )
                                } else {
                                    Text(
                                        text = acceptShotsSelectedOption, fontSize = 20.sp
                                    )

                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space

                                    IconButton(onClick = { acceptShotsSelectedOption = "" }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.cancel_24px),
                                            contentDescription = "Cancel Button",
                                            modifier = Modifier,
                                            tint = Color(0xFFFF6F00)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed

                                DropdownMenu(modifier = Modifier.background(Color.White),
                                    expanded = acceptShotsExpanded,
                                    onDismissRequest = { acceptShotsExpanded = false }) {
                                    DropdownMenuItem(text = { Text("Within 10 Miles") }, onClick = {
                                        acceptShotsSelectedOption = "Within 10 Miles"
                                        acceptShotsStoredOption = "TEN"
                                        showUsersExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 20 miles") }, onClick = {
                                        acceptShotsSelectedOption = "Within 20 Miles"
                                        acceptShotsStoredOption = "TWENTY"
                                        acceptShotsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 30 Miles") }, onClick = {
                                        acceptShotsSelectedOption = "Within 30 Miles"
                                        acceptShotsStoredOption = "THIRTY"
                                        acceptShotsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 40 Miles") }, onClick = {
                                        acceptShotsSelectedOption = "Within 40 Miles"
                                        acceptShotsStoredOption = "FORTY"
                                        acceptShotsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 50 miles") }, onClick = {
                                        acceptShotsSelectedOption = "Within 50 Miles"
                                        acceptShotsStoredOption = "FIFTY"
                                        acceptShotsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 60 Miles") }, onClick = {
                                        acceptShotsSelectedOption = "Within 60 Miles"
                                        acceptShotsStoredOption = "SIXTY"
                                        acceptShotsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 70 Miles") }, onClick = {
                                        acceptShotsSelectedOption = "Within 70 Miles"
                                        acceptShotsStoredOption = "SEVENTY"
                                        acceptShotsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 80 miles") }, onClick = {
                                        acceptShotsSelectedOption = "Within 80 Miles"
                                        acceptShotsStoredOption = "EIGHTY"
                                        acceptShotsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 90 Miles") }, onClick = {
                                        acceptShotsSelectedOption = "Within 90 Miles"
                                        acceptShotsStoredOption = "NINETY"
                                        acceptShotsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Within 100 Miles") }, onClick = {
                                        acceptShotsSelectedOption = "Within 100 Miles"
                                        acceptShotsStoredOption = "ONE_HUNDRED"
                                        acceptShotsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Anywhere") }, onClick = {
                                        acceptShotsSelectedOption = "Anywhere"
                                        acceptShotsStoredOption = "ANYWHERE"
                                        acceptShotsExpanded = false
                                    })
                                }

                                LaunchedEffect(acceptShotsStoredOption) {
//                                    userViewModel.updateUserField { currentUser ->
//                                        currentUser.copy(
//                                            acceptShots = when (acceptShotsStoredOption) {
//                                                "TEN" -> Distance.TEN
//                                                "TWENTY" -> Distance.TWENTY
//                                                "THIRTY" -> Distance.THIRTY
//                                                "FORTY" -> Distance.FORTY
//                                                "FIFTY" -> Distance.FIFTY
//                                                "SIXTY" -> Distance.SIXTY
//                                                "SEVENTY" -> Distance.SEVENTY
//                                                "EIGHTY" -> Distance.EIGHTY
//                                                "NINETY" -> Distance.NINETY
//                                                "ONE_HUNDRED" -> Distance.ONE_HUNDRED
//                                                "ANYWHERE" -> Distance.ANYWHERE
//                                                else -> Distance.TEN
//                                            }
//                                        )
//                                    }
//
//                                    user = user.copy(
//                                        acceptShots = when (acceptShotsStoredOption) {
//                                            "TEN" -> Distance.TEN
//                                            "TWENTY" -> Distance.TWENTY
//                                            "THIRTY" -> Distance.THIRTY
//                                            "FORTY" -> Distance.FORTY
//                                            "FIFTY" -> Distance.FIFTY
//                                            "SIXTY" -> Distance.SIXTY
//                                            "SEVENTY" -> Distance.SEVENTY
//                                            "EIGHTY" -> Distance.EIGHTY
//                                            "NINETY" -> Distance.NINETY
//                                            "ONE_HUNDRED" -> Distance.ONE_HUNDRED
//                                            "ANYWHERE" -> Distance.ANYWHERE
//                                            else -> Distance.TEN
//                                        }
//                                    )
                                }

                            }
                        }
                    }
                }


//
//                val distanceStartValue = when (user?.distance) {
//                    Distance.TEN -> 10f
//                    Distance.TWENTY -> 20f
//                    Distance.THIRTY -> 30f
//                    Distance.FORTY -> 40f
//                    Distance.FIFTY -> 50f
//                    Distance.SIXTY -> 60f
//                    Distance.SEVENTY -> 70f
//                    Distance.EIGHTY -> 80f
//                    Distance.NINETY -> 90f
//                    Distance.ONE_HUNDRED -> 100f
//                    Distance.ANYWHERE -> 15000f
//                    null -> 10f
//                }
//
//                var distanceSliderPosition by remember { mutableFloatStateOf(distanceStartValue) }
//                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
//                    Slider(
//                        value = distanceSliderPosition,
//                        onValueChange = { distanceSliderPosition = it },
//                        colors = SliderDefaults.colors(
//                            thumbColor = MaterialTheme.colorScheme.primary,
//                            activeTrackColor = MaterialTheme.colorScheme.primary,
//                            inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
//                        ),
//                        steps = 7,
//                        valueRange = 20f..100f
//                    )
//                    Text(text = "Within ${distanceSliderPosition.toInt()} miles")
//                }

                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Age",
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 16.sp
                )
                var ageSliderPosition by remember { mutableStateOf(18f..35f) }
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    RangeSlider(
                        value = ageSliderPosition,
                        onValueChange = { range -> ageSliderPosition = range },
                        valueRange = 18f..100f,
                        onValueChangeFinished = {
                            // launch some business logic update with the state you hold
                            // viewModel.updateSelectedSliderValue(sliderPosition)
                        },
                    )
                    Text(text = "${ageSliderPosition.start.toInt()} - ${ageSliderPosition.endInclusive.toInt()}")
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                val existingUser = user
                                //show me
                                //showUsers
                                //acceptShots
                                // age
                                val updatedExistingUser =
                                    existingUser?.copy(
                                        showMe = when (showMeSelectedOption) {
                                            "MEN" -> ShowMe.MEN
                                            "WOMEN" -> ShowMe.WOMEN
                                            "ANYONE" -> ShowMe.ANYONE
                                            else -> ShowMe.WOMEN
                                        }
                                    )?.copy(
                                        showUsers = when (showUsersSelectedOption) {
                                            "Within 10 miles" -> Distance.TEN
                                            "Within 20 miles" -> Distance.TWENTY
                                            "Within 30 miles" -> Distance.THIRTY
                                            "Within 40 miles" -> Distance.FORTY
                                            "Within 50 miles" -> Distance.FIFTY
                                            "Within 60 miles" -> Distance.SIXTY
                                            "Within 70 miles" -> Distance.SEVENTY
                                            "Within 80 miles" -> Distance.EIGHTY
                                            "Within 90 miles" -> Distance.NINETY
                                            "Within 100 miles" -> Distance.ONE_HUNDRED
                                            "Anywhere" -> Distance.ANYWHERE
                                            else -> Distance.ANYWHERE
                                        }
                                    )?.copy(acceptShots = when(acceptShotsSelectedOption) {
                                        "Within 10 miles" -> Distance.TEN
                                        "Within 20 miles" -> Distance.TWENTY
                                        "Within 30 miles" -> Distance.THIRTY
                                        "Within 40 miles" -> Distance.FORTY
                                        "Within 50 miles" -> Distance.FIFTY
                                        "Within 60 miles" -> Distance.SIXTY
                                        "Within 70 miles" -> Distance.SEVENTY
                                        "Within 80 miles" -> Distance.EIGHTY
                                        "Within 90 miles" -> Distance.NINETY
                                        "Within 100 miles" -> Distance.ONE_HUNDRED
                                        "Anywhere" -> Distance.ANYWHERE
                                        else -> Distance.ANYWHERE
                                    })?.copy(ageMinToShow = ageSliderPosition.start.toInt())
                                        ?.copy(ageMaxToShow = ageSliderPosition.endInclusive.toInt())

                                val userData: MutableMap<String, Any> = mutableMapOf()
                                val mediaItems: MutableMap<String, Uri> = mutableMapOf()

                                if (updatedExistingUser != null) {
                                    userData["showMe"] = updatedExistingUser.showMe?.name ?: ""
                                    userData["showUsers"] = updatedExistingUser.showUsers.name
                                    userData["acceptShots"] = updatedExistingUser.acceptShots.name
                                    userData["ageMinToShow"] = updatedExistingUser.ageMinToShow ?: 0
                                    userData["ageMaxToShow"] = updatedExistingUser.ageMaxToShow ?: 0
                                    val userId = firebaseAuth.currentUser?.displayName ?: ""
                                    userViewModel.saveUserDataToFirebase(
                                        userId ?: "", userData,
                                        mediaItems, context
                                    ) {

                                    }
                                }

                            }
                        }
                        navController.navigate("signupHabits")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        //padding is 376
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                ) {
                    Text("Add your filters", fontSize = 16.sp)
                }
            }


        }
    }
}