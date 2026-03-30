package com.example.scamazon_frontend.ui.screens.admin.category

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
    var imageUrl by remember { mutableStateOf("") } // For category
    var logoUrl by remember { mutableStateOf("") } // For brand
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // Image picker for category image upload
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (!isBrand && isEdit && editId != null) {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    val mediaType = "image/*".toMediaTypeOrNull()
                    if (mediaType != null) {
                        val requestFile = okhttp3.RequestBody.create(mediaType, bytes)
                        val body = okhttp3.MultipartBody.Part.createFormData("files", "category_image.jpg", requestFile)
                        viewModel.uploadCategoryImage(editId, body)
                    }
                }
            } else if (!isBrand && !isEdit) {
                selectedImageUri = it
                imageUrl = "Image selected from Gallery"
                Toast.makeText(context, "Image selected. Will upload on create.", Toast.LENGTH_SHORT).show()
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
                    imageUrl = category.imageUrl ?: ""
                }
            } else {
                val brands = (brandsState as? Resource.Success)?.data ?: emptyList()
                val brand = brands.find { it.id == editId }
                if (brand != null) {
                    name = brand.name
                    logoUrl = brand.logoUrl ?: ""
                }
            }
        }
    }

    // Handle save result
    LaunchedEffect(saveState) {
        when (saveState) {
            is Resource.Success -> {
                Toast.makeText(
                    context,
                    if (isEdit) "$title updated!" else "$title created!",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetSaveState()
                // Reload list so changes are reflected immediately when user goes back
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *", fontFamily = Poppins) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (isBrand) {
                // Logo URL for Brand
                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = { Text("Logo URL", fontFamily = Poppins) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            } else {
                // Image URL for Category
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL", fontFamily = Poppins) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                // Image upload button
                OutlinedButton(
                    onClick = { imageLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (selectedImageUri != null) "Selected: Image" else "Upload Category Image",
                        fontFamily = Poppins
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
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
                                    logoUrl = logoUrl.ifBlank { null }
                                )
                            )
                        } else {
                            viewModel.createBrand(
                                CreateBrandRequest(
                                    name = name,
                                    logoUrl = logoUrl.ifBlank { null }
                                )
                            )
                        }
                    } else {
                        if (isEdit && editId != null) {
                            viewModel.updateCategory(
                                editId,
                                UpdateCategoryRequest(
                                    name = name,
                                    imageUrl = imageUrl.ifBlank { null }
                                )
                            )
                        } else {
                            var imagePart: okhttp3.MultipartBody.Part? = null
                            selectedImageUri?.let { uri ->
                                try {
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    val bytes = inputStream?.readBytes()
                                    inputStream?.close()
                                    if (bytes != null) {
                                        val mediaType = "image/*".toMediaTypeOrNull()
                                        if (mediaType != null) {
                                            val requestFile = okhttp3.RequestBody.create(mediaType, bytes)
                                            imagePart = okhttp3.MultipartBody.Part.createFormData("files", "category_image.jpg", requestFile)
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            
                            val finalImageUrl = if (imageUrl == "Image selected from Gallery" || imageUrl.isBlank()) null else imageUrl
                            
                            viewModel.createCategory(
                                CreateCategoryRequest(
                                    name = name,
                                    imageUrl = finalImageUrl
                                ),
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
