package com.example.shots.ui.theme

import android.net.Uri
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.navigation.NavHostController
import com.example.shots.R
import com.example.shots.data.Distance
import com.example.shots.data.ShowMe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(navController: NavHostController, usersViewModel: UsersViewModel) {
    val context = LocalContext.current
    var user = usersViewModel.getUser()

    val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            usersViewModel.updateFilter(user, usersViewModel, navController, context) {
                navController.popBackStack()
            }
        }
    }

    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    DisposableEffect(backCallback) {
        onBackPressedDispatcher?.addCallback(backCallback)

        onDispose {
            backCallback.isEnabled = false
        }
    }

    val userData: MutableMap<String, Any> = mutableMapOf()
    val mediaItems: MutableMap<String, Uri> = mutableMapOf()

    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Filter Options") },
            navigationIcon = {
                IconButton(onClick = {
                    usersViewModel.updateFilter(
                        user, usersViewModel, navController, context
                    ) {
                        navController.popBackStack()
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back Icon")
                }
            })
    }) {
        Modifier.padding(it)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 80.dp, 16.dp, 0.dp)
        ) {

            val showMeValue = when (user?.showMe) {
                ShowMe.MEN -> "Men"
                ShowMe.WOMEN -> "Women"
                ShowMe.ANYONE -> "Anyone"
                else -> "Women"
            }

            var selectedOption by remember { mutableStateOf(showMeValue) }

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
                        selected = selectedOption == "Men",
                        onClick = { selectedOption = "Men" }
                    )
                }
                Column {
                    Text("Men")
                }

                Column {
                    RadioButton(
                        selected = selectedOption == "Women",
                        onClick = { selectedOption = "Women" }
                    )
                }
                Column {
                    Text("Women")
                }

                Column {
                    RadioButton(
                        selected = selectedOption == "Anyone",
                        onClick = { selectedOption = "Anyone" }
                    )
                }
                Column {
                    Text("Anyone")
                }

                user = user?.copy(
                    showMe = when (selectedOption) {
                        "Men" -> ShowMe.MEN
                        "Women" -> ShowMe.WOMEN
                        "Anyone" -> ShowMe.ANYONE
                        else -> ShowMe.WOMEN
                    }
                )
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
                                usersViewModel.updateUserField { currentUser ->
                                    currentUser.copy(
                                        showUsers = when (showUsersStoredOption) {
                                            "TEN" -> Distance.TEN
                                            "TWENTY" -> Distance.TWENTY
                                            "THIRTY" -> Distance.THIRTY
                                            "FORTY" -> Distance.FORTY
                                            "FIFTY" -> Distance.FIFTY
                                            "SIXTY" -> Distance.SIXTY
                                            "SEVENTY" -> Distance.SEVENTY
                                            "EIGHTY" -> Distance.EIGHTY
                                            "NINETY" -> Distance.NINETY
                                            "ONE_HUNDRED" -> Distance.ONE_HUNDRED
                                            "ANYWHERE" -> Distance.ANYWHERE
                                            else -> Distance.TEN
                                        }
                                    )
                                }

                                userData["showUsers"] = showUsersStoredOption
                                Log.d("FilterScreen", "showUsers - ${userData["showUsers"]}")
                                user = user?.copy(
                                    showUsers = when (showUsersStoredOption) {
                                        "TEN" -> Distance.TEN
                                        "TWENTY" -> Distance.TWENTY
                                        "THIRTY" -> Distance.THIRTY
                                        "FORTY" -> Distance.FORTY
                                        "FIFTY" -> Distance.FIFTY
                                        "SIXTY" -> Distance.SIXTY
                                        "SEVENTY" -> Distance.SEVENTY
                                        "EIGHTY" -> Distance.EIGHTY
                                        "NINETY" -> Distance.NINETY
                                        "ONE_HUNDRED" -> Distance.ONE_HUNDRED
                                        "ANYWHERE" -> Distance.ANYWHERE
                                        else -> Distance.TEN
                                    }
                                )
                            }

                        }
                    }
                }
            }


//            val slideStartValue = when (user?.distance ?: "") {
//                Distance.TEN -> 10
//                Distance.TWENTY -> 20
//                Distance.THIRTY -> 30
//                Distance.FORTY -> 40
//                Distance.FIFTY -> 50
//                Distance.SIXTY -> 60
//                Distance.SEVENTY -> 70
//                Distance.EIGHTY -> 80
//                Distance.NINETY -> 90
//                Distance.ONE_HUNDRED -> 100
//                else -> 10
//            }
//            var sliderPosition by remember { mutableFloatStateOf(slideStartValue.toFloat()) }
//            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
//                Slider(
//                    value = sliderPosition,
//                    onValueChange = { sliderPosition = it },
//                    colors = SliderDefaults.colors(
//                        thumbColor = MaterialTheme.colorScheme.primary,
//                        activeTrackColor = MaterialTheme.colorScheme.primary,
//                        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
//                    ),
//                    steps = 7,
//                    valueRange = 20f..100f
//                )
//                Text(text = "Within ${sliderPosition.toInt()} miles")
//
//                user = user?.copy(
//                    distance = when (sliderPosition.toInt()) {
//                        10 -> Distance.TEN
//                        20 -> Distance.TWENTY
//                        30 -> Distance.THIRTY
//                        40 -> Distance.FORTY
//                        50 -> Distance.FIFTY
//                        60 -> Distance.SIXTY
//                        70 -> Distance.SEVENTY
//                        80 -> Distance.EIGHTY
//                        90 -> Distance.NINETY
//                        100 -> Distance.ONE_HUNDRED
//                        else -> Distance.TEN
//                    }
//                )
//            }


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
                                    acceptShotsExpanded = false
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
                                usersViewModel.updateUserField { currentUser ->
                                    currentUser.copy(
                                        showUsers = when (acceptShotsStoredOption) {
                                            "TEN" -> Distance.TEN
                                            "TWENTY" -> Distance.TWENTY
                                            "THIRTY" -> Distance.THIRTY
                                            "FORTY" -> Distance.FORTY
                                            "FIFTY" -> Distance.FIFTY
                                            "SIXTY" -> Distance.SIXTY
                                            "SEVENTY" -> Distance.SEVENTY
                                            "EIGHTY" -> Distance.EIGHTY
                                            "NINETY" -> Distance.NINETY
                                            "ONE_HUNDRED" -> Distance.ONE_HUNDRED
                                            "ANYWHERE" -> Distance.ANYWHERE
                                            else -> Distance.TEN
                                        }
                                    )
                                }

                                userData["acceptShots"] = acceptShotsStoredOption
                                Log.d("FilterScreen", "acceptShots - ${userData["acceptShots"]}")
                                user = user?.copy(
                                    showUsers = when (acceptShotsStoredOption) {
                                        "TEN" -> Distance.TEN
                                        "TWENTY" -> Distance.TWENTY
                                        "THIRTY" -> Distance.THIRTY
                                        "FORTY" -> Distance.FORTY
                                        "FIFTY" -> Distance.FIFTY
                                        "SIXTY" -> Distance.SIXTY
                                        "SEVENTY" -> Distance.SEVENTY
                                        "EIGHTY" -> Distance.EIGHTY
                                        "NINETY" -> Distance.NINETY
                                        "ONE_HUNDRED" -> Distance.ONE_HUNDRED
                                        "ANYWHERE" -> Distance.ANYWHERE
                                        else -> Distance.TEN
                                    }
                                )
                            }

                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Age",
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 16.sp
            )
            var ageSliderPosition by remember {
                mutableStateOf(
                    (user?.ageMinToShow?.toFloat() ?: 18f)..(user?.ageMaxToShow?.toFloat() ?: 35f)
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                RangeSlider(
                    value = ageSliderPosition,
                    onValueChange = { range -> ageSliderPosition = range },
                    valueRange = 18f..100f,
                    onValueChangeFinished = {
                        // launch some business logic update with the state you hold
                        // viewModel.updateSelectedSliderValue(ageSliderPosition)
                    }
                )
                Text(text = "${ageSliderPosition.start.toInt()} - ${ageSliderPosition.endInclusive.toInt()}")

                user = user?.copy(ageMinToShow = ageSliderPosition.start.toInt())
                user = user?.copy(ageMaxToShow = ageSliderPosition.endInclusive.toInt())
            }
        }
    }
}

@Composable
fun DistanceSlider() {
    var sliderPosition by remember { mutableFloatStateOf(20f) }
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            steps = 7,
            valueRange = 20f..100f
        )
        Text(text = "Within ${sliderPosition.toInt()} miles")
    }
}

@Composable
fun AgeRangeSlider() {
    var sliderPosition by remember { mutableStateOf(18f..35f) }
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        RangeSlider(
            value = sliderPosition,
            onValueChange = { range -> sliderPosition = range },
            valueRange = 18f..100f,
            onValueChangeFinished = {
                // launch some business logic update with the state you hold
                // viewModel.updateSelectedSliderValue(sliderPosition)
            },
        )
        Text(text = "${sliderPosition.start.toInt()} - ${sliderPosition.endInclusive.toInt()}")
    }
}