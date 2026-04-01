package com.example.scamazon_frontend.ui.screens.admin.warranty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.warranty.AdminWarrantyDto
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.components.*
import com.example.scamazon_frontend.ui.theme.*
import java.time.LocalDate
import java.time.Period

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWarrantyDetailScreen(
    warrantyId: Int,
    viewModel: AdminWarrantyManagementViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: (Int) -> Unit = {}
) {
    val editingWarranty by viewModel.editingWarranty.collectAsStateWithLifecycle()
    val warrantiesState by viewModel.warrantiesState.collectAsStateWithLifecycle()

    // Try to get from cached list first, then load individually
    LaunchedEffect(warrantyId) {
        val cached = (warrantiesState as? Resource.Success)?.data?.find { it.warrantyId == warrantyId }
        if (cached != null) {
            viewModel.loadWarrantyForEdit(warrantyId)
        } else {
            viewModel.loadWarrantyForEdit(warrantyId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chi tiết Bảo hành #$warrantyId",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Navy)
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(warrantyId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        }
    ) { paddingValues ->
        val warranty = editingWarranty
        if (warranty == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = PrimaryBlue) }
        } else {
            WarrantyDetailContent(
                warranty = warranty,
                modifier = Modifier.padding(paddingValues),
                onEdit = { onNavigateToEdit(warrantyId) }
            )
        }
    }
}

@Composable
private fun WarrantyDetailContent(
    warranty: AdminWarrantyDto,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit
) {
    val today = LocalDate.now()
    val endLocalDate = runCatching { LocalDate.parse(warranty.endDate.take(10)) }.getOrNull()
    val startLocalDate = runCatching { LocalDate.parse(warranty.startDate.take(10)) }.getOrNull()
    val isExpired = endLocalDate != null && endLocalDate.isBefore(today)
    val statusLabel = when {
        isExpired -> "Hết hạn"
        warranty.isActive -> "Còn hiệu lực"
        else -> "Vô hiệu"
    }
    val statusColor: Color = when {
        isExpired -> StatusError
        warranty.isActive -> StatusSuccess
        else -> TextSecondary
    }
    val monthsRemaining = if (endLocalDate != null && !isExpired) {
        val period = Period.between(today, endLocalDate)
        period.years * 12 + period.months
    } else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header status card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlueSoft),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Warranty #${warranty.warrantyId}",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = PrimaryBlue
                        )
                        Text(
                            text = "SN: ${warranty.serialNumber}",
                            fontFamily = Poppins,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = statusLabel,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                if (!isExpired && warranty.isActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (monthsRemaining <= 3) StatusError else StatusSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Còn $monthsRemaining tháng hiệu lực",
                            fontFamily = Poppins,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (monthsRemaining <= 3) StatusError else StatusSuccess
                        )
                    }
                }
            }
        }

        // Product info
        warranty.productName?.let { productName ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Thông tin sản phẩm", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    WarrantyInfoRow("Tên sản phẩm", productName)
                    warranty.productSku?.let { WarrantyInfoRow("SKU", it) }
                    warranty.productId?.let { WarrantyInfoRow("Product ID", it.toString()) }
                }
            }
        }

        // Warranty policy info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Thông tin bảo hành", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                WarrantyInfoRow("Chính sách", warranty.policyName)
                warranty.durationMonths?.let { WarrantyInfoRow("Thời hạn", "$it tháng") }
                startLocalDate?.let { WarrantyInfoRow("Bắt đầu", it.toString()) }
                endLocalDate?.let {
                    WarrantyInfoRow(
                        label = "Kết thúc",
                        value = it.toString(),
                        valueColor = if (isExpired) StatusError else TextPrimary
                    )
                }
                WarrantyInfoRow(
                    label = "Kích hoạt",
                    value = if (warranty.isActive) "Có" else "Không",
                    valueColor = if (warranty.isActive) StatusSuccess else StatusError
                )
            }
        }

        // Notes
        if (!warranty.notes.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlueSoft),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Ghi chú", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(warranty.notes, fontFamily = Poppins, fontSize = 13.sp, color = TextPrimary)
                    }
                }
            }
        }

        // Edit button
        Button(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Chỉnh sửa Warranty", fontFamily = Poppins, color = White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun WarrantyInfoRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, fontFamily = Poppins, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(0.4f))
        Text(value, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = valueColor, modifier = Modifier.weight(0.6f))
    }
}
