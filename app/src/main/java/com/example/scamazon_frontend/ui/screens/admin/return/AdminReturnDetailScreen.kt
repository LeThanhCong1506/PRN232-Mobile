package com.example.scamazon_frontend.ui.screens.admin.`return`

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.returnrequest.ReturnRequestResponse
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.components.*
import com.example.scamazon_frontend.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReturnDetailScreen(
    returnId: Int,
    viewModel: AdminReturnViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {}
) {
    val detailState by viewModel.returnDetailState.collectAsStateWithLifecycle()
    val processState by viewModel.processState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showProcessDialog by remember { mutableStateOf<String?>(null) } // target status
    var adminNote by remember { mutableStateOf("") }

    LaunchedEffect(returnId) {
        viewModel.loadReturnRequestDetail(returnId)
    }

    LaunchedEffect(processState) {
        if (processState is Resource.Success) {
            snackbarHostState.showSnackbar("Đã cập nhật thành công!")
            viewModel.resetProcessState()
            viewModel.loadReturnRequestDetail(returnId) // reload
        } else if (processState is Resource.Error) {
            snackbarHostState.showSnackbar(processState?.message ?: "Thao tác thất bại")
            viewModel.resetProcessState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chi tiết Return #$returnId",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        }
    ) { paddingValues ->
        when (val state = detailState) {
            null, is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = PrimaryBlue) }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorState(
                        message = state.message ?: "Không tải được dữ liệu",
                        onRetry = { viewModel.loadReturnRequestDetail(returnId) }
                    )
                }
            }
            is Resource.Success -> {
                val request = state.data!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(BackgroundLight)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminReturnDetailHeader(request)
                        AdminReturnOrderInfo(request)
                        AdminReturnRequestInfo(request)
                        if (!request.adminNote.isNullOrBlank()) {
                            AdminReturnAdminNoteCard(request.adminNote)
                        }
                        if (request.processedAt != null || request.processedByName != null) {
                            AdminReturnProcessingInfo(request)
                        }
                    }

                    // Action buttons — only for PENDING or APPROVED
                    val statusUp = request.status.uppercase()
                    if (statusUp == "PENDING" || statusUp == "APPROVED") {
                        AdminReturnActionBar(
                            status = statusUp,
                            isLoading = processState is Resource.Loading,
                            onAction = { targetStatus -> showProcessDialog = targetStatus }
                        )
                    }
                }
            }
        }
    }

    // Process confirmation dialog
    showProcessDialog?.let { targetStatus ->
        AlertDialog(
            onDismissRequest = { showProcessDialog = null; adminNote = "" },
            title = {
                Text(
                    text = when (targetStatus) {
                        "APPROVED" -> "Duyệt yêu cầu"
                        "REJECTED" -> "Từ chối yêu cầu"
                        "COMPLETED" -> "Hoàn tất xử lý"
                        else -> "Xác nhận"
                    },
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (targetStatus) {
                            "APPROVED" -> "Bạn có chắc muốn duyệt yêu cầu này?"
                            "REJECTED" -> "Bạn có chắc muốn từ chối yêu cầu này?"
                            "COMPLETED" -> "Xác nhận đã hoàn tất xử lý đổi/trả?"
                            else -> ""
                        },
                        fontFamily = Poppins,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = adminNote,
                        onValueChange = { adminNote = it },
                        label = { Text("Ghi chú admin (tuỳ chọn)", fontFamily = Poppins) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.processReturnRequest(returnId, targetStatus, adminNote)
                        showProcessDialog = null
                        adminNote = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (targetStatus) {
                            "REJECTED" -> StatusError
                            "APPROVED", "COMPLETED" -> StatusSuccess
                            else -> PrimaryBlue
                        }
                    )
                ) {
                    Text(
                        text = when (targetStatus) {
                            "APPROVED" -> "Duyệt"
                            "REJECTED" -> "Từ chối"
                            "COMPLETED" -> "Hoàn tất"
                            else -> "Xác nhận"
                        },
                        fontFamily = Poppins,
                        color = White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showProcessDialog = null; adminNote = "" }) {
                    Text("Huỷ", fontFamily = Poppins, color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun AdminReturnDetailHeader(request: ReturnRequestResponse) {
    val statusColor = when (request.status.uppercase()) {
        "APPROVED", "COMPLETED" -> StatusSuccess
        "REJECTED" -> StatusError
        "SUBMITTED" -> PrimaryBlue
        else -> SecondaryYellow
    }
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
                Text(
                    text = "Request #${request.returnRequestId}",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PrimaryBlue
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = request.status,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatAdminReturnDate(request.createdAt),
                    style = Typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AdminReturnOrderInfo(request: ReturnRequestResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Thông tin đơn hàng", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            ReturnDetailRow(label = "Mã đơn hàng", value = "#${request.orderNumber}")
            ReturnDetailRow(label = "Khách hàng", value = request.userName)
        }
    }
}

@Composable
private fun AdminReturnRequestInfo(request: ReturnRequestResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Chi tiết yêu cầu", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            ReturnDetailRow(label = "Loại", value = request.type, valueColor = PrimaryBlue)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Lý do", fontFamily = Poppins, fontSize = 12.sp, color = TextHint)
            Spacer(modifier = Modifier.height(4.dp))
            Text(request.reason, fontFamily = Poppins, fontSize = 14.sp, color = TextPrimary)
        }
    }
}

@Composable
private fun AdminReturnAdminNoteCard(note: String) {
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
                Text("Ghi chú admin", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PrimaryBlue)
                Spacer(modifier = Modifier.height(4.dp))
                Text(note, fontFamily = Poppins, fontSize = 13.sp, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun AdminReturnProcessingInfo(request: ReturnRequestResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Thông tin xử lý", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            request.processedByName?.let { ReturnDetailRow(label = "Xử lý bởi", value = it) }
            request.processedAt?.let { ReturnDetailRow(label = "Thời gian xử lý", value = formatAdminReturnDate(it)) }
            request.updatedAt?.let { ReturnDetailRow(label = "Cập nhật lần cuối", value = formatAdminReturnDate(it)) }
        }
    }
}

@Composable
private fun AdminReturnActionBar(
    status: String,
    isLoading: Boolean,
    onAction: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (status == "PENDING") {
                Button(
                    onClick = { onAction("APPROVED") },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Duyệt", fontFamily = Poppins, color = White)
                }
                Button(
                    onClick = { onAction("REJECTED") },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Từ chối", fontFamily = Poppins, color = White)
                }
            } else if (status == "APPROVED") {
                Button(
                    onClick = { onAction("COMPLETED") },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hoàn tất xử lý", fontFamily = Poppins, color = White)
                }
            }
        }
    }
}

@Composable
private fun ReturnDetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, fontFamily = Poppins, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(0.4f))
        Text(value, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = valueColor, modifier = Modifier.weight(0.6f))
    }
}

private fun formatAdminReturnDate(isoString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(isoString) ?: return isoString
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        isoString.take(10)
    }
}
