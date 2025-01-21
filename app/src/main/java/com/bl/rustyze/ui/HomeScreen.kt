package com.bl.rustyze.ui

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.bl.rustyze.MainActivity
import com.bl.rustyze.R
import com.bl.rustyze.ui.components.VehicleCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

private var mAuth: FirebaseAuth? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, firebaseAuth: FirebaseAuth) {
    var expanded by remember { mutableStateOf(false) }
    val vehiclesLastSeenData = remember { mutableStateOf(emptyList<Map<String, String>>()) }
    mAuth = FirebaseAuth.getInstance()
    val user: FirebaseUser? = mAuth!!.currentUser
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    if (user != null) {
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val vehicleIds = document.get("vehiclesLastSeen") as? List<String> ?: emptyList()
                    val vehicles = mutableListOf<Map<String, String>>()

                    vehicleIds.forEach { vehicleId ->
                        Log.i("Rustyze", "vehicleId: $vehicleId")
                        db.collection("vehicles").document(vehicleId).collection("comments").get()
                            .addOnSuccessListener { commentsQuerySnapshot ->
                                var totalStars = 0
                                var totalComments = 0

                                // compute the rusty meter percentage
                                for (commentDoc in commentsQuerySnapshot.documents) {
                                    Log.i("Rustyze", "commentDoc: $commentDoc")
                                    val stars = commentDoc.getLong("stars")?.toInt()
                                    if (stars != null && stars in 0..5) {
                                        totalStars += stars
                                        totalComments++
                                    }
                                }

                                val rustyMeterPercentage = if (totalComments > 0) {
                                    (totalStars * 100) / (totalComments * 5)
                                } else {
                                    0
                                }

                                vehicles.add(
                                    mapOf(
                                        "name" to vehicleId,
                                        "rustyMeter" to "$rustyMeterPercentage%"
                                    )
                                )
                                Log.i("Rustyze", "vehicles: $vehicles")
                                vehiclesLastSeenData.value = vehicles
                            }
                    }
                }
            }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rustyze") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("search")
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }

                    // Profile icon with dropdown menu
                    Box(
                        modifier = Modifier
                            .wrapContentSize(Alignment.TopEnd)
                    ) {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(Icons.Filled.Person, contentDescription = "Profile")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                onClick = {
                                    expanded = false
                                    navController.navigate("profile")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    expanded = false
                                    firebaseAuth.signOut()
                                    Toast.makeText(navController.context, "Logged out", Toast.LENGTH_SHORT).show()

                                    // Redirect to login screen
                                    val intent = Intent(navController.context, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(navController.context, intent, null)
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Home") },
                    label = { Text("Home") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Top Rated Section
            item {
                Text(
                    text = "Top Rated",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    content = {
                        LazyRow {
                            items(5) { index ->
                                TopRatedCard(
                                    rank = index + 1,
                                    imageRes = R.drawable.ic_launcher_foreground
                                )
                            }
                        }
                    }
                )
            }

            // Seen Recently Section
            item {
                Text(
                    text = "Seen Recently",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(vehiclesLastSeenData.value.takeLast(3).size) { index ->
                val vehicle = vehiclesLastSeenData.value.takeLast(3)[index]
                VehicleCard(
                    modifier = Modifier.clickable {
                        navController.navigate("details/${vehicle["name"]?.split(" ")?.get(0)}/${vehicle["name"]?.split(" ")?.get(1)}")
                    },
                    vehicleName = vehicle["name"] ?: "Unknown",
                    rustyMeter = vehicle["rustyMeter"] ?: "Unknown"
                )
            }
        }
    }
}

@Composable
fun TopRatedCard(rank: Int, imageRes: Int) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Vehicle Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = "$rank",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
        )
    }
}