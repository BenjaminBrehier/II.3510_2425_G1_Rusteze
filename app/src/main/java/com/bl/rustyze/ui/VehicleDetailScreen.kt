package com.bl.rustyze.ui

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.bl.rustyze.MainActivity
import com.bl.rustyze.R
import com.bl.rustyze.data.models.Vehicle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun VehicleDetailScreen(vehicle: Vehicle, navController: NavController) {
    var expanded by remember { mutableStateOf(false) }
    var rustyMeterPercentage by remember { mutableStateOf(0) }
    var userComment by remember { mutableStateOf("") }
    val comments = remember { mutableStateOf(mutableListOf<Map<String, *>>()) }
    var stars by remember { mutableStateOf(-1) }
    val mAuth = FirebaseAuth.getInstance()
    val user: FirebaseUser? = mAuth.currentUser
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var listenerRegistration: ListenerRegistration? = null

    if (user != null) {
    // Add the vehicle to the user's last seen list
    db.collection("users").document(user.uid).get()
        .addOnSuccessListener { document ->
            if (document != null) {
                val vehiclesLastSeen = document.get("vehiclesLastSeen") as? MutableList<String> ?: mutableListOf()
                val vehicleName = "${vehicle.make} ${vehicle.model}"
                if (!vehiclesLastSeen.contains(vehicleName)) {
                    if (vehiclesLastSeen.size >= 3) {
                        vehiclesLastSeen.removeAt(0)
                    }
                    vehiclesLastSeen.add(vehicleName)
                    val userData: MutableMap<String, Any> = HashMap()
                    userData["vehiclesLastSeen"] = vehiclesLastSeen
                    db.collection("users").document(user.uid).set(userData)
                }
            }
        }
    }

    val vehicleDocRef = db.collection("vehicles").document("${vehicle.make} ${vehicle.model}")
    vehicleDocRef.get().addOnSuccessListener { document ->
        if (document != null) {
            // Récupère la liste existante de commentaires
            val fetchedComments =
                document.get("comments") as? MutableList<Map<String, *>> ?: mutableListOf()
            var totalStars = 0
            var totalComments = 0
            for (comment in fetchedComments) {
                val commentStars = comment["stars"] as? Long
                if (commentStars != null && commentStars in 0..5) {
                    totalStars += commentStars.toInt()
                    totalComments++
                }
            }
            comments.value = fetchedComments
            rustyMeterPercentage = if (totalComments > 0) {
                (totalStars * 100) / (totalComments * 5)
            } else {
                0
            }
        }
    }

    val shareMessage = "${stringResource(R.string.share)} : ${vehicle.make} ${vehicle.model}\n" +
            "${stringResource(R.string.specsYear)}  : ${vehicle.year}\n" +
            "${stringResource(R.string.specsFuel)}  : ${vehicle.fuelType ?: "N/A"}\n" +
            "${stringResource(R.string.specsTransmission)}  : ${vehicle.trany ?: "N/A"}\n\n" +
            "${stringResource(R.string.rustyMeter)}  : $rustyMeterPercentage%\n\n" +
            "${stringResource(R.string.shareVisit)}\n"

    val shareBy = stringResource(R.string.shareBy)

    fun shareVehicleDetails(context: Context) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                shareMessage
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, shareBy))
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "comment_channel",
                "Comment Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new comments"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(comment: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification: Notification = Notification.Builder(context, "comment_channel")
            .setContentTitle("New Comment on ${vehicle.make} ${vehicle.model}")
            .setContentText(comment)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        notificationManager.notify(1, notification)
    }

    fun listenForComments() {
        listenerRegistration?.remove()
        listenerRegistration = db.collection("vehicles")
            .document("${vehicle.make} ${vehicle.model}")
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Log.e("Rustyze", "Error listening for comments: ", error)
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val newComments =
                        documentSnapshot.get("comments") as? List<Map<String, *>> ?: emptyList()

                    // Compare new comments with the current state
                    if (newComments.size > comments.value.size) {
                        val newComment = newComments.lastOrNull()
                        val content = newComment?.get("content") as? String ?: "New comment added"
                        sendNotification(content)
                    }

                    comments.value = newComments.toMutableList()
                }
            }
    }

    coroutineScope.launch {
        createNotificationChannel()
        listenForComments()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "${vehicle.make} ${vehicle.model}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                ) },
                actions = {
                    IconButton(
                        onClick = {
                            shareVehicleDetails(context)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Partager le véhicule",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
                    label = { Text(stringResource(id = R.string.navHome)) }
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

            Spacer(modifier = Modifier.height(50.dp))
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
                        label = stringResource(id = R.string.specsYear),
                        value = vehicle.year.toString()
                    )
                    VehicleDetailItem(
                        icon = Icons.Default.Done,
                        label = stringResource(id = R.string.specsFuel),
                        value = vehicle.fuelType ?: "Unknown"
                    )
                    VehicleDetailItem(
                        icon = Icons.Default.Lock,
                        label = stringResource(id = R.string.specsCylinders),
                        value = vehicle.cylinders?.toString() ?: "N/A"
                    )
                    VehicleDetailItem(
                        icon = Icons.Default.Settings,
                        label = stringResource(id = R.string.specsTransmission),
                        value = vehicle.trany ?: "N/A"
                    )
                    VehicleDetailItem(
                        icon = Icons.Default.Call,
                        label = stringResource(id = R.string.specsCombined),
                        value = vehicle.comb08?.toString() ?: "N/A"
                    )
                }
            }

            // Rusty Meter
            Text(
                text = stringResource(id = R.string.rustyMeter) +": $rustyMeterPercentage%",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            // Comments Section
            Text(
                text = stringResource(id = R.string.comments),
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
                items(comments.value) { comment ->
                    val author = comment["author"] as? String ?: "Anonymous"
                    val content = comment["content"] as? String ?: ""
                    val commentStars = comment["stars"] as? Long ?: 0

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
                                text = stringResource(id = R.string.author) + " : $author",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = stringResource(id = R.string.rating) + " : $commentStars ★",
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
                        label = { Text(stringResource(id = R.string.yourComment)) },
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
                                val vehicleDocRef = db.collection("vehicles").document("${vehicle.make} ${vehicle.model}")

                                vehicleDocRef.get().addOnSuccessListener { document ->
                                    if (document != null) {
                                        val existingComments = document.get("comments") as? MutableList<Map<String, *>> ?: mutableListOf()

                                        existingComments.add(commentData)
                                        Log.i("Rustyze", "Existing comments: $existingComments")
                                        Log.i("Rustyze", "New comment: $commentData")

                                        val updatedVehicleData: MutableMap<String, Any> = HashMap()
                                        updatedVehicleData["comments"] = existingComments

                                        vehicleDocRef.set(updatedVehicleData)
                                            .addOnSuccessListener {
                                                Log.i("Rustyze", "Comment added successfully")
                                                comments.value = existingComments
                                                userComment = ""
                                                stars = 0
                                            }
                                            .addOnFailureListener { exception ->
                                                Log.e("Rustyze", "Error adding comment: ", exception)
                                            }
                                    } else {
                                        val newVehicleData = mapOf(
                                            "comments" to listOf(commentData)
                                        )
                                        vehicleDocRef.set(newVehicleData)
                                            .addOnSuccessListener {
                                                Log.i("Rustyze", "Document created and comment added successfully")
                                                comments.value =
                                                    listOf(commentData).toMutableList()
                                                userComment = ""
                                                stars = 0
                                            }
                                            .addOnFailureListener { exception ->
                                                Log.e("Rustyze", "Error creating document and adding comment: ", exception)
                                            }
                                    }
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
