package com.bl.rustyze.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.bl.rustyze.data.models.Vehicle
import com.bl.rustyze.data.models.parseVehicles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI

@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var suggestions by remember { mutableStateOf(listOf<Vehicle>()) }
    var apiList by remember { mutableStateOf(listOf<Vehicle>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Lancer la requête réseau dans une coroutine
    LaunchedEffect(Unit) {
        val apiUrl = "https://public.opendatasoft.com/api/explore/v2.1/catalog/datasets/all-vehicles-model/records?limit=50"
        try {
            println("Connecting to API: $apiUrl")
            val response = withContext(Dispatchers.IO) {
                val url = URI.create(apiUrl).toURL()
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                println("Response Code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    reader.useLines { lines -> lines.forEach { response.append(it) } }
                    reader.close()
                    connection.disconnect()
                    response.toString()
                } else {
                    connection.disconnect()
                    throw Exception("HTTP $responseCode: Unable to fetch data")
                }
            }

            // Traiter la réponse JSON
            apiList = parseVehicles(response)
            suggestions = apiList
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = e.localizedMessage ?: "Unknown error"
        }
    }

    // Filtrer les suggestions en fonction de la recherche
    LaunchedEffect(query.text) {
        suggestions = apiList.filter {
            it.make.contains(query.text, ignoreCase = true) ||
                    it.model.contains(query.text, ignoreCase = true)
        }
    }

    // UI
    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search for a vehicle") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color.Red,
                modifier = Modifier.padding(8.dp)
            )
        } else if (apiList.isEmpty()) {
            Text(
                text = "Loading vehicles...",
                modifier = Modifier.padding(8.dp)
            )
        } else {
            LazyColumn {
                items(suggestions.size) { index ->
                    val vehicle = suggestions[index]
                    Text(
                        text = "${vehicle.make} ${vehicle.model}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                println("Selected vehicle: ${vehicle.make} ${vehicle.model}")
                            }
                    )
                }
            }
        }
    }
}


