package com.bl.rustyze.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bl.rustyze.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// Data class for user comments
data class UserComment(
    val author: String = "",
    val content: String = "",
    val stars: Int = 0,
    val vehicleId: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(navController: NavController, firebaseAuth: FirebaseAuth) {
    val user: FirebaseUser? = firebaseAuth.currentUser
    val firestore = FirebaseFirestore.getInstance()

    // State for user comments
    var userComments by remember { mutableStateOf<List<UserComment>>(emptyList()) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<UserComment?>(null) }

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
                    Log.e("CommentsScreen", "Error retrieving comments: ${e.message}")
                }
        }
    }

    // Function to delete a comment from Firestore
    fun deleteComment(comment: UserComment) {
        val updatedVehicleData: MutableMap<String, Any> = HashMap()

        Log.i("CommentsScreen", "Deleting comment: $comment")
        updatedVehicleData["comments"] = FieldValue.arrayRemove(comment)
        userComments = userComments.filter { it != comment }
        updatedVehicleData["comments"] = userComments

        firestore.collection("vehicles")
            .document(comment.vehicleId)
            .set(updatedVehicleData)
            .addOnSuccessListener {
                Log.d("CommentsScreen", "Comment deleted successfully")
                userComments = userComments.filter { it != comment }
                Toast.makeText(navController.context, "Comment deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("CommentsScreen", "Error deleting comment: ${e.message}")
                Toast.makeText(navController.context, "Failed to delete comment", Toast.LENGTH_SHORT).show()
            }
    }

    // Confirmation dialog for deleting a comment
    if (showConfirmationDialog && commentToDelete != null) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text(stringResource(id = R.string.confirmDeletionTitle)) },
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
        topBar = {
            TopAppBar(
                title = { Text("Your Comments") },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Home") },
                    label = { Text(stringResource(id = R.string.navHome)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("search") },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text(stringResource(id = R.string.navSearch)) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate("comments") },
                    icon = { Icon(Icons.Default.Email, contentDescription = "my comments") },
                    label = { Text(stringResource(id = R.string.navComments)) }
                )
            }
        },
        content = { padding ->
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(userComments) { comment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
    )
}
