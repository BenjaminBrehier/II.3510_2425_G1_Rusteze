package com.bl.rustyze

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bl.rustyze.data.models.Vehicle
import com.bl.rustyze.data.models.parseVehicles
import com.bl.rustyze.ui.HomeScreen
import com.bl.rustyze.ui.LoginScreen
import com.bl.rustyze.ui.ProfileScreen
import com.bl.rustyze.ui.SearchScreen
import com.bl.rustyze.ui.VehicleDetailScreen
import com.bl.rustyze.ui.theme.RustyzeTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI

class MainActivity : ComponentActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        firebaseAuth = FirebaseAuth.getInstance()

        setContent {
            RustyzeTheme {
                val navController = rememberNavController()
                var apiList by remember { mutableStateOf(listOf<Vehicle>()) }

                // Récupération des données si la liste est vide
                LaunchedEffect(Unit) {
                    if (apiList.isEmpty()) {
                        val apiUrl = "https://public.opendatasoft.com/api/explore/v2.1/catalog/datasets/all-vehicles-model/records?limit=100"
                        try {
                            val response = withContext(Dispatchers.IO) {
                                val url = URI.create(apiUrl).toURL()
                                val connection = url.openConnection() as HttpURLConnection
                                connection.requestMethod = "GET"

                                val responseCode = connection.responseCode
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

                        val vehicles = parseVehicles(response)
                            .filter { it.make.split(" ").size == 1 && it.model.split(" ").size == 1 }
                            .distinctBy { it.make to it.model }
                        apiList = vehicles
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }


                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (firebaseAuth.currentUser != null) {
                        // User is already logged in
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") {
                                HomeScreen(navController, firebaseAuth)
                            }
                            composable("search") {
                                SearchScreen(navController, apiList)
                            }
                            composable("profile") {
                                ProfileScreen(navController, firebaseAuth)
                            }
                            composable(
                                "details/{make}/{model}",
                                arguments = listOf(
                                    navArgument("make") { type = NavType.StringType },
                                    navArgument("model") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val make = backStackEntry.arguments?.getString("make") ?: ""
                                val model = backStackEntry.arguments?.getString("model") ?: ""
                                Log.i("MainActivity", "Vehicle: $make $model")
                                // Rechercher le véhicule correspondant dans apiList
                                val vehicle = apiList.find { it.make == make && it.model == model }

                                if (vehicle != null) {
                                    VehicleDetailScreen(vehicle, navController)
                                } else {
                                    Text(text = "Vehicle not found", color = Color.Red)
                                }
                            }
                        }
                    } else {
                        LoginScreen(
                            onAuthSuccess = {
                                val user = firebaseAuth.currentUser
                                Toast.makeText(this, "Welcome ${user?.email}", Toast.LENGTH_SHORT).show()
                                // Redirect after successful login
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            },
                            onAuthFailure = { errorMessage ->
                                Toast.makeText(this, "Authentication Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
