package com.example.scamazon_frontend.ui.screens.auth

import android.app.Activity
import android.net.Uri
import org.json.JSONObject
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamazon_frontend.core.utils.OAuthCallbackHolder
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.core.utils.SocialAuthConfig
import com.example.scamazon_frontend.data.models.auth.LoginRequest
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.components.*
import com.example.scamazon_frontend.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

/** Convert raw backend error body (may be JSON) into a human-readable message. */
private fun parseAuthError(raw: String?): String {
    if (raw.isNullOrBlank()) return "Sign-in failed. Please try again."
    return try {
        val obj = JSONObject(raw)
        // {"Message": "..."} → use that message directly
        if (obj.has("Message")) return obj.getString("Message")
        // {"field": ["error1", ...]} → validation errors, show generic message
        "Sign-in failed. Please try again."
    } catch (_: Exception) {
        // Not JSON — return as-is if it looks like a plain message
        if (raw.startsWith("{") || raw.startsWith("[")) "Sign-in failed. Please try again."
        else raw
    }
}

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateToRegister: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToAdminDashboard: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val isLoading = loginState is Resource.Loading

    // GitHub OAuth callback
    val githubCode by OAuthCallbackHolder.githubCode.collectAsStateWithLifecycle()
    val githubError by OAuthCallbackHolder.githubError.collectAsStateWithLifecycle()

    // Google Sign-In client
    val googleSignInClient = remember {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(SocialAuthConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, options)
    }

    // Google Sign-In launcher
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val serverAuthCode = account.serverAuthCode
                if (!serverAuthCode.isNullOrBlank()) {
                    viewModel.googleLogin(serverAuthCode)
                } else {
                    emailError = "Google sign-in failed: no auth code returned"
                }
            } catch (e: ApiException) {
                emailError = "Google sign-in failed (${e.statusCode})"
            }
        }
    }

    // Handle login result (email/password + social)
    LaunchedEffect(loginState) {
        when (loginState) {
            is Resource.Success -> {
                val role = (loginState as? Resource.Success)?.data?.role?.lowercase()
                viewModel.resetState()
                if (role == "admin" || role == "staff") {
                    onNavigateToAdminDashboard()
                } else {
                    onNavigateToHome()
                }
            }
            is Resource.Error -> {
                emailError = parseAuthError((loginState as Resource.Error).message)
            }
            else -> {}
        }
    }

    // Handle GitHub OAuth code received from deep link (via backend HTTPS redirect)
    LaunchedEffect(githubCode) {
        val code = githubCode
        if (!code.isNullOrBlank()) {
            OAuthCallbackHolder.clearGitHubCode()
            viewModel.githubLogin(code)
        }
    }

    // Handle GitHub OAuth error from deep link
    LaunchedEffect(githubError) {
        val err = githubError
        if (!err.isNullOrBlank()) {
            OAuthCallbackHolder.clearGitHubError()
            emailError = when (err) {
                "access_denied" -> "GitHub sign-in was cancelled."
                "no_code"       -> "GitHub sign-in failed. Please try again."
                else            -> "GitHub sign-in failed: $err"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            ScamazonLogo(
                size = 120.dp,
                showBrandName = true,
                showTagline = true
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Welcome Text
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Welcome Back",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sign in to continue shopping",
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Email Field
            LafyuuEmailField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                placeholder = "Email Address",
                isError = emailError != null,
                errorMessage = emailError,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            LafyuuPasswordField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                placeholder = "Password",
                isError = passwordError != null,
                errorMessage = passwordError,
                imeAction = ImeAction.Done,
                onImeAction = {
                    if (!isLoading) {
                        viewModel.login(LoginRequest(email = email, password = password))
                    }
                }
            )

            // Forgot Password - aligned right
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                LafyuuTextButton(
                    text = "Forgot Password?",
                    onClick = onNavigateToForgotPassword
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In Button
            LafyuuPrimaryButton(
                text = if (isLoading) "Signing In..." else "Sign In",
                onClick = {
                    viewModel.login(LoginRequest(email = email, password = password))
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── OR divider ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderLight)
                Text(
                    text = "  OR  ",
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderLight)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Google Sign-In ───────────────────────────────────────────
            SocialLoginButton(
                text = "Continue with Google",
                iconContent = {
                    // Google "G" coloured text as a stand-in icon
                    Text(
                        text = "G",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF4285F4)
                    )
                },
                onClick = {
                    if (!isLoading) {
                        // Sign out first so the account picker always appears
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleLauncher.launch(googleSignInClient.signInIntent)
                        }
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── GitHub Sign-In ───────────────────────────────────────────
            SocialLoginButton(
                text = "Continue with GitHub",
                iconContent = {
                    Text(
                        text = "⬡",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF24292E)
                    )
                },
                onClick = {
                    if (!isLoading) {
                        val uri = Uri.parse(
                            "https://github.com/login/oauth/authorize" +
                                    "?client_id=${SocialAuthConfig.GITHUB_CLIENT_ID}" +
                                    "&redirect_uri=${Uri.encode(SocialAuthConfig.GITHUB_REDIRECT_URI)}" +
                                    "&scope=user:email"
                        )
                        CustomTabsIntent.Builder().build().launchUrl(context, uri)
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── New user divider ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderLight)
                Text(
                    text = "  New to STEM?  ",
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderLight)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Register Button
            LafyuuOutlinedButton(
                text = "Create Account",
                onClick = onNavigateToRegister
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Text(
                text = "By signing in, you agree to our Terms of Service\nand Privacy Policy",
                fontFamily = Poppins,
                fontSize = 11.sp,
                color = TextHint,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Full-screen loading overlay for social login
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }
    }
}

// ── Reusable social login button ─────────────────────────────────────────────

@Composable
private fun SocialLoginButton(
    text: String,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                iconContent()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    ScamazonFrontendTheme {
        LoginScreen()
    }
}
