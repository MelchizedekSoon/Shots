package com.example.shots

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

object DialogUtils {

    @Composable
    fun blockedUserRemovalDialog(wasConfirmed: (Boolean) -> Int): Boolean {
        var openDialog by remember { mutableStateOf(true) }
        var hasBeenConfirmed by remember { mutableStateOf(false) }
        Column {


            if (openDialog) {
                AlertDialog(
                    onDismissRequest = {
                        openDialog = false
                        hasBeenConfirmed = false
                        wasConfirmed(false)
                    },
                    title = {
                        Text(text = "Unblock user")
                    },
                    text = {
                        Text("Are you sure you want to unblock this user?")
                    },
                    confirmButton = {
                        Button(

                            onClick = {
                                openDialog = false
                                hasBeenConfirmed = true
                                wasConfirmed(true)
                            }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                openDialog = false
                                hasBeenConfirmed = false
                                wasConfirmed(false)
                            }) {
                            Text("No")
                        }
                    }
                )
            }

        }
        return hasBeenConfirmed
    }

    @Composable
    fun bookmarkRemovalDialog(wasConfirmed: (Boolean) -> Int): Boolean {
        var openDialog by remember { mutableStateOf(true) }
        var hasBeenConfirmed by remember { mutableStateOf(false) }
        Column {


            if (openDialog) {
                AlertDialog(
                    onDismissRequest = {
                        openDialog = false
                        hasBeenConfirmed = false
                        wasConfirmed(false)
                    },
                    title = {
                        Text(text = "Remove bookmark")
                    },
                    text = {
                        Text("Are you sure you want to remove this bookmark?")
                    },
                    confirmButton = {
                        Button(

                            onClick = {
                                openDialog = false
                                hasBeenConfirmed = true
                                wasConfirmed(true)
                            }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                openDialog = false
                                hasBeenConfirmed = false
                                wasConfirmed(false)
                            }) {
                            Text("No")
                        }
                    }
                )
            }

        }
        return hasBeenConfirmed
    }
}

