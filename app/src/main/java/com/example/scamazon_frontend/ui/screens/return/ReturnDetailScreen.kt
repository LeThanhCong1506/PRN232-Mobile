package com.example.scamazon_frontend.ui.screens.`return`

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

@Composable
fun ReturnDetailScreen(
    returnId: Int,
    viewModel: ReturnViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {}
) {
    val detailState by viewModel.returnDetailState.collectAsStateWithLifecycle()

    LaunchedEffect(returnId) {
        viewModel.loadReturnRequestDetail(returnId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        LafyuuTopAppBar(title = "Return Request Detail", onBackClick = onNavigateBack)

        when (val state = detailState) {
            null, is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(
                        message = state.message ?: "Failed to load return request",
                        onRetry = { viewModel.loadReturnRequestDetail(returnId) }
                    )
                }
            }
            is Resource.Success -> {
                val request = state.data!!
                ReturnDetailContent(request = request)
            }
        }
    }
}

@Composable
private fun ReturnDetailContent(request: ReturnRequestResponse) {
    val statusColor = when (request.status.uppercase()) {
        "APPROVED", "COMPLETED" -> StatusSuccess
        "REJECTED" -> StatusError
        else -> SecondaryYellow
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlueSoft),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        fontSize = 18.sp,
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
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatReturnDate(request.createdAt),
                        style = Typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Order info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Order Information",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                ReturnInfoRow(label = "Order Number", value = "#${request.orderNumber}")
                ReturnInfoRow(label = "Customer", value = request.userName)
            }
        }

        // Request details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Request Details",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                ReturnInfoRow(
                    label = "Type",
                    value = request.type,
                    valueColor = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reason",
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = TextHint
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = request.reason,
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            }
        }

        // Admin note (if any)
        if (!request.adminNote.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlueSoft),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Admin Note",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = request.adminNote,
                            fontFamily = Poppins,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Processing info
        if (request.processedAt != null || request.processedByName != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Processing Info",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    request.processedByName?.let {
                        ReturnInfoRow(label = "Processed By", value = it)
                    }
                    request.processedAt?.let {
                        ReturnInfoRow(label = "Processed At", value = formatReturnDate(it))
                    }
                    request.updatedAt?.let {
                        ReturnInfoRow(label = "Last Updated", value = formatReturnDate(it))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ReturnInfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontFamily = Poppins,
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = valueColor,
            modifier = Modifier.weight(0.6f)
        )
    }
}

private fun formatReturnDate(isoString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(isoString) ?: return isoString
        val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        isoString
    }
}
