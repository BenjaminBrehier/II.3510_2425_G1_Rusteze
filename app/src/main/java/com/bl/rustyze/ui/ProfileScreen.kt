package com.bl.rustyze.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
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
import java.util.Locale


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
                    selected = false,
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
            }
        }
    )
}


