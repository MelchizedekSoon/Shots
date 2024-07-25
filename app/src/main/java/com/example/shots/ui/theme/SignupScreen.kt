package com.example.shots.ui.theme

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.shots.GetStreamClientModule
import com.example.shots.NetworkBoundResource
import com.example.shots.R
import com.example.shots.client
import com.example.shots.data.Gender
import com.example.shots.data.TypeOfMedia
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun SignupScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    bookmarkViewModel: BookmarkViewModel,
    receivedLikeViewModel: ReceivedLikeViewModel,
    sentLikeViewModel: SentLikeViewModel,
    receivedShotViewModel: ReceivedShotViewModel,
    sentShotViewModel: SentShotViewModel,
    blockedUserViewModel: BlockedUserViewModel,
    userWhoBlockedYouViewModel: UserWhoBlockedYouViewModel,
    editProfileViewModel: EditProfileViewModel,
    signupViewModel: SignupViewModel,
    firebaseViewModel: FirebaseViewModel,
    dataStore: DataStore<Preferences>
) {

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navController.navigate("login")
        }
    }

    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val userData: MutableMap<String, Any> =
        mutableMapOf()
    val mediaItems: MutableMap<String, Uri> =
        mutableMapOf()

    var locationWasGranted by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationWasGranted = // Permission denied, handle accordingly
            isGranted
    }

    val isEditingMediaOne by rememberSaveable { mutableStateOf(false) }
    var isEditingMediaProfileVideo by rememberSaveable { mutableStateOf(false) }
    var mediaOneWasClicked by remember { mutableStateOf(false) }
    var mediaProfileVideoWasClicked by remember { mutableStateOf(false) }
    val isEditingProfile by rememberSaveable { mutableStateOf(false) }
    var isInitializing by rememberSaveable { mutableStateOf(false) }
    var isReadyToLaunch by rememberSaveable {
        mutableStateOf(false)
    }

    DisposableEffect(backCallback) {
        onBackPressedDispatcher?.addCallback(backCallback)

        onDispose {
            backCallback.isEnabled = false
        }
    }

    val emailState = remember { mutableStateOf(signupViewModel.emailText.value) }
    val passwordState = remember { mutableStateOf(signupViewModel.passwordText.value) }
    val usernameState = remember { mutableStateOf(signupViewModel.usernameText.value) }

    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(
            context
        )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        }) {

        Modifier.padding(it)

        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    object :
                        CancellationToken() {
                        override fun onCanceledRequested(
                            p0: OnTokenCanceledListener
                        ) =
                            CancellationTokenSource().token

                        override fun isCancellationRequested() =
                            false
                    })
                    .addOnSuccessListener { location: Location? ->

                        Log.d(
                            "SignupScreen",
                            "location.latitude = ${location?.latitude}"
                        )
                        Log.d(
                            "SignupScreen",
                            "location.longitude = ${location?.longitude}"
                        )
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { returnedLocation: Location? ->
                                // Got last known location. In some rare situations this can be null.
                                if (returnedLocation != null) {
                                    userData["latitude"] =
                                        returnedLocation.latitude
                                    userData["longitude"] =
                                        returnedLocation.longitude
                                }
                            }

                    }
            }

            else -> {

                locationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

                if (locationWasGranted) {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        object :
                            CancellationToken() {
                            override fun onCanceledRequested(
                                p0: OnTokenCanceledListener
                            ) =
                                CancellationTokenSource().token

                            override fun isCancellationRequested() =
                                false
                        })
                        .addOnSuccessListener { location: Location? ->

                            Log.d(
                                "SignupScreen",
                                "location.latitude = ${location?.latitude}"
                            )
                            Log.d(
                                "SignupScreen",
                                "location.longitude = ${location?.longitude}"
                            )
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { returnedLocation: Location? ->
                                    // Got last known location. In some rare situations this can be null.
                                    if (returnedLocation != null) {
                                        userData["latitude"] =
                                            returnedLocation.latitude
                                        userData["longitude"] =
                                            returnedLocation.longitude
                                    }
                                }
                        }
                }
            }

        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        LaunchedEffect(Unit) {
            scope.launch {
                dataStore.edit { preferences ->
                    preferences[intPreferencesKey("currentScreen")] = 1
                }
            }
        }

//        IconButton(
//            onClick = { navController.navigate("login") },
//            modifier = Modifier.clickable {
//                navController.navigate("login")
//            }
//        ) {
//            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
//                modifier = Modifier.clickable {
//                    navController.navigate("login")
//                })
//        }

        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Image(
                    painterResource(R.drawable.shots_3_cropped),
                    "Shots Logo",
                    modifier = Modifier
                        .height(260.dp)
                        .aspectRatio(1f)
                )
                Text(
                    text = "Sign up", fontSize = 24.sp,
                    modifier = Modifier
                        .padding(0.dp, 40.dp, 0.dp, 0.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    modifier = Modifier
                        //padding originally 200
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                    value = emailState.value,
                    onValueChange = { emailState.value = it },
                    label = {
                        Text(
                            "Email"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        //padding is 288.dp
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                    value = passwordState.value,
                    onValueChange = { passwordState.value = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                    value = usernameState.value, // Use the value from usernameState
                    onValueChange = {
                        usernameState.value = it
                    },
                    label = {
                        Text(
                            "Username",
                            style = Typography.bodyMedium
                        )
                    }
                )

                var validUsername by remember { mutableStateOf(false) }

                LaunchedEffect(usernameState.value) {
                    if (usernameState.value.text != "") {
                        validUsername = signupViewModel.checkForAvailability(
                            context,
                            usernameState.value.text.lowercase(Locale.ROOT)
                        )
                    }
                }

                if (usernameState.value.text.isNotEmpty()) {
                    if (!validUsername) {
                        Text(
                            text = "Username is taken, invalid, or banned!",
                            color = Color(0xFFFF0000)
                        )
                    } else {
                        Text(
                            text = "Username is valid!",
                            color = Color(0xFF00FF00)
                        )
                    }
                }

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

                val currentDate = LocalDate.now()
                val minBirthDate = currentDate.minusYears(18)
                val minBirthDateMillis =
                    minBirthDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = minBirthDateMillis
                )

                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.padding(32.dp)
                )

                val selectedDate = datePickerState.selectedDateMillis?.let {
                    Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate()
                }

                Text("Selected: ${selectedDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "No selection"}")

                Text(
                    text = "Add your gender", fontSize = 24.sp,
                    modifier = Modifier
                        .padding(0.dp, 48.dp, 0.dp, 0.dp)
                )

                Text(
                    text = "Though required, this can be skipped for now.", fontSize = 12.sp,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                )

                var genderExpanded by remember { mutableStateOf(false) }
                var genderStoredOption by rememberSaveable {
                    mutableStateOf("")
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

                Box {
                    Column {
                        Box(
                            modifier = Modifier
                                .height(60.dp)
                                .padding(horizontal = 32.dp)
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
//                                    hasBeenChanged = true
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Add your media", fontSize = 24.sp,
                    modifier = Modifier
                        .padding(0.dp, 48.dp, 0.dp, 0.dp)
                )

                Text(
                    text = "Though required, this can be skipped for now.", fontSize = 12.sp,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                //MediaOne
                var mediaOneState by rememberSaveable {
                    mutableStateOf(
                        ""
                    )
                }

                var typeOfMediaOneState by rememberSaveable { mutableStateOf(TypeOfMedia.UNKNOWN) }

                val pickMediaOne =
                    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                        if (uri != null) {
                            mediaOneState = uri.toString()
                        }
                    }

                Card(
                    modifier = Modifier
                        .padding(32.dp, 0.dp, 32.dp, 0.dp)
                        .shadow(16.dp)
                        .height(320.dp)
                        .clickable {
                            // Perform additional actions here if needed
                            if (!isEditingProfile) {
                                pickMediaOne.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                    )
                                )
                                mediaOneWasClicked = true
                            }
                        }, colors = CardColors(
                        contentColor = Color.Black,
                        containerColor = Color(
                            0xFFe5e4e2
                        ),
                        disabledContainerColor = Color.Red,
                        disabledContentColor = Color.Red,
                    )
                ) {
                    if (mediaOneState == "") {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(16.dp)
                                .height(180.dp),
                            colors = CardColors(
                                containerColor = Color.White,
                                disabledContentColor = Color.White,
                                disabledContainerColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    painter = painterResource(id = R.drawable.add_circle_24px),
                                    contentDescription = "Add Media Button",
                                    modifier = Modifier
                                        .height(48.dp)
                                        .width(48.dp)
                                        .align(Alignment.Center),
                                    tint = Color(0xFFFF6F00)
                                )
                            }
                            if (isEditingMediaOne) {
                                Loader()
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Log.d(
                                TAG,
                                "Glide Image - mediaOne Image - $mediaOneState"
                            )
                            GlideImage(
                                model = mediaOneState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                    },
                                contentScale = ContentScale.Crop,
                                contentDescription = "mediaOne image"
                            )
                            Icon(
                                painter = painterResource(R.drawable.cancel_svgrepo_com_2),
                                "Cancel Icon",
                                tint = Color.Red,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                    .clickable {
                                        mediaOneState = ""
                                        typeOfMediaOneState = TypeOfMedia.UNKNOWN
                                        mediaOneWasClicked = true
                                    }
                            )
                            if (isEditingMediaOne) {
                                Loader()
                            }
                        }
                    }
                    LaunchedEffect(
                        mediaOneState
                    ) {
                        Log.d(
                            "EditProfileScreen",
                            "mediaOneWasClicked = $mediaOneWasClicked"
                        )
                        if (mediaOneWasClicked) {
                            mediaItems["mediaOne"] = mediaOneState.toUri()
                            mediaOneWasClicked = false
                        }
                    }
                }

                //MediaOne
                var mediaProfileVideoState by rememberSaveable {
                    mutableStateOf(
                        ""
                    )
                }

                var typeOfMediaProfileVideoState by rememberSaveable { mutableStateOf(TypeOfMedia.UNKNOWN) }

                val pickMediaProfileVideo =
                    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                        if (uri != null) {
                            mediaProfileVideoState = uri.toString()
                        }
                    }

                Text(
                    text = "Add your profile video", fontSize = 24.sp,
                    modifier = Modifier
                        .padding(0.dp, 48.dp, 0.dp, 0.dp)
                )

                Text(
                    text = "Though required, this can be skipped for now.", fontSize = 12.sp,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .padding(32.dp, 0.dp, 32.dp, 0.dp)
                        .shadow(16.dp)
                        .height(320.dp)
                        .clickable {
                            if (!isEditingProfile) {
                                pickMediaProfileVideo.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                    )
                                )
                                mediaProfileVideoWasClicked = true
                            }
                        }, colors = CardColors(
                        contentColor = Color.Black,
                        containerColor = Color(
                            0xFFe5e4e2
                        ),
                        disabledContainerColor = Color.Red,
                        disabledContentColor = Color.Red,
                    )
                ) {
                    if (mediaProfileVideoState == "") {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(16.dp)
                                .height(180.dp),
                            colors = CardColors(
                                containerColor = Color.White,
                                disabledContentColor = Color.White,
                                disabledContainerColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    painter = painterResource(id = R.drawable.add_circle_24px),
                                    contentDescription = "Add Media Button",
                                    modifier = Modifier
                                        .height(48.dp)
                                        .width(48.dp)
                                        .align(Alignment.Center),
                                    tint = Color(0xFFFF6F00)
                                )
                            }
//                            if (isEditingMediaProfileVideo) {
//                                Loader()
//                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Log.d(
                                TAG,
                                "Glide Image - mediaProfileVideo Image - $mediaProfileVideoState"
                            )
                            GlideImage(
                                model = mediaProfileVideoState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                    },
                                contentScale = ContentScale.Crop,
                                contentDescription = "mediaProfileVideo image"
                            )
                            Icon(
                                painter = painterResource(R.drawable.cancel_svgrepo_com_2),
                                "Cancel Icon",
                                tint = Color.Red,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                    .clickable {
                                        mediaProfileVideoState = ""
                                        typeOfMediaProfileVideoState = TypeOfMedia.UNKNOWN
                                        mediaProfileVideoWasClicked = true
                                    }
                            )
//                            if (isEditingMediaProfileVideo) {
//                                Loader()
//                            }
                        }
                    }
                    LaunchedEffect(
                        mediaProfileVideoState
                    ) {
                        Log.d(
                            "EditProfileScreen",
                            "mediaProfileVideoWasClicked = $mediaProfileVideoWasClicked"
                        )
                        if (mediaProfileVideoWasClicked) {
                            mediaItems["mediaProfileVideo"] = mediaProfileVideoState.toUri()
                            mediaProfileVideoWasClicked = false
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        isInitializing = true
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
                                if (validUsername) {

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

                                            val user = signupViewModel.createUser(
                                                emailState.value.text,
                                                passwordState.value.text
                                            )

                                            Log.d("SignupScreen", "user - $user")

                                            if (user == null) {
                                                snackbarHostState.showSnackbar(
                                                    message = "Account invalid or already exists. If it exists, log in.",
                                                    duration = SnackbarDuration.Short,
                                                )
                                            } else {

                                                dataStore.edit { preferences ->
                                                    preferences[booleanPreferencesKey("isLoggedIn")] =
                                                        true
                                                    preferences[booleanPreferencesKey("hasSignedUp")] =
                                                        true
                                                }

                                                user.updateProfile(
                                                    UserProfileChangeRequest.Builder()
                                                        .setDisplayName(usernameState.value.text)
                                                        .build()
                                                ).addOnSuccessListener {

                                                    isInitializing = true

                                                    Log.d(
                                                        "SignupScreen",
                                                        "isInitializing = ${isInitializing} " +
                                                                "and should be showing loader"
                                                    )

                                                    userData["id"] = user.displayName ?: ""
                                                    userData["birthday"] =
                                                        System.currentTimeMillis() -
                                                                selectedDateInMillis
                                                    userData["displayName"] =
                                                        user.displayName ?: ""
                                                    userData["userName"] =
                                                        user.displayName ?: ""
                                                    userData["gender"] =
                                                        when (genderSelectedOption) {
                                                            "Man" -> "MAN"
                                                            "Woman" -> "WOMAN"
                                                            "Non-Binary" -> "NON_BINARY"
                                                            else -> "UNKNOWN"
                                                        }

                                                    userViewModel.saveAndStoreData(
                                                        user.displayName ?: "",
                                                        userData, mediaItems, context
                                                    ) {}

                                                    userViewModel.loadUsers()

                                                    bookmarkViewModel.loadBookmarks()

                                                    receivedLikeViewModel.loadReceivedLikes()

                                                    sentLikeViewModel.loadSentLikes()

                                                    receivedShotViewModel.loadReceivedShots()

                                                    sentShotViewModel.loadSentShots()

                                                    blockedUserViewModel.loadBlockedUsers()

                                                    userWhoBlockedYouViewModel.loadUsersWhoBlockedYou()

                                                    editProfileViewModel.loadEditProfileOptions()

                                                    scope.launch(Dispatchers.IO) {
                                                        NetworkBoundResource().createUser(
                                                            userViewModel
                                                        )
                                                    }

                                                    client =
                                                        GetStreamClientModule.provideGetStreamClient(
                                                            context,
                                                            userViewModel,
                                                            firebaseViewModel
                                                        )

                                                    Log.d(
                                                        "MainActivity",
                                                        "client on MainActivity = $client"
                                                    )

                                                    isInitializing = false
                                                    isReadyToLaunch = true

                                                }.addOnFailureListener {
                                                    //I don't have anything here
                                                }


                                                if (isReadyToLaunch) {
                                                    scope.launch(Dispatchers.Main) {
                                                        navController.navigate("users")
                                                    }
                                                }

                                            }

                                        }
                                    }


                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Please add a valid username.")
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
                    Text("Sign Up", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

            }
        }

    }
    if (isInitializing) {
        Log.d("SignupScreen", "Inside (outside) of isInitializing right above loader")
        Loader()
    }
    if (isReadyToLaunch) {
        navController.navigate("users")
    }
}





