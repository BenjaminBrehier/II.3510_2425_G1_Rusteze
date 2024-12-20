package com.bl.rustyze

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (firebaseAuth.currentUser != null) {
                        // User is already logged in
                        WelcomeScreen(
                            name = firebaseAuth.currentUser?.email ?: "User",
                            onLogout = {
                                firebaseAuth.signOut()
                                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        LoginScreen(
                            onAuthSuccess = {
                                val user = firebaseAuth.currentUser
                                Toast.makeText(this, "Welcome ${user?.email}", Toast.LENGTH_SHORT).show()
                                // Redirect to MainActivity after successful login
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

@Composable
fun WelcomeScreen(name: String, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome, $name!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Logout")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    RustyzeTheme {
        WelcomeScreen(
            name = "User",
            onLogout = {}
        )
    }
}
