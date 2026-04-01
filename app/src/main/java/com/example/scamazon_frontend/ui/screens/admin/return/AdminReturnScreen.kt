package com.example.scamazon_frontend.ui.screens.admin.`return`

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
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
fun AdminReturnScreen(
    viewModel: AdminReturnViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (Int) -> Unit = {}
) {
    val returnState by viewModel.returnListState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadAllReturnRequests()
    }

    Scaffold(
        topBar = { LafyuuTopAppBar(title = "Manage Returns", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundWhite)
        ) {
            when (returnState) {
                is Resource.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
                is Resource.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(
                        message = returnState.message ?: "Failed to load requests",
                        onRetry = { viewModel.loadAllReturnRequests() }
                    )
                }
                is Resource.Success -> {
                    val requests = returnState.data?.items ?: emptyList()
                    if (requests.isEmpty()) {
                        EmptyState(title = "No Return Requests", message = "There are no returns or exchanges to process.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.background(BackgroundLight).fillMaxSize()
                        ) {
                            items(requests) { req ->
                                AdminReturnCard(
                                    request = req,
                                    onClick = { onNavigateToDetail(req.returnRequestId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminReturnCard(request: ReturnRequestResponse, onClick: () -> Unit) {
    val statusColor = when (request.status.uppercase()) {
        "APPROVED", "COMPLETED" -> StatusSuccess
        "REJECTED" -> StatusError
        else -> SecondaryYellow // PENDING
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(
                        text = request.status,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Type: ${request.type}", style = Typography.bodyMedium, color = PrimaryBlue, fontWeight = FontWeight.Bold)
            Text("Order: #${request.orderNumber} by ${request.userName}", style = Typography.bodyMedium, color = TextSecondary)

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(4.dp))
            Text("Reason: ${request.reason}", style = Typography.bodySmall, color = TextSecondary, maxLines = 2)

            if (!request.adminNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Note", tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Note: ${request.adminNote}",
                        fontFamily = Poppins,
                        fontSize = 11.sp,
                        color = PrimaryBlue
                    )
                }
            }
        }
    }
}

