package com.example.shots.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderSearchScreen(navController: NavController) {
    val genderList = listOf(
        "Abinary",
        "Agender",
        "Ambigender",
        "Androgyne",
        "Androgynous",
        "Aporagender",
        "Autigender",
        "Bakla",
        "Bigender",
        "Binary",
        "Bissu",
        "Butch",
        "Calabai",
        "Calalai",
        "Cis",
        "Cisgender",
        "Cis female",
        "Cis male",
        "Cis man",
        "Cis woman",
        "Demi-boy",
        "Demiflux",
        "Demigender",
        "Demi-girl",
        "Demi-guy",
        "Demi-man",
        "Demi-woman",
        "Dual gender",
        "Faʻafafine",
        "Female",
        "Female to male",
        "Femme",
        "FTM",
        "Gender bender",
        "Gender diverse",
        "Gender gifted",
        "Genderfae",
        "Genderfluid",
        "Genderflux",
        "Genderfuck",
        "Genderless",
        "Gender nonconforming",
        "Genderqueer",
        "Gender questioning",
        "Gender variant",
        "Graygender",
        "Hijra",
        "Intergender",
        "Intersex",
        "Kathoey",
        "Māhū",
        "Male",
        "Male to female",
        "Man",
        "Man of trans experience",
        "Maverique",
        "Meta-gender",
        "MTF",
        "Multigender",
        "Muxe",
        "Neither",
        "Neurogender",
        "Neutrois",
        "Non-binary",
        "Non-binary transgender",
        "Omnigender",
        "Other",
        "Pangender",
        "Person of transgendered experience",
        "Polygender",
        "Sekhet",
        "Third gender",
        "Trans",
        "Trans*",
        "Trans female",
        "Trans male",
        "Trans man",
        "Trans person",
        "Trans woman",
        "Transgender",
        "Transgender female",
        "Transgender male",
        "Transgender man",
        "Transgender person",
        "Transgender woman",
        "Transfeminine",
        "Transmasculine",
        "Transsexual",
        "Transsexual female",
        "Transsexual male",
        "Transsexual man",
        "Transsexual person",
        "Transsexual woman",
        "Travesti",
        "Trigender",
        "Tumtum",
        "Two spirit",
        "Vakasalewalewa",
        "Waria",
        "Winkte",
        "Woman",
        "Woman of trans experience",
        "X-gender",
        "X-jendā",
        "Xenogender"
    )
    var selectedItem by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box() {
                        Box() {
                            TextField(
                                value = value,
                                onValueChange = { newValue ->
                                    value = newValue
                                },
                                placeholder = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Search,
                                            contentDescription = "Search Icon"
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Search Gender",
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .height(54.dp) // Adjust the height as needed
                                    .fillMaxWidth(), // Ensure it fills the available width
                                textStyle = TextStyle(fontSize = 12.sp)
                            )
                        }
                        // TextField should be here if needed
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back Icon")
                    }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            var text by remember { mutableStateOf("") }
            var expanded by remember { mutableStateOf(false) }
            val filteredSuggestions = remember {
                mutableStateListOf<String>()
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (value.isNotEmpty()) {
                    filteredSuggestions.clear()
                    filteredSuggestions.addAll(
                        genderList.filter { suggestion ->
                            suggestion.contains(
                                value,
                                ignoreCase = true
                            )
                        }
                    )
                    expanded = true // Expand dropdown when text is typed
                }
                filteredSuggestions.forEach { suggestion ->
                    item {
                        Box(
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("editProfile/$suggestion")
                                }
                                .padding(16.dp)
                        ) {
                            Text(text = suggestion)
                            Spacer(modifier = Modifier.height(8.dp)) // Add additional space after each item
                            HorizontalDivider() // Optionally add a divider between items
                        }
                    }
                }
            }


        }
    }
}

@Composable
fun AutocompleteTextFieldWithSearch(
    suggestions: List<String>,
    onItemSelected: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val filteredSuggestions = remember {
        mutableStateListOf<String>()
    }


    Box {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text field for search input
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                        if (it.isNotEmpty()) {
                            filteredSuggestions.clear()
                            filteredSuggestions.addAll(
                                suggestions.filter { suggestion ->
                                    suggestion.contains(
                                        it,
                                        ignoreCase = true
                                    )
                                }
                            )
                            expanded = true // Expand dropdown when text is typed
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {

                        Row {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.clickable { expanded = true }
                            )
                            Text("Search gender...")
                        }
                    },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            text = ""
                            expanded = false // Collapse dropdown when clear icon is clicked
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                )
            }

            // Autocomplete dropdown menu
            if (expanded) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        filteredSuggestions.forEach { suggestion ->
                            Text(
                                text = suggestion,
                                modifier = Modifier
                                    .clickable {
                                        text = suggestion
                                        expanded = false // Collapse dropdown when item is clicked
                                        onItemSelected(suggestion)
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun GenderSearchScreenPreview() {
    GenderSearchScreen(navController = rememberNavController())
}
