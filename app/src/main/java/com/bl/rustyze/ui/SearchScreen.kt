package com.bl.rustyze.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var suggestions by remember { mutableStateOf(listOf<String>()) }

    // A remplacer par une liste de véhicules
    val allVehicles = listOf("Peugeot 1007", "Moto Guzzi V7", "Seat Leon", "Audi A3", "BMW X5", "Tesla Model S")

    // Filtrer en fonction de la recherche
    LaunchedEffect(query.text) {
        suggestions = allVehicles.filter { it.contains(query.text, ignoreCase = true) }
    }

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
                Text(
                    text = suggestions[index],
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {

                        }
                )
            }
        }
    }
}
