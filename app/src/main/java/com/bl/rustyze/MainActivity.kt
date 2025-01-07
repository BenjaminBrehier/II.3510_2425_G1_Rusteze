package com.bl.rustyze

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bl.rustyze.ui.HomeScreen
import com.bl.rustyze.ui.SearchScreen
import com.bl.rustyze.ui.theme.RustyzeTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        setContent {
            RustyzeTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (firebaseAuth.currentUser != null) {
                        // User is already logged in
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") {
                                HomeScreen(navController)
                            }
                            composable("search") {
                                SearchScreen()
                            }
                        }
                    } else {
                        LoginScreen(
                            onAuthSuccess = {
                                val user = firebaseAuth.currentUser
                                Toast.makeText(this, "Welcome ${user?.email}", Toast.LENGTH_SHORT).show()
                                // Redirect to com.bl.rustyze.MainActivity after successful login
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
