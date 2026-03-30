package com.example.scamazon_frontend.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.profile.UpdateProfileRequest
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.components.*
import com.example.scamazon_frontend.ui.theme.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull

@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {}
) {
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()

    // Form fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var ward by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var initialized by remember { mutableStateOf(false) }

    // Populate form when profile loads (only once per screen entry)
    LaunchedEffect(profileState) {
        val state = profileState
        if (state is Resource.Success && !initialized) {
            val profile = state.data!!
            fullName = profile.fullName ?: ""
            email = profile.email ?: ""
            phone = profile.phone ?: ""
            address = profile.address ?: ""
            city = profile.city ?: ""
            district = profile.district ?: ""
            ward = profile.ward ?: ""
            avatarUrl = profile.avatarUrl
            initialized = true
        }
    }

    val context = LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                val mimeType = context.contentResolver.getType(it)
                    ?.takeIf { t -> t.startsWith("image/") }
                    ?: "image/jpeg"
                val extension = when (mimeType) {
                    "image/png"  -> "png"
                    "image/gif"  -> "gif"
                    "image/webp" -> "webp"
                    else         -> "jpg"
                }
                val mediaType = mimeType.toMediaTypeOrNull()
                if (mediaType != null) {
                    val requestFile = okhttp3.RequestBody.create(mediaType, bytes)
                    val body = okhttp3.MultipartBody.Part.createFormData("file", "avatar.$extension", requestFile)
                    viewModel.uploadAvatar(body)
                }
            }
        }
    }

    // Handle update result
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is Resource.Success -> {
                // Refresh local avatar URL so the form shows the newly uploaded image
                state.data?.avatarUrl?.let { newUrl -> avatarUrl = newUrl }
                snackbarHostState.showSnackbar("Profile updated successfully!")
                viewModel.resetUpdateState()
                onNavigateBack()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(state.message ?: "Update failed")
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundWhite)
        ) {
            LafyuuTopAppBar(title = "Edit Profile", onBackClick = onNavigateBack)

            when (profileState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profileState.message ?: "Error",
                            style = Typography.bodyLarge,
                            color = StatusError
                        )
                    }
                }
                is Resource.Success -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(Dimens.ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                coil.compose.AsyncImage(
                                    model = avatarUrl ?: "https://via.placeholder.com/150",
                                    contentDescription = "Avatar",
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { launcher.launch("image/*") }) {
                                    Text("Change Picture", color = PrimaryBlue)
                                }
                            }
                        }

                        // Full Name
                        Text(text = "Full Name", style = Typography.titleSmall, color = TextPrimary)
                        LafyuuTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = "Enter your full name",
                            leadingIcon = Icons.Default.Person
                        )

                        // Email
                        Text(text = "Email", style = Typography.titleSmall, color = TextPrimary)
                        LafyuuTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "Enter your email",
                            leadingIcon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email
                        )

                        // Phone
                        Text(text = "Phone", style = Typography.titleSmall, color = TextPrimary)
                        LafyuuTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = "Enter your phone number",
                            leadingIcon = Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone
                        )

                        // Address
                        Text(text = "Address", style = Typography.titleSmall, color = TextPrimary)
                        LafyuuTextField(
                            value = address,
                            onValueChange = { address = it },
                            placeholder = "Street address",
                            leadingIcon = Icons.Default.LocationOn
                        )

                        // City / District
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "City", style = Typography.titleSmall, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                LafyuuTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    placeholder = "City"
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "District", style = Typography.titleSmall, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                LafyuuTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    placeholder = "District"
                                )
                            }
                        }

                        // Ward
                        Text(text = "Ward", style = Typography.titleSmall, color = TextPrimary)
                        LafyuuTextField(
                            value = ward,
                            onValueChange = { ward = it },
                            placeholder = "Ward",
                            imeAction = ImeAction.Done
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Save Button
                    val isUpdating = updateState is Resource.Loading
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = White
                    ) {
                        LafyuuPrimaryButton(
                            text = if (isUpdating) "Saving..." else "Save Changes",
                            onClick = {
                                viewModel.updateProfile(
                                    UpdateProfileRequest(
                                        fullName = fullName.ifBlank { null },
                                        email = email.ifBlank { null },
                                        phone = phone.ifBlank { null },
                                        address = address.ifBlank { null },
                                        city = city.ifBlank { null },
                                        district = district.ifBlank { null },
                                        ward = ward.ifBlank { null }
                                    )
                                )
                            },
                            enabled = !isUpdating,
                            modifier = Modifier.padding(Dimens.ScreenPadding)
                        )
                    }
                }
            }
        }
    }
}
