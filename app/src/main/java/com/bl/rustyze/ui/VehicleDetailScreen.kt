package com.bl.rustyze.ui

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.bl.rustyze.MainActivity
import com.bl.rustyze.data.models.Vehicle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(vehicle: Vehicle, navController: NavController) {
    var expanded by remember { mutableStateOf(false) }
    var rustyMeterPercentage by remember { mutableStateOf(0) }
    var comments by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    var userComment by remember { mutableStateOf("") }
    var stars by remember { mutableStateOf(-1) }
    val mAuth = FirebaseAuth.getInstance()
    val user: FirebaseUser? = mAuth.currentUser
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    if (user != null) {
        // Add the vehicle to the user's last seen list
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val vehiclesLastSeen = document.get("vehiclesLastSeen") as? MutableList<String> ?: mutableListOf()
                    val vehicleName = "${vehicle.make} ${vehicle.model}"
                    if (!vehiclesLastSeen.contains(vehicleName)) {
                        vehiclesLastSeen.add(vehicleName)
                        val userData: MutableMap<String, Any> = HashMap()
                        userData["vehiclesLastSeen"] = vehiclesLastSeen
                        db.collection("users").document(user.uid).set(userData)
                    }
                }
            }
    }
    // Fetch comments and compute Rusty Meter
    db.collection("vehicles").document("${vehicle.make} ${vehicle.model}").collection("comments").get()
        .addOnSuccessListener { commentsQuerySnapshot ->
            var totalStars = 0
            var totalComments = 0
            val fetchedComments = mutableListOf<Map<String, Any>>()

            for (commentDoc in commentsQuerySnapshot.documents) {
                val commentData = commentDoc.data
                if (commentData != null) {
                    fetchedComments.add(commentData)
                    val stars = commentData["stars"] as? Long
                    if (stars != null && stars in 1..5) {
                        totalStars += stars.toInt()
                        totalComments++
                    }
                }
            }

            comments = fetchedComments
            rustyMeterPercentage = if (totalComments > 0) {
                (totalStars * 100) / (totalComments * 5)
            } else {
                0
            }
        }

    Scaffold(
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "${vehicle.make} ${vehicle.model}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Vehicle Details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VehicleDetailItem(
                        icon = Icons.Default.CheckCircle,
                        label = "Year",
                        value = vehicle.year.toString()
                    )
                    VehicleDetailItem(
                        icon = Icons.Default.Done,
                        label = "Fuel Type",
                        value = vehicle.fuelType ?: "Unknown"
                    )
                    VehicleDetailItem(
                        icon = Icons.Default.Lock,
                        label = "Cylinders",
                        value = vehicle.cylinders?.toString() ?: "N/A"
                    )
                    VehicleDetailItem(
                        icon = Icons.Default.Settings,
                        label = "Transmission",
                        value = vehicle.trany ?: "N/A"
                    )
                    VehicleDetailItem(
                        icon = Icons.Default.Call,
                        label = "Combined MPG",
                        value = vehicle.comb08?.toString() ?: "N/A"
                    )
                }
            }

            // Rusty Meter
            Text(
                text = "Rusty Meter: $rustyMeterPercentage%",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            // Comments Section
            Text(
                text = "Comments",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight(0.6f)
                    .padding(vertical = 8.dp)
            ) {
                items(comments) { comment ->
                    val author = comment["author"] as? String ?: "Anonymous"
                    val content = comment["content"] as? String ?: ""
                    val stars = comment["stars"] as? Long ?: 0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Author: $author",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Rating: $stars ★",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(text = content, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Add a comment section
            if (user != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {

                    OutlinedTextField(
                        value = userComment,
                        onValueChange = { userComment = it },
                        label = { Text("Your Comment") },
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .wrapContentSize(Alignment.TopEnd)
                    ) {

                        // Button to select the number of stars
                        IconButton(onClick = { expanded = !expanded }) {
                            if (stars in 0..5) {
                                Text(
                                    text = stars.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(Icons.Filled.Star, contentDescription = "Profile")
                            }
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            (0..5).forEach { number ->
                                DropdownMenuItem(
                                    text = { Text(number.toString()) },
                                    onClick = {
                                        stars = number
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }


                    // Button send comment
                    IconButton(
                        onClick = {
                            if (stars in 0..5 && userComment.isNotBlank()) {
                                val commentData = mapOf(
                                    "author" to user.email,
                                    "content" to userComment,
                                    "stars" to stars
                                )
                                db.collection("vehicles")
                                    .document("${vehicle.make} ${vehicle.model}")
                                    .collection("comments").add(commentData)
                                    .addOnSuccessListener {
                                        Log.i("Rustyze", "Comment added successfully")
                                        userComment = ""
                                        stars = 0
                                    }
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Comment",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun VehicleDetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
