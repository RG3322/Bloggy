package com.example.bloggy.screens // FIXME: Adjust to your project's package name

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bloggy.ui.theme.BloggyTheme // FIXME: Adjust to your Theme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

const val TAG_FIREBASE_SIGNUP = "FirebaseSignUpScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseSignUpScreen(
    onUserRegistered: (AuthResult) -> Unit, // Callback on successful registration
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val auth: FirebaseAuth = Firebase.auth
    val coroutineScope = rememberCoroutineScope()

    // --- Google Sign-In ---
    val oneTapClient = remember { Identity.getSignInClient(context) }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val googleIdToken = credential.googleIdToken
                if (googleIdToken != null) {
                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                            val authResult = auth.signInWithCredential(firebaseCredential).await()
                            Log.d(TAG_FIREBASE_SIGNUP, "Google Sign-In successful: ${authResult.user?.displayName}")
                            onUserRegistered(authResult)
                        } catch (e: Exception) {
                            Log.e(TAG_FIREBASE_SIGNUP, "Google Sign-In Firebase Error", e)
                            errorMessage = "Google Sign-In failed: ${e.localizedMessage}"
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    errorMessage = "Google Sign-In failed: No ID token found."
                    Log.w(TAG_FIREBASE_SIGNUP, "Google Sign-In: No ID token!")
                }
            } catch (e: ApiException) {
                Log.e(TAG_FIREBASE_SIGNUP, "Google Sign-In API Error", e)
                errorMessage = "Google Sign-In failed: ${e.localizedMessage}"
            }
        } else {
            Log.d(TAG_FIREBASE_SIGNUP, "Google Sign-In cancelled or failed. Result code: ${result.resultCode}")
            // errorMessage = "Google Sign-In was cancelled." // Optional: show message
        }
    }

    fun beginGoogleSignIn() {
        isLoading = true
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(com.example.bloggy.R.string.default_web_client_id)) // FIXME: R.string.default_web_client_id
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false) // Or true if you want to try auto sign-in
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    googleSignInLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    Log.e(TAG_FIREBASE_SIGNUP, "Google Sign-In launch error", e)
                    errorMessage = "Could not start Google Sign-In: ${e.localizedMessage}"
                } finally {
                    //isLoading = false //isLoading is handled in the launcher result
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG_FIREBASE_SIGNUP, "Google Sign-In begin failed", e)
                errorMessage = "Google Sign-In setup failed: ${e.localizedMessage}"
                isLoading = false
            }
    }
    // --- End Google Sign-In ---


    fun registerWithEmailPassword() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password cannot be empty."
            return
        }
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                Log.d(TAG_FIREBASE_SIGNUP, "Email/Pass Registration successful: ${authResult.user?.email}")
                onUserRegistered(authResult)
            } catch (e: Exception) {
                Log.e(TAG_FIREBASE_SIGNUP, "Email/Pass Registration Error", e)
                errorMessage = e.localizedMessage ?: "Registration failed."
            } finally {
                isLoading = false
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bloggy - Sign Up", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (min. 6 characters)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = { registerWithEmailPassword() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Register with Email")
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton( // Or just Button, depends on your styling preference
            onClick = { beginGoogleSignIn() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            // Add Google Icon (Optional, requires material-icons-extended dependency)
            // Icon(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = "Google logo", modifier = Modifier.size(20.dp))
            // Spacer(Modifier.width(8.dp))
            Text("Sign up with Google")
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = { if (!isLoading) onNavigateToLogin() }) {
            Text("Already a user? Sign In")
        }
    }
}

// You need to have R.string.default_web_client_id defined in your strings.xml
// This value comes from your google-services.json file, specifically the "client_id"
// of type 3 under "client" where "client_type" is 3.
// Example strings.xml entry:
// <string name="default_web_client_id" translatable="false">YOUR_WEB_CLIENT_ID_HERE</string>

@Preview(showBackground = true)
@Composable
fun FirebaseSignUpScreenPreview() {
    BloggyTheme { // FIXME: Your theme
        FirebaseSignUpScreen(
            onUserRegistered = { Log.d("Preview", "User registered: ${it.user?.email}") },
            onNavigateToLogin = { Log.d("Preview", "Navigate to Login") }
        )
    }
}