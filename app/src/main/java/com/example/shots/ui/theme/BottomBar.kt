package com.example.shots.ui.theme

import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationDefaults
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shots.R

@Composable
fun BottomBar(navController: NavController, usersViewModel: UsersViewModel) {
    var newShotsCount by remember { mutableStateOf(0) }
    var newMessagesCount by remember { mutableStateOf(0) }

    val lastRoute by remember(navController) {
        derivedStateOf {
            navController.currentBackStackEntry?.destination?.route
        }
    }
    val selectedItem by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val user = usersViewModel.getUser()
        newShotsCount = user?.newShotsCount ?: 0
        newMessagesCount = user?.newMessagesCount ?: 0
    }

    BottomNavigation(
        windowInsets = BottomNavigationDefaults.windowInsets,
        backgroundColor = Color.White
    ) {
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.groups_24px),
                    contentDescription = "Users Icon",
                    modifier = Modifier
                        .size(28.dp),
                    tint = if (lastRoute.equals("users")) Color(0xFFFF6F00) else Color.Black
                )
            },
            selected = selectedItem == 0,
            onClick = {
                if (!lastRoute.equals("users")) {
                    navController.navigate("users")
                }
            },
        )
        BottomNavigationItem(
            icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.bookmark_24px),
                        contentDescription = "Profile Icon",
                        modifier = Modifier
                            .size(28.dp),
//                        .align(Alignment.CenterVertically),
                        // Align the icon vertically
                        tint = if (lastRoute.equals("bookmark")) Color(0xFFFF6F00) else Color.Black
                    )

            },
            selected = selectedItem == 1,
            onClick = {
                if (!lastRoute.equals("bookmark")) {
                    navController.navigate("bookmark")
                }
            },
        )
        BottomNavigationItem(
            icon = {
                if (newShotsCount > 0) {
                    BadgedBox(badge = {
                        Badge(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ) { Text("$newShotsCount") }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.sports_basketball_24px),
                            contentDescription = "Shots Icon",
                            modifier = Modifier
                                .size(28.dp),
                            tint = if (lastRoute.equals("shots")) Color(0xFFFF6F00) else Color.Black
                        )
                    }
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.sports_basketball_24px),
                        contentDescription = "Shots Icon",
                        modifier = Modifier
                            .size(28.dp),
                        tint = if (lastRoute.equals("shots")) Color(0xFFFF6F00) else Color.Black
                    )
                }
            },
            selected = selectedItem == 2,
            onClick = {
                if (!lastRoute.equals("shots")) {
                    navController.navigate("shots")
                }
            },
        )
        BottomNavigationItem(
            icon = {
                if (newMessagesCount > 0) {
                    BadgedBox(badge = {
                        Badge(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ) { Text("$newMessagesCount") }
                    }) {
                        Icon(
                            //This is for messages, I'm still uncertain the exact icon but
                            // this is for now
                            painter = painterResource(id = R.drawable.chat_bubble_24px),
                            contentDescription = "Chat Icon",
                            modifier = Modifier
                                .size(28.dp),
                            tint = if (lastRoute.equals("channels")) Color(0xFFFF6F00) else Color.Black
                        )
                    }
                } else {
                    Icon(
                        //This is for messages, I'm still uncertain the exact icon but
                        // this is for now
                        painter = painterResource(id = R.drawable.chat_bubble_24px),
                        contentDescription = "Chat Icon",
                        modifier = Modifier
                            .size(28.dp),
                        tint = if (lastRoute.equals("channels")) Color(0xFFFF6F00) else Color.Black
                    )
                }
            },
            selected = selectedItem == 3,
            onClick = {
                if (!lastRoute.equals("channels")) {
                    navController.navigate("channels")
                }
            },
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.account_circle_24px),
                    contentDescription = "Profile Icon",
                    modifier = Modifier
                        .size(28.dp),
                    tint = if (lastRoute.equals("profile")) Color(0xFFFF6F00) else Color.Black
                )
            },
            selected = selectedItem == 4,
            onClick = {
                if (!lastRoute.equals("profile")) {
                    navController.navigate("profile")
                }
            },
        )
    }
}

//@Preview
//@Composable
//fun BottomBarPreview() {
//    BottomBar(navController = rememberNavController())
//}