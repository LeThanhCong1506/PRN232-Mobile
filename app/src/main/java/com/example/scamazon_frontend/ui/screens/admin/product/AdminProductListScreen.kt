package com.example.scamazon_frontend.ui.screens.admin.product

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.product.ProductDto
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductListScreen(
    viewModel: AdminProductViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateToAddProduct: () -> Unit = {},
    onNavigateToEditProduct: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val productsState by viewModel.productsState.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()
    val toggleActiveState by viewModel.toggleActiveState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<ProductDto?>(null) }
    var currentPage by remember { mutableIntStateOf(1) }

    // Reload products when screen resumes (e.g. after creating/editing a product)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadProducts(page = currentPage, search = searchQuery.ifEmpty { null })
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(deleteState) {
        when (deleteState) {
            is Resource.Success -> {
                Toast.makeText(context, "Product deleted successfully", Toast.LENGTH_SHORT).show()
                viewModel.resetDeleteState()
            }
            is Resource.Error -> {
                Toast.makeText(context, (deleteState as Resource.Error).message ?: "Failed to delete product", Toast.LENGTH_SHORT).show()
                viewModel.resetDeleteState()
            }
            else -> {}
        }
    }

    LaunchedEffect(toggleActiveState) {
        when (toggleActiveState) {
            is Resource.Error -> {
                Toast.makeText(context, (toggleActiveState as Resource.Error).message ?: "Failed to toggle product", Toast.LENGTH_SHORT).show()
                viewModel.resetToggleState()
            }
            is Resource.Success -> viewModel.resetToggleState()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Products",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddProduct,
                containerColor = PrimaryBlue,
                contentColor = White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Product")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundLight)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.loadProducts(search = it.ifEmpty { null })
                },
                placeholder = {
                    Text("Search products...", fontFamily = Poppins, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.loadProducts()
                        }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderDefault,
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                )
            )

            when (productsState) {
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = (productsState as Resource.Error).message ?: "Error",
                                color = StatusError,
                                fontFamily = Poppins
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadProducts() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBlue,
                                    contentColor = White
                                )
                            ) {
                                Text("Retry", fontFamily = Poppins, color = White)
                            }
                        }
                    }
                }

                is Resource.Success -> {
                    val products = (productsState as Resource.Success).data?.items ?: emptyList()
                    val pagination = (productsState as Resource.Success).data?.pagination
                    val totalPages = pagination?.totalPages ?: 1

                    if (products.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No products found",
                                fontFamily = Poppins,
                                color = TextSecondary
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Total count info
                            Text(
                                text = "${pagination?.totalItems ?: products.size} sản phẩm",
                                fontFamily = Poppins,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(products) { product ->
                                    AdminProductItem(
                                        product = product,
                                        onEdit = { onNavigateToEditProduct(product.slug) },
                                        onDelete = { showDeleteDialog = product },
                                        onToggleActive = { viewModel.toggleActive(product.id) }
                                    )
                                }
                            }
                            // Pagination bar — dùng currentPage local làm source of truth
                            if (totalPages > 1) {
                                PaginationBar(
                                    currentPage = currentPage,
                                    totalPages = totalPages,
                                    onPrevious = {
                                        val prev = currentPage - 1
                                        currentPage = prev
                                        viewModel.loadProducts(page = prev, search = searchQuery.ifEmpty { null })
                                    },
                                    onNext = {
                                        val next = currentPage + 1
                                        currentPage = next
                                        viewModel.loadProducts(page = next, search = searchQuery.ifEmpty { null })
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                Text("Delete Product", fontFamily = Poppins, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${product.name}\"?",
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(product.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = StatusError, fontFamily = Poppins)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", color = TextSecondary, fontFamily = Poppins)
                }
            }
        )
    }
}

@Composable
private fun AdminProductItem(
    product: ProductDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    val isActive = product.isActive
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) White else BackgroundLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            AsyncImage(
                model = product.primaryImage,
                contentDescription = product.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BackgroundLight),
                contentScale = ContentScale.Crop,
                alpha = if (isActive) 1f else 0.4f
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Product Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (isActive) TextPrimary else TextHint,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatPrice(product.price),
                        fontFamily = Poppins,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) PrimaryBlue else TextHint
                    )
                    if (product.salePrice != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatPrice(product.salePrice),
                            fontFamily = Poppins,
                            fontSize = 12.sp,
                            color = StatusError
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Stock: ${product.stockQuantity}",
                    fontFamily = Poppins,
                    fontSize = 11.sp,
                    color = if (product.stockQuantity <= 10) StatusError else TextSecondary
                )
            }

            // Actions column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Toggle + Status badge on same row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isActive) StatusSuccess.copy(alpha = 0.12f) else StatusError.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (isActive) "Active" else "Inactive",
                            fontFamily = Poppins,
                            fontSize = 9.sp,
                            color = if (isActive) StatusSuccess else StatusError,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                        )
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { onToggleActive() },
                        modifier = Modifier
                            .width(44.dp)
                            .height(26.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = White,
                            checkedTrackColor = StatusSuccess,
                            uncheckedThumbColor = White,
                            uncheckedTrackColor = TextHint
                        )
                    )
                }
                // Edit + Delete in one row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = StatusError,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrevious,
            enabled = currentPage > 1,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "Previous",
                tint = if (currentPage > 1) PrimaryBlue else TextHint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = PrimaryBlue.copy(alpha = 0.1f)
        ) {
            Text(
                text = "Trang $currentPage / $totalPages",
                fontFamily = Poppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryBlue,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onNext,
            enabled = currentPage < totalPages,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Next",
                tint = if (currentPage < totalPages) PrimaryBlue else TextHint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${formatter.format(price)}d"
}
