package com.example.shots

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PermissionsUtils {


    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun PermissionsForSendingShot(navController: NavController, userId: String) {
        val snackbarHostState = remember { SnackbarHostState() }
        var snackbarMessage by remember {
            mutableStateOf("")
        }
        val scope = rememberCoroutineScope()
        val cameraAndAudioPermissionRequest =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val cameraPermissionGranted = permissions[Manifest.permission.CAMERA] ?: false
                val audioPermissionGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

                Log.d("UserProfileScreen", "cameraPermissionGranted - $cameraPermissionGranted")
                Log.d("UserProfileScreen", "audioPermissionGranted - $audioPermissionGranted")

                if (cameraPermissionGranted && audioPermissionGranted) {
                    // Both camera and audio permissions are granted
                    // Implement camera related code
                    navController.navigate("camera/$userId")
                } else {
                    // Handle the case where either camera or audio permission is denied
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            snackbarMessage =
                                "Please grant camera and audio permissions to use this feature."
                            snackbarHostState.showSnackbar(snackbarMessage)
                        }
                    }
                }
            }
    }

    @OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun multiplePermissionDemo(cameraPermissionRequest: (Boolean) -> Unit) {
        val multiplePermission = rememberMultiplePermissionsState(
            permissions = listOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            )
        )

        val context = LocalContext.current

        val showRationalDialog = remember { mutableStateOf(false) }
        if (multiplePermission.revokedPermissions.isEmpty()) {
            cameraPermissionRequest(true)
            showRationalDialog.value = false
        } else {
            showRationalDialog.value = true
        }
        if (showRationalDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showRationalDialog.value = false
                },
                title = {
                    Text(
                        text = "Permission",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        "We need camera permission to record shot (video). Please grant the permissions.",
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showRationalDialog.value = false
                            val intent = Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(context, intent, null)

                        }) {
                        Text("OK", style = TextStyle(color = Color.Black))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            cameraPermissionRequest(false)
                            showRationalDialog.value = false
                        }) {
                        Text("Cancel", style = TextStyle(color = Color.Black))
                    }
                },
            )
        }

//        Scaffold(topBar = {
//            CenterAlignedTopAppBar(
//                title = { Text(text = "Request Multiple Permission", color = Color.White) },
//                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primary
//                )
//            )
//        }) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(it),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Button(onClick = {
//                        if (!multiplePermission.allPermissionsGranted) {
//                            if (multiplePermission.shouldShowRationale) {
//                                // Show a rationale if needed (optional)
//                                showRationalDialog.value = true
//                            } else {
//                                // Request the permission
//                                multiplePermission.launchMultiplePermissionRequest()
//
//                            }
//                        } else {
//                            Toast.makeText(
//                                context,
//                                "We have camera and audio permission",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }) {
//                        Text(text = "Ask for permission")
//                    }
//                    Text(
//                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
//                        text = if (multiplePermission.allPermissionsGranted) {
//                            "All Permission Granted"
//                        } else if (multiplePermission.shouldShowRationale) {
//                            // If the user has denied the permission but the rationale can be shown,
//                            // then gently explain why the app requires this permission
//                            if (multiplePermission.revokedPermissions.size == 2) {
//                                "We need camera and audio permission to shoot video"
//                            } else if (multiplePermission.revokedPermissions.first().permission == android.Manifest.permission.CAMERA) {
//                                "We need camera permission. Please grant the permission."
//                            } else {
//                                "We need audio permission. Please grant the permission."
//                            }
//                        } else {
//                            // If it's the first time the user lands on this feature, or the user
//                            // doesn't want to be asked again for this permission, explain that the
//                            // permission is required
//                            "We need camera and audio permission to shoot video"
//                        },
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 16.sp
//                    )
//                }
//            }
//        }
    }

}