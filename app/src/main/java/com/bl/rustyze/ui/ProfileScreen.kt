package com.bl.rustyze.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.bl.rustyze.MainActivity
import com.bl.rustyze.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

// Data class for user comments
data class UserComment(
    val author: String = "",
    val content: String = "",
    val stars: Int = 0,
    val vehicleId: String = ""
)


fun updateLocale(context: Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}

@Composable
fun ProfileScreen(navController: NavController, firebaseAuth: FirebaseAuth) {
    val user: FirebaseUser? = firebaseAuth.currentUser
    val languageOptions = listOf("English" to "en", "Français" to "fr", "Greek" to "el")
    val selectedLanguage = remember { mutableStateOf(languageOptions[0]) }
    var expanded by remember { mutableStateOf(false) }

    // State for user comments
    var userComments by remember { mutableStateOf<List<UserComment>>(emptyList()) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<UserComment?>(null) }

    val firestore = FirebaseFirestore.getInstance()

    // Load comments from Firestore
    LaunchedEffect(user) {
        if (user != null) {
            val fetchedComments = mutableListOf<UserComment>()
            firestore.collection("vehicles")
                .get()
                .addOnSuccessListener { vehiclesQuerySnapshot ->
                    for (vehicleDoc in vehiclesQuerySnapshot.documents) {
                        val vehicleId = vehicleDoc.id
                        // Retrieve comments of this vehicle
                        val comments = vehicleDoc.get("comments") as? List<Map<String, Any>> ?: emptyList()

                        // Filter comments written by the current user
                        comments.forEach { commentData ->
                            val author = commentData["author"] as? String
                            if (author == user.email) {
                                val content = commentData["content"] as? String ?: ""
                                val stars = (commentData["stars"] as? Long)?.toInt() ?: 0
                                fetchedComments.add(UserComment(author = author ?: "", content = content, stars = stars, vehicleId = vehicleId))
                            }
                        }
                    }
                    userComments = fetchedComments
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileScreen", "Error retrieving comments: ${e.message}")
                }
        }
    }

    // Function to delete a comment from Firestore
    fun deleteComment(comment: UserComment) {
        val updatedVehicleData: MutableMap<String, Any> = HashMap()

        Log.i("ProfileScreen", "Deleting comment: $comment")
        updatedVehicleData["comments"] = FieldValue.arrayRemove(comment)
        userComments = userComments.filter { it != comment }
        updatedVehicleData["comments"] = userComments

        firestore.collection("vehicles")
            .document(comment.vehicleId)
            .set(updatedVehicleData)
            .addOnSuccessListener {
                Log.d("ProfileScreen", "Comment deleted successfully")
                userComments = userComments.filter { it != comment }
                Toast.makeText(navController.context, "Comment deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileScreen", "Error deleting comment: ${e.message}")
                Toast.makeText(navController.context, "Failed to delete comment", Toast.LENGTH_SHORT).show()
            }
    }

    // Confirmation dialog for deleting a comment
    if (showConfirmationDialog && commentToDelete != null) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text(stringResource(id = R.string.confirmDeletionTitle))},
            text = { Text(stringResource(id = R.string.confirmDeletionMessage)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteComment(commentToDelete!!)
                        showConfirmationDialog = false
                    }
                ) {
                    Text(stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text(stringResource(id = R.string.no))
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Home") },
                    label = { BasicText(stringResource(id = R.string.navHome)) }
                )
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Profile Picture
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // User Info
                Text(
                    text = user?.displayName ?: "Guest",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user?.email ?: "Email not available",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Language Picker
                Box(
                    modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                ) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.Place, contentDescription = "Language Icon")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        languageOptions.forEach { (label, code) ->
                            DropdownMenuItem(
                                text = { Text(text = label) },
                                onClick = {
                                    expanded = false
                                    selectedLanguage.value = label to code
                                    updateLocale(navController.context, code)
                                    Toast.makeText(
                                        navController.context,
                                        "Language changed to $label",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Logout Button
                Button(
                    onClick = {
                        firebaseAuth.signOut()
                        Toast.makeText(
                            navController.context,
                            "Logged out successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(navController.context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(navController.context, intent, null)
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout Icon",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.logout), color = Color.White)
                }

                Spacer(modifier = Modifier.height(32.dp))
                // Display user comments
                Text(
                    text = "Your Comments",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start).padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    items(userComments) { comment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Vehicle: ${comment.vehicleId}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = comment.content,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Delete Button
                                    IconButton(
                                        onClick = {
                                            commentToDelete = comment
                                            showConfirmationDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Comment")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

