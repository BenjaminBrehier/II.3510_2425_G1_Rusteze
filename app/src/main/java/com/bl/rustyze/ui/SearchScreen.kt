package com.bl.rustyze.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bl.rustyze.R
import com.bl.rustyze.data.models.Vehicle
import com.bl.rustyze.data.models.parseVehicles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SearchScreen(
    navController: NavController,
    apiList: List<Vehicle>,
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var suggestions by remember { mutableStateOf(apiList) }

    // Filtrer les suggestions
    LaunchedEffect(query.text) {
        suggestions = if (query.text.isEmpty()) apiList
        else apiList.filter {
            it.make.contains(query.text, ignoreCase = true) ||
                    it.model.contains(query.text, ignoreCase = true)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Home") },
                    label = { Text(stringResource(id = R.string.navHome)) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate("search") },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text(stringResource(id = R.string.navSearch)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("comments") },
                    icon = { Icon(Icons.Default.Email, contentDescription = "my comments") },
                    label = { Text(stringResource(id = R.string.navComments)) }
                )
            }
        },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search for a vehicle") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(suggestions.size) { index ->
                    val vehicle = suggestions[index]
                    Text(
                        text = "${vehicle.make} ${vehicle.model}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                navController.navigate("details/${vehicle.make}/${vehicle.model}")
                            }
                    )
                }
            }
        }
    }
}
