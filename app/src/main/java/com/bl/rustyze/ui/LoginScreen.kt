package com.bl.rustyze.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bl.rustyze.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider

@Composable
fun LoginScreen(
    onAuthSuccess: () -> Unit,
    onAuthFailure: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) } // Toggle between login and register
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    // Fonction pour connecter avec Firebase en utilisant le compte Google
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onAuthSuccess()
                } else {
                    onAuthFailure("Google authentication failed")
                }
            }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                onAuthFailure("Google sign-in failed: ${e.message}")
            }
        }
    )

    fun startGoogleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)) // Utilisez votre client ID Firebase
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    // Fonction pour connecter avec Firebase en utilisant Twitter (X)
    fun startTwitterSignIn() {
        val provider = OAuthProvider.newBuilder("twitter.com")
        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null) {
            pendingResultTask
                .addOnSuccessListener { result ->
                    onAuthSuccess()
                }
                .addOnFailureListener { exception ->
                    onAuthFailure("Twitter authentication failed: ${exception.message}")
                }
        } else {
            auth.startActivityForSignInWithProvider(context as Activity, provider.build())
                .addOnSuccessListener { result ->
                    onAuthSuccess()
                }
                .addOnFailureListener { exception ->
                    onAuthFailure("Twitter authentication failed: ${exception.message}")
                }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLogin) "Login" else "Register",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isLogin) {
                    // Login with email/password
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onAuthSuccess()
                            } else {
                                onAuthFailure(task.exception?.message ?: "Unknown error")
                            }
                        }
                } else {
                    // Register with email/password
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onAuthSuccess()
                            } else {
                                onAuthFailure(task.exception?.message ?: "Unknown error")
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isLogin) "Login" else "Register")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { isLogin = !isLogin },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isLogin) "Don't have an account? Register" else "Already have an account? Login"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SocialLoginButton(
            text = "Login with Google",
            backgroundColor = Color(0xFF3F85F4),
            icon = ImageVector.vectorResource(id = R.drawable.ic_google),
            onClick = { startGoogleSignIn() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SocialLoginButton(
            text = "Login with X",
            backgroundColor = Color.Black,
            icon = ImageVector.vectorResource(id = R.drawable.ic_x),
            onClick = { startTwitterSignIn()}
        )
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    backgroundColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}