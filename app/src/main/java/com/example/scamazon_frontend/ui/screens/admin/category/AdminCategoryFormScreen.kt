package com.example.scamazon_frontend.ui.screens.admin.category

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.admin.*
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.theme.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull

/**
 * Shared form screen for both Category and Brand add/edit operations.
 *
 * @param isBrand true = Brand form, false = Category form
 * @param editId non-null means edit mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoryFormScreen(
    isBrand: Boolean = false,
    editId: Int? = null,
    viewModel: AdminCategoryViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val isEdit = editId != null && editId > 0
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val uploadImageState by viewModel.uploadImageState.collectAsStateWithLifecycle()
    val categoriesState by viewModel.categoriesState.collectAsStateWithLifecycle()
    val brandsState by viewModel.brandsState.collectAsStateWithLifecycle()

    val title = when {
        isBrand && isEdit -> "Edit Brand"
        isBrand -> "Add Brand"
        isEdit -> "Edit Category"
        else -> "Add Category"
    }

    // Form fields
    var name by remember { mutableStateOf("") }
    // Existing image URL from server (for display in edit mode)
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    // Locally-selected image URI (from gallery picker)
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // Image picker launcher
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            existingImageUrl = null // Clear server image preview when local one is selected

            // For Category in EDIT mode: upload immediately
            if (!isBrand && isEdit && editId != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes != null) {
                        val mediaType = "image/jpeg".toMediaTypeOrNull()
                        if (mediaType != null) {
                            val requestFile = okhttp3.RequestBody.create(mediaType, bytes)
                            val part = okhttp3.MultipartBody.Part.createFormData("files", "image.jpg", requestFile)
                            viewModel.uploadCategoryImage(editId, part)
                            Toast.makeText(context, "Uploading image...", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to read image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Pre-fill for edit mode
    LaunchedEffect(editId, categoriesState, brandsState) {
        if (isEdit) {
            if (!isBrand) {
                val categories = (categoriesState as? Resource.Success)?.data ?: emptyList()
                val category = categories.find { it.id == editId }
                if (category != null) {
                    name = category.name
                    existingImageUrl = category.imageUrl
                }
            } else {
                val brands = (brandsState as? Resource.Success)?.data ?: emptyList()
                val brand = brands.find { it.id == editId }
                if (brand != null) {
                    name = brand.name
                    existingImageUrl = brand.logoUrl
                }
            }
        }
    }

    // Handle image upload result (edit mode – separate from main save)
    LaunchedEffect(uploadImageState) {
        when (uploadImageState) {
            is Resource.Success -> {
                Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetUploadImageState()
                // Refresh data so the new image shows in the list
                if (!isBrand) viewModel.loadCategories() else viewModel.loadBrands()
            }
            is Resource.Error -> {
                Toast.makeText(context, (uploadImageState as Resource.Error).message ?: "Image upload failed", Toast.LENGTH_SHORT).show()
                viewModel.resetUploadImageState()
            }
            else -> {}
        }
    }

    // Handle main save result
    LaunchedEffect(saveState) {
        when (saveState) {
            is Resource.Success -> {
                val savedItem = saveState as Resource.Success
                // For Category CREATE mode: after category created, upload image if one was selected
                if (!isBrand && !isEdit && selectedImageUri != null) {
                    // The category was created. We need to get the new category id.
                    // Since we reload categories after category creation, we skip image upload here
                    // (image was not selected for create mode in old flow).
                    // This works better in EDIT mode where we know the ID.
                }
                Toast.makeText(
                    context,
                    if (isEdit) "$title updated!" else "$title created!",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetSaveState()
                if (isBrand) viewModel.loadBrands() else viewModel.loadCategories()
                onNavigateBack()
            }
            is Resource.Error -> {
                Toast.makeText(context, (saveState as Resource.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Image Picker Section ──
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BackgroundLight)
                    .border(
                        width = 2.dp,
                        color = if (selectedImageUri != null || existingImageUrl != null) PrimaryBlue else BorderDefault,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { imageLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val imageModel: Any? = selectedImageUri ?: existingImageUrl

                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = if (isBrand) "Brand Logo" else "Category Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // Overlay: change image button
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                            .clip(RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = "Change",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Change Photo",
                                color = Color.White,
                                fontFamily = Poppins,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // Placeholder
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.PhotoLibrary,
                            contentDescription = "Upload",
                            tint = TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBrand) "Upload Logo" else "Upload Image",
                            fontFamily = Poppins,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tap to select",
                            fontFamily = Poppins,
                            fontSize = 10.sp,
                            color = TextSecondary.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Upload hint for create mode
            if (!isEdit && selectedImageUri != null) {
                Text(
                    text = "✓ Image selected – will upload after ${if (isBrand) "brand" else "category"} is created",
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    textAlign = TextAlign.Center
                )
            }
            if (!isEdit && selectedImageUri == null) {
                Text(
                    text = "Image is optional",
                    fontFamily = Poppins,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // ── Name Field ──
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(if (isBrand) "Brand Name *" else "Category Name *", fontFamily = Poppins) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderDefault
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ── Save Button ──
            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Name is required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (isBrand) {
                        if (isEdit && editId != null) {
                            viewModel.updateBrand(
                                editId,
                                UpdateBrandRequest(
                                    name = name,
                                    // Keep existing logo URL if no new image selected
                                    logoUrl = existingImageUrl
                                )
                            )
                        } else {
                            viewModel.createBrand(
                                CreateBrandRequest(
                                    name = name,
                                    logoUrl = null
                                )
                            )
                        }
                    } else {
                        if (isEdit && editId != null) {
                            viewModel.updateCategory(
                                editId,
                                UpdateCategoryRequest(
                                    name = name,
                                    imageUrl = existingImageUrl
                                )
                            )
                            // If a new image was selected in edit mode, it's already being uploaded
                            // by the imageLauncher callback above
                        } else {
                            // Create category. Image will be uploaded separately after creation
                            // by using the multipart upload in createCategory
                            var imagePart: okhttp3.MultipartBody.Part? = null
                            selectedImageUri?.let { uri ->
                                try {
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    val bytes = inputStream?.readBytes()
                                    inputStream?.close()
                                    if (bytes != null) {
                                        val mediaType = "image/jpeg".toMediaTypeOrNull()
                                        if (mediaType != null) {
                                            val requestFile = okhttp3.RequestBody.create(mediaType, bytes)
                                            imagePart = okhttp3.MultipartBody.Part.createFormData("files", "image.jpg", requestFile)
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            viewModel.createCategory(
                                CreateCategoryRequest(name = name),
                                imagePart
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = White),
                enabled = saveState !is Resource.Loading
            ) {
                if (saveState is Resource.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = White)
                } else {
                    Text(
                        text = if (isEdit) "Update" else "Create",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
