package com.example.bloggy //

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bloggy.ui.theme.BloggyTheme // FIXME: Adjust to your Theme file location

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClicked: (email: String, password: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    // Optional: Pass an error message from a ViewModel if login fails server-side
    // serverLoginError: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    // Use this for errors that aren't specific to one field (e.g., "Invalid credentials")
    var generalLoginError by remember { mutableStateOf<String?>(null) }


    val focusManager = LocalFocusManager.current

    fun validateAndLogin() {
        focusManager.clearFocus() // Hide keyboard
        var isValid = true
        generalLoginError = null // Clear general error on new attempt

        if (email.isBlank()) {
            emailError = "Email cannot be empty"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Enter a valid email address"
            isValid = false
        } else {
            emailError = null
        }

        if (password.isBlank()) {
            passwordError = "Password cannot be empty"
            isValid = false
        } else {
            passwordError = null
        }

        if (isValid) {
            // In a real app, onLoginClicked would likely trigger a ViewModel function
            // which might then update generalLoginError based on the server's response.
            onLoginClicked(email, password)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bloggy - Login") }, // Or just "Login"
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // General Login Error Display (e.g., for "Invalid Credentials")
            generalLoginError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium, // Slightly larger for general error
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            // If you passed a serverLoginError prop, you'd display it here too.
            // serverLoginError?.let { ... }


            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = null; generalLoginError = null },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                isError = emailError != null || generalLoginError != null // Mark field red on general error too
            )
            emailError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 4.dp)
                        .fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = null; generalLoginError = null },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { validateAndLogin() }
                ),
                singleLine = true,
                isError = passwordError != null || generalLoginError != null // Mark field red on general error
            )
            passwordError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 4.dp)
                        .fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = { validateAndLogin() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Register Text Button
            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? Register")
            }
        }
    }
}

@Preview(showBackground = true, name = "Login Screen Light")
@Composable
fun LoginScreenPreview() {
    BloggyTheme { // FIXME: Replace with your actual Theme
        LoginScreen(
            onLoginClicked = { email, password ->
                println("Preview Login: $email, $password")
            },
            onNavigateToRegister = {
                println("Preview Register Clicked")
            }
        )
    }
}

@Preview(showBackground = true, name = "Login Screen Dark")
@Composable
fun LoginScreenDarkPreview() {
    BloggyTheme(darkTheme = true) { // FIXME: Replace with your actual Theme
        LoginScreen(
            onLoginClicked = { email, password ->
                println("Preview Login: $email, $password")
            },
            onNavigateToRegister = {
                println("Preview Register Clicked")
            }
            // serverLoginError = "Invalid credentials. Try again." // Example server error
        )
    }
}

@Preview(showBackground = true, name = "Login Screen Field Error")
@Composable
fun LoginScreenFieldErrorPreview() {
    BloggyTheme { // FIXME: Replace with your actual Theme
        // This preview is more for the visual look if error messages were present.
        var email by remember { mutableStateOf("") } // Keep state for preview interaction
        var password by remember { mutableStateOf("") }
        var emailError by remember { mutableStateOf("Email cannot be empty") }
        var passwordError by remember { mutableStateOf("Password cannot be empty") }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                isError = emailError != null
            )
            emailError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null
            )
            passwordError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}