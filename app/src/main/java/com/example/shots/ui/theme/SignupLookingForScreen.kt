package com.example.shots.ui.theme

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.RoomDatabase
import com.example.shots.FirebaseModule
import com.example.shots.RoomModule
import com.example.shots.ViewModelModule

/** ATTENTION! THIS SCREEN IS NOT BEING USED CURRENTLY!!!!
 */

@Composable
fun SignupLookingForScreen(navController: NavController, signupViewModel: SignupViewModel) {
    val firebaseAuth = FirebaseModule.provideFirebaseAuth()
    val firestore = FirebaseModule.provideFirestore()
    val firebaseStorage = FirebaseModule.provideStorage()
    val firebaseRepository =
        FirebaseModule.provideFirebaseRepository(firebaseAuth, firestore, firebaseStorage)
    val appDatabase = RoomModule.provideAppDatabase(LocalContext.current)
    val usersViewModel = ViewModelModule.provideUsersViewModel(firebaseRepository, firebaseAuth,
        appDatabase)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
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
            var linkState = remember { mutableStateOf("") }
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(it)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            IconButton(
                onClick = {
                    //navigate to next

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
                    text = "Add your link", fontSize = 24.sp,
                    modifier = Modifier
                        .padding(0.dp, 48.dp, 0.dp, 0.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                    value = linkState.value, // Use the value from usernameState
                    onValueChange = { textValue: String ->
                        linkState.value = textValue
                    },
                    label = {
                        Text(
                            "Link",
                            style = Typography.bodyMedium
                        )
                    }
                )
                val context = LocalContext.current
                var validUsername by remember { mutableStateOf(false) }
//                LaunchedEffect(linkState.value) {
//                    if (linkState.value != "") {
//                        validUsername = !signupViewModel.checkForBannedWords(
//                            context, linkState.value
//                        )
//                        Log.d(
//                            ContentValues.TAG, "validUsername ended up being" +
//                                    " $validUsername"
//                        )
//                    }
//                }
//                if (linkState.value.isNotEmpty()) {
//                    if (!validUsername) {
//                        Text(
//                            text = "This display name is not allowed.",
//                            color = Color(0xFFFF0000)
//                        )
//                    }
//                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
//                        scope.launch {
//                            if (validUsername) {
//                                usersViewModel.getUser()?.displayName = linkState.value
//                                //now navigate
////                                navController.navigate()
//                            }
//                        }
                        usersViewModel.getUser()?.link = linkState.value
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        //padding is 376
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                ) {
                    Text("Add your link", fontSize = 16.sp)
                }
            }
        }

    }
}