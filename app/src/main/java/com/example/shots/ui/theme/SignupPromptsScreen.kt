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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavController
import com.example.shots.FirebaseModule
import com.example.shots.PromptsUtils
import com.example.shots.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupPromptsScreen(
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
    val context = LocalContext.current

    val user by remember { mutableStateOf(usersViewModel.getUser()) }

    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navController.navigate("signupAboutMe")
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
                        preferences[intPreferencesKey("currentScreen")] = 7
                    }
                }
            }
            IconButton(
                onClick = { navController.navigate("signupAboutMe") },
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

                            userData["promptOneQuestion"] = ""
                            userData["promptOneAnswer"] = ""
                            userData["promptTwoQuestion"] = ""
                            userData["promptTwoAnswer"] = ""
                            userData["promptThreeQuestion"] = ""
                            userData["promptThreeAnswer"] = ""

                            usersViewModel.saveUserDataToFirebase(
                                firebaseAuth.currentUser?.displayName ?: "", userData,
                                mediaItems, context
                            ) {}
                        }
                    }
                    navController.navigate("signupLink")
                },
                modifier = Modifier
                    .padding(it)
                    .padding(0.dp, 0.dp, 8.dp, 0.dp)
                    .align(Alignment.TopEnd)
            ) {
                Text(text = "Skip")
            }
            Column(
                modifier = Modifier.padding(0.dp, 48.dp, 0.dp, 0.dp),
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
                    text = "Add prompts",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(0.dp, 48.dp, 0.dp, 0.dp)
                )
//                Text(
//                    text = "Your prompts can be changed later.",
//                    fontSize = 12.sp,
//                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp)
//                )
                Spacer(modifier = Modifier.height(32.dp))


                //prompts items - a work in progress
                val prompts: List<PromptsUtils.Prompt> =
                    PromptsUtils.loadPromptsFromJson(LocalContext.current)
                Log.d(ContentValues.TAG, "$prompts")
                val focusRequester = remember { FocusRequester() }
                val keyboardController = LocalSoftwareKeyboardController.current
                var promptOneSelection by remember {
                    mutableStateOf(
                        ""
                    )
                }
                var promptOneExpanded by remember { mutableStateOf(false) }
                var promptOneAnswerState by rememberSaveable {
                    mutableStateOf(
                        ""
                    )
                }
                var promptTwoSelection by remember {
                    mutableStateOf("")
                }
                var promptTwoExpanded by remember { mutableStateOf(false) }
                var promptTwoAnswerState by rememberSaveable {
                    mutableStateOf(
                        ""
                    )
                }
                var promptThreeSelection by remember {
                    mutableStateOf(
                        ""
                    )
                }
                var promptThreeExpanded by remember { mutableStateOf(false) }
                var promptThreeAnswerState by rememberSaveable {
                    mutableStateOf(
                        ""
                    )
                }


                //promptOne starts
                //promptOne - question and answer

                Box(modifier = Modifier.padding(horizontal = 32.dp)) {
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
                                        promptOneExpanded = true
                                    }, verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(
                                        id = R.drawable.right_quote_svgrepo_com
                                    ), contentDescription = "Quote Icon", tint = Color(0xFFFF6F00)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (promptOneSelection == "") {
                                    Text(
                                        text = "Pick a prompt", fontSize = 20.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = false,
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        text = "Add", fontSize = 20.sp, color = Color(0xFFFF6F00)
                                    )
                                } else {
                                    Text(
                                        text = promptOneSelection,
                                        fontSize = 20.sp,

                                        )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        modifier = Modifier.clickable {
                                            promptOneSelection = ""
                                        },
                                        text = "Reset",
                                        fontSize = 20.sp,
                                        color = Color(0xFFFF6F00)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                DropdownMenu(modifier = Modifier.background(Color.White),
                                    expanded = promptOneExpanded,
                                    onDismissRequest = { promptOneExpanded = false }) {
                                    for (prompt in prompts) {
                                        DropdownMenuItem(text = { Text(prompt.prompt) }, onClick = {
                                            promptOneSelection = prompt.prompt
                                            promptOneExpanded = false
                                        })
                                    }
                                }
                            }
                        }
                        LaunchedEffect(promptOneSelection) {
                            signupViewModel.updateSignUpUser { currentUser ->
                                currentUser.copy(promptOneQuestion = promptOneSelection)
                            }
                            hasBeenChanged = true
                        }
                    }
                }

                //Prompt answer for promptOne

                Card(modifier = Modifier.padding(horizontal = 32.dp)) {
                    TextField(value = promptOneAnswerState,
                        onValueChange = { newValue ->
                            if (newValue.length <= 120) {
                                promptOneAnswerState = newValue
                            }
                        },
                        singleLine = false,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            focusRequester.freeFocus()
                        }),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(
                                0xFFFFD7B5
                            )
                        ),
                        label = { Text(text = "Answer") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .onFocusChanged { isFocused = it.isFocused }
                            .focusRequester(focusRequester))
                    if (!isFocused) {
                        DisposableEffect(Unit) {
                            focusRequester.freeFocus()
                            onDispose {
                                keyboardController?.hide()
                            }
                        }
                    }
                    LaunchedEffect(promptOneAnswerState) {
                        signupViewModel.updateSignUpUser { currentUser ->
                            currentUser.copy(promptOneAnswer = promptOneAnswerState)
                        }
                        hasBeenChanged = true
                    }
                }
                //promptOne ends

                //promptTwo starts
                //promptTwo question

                Box(modifier = Modifier.padding(horizontal = 32.dp)) {
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
                                        promptTwoExpanded = true
                                    }, verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(
                                        id = R.drawable.right_quote_svgrepo_com
                                    ), contentDescription = "Quote Icon", tint = Color(0xFFFF6F00)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (promptTwoSelection == "") {
                                    Text(
                                        text = "Pick a prompt", fontSize = 20.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = false,
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        text = "Add", fontSize = 20.sp, color = Color(0xFFFF6F00)
                                    )
                                } else {
                                    Text(
                                        text = promptTwoSelection,
                                        fontSize = 20.sp,

                                        )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        modifier = Modifier.clickable {
                                            promptTwoSelection = ""
                                        },
                                        text = "Reset",
                                        fontSize = 20.sp,
                                        color = Color(0xFFFF6F00)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                DropdownMenu(modifier = Modifier.background(Color.White),
                                    expanded = promptTwoExpanded,
                                    onDismissRequest = { promptTwoExpanded = false }) {
                                    for (prompt in prompts) {
                                        DropdownMenuItem(text = { Text(prompt.prompt) }, onClick = {
                                            promptTwoSelection = prompt.prompt
                                            promptTwoExpanded = false
                                        })
                                    }
                                }
                            }
                        }
                        LaunchedEffect(promptTwoSelection) {
                            signupViewModel.updateSignUpUser { currentUser ->
                                currentUser.copy(promptTwoQuestion = promptTwoSelection)
                            }
                            hasBeenChanged = true
                        }
                    }
                }

                //Prompt answer for promptTwo

                Card(modifier = Modifier.padding(horizontal = 32.dp)) {
                    TextField(value = promptTwoAnswerState,
                        onValueChange = { newValue ->
                            if (newValue.length <= 120) {
                                promptTwoAnswerState = newValue
                            }
                        },
                        singleLine = false,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            focusRequester.freeFocus()
                        }),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(
                                0xFFFFD7B5
                            )
                        ),
                        label = { Text(text = "Answer") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .onFocusChanged { isFocused = it.isFocused }
                            .focusRequester(focusRequester))
                    if (!isFocused) {
                        DisposableEffect(Unit) {
                            focusRequester.freeFocus()
                            onDispose {
                                keyboardController?.hide()
                            }
                        }
                    }
                    LaunchedEffect(promptTwoAnswerState) {
                        signupViewModel.updateSignUpUser { currentUser ->
                            currentUser.copy(promptTwoAnswer = promptTwoAnswerState)
                        }
                        hasBeenChanged = true
                    }
                }

                //promptTwo ends

                //promptThree starts
                //promptThree question

                Box(modifier = Modifier.padding(horizontal = 32.dp)) {
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
                                        promptThreeExpanded = true
                                    }, verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(
                                        id = R.drawable.right_quote_svgrepo_com
                                    ), contentDescription = "Quote Icon", tint = Color(0xFFFF6F00)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (promptThreeSelection == "") {
                                    Text(
                                        text = "Pick a prompt", fontSize = 20.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = false,
                                    )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        text = "Add", fontSize = 20.sp, color = Color(0xFFFF6F00)
                                    )
                                } else {
                                    Text(
                                        text = promptThreeSelection,
                                        fontSize = 20.sp,

                                        )
                                    Spacer(modifier = Modifier.weight(1f)) // Use weight to occupy remaining space
                                    Text(
                                        modifier = Modifier.clickable {
                                            promptThreeSelection = ""
                                        },
                                        text = "Reset",
                                        fontSize = 20.sp,
                                        color = Color(0xFFFF6F00)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed
                                DropdownMenu(modifier = Modifier.background(Color.White),
                                    expanded = promptThreeExpanded,
                                    onDismissRequest = { promptThreeExpanded = false }) {
                                    for (prompt in prompts) {
                                        DropdownMenuItem(text = { Text(prompt.prompt) }, onClick = {
                                            promptThreeSelection = prompt.prompt
                                            promptThreeExpanded = false
                                        })
                                    }
                                }
                            }
                        }
                        LaunchedEffect(promptThreeSelection) {
                            signupViewModel.updateSignUpUser { currentUser ->
                                currentUser.copy(promptThreeQuestion = promptThreeSelection)
                            }
                            hasBeenChanged = true
                        }
                    }
                }


                //Prompt answer for promptThree

                Card(modifier = Modifier.padding(horizontal = 32.dp)) {
                    TextField(value = promptThreeAnswerState,
                        onValueChange = { newValue ->
                            if (newValue.length <= 120) {
                                promptThreeAnswerState = newValue
                            }
                        },
                        singleLine = false,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            focusRequester.freeFocus()
                        }),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(
                                0xFFFFD7B5
                            )
                        ),
                        label = { Text(text = "Answer") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .onFocusChanged { isFocused = it.isFocused }
                            .focusRequester(focusRequester))
                    if (!isFocused) {
                        DisposableEffect(Unit) {
                            focusRequester.freeFocus()
                            onDispose {
                                keyboardController?.hide()
                            }
                        }
                    }
                    LaunchedEffect(promptThreeAnswerState) {
                        signupViewModel.updateSignUpUser { currentUser ->
                            currentUser.copy(promptThreeAnswer = promptThreeAnswerState)
                        }
                        hasBeenChanged = true
                    }
                }

                //promptThree ends

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        if (hasBeenChanged) {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val existingUser = usersViewModel.getUser()
                                    val updatedExistingUser =
                                        existingUser?.copy(promptOneQuestion = promptOneSelection)
                                            ?.copy(promptOneAnswer = promptOneAnswerState)
                                            ?.copy(promptTwoQuestion = promptTwoSelection)
                                            ?.copy(promptTwoAnswer = promptTwoAnswerState)
                                            ?.copy(promptThreeQuestion = promptThreeSelection)
                                            ?.copy(promptThreeAnswer = promptThreeAnswerState)
                                    if (updatedExistingUser != null) {
                                        usersViewModel.userDao.update(updatedExistingUser)
                                    }
                                    val userId = user?.id ?: ""
                                    if (updatedExistingUser != null) {
                                        val userData: MutableMap<String, Any> = mutableMapOf()
                                        val mediaItems: MutableMap<String, Uri> = mutableMapOf()
                                        userData["aboutMe"] = updatedExistingUser.aboutMe ?: ""
                                        usersViewModel.saveUserDataToFirebase(
                                            userId ?: "", userData,
                                            mediaItems, context
                                        ) {

                                        }
                                    }
                                }
                            }
                            navController.navigate("signupLink")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        //padding is 376
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                ) {
                    Text("Add your prompts", fontSize = 16.sp)
                }
            }
        }

    }
}