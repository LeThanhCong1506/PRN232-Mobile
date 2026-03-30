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
import com.example.scamazon_frontend.ui.components.LafyuuPasswordField
import com.example.scamazon_frontend.ui.components.LafyuuPrimaryButton
import com.example.scamazon_frontend.ui.components.LafyuuTextField
import com.example.scamazon_frontend.ui.components.LafyuuTopAppBar
import com.example.scamazon_frontend.ui.theme.BackgroundWhite
import com.example.scamazon_frontend.ui.theme.Poppins
import com.example.scamazon_frontend.ui.theme.TextPrimary
import com.example.scamazon_frontend.ui.theme.TextSecondary
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    email: String,
    viewModel: AuthViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    var token by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    val resetPasswordState by viewModel.resetPasswordState.collectAsStateWithLifecycle()
    val isLoading = resetPasswordState is Resource.Loading

    LaunchedEffect(resetPasswordState) {
        when (resetPasswordState) {
            is Resource.Success -> {
                viewModel.resetState()
                Toast.makeText(context, "Password reset successfully", Toast.LENGTH_SHORT).show()
                onNavigateToLogin()
            }
            is Resource.Error -> {
                errorMsg = resetPasswordState?.message
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            LafyuuTopAppBar(
                title = "Reset Password",
                onBackClick = onNavigateBack
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
                    text = "Reset Password",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Enter the token you received in your email and your new password.",
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                LafyuuTextField(
                    value = token,
                    onValueChange = {
                        token = it
                        errorMsg = null
                    },
                    placeholder = "Token",
                    isError = errorMsg != null
                )

                Spacer(modifier = Modifier.height(16.dp))

                LafyuuPasswordField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMsg = null
                    },
                    placeholder = "New Password",
                    isError = errorMsg != null,
                    imeAction = ImeAction.Next
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                LafyuuPasswordField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMsg = null
                    },
                    placeholder = "Confirm Password",
                    isError = errorMsg != null,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        if (newPassword != confirmPassword) {
                            errorMsg = "Passwords do not match"
                        } else if (!isLoading) {
                            viewModel.resetPassword(email, token, newPassword)
                        }
                    }
                )

                if (errorMsg != null) {
                    Text(text = errorMsg ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                LafyuuPrimaryButton(
                    text = if (isLoading) "Saving..." else "Save Password",
                    onClick = {
                        if (newPassword != confirmPassword) {
                            errorMsg = "Passwords do not match"
                        } else if (token.isBlank() || newPassword.isBlank()) {
                            errorMsg = "Please fill in all fields"
                        } else {
                            viewModel.resetPassword(email, token, newPassword)
                        }
                    },
                    enabled = !isLoading
                )
            }
        }
    }
}
