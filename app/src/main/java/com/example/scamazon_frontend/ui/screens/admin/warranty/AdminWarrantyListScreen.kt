package com.example.scamazon_frontend.ui.screens.admin.warranty

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.warranty.AdminWarrantyDto
import com.example.scamazon_frontend.di.ViewModelFactory
import java.time.LocalDate
import java.time.Period
import com.example.scamazon_frontend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWarrantyListScreen(
    viewModel: AdminWarrantyManagementViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateToAdd: () -> Unit = {},
    onNavigateToEdit: (Int) -> Unit = {},
    onNavigateToDetail: (Int) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val warrantiesState by viewModel.warrantiesState.collectAsStateWithLifecycle()
    val serialSearchState by viewModel.serialSearchState.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    var searchSerial by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<AdminWarrantyDto?>(null) }

    LaunchedEffect(deleteState) {
        when (deleteState) {
            is Resource.Success -> {
                Toast.makeText(context, "Đã xóa warranty", Toast.LENGTH_SHORT).show()
                viewModel.resetDeleteState()
            }
            is Resource.Error -> {
                Toast.makeText(context, (deleteState as Resource.Error).message ?: "Xóa thất bại", Toast.LENGTH_SHORT).show()
                viewModel.resetDeleteState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Quản lý Bảo hành", fontFamily = Poppins, fontWeight = FontWeight.Bold, color = TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Navy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = PrimaryBlue,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm warranty")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundWhite)
        ) {
            // Serial Number Search
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchSerial,
                    onValueChange = {
                        searchSerial = it
                        if (it.isEmpty()) viewModel.clearSerialSearch()
                    },
                    placeholder = { Text("Tìm theo Serial Number", style = Typography.bodySmall, color = TextHint) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextHint) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderLight
                    )
                )
                Button(
                    onClick = { viewModel.searchBySerial(searchSerial) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Tìm", color = White, fontFamily = Poppins)
                }
            }

            // Serial search result
            when (val searchState = serialSearchState) {
                is Resource.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PrimaryBlue)
                is Resource.Success -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        WarrantyListItem(
                            warranty = searchState.data!!,
                            onClick = { onNavigateToDetail(searchState.data.warrantyId) },
                            onEdit = {
                                viewModel.loadWarrantyForEdit(searchState.data.warrantyId)
                                onNavigateToEdit(searchState.data.warrantyId)
                            },
                            onDelete = { showDeleteDialog = searchState.data }
                        )
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = "Không tìm thấy: ${searchState.message}",
                        color = StatusError,
                        style = Typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                null -> {}
            }

            // Filter chips
            val filters = listOf("all" to "Tất cả", "active" to "Còn hiệu lực", "expired" to "Hết hạn")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { viewModel.loadWarranties(key) },
                        label = { Text(label, fontFamily = Poppins, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = White
                        )
                    )
                }
            }

            // Warranty list
            when (val state = warrantiesState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message ?: "Lỗi tải dữ liệu", color = StatusError)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadWarranties(selectedFilter) }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                is Resource.Success -> {
                    val list = state.data ?: emptyList()
                    if (list.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Không có warranty nào", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            items(list, key = { it.warrantyId }) { warranty ->
                                WarrantyListItem(
                                    warranty = warranty,
                                    onClick = { onNavigateToDetail(warranty.warrantyId) },
                                    onEdit = {
                                        viewModel.loadWarrantyForEdit(warranty.warrantyId)
                                        onNavigateToEdit(warranty.warrantyId)
                                    },
                                    onDelete = { showDeleteDialog = warranty }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { warranty ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Xác nhận xóa", fontFamily = Poppins, fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn xóa warranty cho serial ${warranty.serialNumber}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteWarranty(warranty.warrantyId)
                    showDeleteDialog = null
                }) {
                    Text("Xóa", color = StatusError, fontFamily = Poppins)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Hủy", fontFamily = Poppins)
                }
            }
        )
    }
}

@Composable
private fun WarrantyListItem(
    warranty: AdminWarrantyDto,
    onClick: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Compute status locally from isActive + endDate
    val today = LocalDate.now()
    val endLocalDate = runCatching { LocalDate.parse(warranty.endDate.take(10)) }.getOrNull()
    val isExpired = endLocalDate != null && endLocalDate.isBefore(today)
    val statusLabel = when {
        isExpired -> "Hết hạn"
        warranty.isActive -> "Còn hiệu lực"
        else -> "Vô hiệu"
    }
    val statusColor = when {
        isExpired -> StatusError
        warranty.isActive -> Color(0xFF2E7D32)
        else -> TextSecondary
    }
    val monthsRemaining = if (endLocalDate != null && !isExpired) {
        val period = Period.between(today, endLocalDate)
        period.years * 12 + period.months
    } else 0

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = warranty.productName ?: "Unknown Product",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "SN: ${warranty.serialNumber}",
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    warranty.productSku?.let { sku ->
                        Text(
                            text = "SKU: $sku",
                            fontFamily = Poppins,
                            fontSize = 11.sp,
                            color = TextHint
                        )
                    }
                }
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontFamily = Poppins,
                        fontSize = 11.sp,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Chính sách: ${warranty.policyName}",
                        fontFamily = Poppins,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = if (isExpired)
                            "Đã hết hạn: ${warranty.endDate.take(10)}"
                        else
                            "Hết hạn: ${warranty.endDate.take(10)}  •  Còn ${monthsRemaining} tháng",
                        fontFamily = Poppins,
                        fontSize = 11.sp,
                        color = if (monthsRemaining <= 3 || isExpired) StatusError else TextSecondary
                    )
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = StatusError, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
