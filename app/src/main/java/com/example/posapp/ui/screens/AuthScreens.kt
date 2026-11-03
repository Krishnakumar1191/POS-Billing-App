package com.example.posapp.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.posapp.data.entities.User
import com.example.posapp.ui.PosViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: PosViewModel, onNavigateToLogin: () -> Unit, onNavigateToDashboard: () -> Unit) {
    LaunchedEffect(key1 = true) {
        delay(1500)
        viewModel.checkAutoLogin { isLoggedIn ->
            if (isLoggedIn) {
                onNavigateToDashboard()
            } else {
                onNavigateToLogin()
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "App Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "POS Billing",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun LoginScreen(viewModel: PosViewModel, onLoginSuccess: () -> Unit, onNavigateToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    var isLoggingIn by remember { mutableStateOf(false) }

    fun validateAndLogin() {
        var isValid = true
        if (email.isBlank()) { 
            emailError = "Email, Phone, or Username is required"
            isValid = false 
        } else {
            emailError = null
        }
        
        if (password.isBlank()) { 
            passwordError = "Password is required"
            isValid = false 
        } else {
            passwordError = null
        }

        if (isValid) {
            authError = null
            isLoggingIn = true
            viewModel.login(
                identifier = email.trim(),
                passwordHash = password,
                onSuccess = {
                    isLoggingIn = false
                    onLoginSuccess()
                },
                onError = { 
                    isLoggingIn = false
                    authError = it 
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary)) {
        // Top Section Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        // Bottom White Card Section
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (authError != null) {
                    Text(
                        text = authError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null },
                    label = { Text("Email, Username, or Phone") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    isError = emailError != null,
                    supportingText = { if (emailError != null) Text(emailError!!) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = null },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    isError = passwordError != null,
                    supportingText = { if (passwordError != null) Text(passwordError!!) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); validateAndLogin() }),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = "Forgot Password?",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { focusManager.clearFocus(); validateAndLogin() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isLoggingIn
                ) {
                    if (isLoggingIn) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("LOGIN", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Don't have an account?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Register",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onNavigateToRegister).padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(viewModel: PosViewModel, onRegisterSuccess: () -> Unit, onNavigateBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var companyError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }
    
    var isRegistering by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    fun validateAndRegister() {
        var isValid = true

        if (name.trim().isBlank()) { nameError = "Name cannot be empty"; isValid = false } else nameError = null
        if (companyName.trim().isBlank()) { companyError = "Company Name cannot be empty"; isValid = false } else companyError = null

        // Email Validation
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { 
            emailError = "Please enter a valid email address"
            isValid = false 
        } else {
            emailError = null
        }

        // Phone Validation (Exactly 10 digits)
        val digitsOnly = mobile.filter { it.isDigit() }
        if (digitsOnly.length != 10) {
            mobileError = "Phone number must be exactly 10 digits"
            isValid = false
        } else {
            mobileError = null
        }

        // Password Validation (Minimum 6 characters)
        if (password.length < 6) { 
            passwordError = "Password must be at least 6 characters"
            isValid = false 
        } else {
            passwordError = null
        }

        if (isValid) {
            authError = null
            isRegistering = true
            
            val user = User(
                mobileNumber = digitsOnly, 
                email = email.trim(), 
                name = name.trim(),
                passwordHash = password, 
                businessName = companyName.trim(),
                companyName = companyName.trim(), 
                createDate = System.currentTimeMillis()
            )
            
            viewModel.register(
                user = user, 
                onSuccess = {
                    isRegistering = false
                    onRegisterSuccess()
                },
                onError = { errorMsg ->
                    isRegistering = false
                    authError = errorMsg
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary)) {
        // Top Section Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.25f)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sign up to securely sync your POS",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        // Bottom White Card Section
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.80f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (authError != null) {
                    Text(
                        text = authError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )
                }
                
                OutlinedTextField(
                    value = name, onValueChange = { name = it; nameError = null },
                    label = { Text("Full Name (Username)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    isError = nameError != null,
                    supportingText = { if (nameError != null) Text(nameError!!) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, focusedLabelColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email, onValueChange = { email = it; emailError = null },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    isError = emailError != null,
                    supportingText = { if (emailError != null) Text(emailError!!) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, focusedLabelColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = mobile, onValueChange = { mobile = it; mobileError = null },
                    label = { Text("Mobile Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    isError = mobileError != null,
                    supportingText = { if (mobileError != null) Text(mobileError!!) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, focusedLabelColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = companyName, onValueChange = { companyName = it; companyError = null },
                    label = { Text("Company Name") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    isError = companyError != null,
                    supportingText = { if (companyError != null) Text(companyError!!) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, focusedLabelColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password, onValueChange = { password = it; passwordError = null },
                    label = { Text("Password (Min 6 Chars)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    isError = passwordError != null,
                    supportingText = { if (passwordError != null) Text(passwordError!!) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); validateAndRegister() }),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, focusedLabelColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { focusManager.clearFocus(); validateAndRegister() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isRegistering
                ) {
                    if (isRegistering) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("REGISTER TO CLOUD", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Already have an account?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Log In",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onNavigateBack).padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}