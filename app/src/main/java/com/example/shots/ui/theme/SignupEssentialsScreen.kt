package com.example.shots.ui.theme

import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavController
import com.example.shots.FirebaseModule
import com.example.shots.R
import com.example.shots.data.Education
import com.example.shots.data.Kids
import com.example.shots.data.Pets
import com.example.shots.data.Religion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupEssentialsScreen(
    navController: NavController,
    signupViewModel: SignupViewModel,
    usersViewModel: UsersViewModel,
    dataStore: DataStore<Preferences>
) {
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    val firestore = FirebaseModule.provideFirestore()
    val firebaseStorage = FirebaseModule.provideStorage()
    val firebaseRepository =
        FirebaseModule.provideFirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isFocused by remember { mutableStateOf(false) }
//    var hasBeenChanged by rememberSaveable { mutableStateOf(false) }
    var hasBeenChanged = false
    var isGoingBack by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val user by remember { mutableStateOf(usersViewModel.getUser()) }

    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navController.navigate("signupDetails")
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
        snackbarHost = { ->
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
                        preferences[intPreferencesKey("currentScreen")] = 10
                    }
                }
            }
            IconButton(
                onClick = {
                    navController.navigate("signupDetails")
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

                            val updatedExistingUser = user

                            if (updatedExistingUser != null) {
                                userData["work"] = ""
                                userData["religion"] = "UNKNOWN"
                                userData["education"] = "UNKNOWN"
                                userData["pets"] = "UNKNOWN"

                                val userId = user?.id ?: ""
                                usersViewModel.saveUserDataToFirebase(
                                    userId, userData,
                                    mediaItems, context
                                ) {
                                    navController.navigate("signupFilter")
                                }
                            }
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
                    text = "Add Your Essentials", fontSize = 24.sp,
                    modifier = Modifier
                        .padding(0.dp, 48.dp, 0.dp, 0.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))


                val focusRequester = remember { FocusRequester() }
                val keyboardController = LocalSoftwareKeyboardController.current
                val workAdded = remember { mutableStateOf(false) }
                var workState by remember {
                    mutableStateOf(
                        user?.work ?: ""
                    )
                }

                var educationExpanded by remember { mutableStateOf(false) }
                var educationStoredOption by rememberSaveable {
                    mutableStateOf(
                        when (user?.education) {
                            Education.SOME_HIGH_SCHOOL -> "SOME_HIGH_SCHOOL"
                            Education.HIGH_SCHOOL -> "HIGH_SCHOOL"
                            Education.SOME_COLLEGE -> "SOME_COLLEGE"
                            Education.UNDERGRAD_DEGREE -> "UNDERGRAD_DEGREE"
                            Education.SOME_GRAD_SCHOOL -> "SOME_GRAD_SCHOOL"
                            Education.GRAD_DEGREE -> "GRAD_DEGREE"
                            Education.TECH_TRADE_SCHOOL -> "TECH_TRADE_SCHOOL"
                            else -> ""
                        }
                    )
                }
                var educationSelectedOption by rememberSaveable {
                    mutableStateOf(
                        when (educationStoredOption) {
                            "SOME_HIGH_SCHOOL" -> "Some High School"
                            "HIGH_SCHOOL" -> "High School"
                            "SOME_COLLEGE" -> "Some College"
                            "UNDERGRAD_DEGREE" -> "Undergrad Degree"
                            "SOME_GRAD_SCHOOL" -> "Some Grad School"
                            "GRAD_DEGREE" -> "Grad Degree"
                            "TECH_TRADE_SCHOOL" -> "Tech/Trade School"
                            else -> ""
                        }
                    )
                }

                var kidsExpanded by remember { mutableStateOf(false) }
                var kidsStoredOption by rememberSaveable {
                    mutableStateOf(
                        when (user?.kids) {
                            Kids.ONE_DAY -> "ONE_DAY"
                            Kids.DONT_WANT -> "DONT_WANT"
                            Kids.HAVE_AND_WANT_MORE -> "HAVE_AND_WANT_MORE"
                            Kids.HAVE_AND_DONT_WANT_MORE -> "HAVE_AND_DONT_WANT_MORE"
                            Kids.UNSURE -> "UNSURE"
                            else -> ""
                        }
                    )
                }
                var kidsSelectedOption by rememberSaveable {
                    mutableStateOf(
                        when (kidsStoredOption) {
                            "ONE_DAY" -> "One day"
                            "DONT_WANT" -> "Don't want"
                            "HAVE_AND_WANT_MORE" -> "Have and want more"
                            "HAVE_AND_DONT_WANT_MORE" -> "Have and don't want more"
                            "UNSURE" -> "Unsure"
                            else -> {
                                ""
                            }
                        }
                    )
                }

                var religionExpanded by remember { mutableStateOf(false) }
                var religionStoredOption by remember {
                    mutableStateOf(
                        when (user?.religion) {
                            Religion.CHRISTIANITY -> "CHRISTIANITY"
                            Religion.ISLAM -> "ISLAM"
                            Religion.HINDUISM -> "HINDUISM"
                            Religion.BUDDHISM -> "BUDDHISM"
                            Religion.SIKHISM -> "SIKHISM"
                            Religion.BAHAI_FAITH -> "BAHAI_FAITH"
                            Religion.CONFUCIANISM -> "CONFUCIANISM"
                            Religion.JAINISM -> "JAINISM"
                            Religion.SHINTOISM -> "SHINTOISM"
                            else -> ""
                        }
                    )
                }
                var religionSelectedOption by rememberSaveable {
                    mutableStateOf(
                        when (religionStoredOption) {
                            "CHRISTIANITY" -> "Christianity"
                            "ISLAM" -> "Islam"
                            "HINDUISM" -> "Hinduism"
                            "BUDDHISM" -> "Buddhism"
                            "SIKHISM" -> "Sikhism"
                            "BAHAI_FAITH" -> "Baháʼí Faith"
                            "CONFUCIANISM" -> "Confucianism"
                            "JAINISM" -> "Jainism"
                            "SHINTOISM" -> "Shintoism"
                            else -> ""
                        }
                    )
                }

                var petsExpanded by remember { mutableStateOf(false) }
                var petsStoredOption by
                rememberSaveable {
                    mutableStateOf(
                        when (user?.pets) {
                            Pets.DOG -> "DOG"
                            Pets.CAT -> "CAT"
                            Pets.FISH -> "FISH"
                            Pets.HAMSTER_OR_GUINEA_PIG -> "HAMSTER_OR_GUINEA_PIG"
                            Pets.BIRD -> "BIRD"
                            Pets.RABBIT -> "RABBIT"
                            Pets.REPTILE -> "REPTILE"
                            Pets.AMPHIBIAN -> "AMPHIBIAN"
                            else -> ""
                        }
                    )
                }
                var petsSelectedOption by rememberSaveable {
                    mutableStateOf(
                        when (petsStoredOption) {
                            "DOG" -> "Dog"
                            "CAT" -> "Cat"
                            "FISH" -> "Fish"
                            "HAMSTER_OR_GUINEA_PIG" -> "Hamster/Guinea Pig"
                            "BIRD" -> "Bird"
                            "RABBIT" -> "Rabbit"
                            "REPTILE" -> "Reptile"
                            "AMPHIBIAN" -> "Amphibian"
                            else -> ""
                        }
                    )
                }
                Box() {
                    Column {
                        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                        Box(
                            modifier = Modifier
                                .height(60.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        workAdded.value = true
                                    }, verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(
                                        id = R.drawable.work_fill0_wght400_grad0_opsz24
                                    ),
                                    contentDescription = "Work Icon",
                                    tint = Color(0xFFFF6F00)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (workState == "") {
                                    Text(
                                        text = "Work", fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        text = "Add",
                                        fontSize = 20.sp,
                                        color = Color(0xFFFF6F00)
                                    )
                                } else {
                                    Text(
                                        text = workState, fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    IconButton(onClick = { workState = "" }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.cancel_24px),
                                            contentDescription = "Cancel Button",
                                            modifier = Modifier,
                                            tint = Color(0xFFFF6F00)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                if (workAdded.value) {
                                    ModalBottomSheet(onDismissRequest = {
                                        workAdded.value = false
                                    }) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally// Set the width to match the parent
                                        ) {
                                            Text(
                                                text = "What do you do for work?",
                                                fontSize = 20.sp,
                                                textAlign = TextAlign.Center,
                                                color = Color.Black
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                            ) {
                                                Column() {
                                                    Row() {
                                                        TextField(value = workState,
                                                            onValueChange = { newValue: String ->
                                                                if (newValue.length <= 35) {
                                                                    workState = newValue
                                                                }
                                                            },
//                                                                label = { Text(text = "Work") },
                                                            singleLine = false,
                                                            keyboardOptions = KeyboardOptions(
                                                                imeAction = ImeAction.Done
                                                            ),
                                                            keyboardActions = KeyboardActions(
                                                                onDone = {
                                                                    keyboardController?.hide()
                                                                    focusRequester.freeFocus()
                                                                }),
                                                            colors = TextFieldDefaults.textFieldColors(
                                                                containerColor = Color(
                                                                    0xFFFFD7B5
                                                                )
                                                            ),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .onFocusChanged {
                                                                    isFocused = it.isFocused
                                                                }
                                                                .focusRequester(
                                                                    focusRequester
                                                                ))
                                                        if (!isFocused) {
                                                            DisposableEffect(Unit) {
                                                                focusRequester.freeFocus()
                                                                onDispose {
                                                                    keyboardController?.hide()
                                                                }
                                                            }
                                                        }
                                                    }

                                                }
                                            }
                                            Spacer(Modifier.height(48.dp))
                                            Button(onClick = {
                                                workAdded.value = false
                                            }) {
                                                Text(text = "Add", color = Color.White)
                                            }
                                            Spacer(Modifier.height(120.dp))
                                        }
                                    }
                                }
                                LaunchedEffect(workState) {
                                    hasBeenChanged = true
                                }
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
                                        educationExpanded = true
                                    }, verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(
                                        id = R.drawable.education_svgrepo_com
                                    ),
                                    contentDescription = "Education Icon",
                                    tint = Color(0xFFFF6F00)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (educationSelectedOption == "") {
                                    Text(
                                        text = "Education", fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        text = "Add",
                                        fontSize = 20.sp,
                                        color = Color(0xFFFF6F00)
                                    )
                                } else {
                                    Text(
                                        text = educationSelectedOption, fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    IconButton(onClick = { educationSelectedOption = "" }) {
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
                                    expanded = educationExpanded,
                                    onDismissRequest = { educationExpanded = false }) {
                                    DropdownMenuItem(text = { Text("Some High School") },
                                        onClick = {
                                            educationSelectedOption = "Some High School"
                                            educationStoredOption = "SOME_HIGH_SCHOOL"
                                            educationExpanded = false
                                        })
                                    DropdownMenuItem(text = { Text("High School") }, onClick = {
                                        educationSelectedOption = "High School"
                                        educationStoredOption = "HIGH_SCHOOL"
                                        educationExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Some College") },
                                        onClick = {
                                            educationSelectedOption = "Some College"
                                            educationStoredOption = "SOME_COLLEGE"
                                            educationExpanded = false
                                        })
                                    DropdownMenuItem(text = { Text("Undergrad Degree") },
                                        onClick = {
                                            educationSelectedOption = "Undergrad Degree"
                                            educationStoredOption = "UNDERGRAD_DEGREE"
                                            educationExpanded = false
                                        })
                                    DropdownMenuItem(text = { Text("Some Grad School") },
                                        onClick = {
                                            educationSelectedOption = "Some Grad School"
                                            educationStoredOption = "SOME_GRAD_SCHOOL"
                                            educationExpanded = false
                                        })
                                    DropdownMenuItem(text = { Text("Grad Degree") }, onClick = {
                                        educationSelectedOption = "Grad Degree"
                                        educationStoredOption = "GRAD_DEGREE"
                                        educationExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Technical/Trade School") },
                                        onClick = {
                                            educationSelectedOption = "Technical/Trade School"
                                            educationStoredOption = "TECH_TRADE_SCHOOL"
                                            educationExpanded = false
                                        })
                                }
                                LaunchedEffect(educationStoredOption) {
                                    hasBeenChanged = true
                                }
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
                                    .clickable { kidsExpanded = true },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(
                                        id = R.drawable.children_svgrepo_com
                                    ),
                                    contentDescription = "Education Icon",
                                    tint = Color(0xFFFF6F00)
                                )
                                Spacer(modifier = Modifier.width(8.dp)) // Adjust the space as needed
                                if (kidsSelectedOption == "") {
                                    Text(
                                        text = "Kids", fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        text = "Add",
                                        fontSize = 20.sp,
                                        color = Color(0xFFFF6F00)
                                    )
                                } else {
                                    Text(
                                        text = kidsSelectedOption, fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    IconButton(onClick = { kidsSelectedOption = "" }) {
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
                                    expanded = kidsExpanded,
                                    onDismissRequest = { kidsExpanded = false }) {
                                    DropdownMenuItem(text = { Text("One day") }, onClick = {
                                        kidsSelectedOption = "One day"
                                        kidsStoredOption = "ONE_DAY"
                                        kidsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Don't want") }, onClick = {
                                        kidsSelectedOption = "Don't want"
                                        kidsStoredOption = "DONT_WANT"
                                        kidsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Have and want more") },
                                        onClick = {
                                            kidsSelectedOption = "Have and want more"
                                            kidsStoredOption = "HAVE_AND_WANT_MORE"
                                            kidsExpanded = false
                                        })
                                    DropdownMenuItem(text = { Text("Have and don't want more") },
                                        onClick = {
                                            kidsSelectedOption = "Have and don't want more"
                                            kidsStoredOption = "HAVE_AND_DONT_WANT_MORE"
                                            kidsExpanded = false
                                        })
                                    DropdownMenuItem(text = { Text("Unsure") }, onClick = {
                                        kidsSelectedOption = "Unsure"
                                        kidsStoredOption = "UNSURE"
                                        kidsExpanded = false
                                    })
                                }
                                LaunchedEffect(kidsSelectedOption) {
                                    hasBeenChanged = true
                                }
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
                                    .clickable { religionExpanded = true },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(
                                        id = R.drawable.praying_svgrepo_com
                                    ),
                                    contentDescription = "Education Icon",
                                    tint = Color(0xFFFF6F00)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (religionSelectedOption == "") {
                                    Text(
                                        text = "Religion", fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        text = "Add",
                                        fontSize = 20.sp,
                                        color = Color(0xFFFF6F00)
                                    )
                                } else {
                                    Text(
                                        text = religionSelectedOption, fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    IconButton(onClick = { religionSelectedOption = "" }) {
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
                                    expanded = religionExpanded,
                                    onDismissRequest = { religionExpanded = false }) {
                                    DropdownMenuItem(text = { Text("Christianity") },
                                        onClick = {
                                            religionSelectedOption = "Christianity"
                                            religionStoredOption = "CHRISTIANITY"
                                            religionExpanded = false
                                        })
                                    DropdownMenuItem(text = { Text("Islam") }, onClick = {
                                        religionSelectedOption = "Islam"
                                        religionStoredOption = "ISLAM"
                                        religionExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Hinduism") }, onClick = {
                                        religionSelectedOption = "Hinduism"
                                        religionStoredOption = "HINDUISM"
                                        religionExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Buddhism") }, onClick = {
                                        religionSelectedOption = "Buddhism"
                                        religionStoredOption = "BUDDHISM"
                                        religionExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Sikhism") }, onClick = {
                                        religionSelectedOption = "Sikhism"
                                        religionStoredOption = "SIKHISM"
                                        religionExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Judaism") }, onClick = {
                                        religionSelectedOption = "Judaism"
                                        religionStoredOption = "JUDAISM"
                                        religionExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Baháʼí Faith") },
                                        onClick = {
                                            religionSelectedOption = "Baháʼí Faith"
                                            religionStoredOption = "BAHAI_FAITH"
                                            religionExpanded = false
                                        })
                                    DropdownMenuItem(text = { Text("Confucianism") },
                                        onClick = {
                                            religionSelectedOption = "Confucianism"
                                            religionStoredOption = "CONFUCIANISM"
                                            religionExpanded = false
                                        })
                                    DropdownMenuItem(text = { Text("Jainism") }, onClick = {
                                        religionSelectedOption = "Jainism"
                                        religionStoredOption = "JAINISM"
                                        religionExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Shintoism") }, onClick = {
                                        religionSelectedOption = "Shintoism"
                                        religionStoredOption = "SHINTOISM"
                                        religionExpanded = false
                                    })
                                }
                                LaunchedEffect(religionSelectedOption) {
                                    hasBeenChanged = true
                                }
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
                                        petsExpanded = true
                                    }, verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(
                                        id = R.drawable.pets_svgrepo_com
                                    ),
                                    contentDescription = "Education Icon",
                                    tint = Color(0xFFFF6F00)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (petsSelectedOption == "") {
                                    Text(
                                        text = "Pets", fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        text = "Add",
                                        fontSize = 20.sp,
                                        color = Color(0xFFFF6F00)
                                    )
                                } else {
                                    Text(text = petsSelectedOption, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    IconButton(onClick = { petsSelectedOption = "" }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.cancel_24px),
                                            contentDescription = "Cancel Button",
                                            modifier = Modifier,
                                            tint = Color(0xFFFF6F00)
                                        )
                                    }
                                }
                                DropdownMenu(modifier = Modifier.background(Color.White),
                                    expanded = petsExpanded,
                                    onDismissRequest = { petsExpanded = false }) {
                                    DropdownMenuItem(text = { Text("Dog") }, onClick = {
                                        petsSelectedOption = "Dog"
                                        petsStoredOption = "DOG"
                                        petsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Cat") }, onClick = {
                                        petsSelectedOption = "Cat"
                                        petsStoredOption = "CAT"
                                        petsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Fish") }, onClick = {
                                        petsSelectedOption = "Fish"
                                        petsStoredOption = "FISH"
                                        petsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Hamster/Guinea Pig") },
                                        onClick = {
                                            petsSelectedOption = "Hamster/Guinea Pig"
                                            petsStoredOption = "HAMSTER_OR_GUINEA_PIG"
                                            petsExpanded = false
                                        })
                                    DropdownMenuItem(text = { Text("Bird") }, onClick = {
                                        petsSelectedOption = "Bird"
                                        petsStoredOption = "BIRD"
                                        petsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Rabbit") }, onClick = {
                                        petsSelectedOption = "Rabbit"
                                        petsStoredOption = "RABBIT"
                                        petsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Reptile") }, onClick = {
                                        petsSelectedOption = "Reptile"
                                        petsStoredOption = "REPTILE"
                                        petsExpanded = false
                                    })
                                    DropdownMenuItem(text = { Text("Amphibian") }, onClick = {
                                        petsSelectedOption = "Amphibian"
                                        petsStoredOption = "AMPHIBIAN"
                                        petsExpanded = false
                                    })
                                }
                                LaunchedEffect(petsStoredOption) {
                                    hasBeenChanged = true
                                }
                                Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                            }
                        }
                        HorizontalDivider(thickness = 1.dp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        if (workState.isNotBlank() || educationStoredOption != "UNKNOWN" ||
                            kidsStoredOption != "UNKNOWN" || religionStoredOption != "UNKNOWN" ||
                            petsStoredOption != "UNKNOWN"
                        ) {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val existingUser = user
                                    val updatedExistingUser =
                                        existingUser?.copy(work = workState)
                                            ?.copy(
                                                education = when (educationStoredOption) {
                                                    "SOME_HIGH_SCHOOL" -> Education.SOME_HIGH_SCHOOL
                                                    "HIGH_SCHOOL" -> Education.HIGH_SCHOOL
                                                    "SOME_COLLEGE" -> Education.SOME_COLLEGE
                                                    "UNDERGRAD_DEGREE" -> Education.UNDERGRAD_DEGREE
                                                    "SOME_GRAD_SCHOOL" -> Education.SOME_GRAD_SCHOOL
                                                    "GRAD_DEGREE" -> Education.GRAD_DEGREE
                                                    "TECH_TRADE_SCHOOL" -> Education.TECH_TRADE_SCHOOL
                                                    else -> Education.UNKNOWN
                                                }
                                            )
                                            ?.copy(
                                                kids = when (kidsStoredOption) {
                                                    "ONE_DAY" -> Kids.ONE_DAY
                                                    "DONT_WANT" -> Kids.DONT_WANT
                                                    "HAVE_AND_WANT_MORE" -> Kids.HAVE_AND_WANT_MORE
                                                    "HAVE_AND_DONT_WANT_MORE" -> Kids.HAVE_AND_DONT_WANT_MORE
                                                    "UNSURE" -> Kids.UNSURE
                                                    else -> Kids.UNKNOWN
                                                }
                                            )
                                            ?.copy(
                                                religion = when (religionStoredOption) {
                                                    "CHRISTIANITY" -> Religion.CHRISTIANITY
                                                    "ISLAM" -> Religion.ISLAM
                                                    "HINDUISM" -> Religion.HINDUISM
                                                    "BUDDHISM" -> Religion.BUDDHISM
                                                    "SIKHISM" -> Religion.SIKHISM
                                                    "JUDAISM" -> Religion.JUDAISM
                                                    "BAHAI_FAITH" -> Religion.BAHAI_FAITH
                                                    "CONFUCIANISM" -> Religion.CONFUCIANISM
                                                    "JAINISM" -> Religion.JAINISM
                                                    "SHINTOISM" -> Religion.SHINTOISM
                                                    else -> Religion.UNKNOWN
                                                }
                                            )
                                            ?.copy(
                                                pets = when (petsStoredOption) {
                                                    "DOG" -> Pets.DOG
                                                    "CAT" -> Pets.CAT
                                                    "FISH" -> Pets.FISH
                                                    "HAMSTER_OR_GUINEA_PIG" -> Pets.HAMSTER_OR_GUINEA_PIG
                                                    "BIRD" -> Pets.BIRD
                                                    "RABBIT" -> Pets.RABBIT
                                                    "REPTILE" -> Pets.REPTILE
                                                    "AMPHIBIAN" -> Pets.AMPHIBIAN
                                                    else -> Pets.UNKNOWN
                                                }
                                            )
                                    if (updatedExistingUser != null) {
                                        usersViewModel.userDao.update(updatedExistingUser)
                                    }

                                    val userId = user?.id ?: ""
                                    if (updatedExistingUser != null) {
                                        val userData: MutableMap<String, Any> = mutableMapOf()
                                        val mediaItems: MutableMap<String, Uri> = mutableMapOf()
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
                                        usersViewModel.saveUserDataToFirebase(
                                            userId, userData,
                                            mediaItems, context
                                        ) {

                                        }
                                    }
                                }
                            }
                            navController.navigate("signupFilter")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        //padding is 376
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                ) {
                    Text("Add your essentials", fontSize = 16.sp)
                }
            }
        }

    }
}