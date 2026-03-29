package com.example.scamazon_frontend.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.components.LafyuuEmailField
import com.example.scamazon_frontend.ui.components.LafyuuPrimaryButton
import com.example.scamazon_frontend.ui.components.MainTopAppBar
import com.example.scamazon_frontend.ui.theme.BackgroundWhite
import com.example.scamazon_frontend.ui.theme.Poppins
import com.example.scamazon_frontend.ui.theme.TextPrimary
import com.example.scamazon_frontend.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit,
    onNavigateToResetPassword: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    val forgotPasswordState by viewModel.forgotPasswordState.collectAsStateWithLifecycle()
    val isLoading = forgotPasswordState is Resource.Loading

    LaunchedEffect(forgotPasswordState) {
        when (forgotPasswordState) {
            is Resource.Success -> {
                viewModel.resetState()
                onNavigateToResetPassword(email)
            }
            is Resource.Error -> {
                emailError = forgotPasswordState?.message
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Forgot Password",
                onNavigationClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Forgot Password",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Enter your email for the verification process, we will send a token to your email.",
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                LafyuuEmailField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    placeholder = "Your Email",
                    isError = emailError != null,
                    errorMessage = emailError,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        if (email.isNotBlank() && !isLoading) {
                            viewModel.forgotPassword(email)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                LafyuuPrimaryButton(
                    text = if (isLoading) "Sending..." else "Send",
                    onClick = {
                        if (email.isBlank()) {
                            emailError = "Please enter your email"
                        } else {
                            viewModel.forgotPassword(email)
                        }
                    },
                    enabled = !isLoading
                )
            }
        }
    }
}
