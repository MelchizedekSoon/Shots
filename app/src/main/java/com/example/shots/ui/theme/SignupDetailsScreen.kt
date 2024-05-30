package com.example.shots.ui.theme

import android.content.ContentValues
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.shots.data.Gender
import com.example.shots.data.LookingFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupDetailsScreen(
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
    var hasBeenChanged by remember { mutableStateOf(false) }
    var isGoingBack by rememberSaveable { mutableStateOf(false) }
    var context = LocalContext.current

    var user by remember{ mutableStateOf(usersViewModel.getUser()) }

    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navController.navigate("signupLink")
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            LaunchedEffect(Unit) {
                scope.launch {
                    dataStore.edit { preferences ->
                        preferences[intPreferencesKey("currentScreen")] = 9
                    }
                }
            }
            IconButton(
                onClick = { navController.navigate("signupLink") },
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
                                userData["lookingFor"] = "UNKNOWN"
                                userData["gender"] = "UNKNOWN"
                                userData["height"] = ""

                                val userId = user?.id ?: ""
                                usersViewModel.saveUserDataToFirebase(
                                    userId, userData,
                                    mediaItems, context
                                ) {
                                    navController.navigate("signupEssentials")
                                }
                            }
                        }
                    }
                    navController.navigate("signupEssentials")
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
                    text = "Add Your Details",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(0.dp, 48.dp, 0.dp, 0.dp)
                )
                Text(
                    text = "(Gender is required later to interact with other users.)",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))


//                Text(
//                    text = "Looking For",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.ExtraBold,
//                    modifier = Modifier.fillMaxWidth()
//                        .padding(horizontal = 32.dp)
//                )
//                Spacer(Modifier.height(8.dp))
                var lookingForExpanded by remember { mutableStateOf(false) }
                var lookingForStoredOption by rememberSaveable {
                    mutableStateOf(
                        when (user?.lookingFor) {
                            LookingFor.LONG_TERM -> "LONG_TERM"
                            LookingFor.LONG_TERM_BUT_OPEN_MINDED -> "LONG_TERM_BUT_OPEN_MINDED"
                            LookingFor.SHORT_TERM -> "SHORT_TERM"
                            LookingFor.SHORT_TERM_BUT_OPEN_MINDED -> "SHORT_TERM_BUT_OPEN_MINDED"
                            LookingFor.FRIENDS -> "FRIENDS"
                            LookingFor.UNSURE -> "UNSURE"
                            else -> "UNKNOWN"
                        }
                    )
                }
                var lookingForSelectedOption by rememberSaveable {
                    mutableStateOf(
                        when (lookingForStoredOption) {
                            "LONG_TERM" -> "Long-term"
                            "LONG_TERM_BUT_OPEN_MINDED" -> "Long-term but open-minded"
                            "SHORT_TERM" -> "Short-term"
                            "SHORT_TERM_BUT_OPEN_MINDED" -> "Short-term but open-minded"
                            "FRIENDS" -> "Friends"
                            "UNSURE" -> "Unsure"
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
                                        lookingForExpanded = true
                                    }, verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(
                                        id = R.drawable.looking_binocular_svgrepo_com_2
                                    ), contentDescription = "Looking Icon", tint = Color(0xFFFF6F00)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (lookingForSelectedOption == "") {
                                    lookingForStoredOption = "UNKNOWN"
                                    Text(
                                        text = "Looking for?", fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        text = "Add", fontSize = 20.sp, color = Color(0xFFFF6F00)
                                    )
                                } else {
                                    Text(
                                        text = lookingForSelectedOption, fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    IconButton(onClick = { lookingForSelectedOption = "" }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.cancel_24px),
                                            contentDescription = "Add Media Button",
                                            modifier = Modifier,
                                            tint = Color(0xFFFF6F00)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                DropdownMenu(modifier = Modifier.background(Color.White),
                                    expanded = lookingForExpanded,
                                    onDismissRequest = { lookingForExpanded = false }) {
                                    DropdownMenuItem(text = { Text("Long-term") }, onClick = {
                                        lookingForSelectedOption = "Long-term"
                                        lookingForExpanded = false
                                        lookingForStoredOption = "LONG_TERM"
                                    })
                                    DropdownMenuItem(text = { Text("Short-term") }, onClick = {
                                        lookingForSelectedOption = "Short-term"
                                        lookingForExpanded = false
                                        lookingForStoredOption = "SHORT_TERM"
                                    })
                                    DropdownMenuItem(text = { Text("Long-term but open-minded") },
                                        onClick = {
                                            lookingForSelectedOption = "Long-term but open-minded"
                                            lookingForExpanded = false
                                            lookingForStoredOption = "LONG_TERM_BUT_OPEN_MINDED"
                                        })
                                    DropdownMenuItem(text = { Text("Short-term but open-minded") },
                                        onClick = {
                                            lookingForSelectedOption = "Short-term but open-minded"
                                            lookingForExpanded = false
                                            lookingForStoredOption = "SHORT_TERM_BUT_OPEN_MINDED"
                                        })
                                    DropdownMenuItem(text = { Text("Friends") }, onClick = {
                                        lookingForSelectedOption = "Friends"
                                        lookingForExpanded = false
                                        lookingForStoredOption = "FRIENDS"
                                    })
                                    DropdownMenuItem(text = { Text("Unsure") }, onClick = {
                                        lookingForSelectedOption = "Unsure"
                                        lookingForExpanded = false
                                        lookingForStoredOption = "UNSURE"
                                    })
                                }
                                LaunchedEffect(lookingForSelectedOption) {
                                    signupViewModel.updateSignUpUser { currentUser ->
                                        currentUser.copy(
                                            lookingFor = when (lookingForSelectedOption) {
                                                "Long-term" -> LookingFor.LONG_TERM
                                                "Short-term" -> LookingFor.SHORT_TERM
                                                "Long-term but open-minded" -> LookingFor.LONG_TERM_BUT_OPEN_MINDED
                                                "Short-term but open-minded" -> LookingFor.SHORT_TERM_BUT_OPEN_MINDED
                                                "Friends" -> LookingFor.FRIENDS
                                                "Unsure" -> LookingFor.UNSURE
                                                else -> null
                                            }
                                        )
                                    }
                                    hasBeenChanged = true
                                }
                            }
                        }
                    }
                }

//                Text(
//                    text = "Gender", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
//                )
                var genderExpanded by remember { mutableStateOf(false) }
                var genderStoredOption by rememberSaveable {
                    mutableStateOf(
                        when (user?.gender) {
                            Gender.MAN -> "MAN"
                            Gender.WOMAN -> "WOMAN"
                            Gender.NON_BINARY -> "NON_BINARY"
                            else -> "UNKNOWN"
                        }
                    )
                }
                var genderSelectedOption by rememberSaveable {
                    mutableStateOf(
                        when (genderStoredOption) {
                            "MAN" -> "Man"
                            "WOMAN" -> "Woman"
                            "NON_BINARY" -> "Non-Binary"
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
                                        genderExpanded = true
                                    }, verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(
                                        id = R.drawable.male_and_female_symbol_svgrepo_com
                                    ),
                                    contentDescription = "Education Icon",
                                    tint = Color(0xFFFF6F00)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (genderSelectedOption == "") {
                                    genderStoredOption = "UNKNOWN"
                                    Text(
                                        text = "Add your gender", fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        text = "Add", fontSize = 20.sp, color = Color(0xFFFF6F00)
                                    )
                                } else {
                                    Text(
                                        text = genderSelectedOption, fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    IconButton(onClick = { genderSelectedOption = "" }) {
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
                                    expanded = genderExpanded,
                                    onDismissRequest = { genderExpanded = false }) {
                                    DropdownMenuItem(text = { Text("Man") }, onClick = {
                                        genderSelectedOption = "Man"
                                        genderExpanded = false
                                        genderStoredOption = "MAN"
                                    })
                                    DropdownMenuItem(text = { Text("Woman") }, onClick = {
                                        genderSelectedOption = "Woman"
                                        genderExpanded = false
                                        genderStoredOption = "WOMAN"
                                    })
                                    DropdownMenuItem(text = { Text("Non-Binary") }, onClick = {
                                        genderSelectedOption = "Non-Binary"
                                        genderExpanded = false
                                        genderStoredOption = "NON_BINARY"
                                    })
                                }
                                LaunchedEffect(genderSelectedOption) {
                                    signupViewModel.updateSignUpUser { currentUser ->
                                        currentUser.copy(
                                            gender = when (genderSelectedOption) {
                                                "Man" -> Gender.MAN
                                                "Woman" -> Gender.WOMAN
                                                "Non-Binary" -> Gender.NON_BINARY
                                                else -> null
                                            }
                                        )
                                    }
                                    hasBeenChanged = true
                                }
                            }
                        }
                    }
                }

                //Sexual Orientation - May add
//                item {
//                    Text(
//                        text = "Sexual Orientation",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.ExtraBold
//                    )
//                }
//                item {
//                    Box() {
//                        Column {
//                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
//                            Box(
//                                modifier = Modifier
//                                    .height(60.dp)
//                                    .fillMaxWidth()
//                            ) {
//                                Row(
//                                    modifier = Modifier.fillMaxSize(),
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
////                                    Icon(
////                                        painterResource(
////                                            id = R.drawable.work_fill0_wght400_grad0_opsz24
////                                        ),
////                                        contentDescription = "Work Icon",
////                                        tint = Color(0xFFFF6F00)
////                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    var expanded by remember { mutableStateOf(false) }
//                                    var selectedOption by remember {
//                                        mutableStateOf(
//                                            ""
//                                        )
//                                    }
//                                    Text(text = selectedOption.ifEmpty { "Select an option" },
//                                        fontSize = 20.sp,
//                                        modifier = Modifier.clickable { expanded = true })
//                                    DropdownMenu(modifier = Modifier.background(Color.White),
//                                        expanded = expanded,
//                                        onDismissRequest = { expanded = false }) {
//                                        DropdownMenuItem(text = { Text("Straight") }, onClick = {
//                                            selectedOption = "Straight"
//                                            expanded = false
//                                        })
//                                        DropdownMenuItem(text = { Text("Gay") }, onClick = {
//                                            selectedOption = "Gay"
//                                            expanded = false
//                                        })
//                                        DropdownMenuItem(text = { Text("Lesbian") }, onClick = {
//                                            selectedOption = "Lesbian"
//                                            expanded = false
//                                        })
//                                        DropdownMenuItem(text = { Text("Bisexual") }, onClick = {
//                                            selectedOption = "Straight"
//                                            expanded = false
//                                        })
//                                        DropdownMenuItem(text = { Text("Asexual") }, onClick = {
//                                            selectedOption = "Gay"
//                                            expanded = false
//                                        })
//                                        DropdownMenuItem(text = { Text("Demisexual") }, onClick = {
//                                            selectedOption = "Lesbian"
//                                            expanded = false
//                                        })
//                                        DropdownMenuItem(text = { Text("Pansexual") }, onClick = {
//                                            selectedOption = "Straight"
//                                            expanded = false
//                                        })
//                                        DropdownMenuItem(text = { Text("Queer") }, onClick = {
//                                            selectedOption = "Gay"
//                                            expanded = false
//                                        })
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
                //Living in - May add
//                item {
//                    Text(
//                        text = "Living in", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
//                    )
//                }
//                item {
//                    Box() {
//                        Column {
//                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
//                            Box(
//                                modifier = Modifier
//                                    .height(60.dp)
//                                    .fillMaxWidth()
//                            ) {
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxSize()
//                                        .align(Alignment.Center)
//                                ) {
//                                    Icon(
//                                        painterResource(id = R.drawable.location_marker_svgrepo_com),
//                                        contentDescription = "Work Icon",
//                                        tint = Color(0xFFFF6F00)
//                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    val sheetState = rememberModalBottomSheetState()
//                                    val scope = rememberCoroutineScope()
//                                    var showBottomSheet by remember { mutableStateOf(false) }
//                                    if (showBottomSheet) {
//                                        ModalBottomSheet(
//                                            modifier = Modifier.fillMaxWidth(),
//                                            onDismissRequest = {
//                                                showBottomSheet = false
//                                            },
//                                            sheetState = sheetState
//                                        ) {
//                                            var textFieldValue by remember {
//                                                mutableStateOf(
//                                                    TextFieldValue()
//                                                )
//                                            }
//                                            val suggestions by remember {
//                                                mutableStateOf(
//                                                    mutableListOf<String>()
//                                                )
//                                            }
//                                            val httpClient = remember { OkHttpClient() }
//                                            val apiKey =
//                                                "YOUR_PLACES_API_KEY" // Replace with your Places API key
//
//                                            LaunchedEffect(textFieldValue.text) {
//                                                if (textFieldValue.text.isNotEmpty()) {
//                                                    val fetchedSuggestions =
//                                                        withContext(Dispatchers.IO) {
//                                                            fetchCityAndStateSuggestions(
//                                                                textFieldValue.text
//                                                            )
//                                                        }
//                                                    suggestions.clear()
//                                                    suggestions.addAll(fetchedSuggestions)
//                                                }
//                                            }
//
//                                            TextField(
//                                                value = textFieldValue,
//                                                onValueChange = { newValue ->
//                                                    textFieldValue = newValue
//                                                }
//                                            )
//
//                                            LazyColumn {
//                                                items(suggestions) { suggestion ->
//                                                    Text(text = suggestion)
//                                                }
//                                            }
//                                        }
//                                    }
//                                    Text(
//                                        modifier = Modifier.clickable {
//                                            showBottomSheet = true
//                                        }, text = "Work", fontSize = 20.sp
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }

//                Text(
//                    text = "Height", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
//                )
                var heightResult: String? by remember { mutableStateOf(null) }
                var heightState by remember {
                    mutableStateOf(user?.height ?: "")
                }
                val heightAdded = remember { mutableStateOf(false) }
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
                                        heightAdded.value = true
                                    }, verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ruler_2_svgrepo_com),
                                    contentDescription = "Ruler for Height Icon",
                                    tint = Color(0xFFFF6F00)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (heightState == "") {
                                    Text(
                                        text = "Add your height", fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        text = "Add", fontSize = 20.sp, color = Color(0xFFFF6F00)
                                    )
                                } else {
                                    Text(
                                        text = heightState, fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    IconButton(onClick = { heightState = "" }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.cancel_24px),
                                            contentDescription = "Cancel Button",
                                            modifier = Modifier,
                                            tint = Color(0xFFFF6F00)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                if (heightAdded.value) {
                                    var feetToInt = 3
                                    var inchesToInt = 0
                                    ModalBottomSheet(onDismissRequest = {
                                        heightAdded.value = false
                                    }) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally// Set the width to match the parent
                                        ) {
                                            Text(
                                                text = "Add your height",
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
                                                        Box(
                                                            modifier = Modifier.weight(
                                                                1f
                                                            )
                                                        ) {
                                                            Column() {
                                                                Text(
                                                                    text = "Feet",
                                                                    color = Color.Black
                                                                )
                                                                var feet by remember {
                                                                    mutableStateOf(
                                                                        "3"
                                                                    )
                                                                }
                                                                TextField(
                                                                    value = feet,
                                                                    onValueChange = {
                                                                        feet = it
                                                                    },
                                                                    colors = TextFieldDefaults.textFieldColors(
                                                                        containerColor = Color(
                                                                            0xFFFFD7B5
                                                                        )
                                                                    ),
                                                                    keyboardOptions = KeyboardOptions(
                                                                        keyboardType = KeyboardType.Number
                                                                    )
                                                                )
                                                                try {
                                                                    feetToInt = feet.toInt()
                                                                } catch (_: NumberFormatException) {
                                                                }
                                                            }
                                                        }
                                                        Spacer(Modifier.width(32.dp))
                                                        Box(
                                                            modifier = Modifier.weight(
                                                                1f
                                                            )
                                                        ) {
                                                            Column() {
                                                                Text(
                                                                    text = "Inches",
                                                                    color = Color.Black
                                                                )
                                                                var inches by remember {
                                                                    mutableStateOf(
                                                                        "0"
                                                                    )
                                                                }
                                                                TextField(
                                                                    value = inches,
                                                                    onValueChange = {
                                                                        inches = it
                                                                    },
                                                                    colors = TextFieldDefaults.textFieldColors(
                                                                        containerColor = Color(
                                                                            0xFFFFD7B5
                                                                        )
                                                                    )
                                                                )
                                                                try {
                                                                    inchesToInt = inches.toInt()
                                                                } catch (_: NumberFormatException) {
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if ((inchesToInt < 0 || inchesToInt > 11) || (feetToInt < 3 || feetToInt > 7)) {
                                                        Spacer(Modifier.height(8.dp))
                                                        Text(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            text = "Height must be 3 - 7 feet and 0 - 11 inches",
                                                            color = Color.Red,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(Modifier.height(48.dp))
                                            Button(onClick = {
                                                if (inchesToInt in 0..11 && feetToInt >= 3 && feetToInt <= 7) {
                                                    heightState = "$feetToInt'$inchesToInt"
                                                    heightAdded.value = false
                                                    heightResult = heightState
//                                                        scope.launch {
//                                                            snackbarMessage = "Height added!"
//                                                            snackbarHostState.showSnackbar(
//                                                                snackbarMessage,
//                                                                duration = SnackbarDuration.Short
//                                                            )
//                                                        }
                                                    //add height to the DB
                                                }
                                            }) {
                                                Text(text = "Add", color = Color.White)
                                            }
                                            Spacer(Modifier.height(120.dp))
                                        }
                                    }
                                }
                                LaunchedEffect(heightState) {
                                    signupViewModel.updateSignUpUser { currentUser ->
                                        currentUser.copy(height = heightState)
                                    }
                                    hasBeenChanged = true
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    shape = RoundedCornerShape(0.dp), onClick = {
                        if (lookingForStoredOption != "UNKNOWN" || genderStoredOption != "UNKNOWN"
                            || heightResult?.isNotBlank() == true
                        ) {
                            Log.d(
                                ContentValues.TAG,
                                "User values - ${signupViewModel.getSignUpUser()}"
                            )
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val existingUser = user
                                    Log.d(
                                        ContentValues.TAG,
                                        "In age screen, the returned initial user is ${existingUser}"
                                    )
                                    val updatedExistingUser =
                                        existingUser?.copy(
                                            lookingFor =
                                            when (lookingForStoredOption) {
                                                "LONG_TERM" -> LookingFor.LONG_TERM
                                                "LONG_TERM_BUT_OPEN_MINDED" -> LookingFor.LONG_TERM_BUT_OPEN_MINDED
                                                "FRIENDS" -> LookingFor.FRIENDS
                                                "UNSURE" -> LookingFor.UNSURE
                                                "SHORT_TERM" -> LookingFor.SHORT_TERM
                                                "SHORT_TERM_BUT_OPEN_MINDED" -> LookingFor.SHORT_TERM_BUT_OPEN_MINDED
                                                else -> LookingFor.UNKNOWN
                                            }
                                        )
                                            ?.copy(
                                                gender = when (genderStoredOption) {
                                                    "MAN" -> Gender.MAN
                                                    "WOMAN" -> Gender.WOMAN
                                                    "NON_BINARY" -> Gender.NON_BINARY
                                                    else -> Gender.UNKNOWN
                                                }
                                            )
                                            ?.copy(height = heightState)
                                    Log.d(
                                        ContentValues.TAG,
                                        "Finally, the user on age screen is - ${updatedExistingUser}"
                                    )
                                    if (updatedExistingUser != null) {
                                        usersViewModel.userDao.update(updatedExistingUser)
                                    }
                                    val userId = firebaseAuth.currentUser?.displayName ?: ""
                                    if (updatedExistingUser != null) {
                                        val userData: MutableMap<String, Any> = mutableMapOf()
                                        val mediaItems: MutableMap<String, Uri> = mutableMapOf()
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
                                        userData["gender"] =
                                            when (updatedExistingUser.gender) {
                                                Gender.MAN -> "MAN"
                                                Gender.WOMAN -> "WOMAN"
                                                Gender.NON_BINARY -> "NON_BINARY"
                                                else -> "UNKNOWN"
                                            }
                                        userData["height"] =
                                            updatedExistingUser.height ?: ""
                                        usersViewModel.saveUserDataToFirebase(
                                            userId ?: "", userData,
                                            mediaItems, context
                                        ) {
                                            navController.navigate("signupEssentials")
                                        }
                                    }
                                }
                            }
                        }

                    }, modifier = Modifier
                        .fillMaxWidth()
                        //padding is 376
                        .height(60.dp)
                        .padding(horizontal = 32.dp)
                ) {
                    Text("Add your details", fontSize = 16.sp)
                }
            }
        }

    }
}