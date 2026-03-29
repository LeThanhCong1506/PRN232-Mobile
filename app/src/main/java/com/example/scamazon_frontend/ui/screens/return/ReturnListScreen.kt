package com.example.scamazon_frontend.ui.screens.`return`

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun ReturnListScreen(
    viewModel: ReturnViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {},
    onReturnClick: (Int) -> Unit = {}
) {
    val returnState by viewModel.returnListState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadMyReturnRequests()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        LafyuuTopAppBar(title = "My Returns & Exchanges", onBackClick = onNavigateBack)

        when (returnState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(
                        message = returnState.message ?: "Failed to load returns",
                        onRetry = { viewModel.loadMyReturnRequests() }
                    )
                }
            }
            is Resource.Success -> {
                val returnedItems = returnState.data?.items ?: emptyList()
                if (returnedItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            title = "No Return Requests",
                            message = "You haven't requested any returns or exchanges yet."
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BackgroundLight),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(returnedItems) { request ->
                            ReturnCard(request = request, onClick = { onReturnClick(request.returnRequestId) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReturnCard(request: ReturnRequestResponse, onClick: () -> Unit) {
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
                    text = "Order #${request.orderNumber}",
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

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Type", fontFamily = Poppins, fontSize = 11.sp, color = TextHint)
                    Text(request.type, fontFamily = Poppins, fontSize = 13.sp, color = PrimaryBlue, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Date", fontFamily = Poppins, fontSize = 11.sp, color = TextHint)
                    Text(formatDate(request.createdAt), fontFamily = Poppins, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Reason", fontFamily = Poppins, fontSize = 11.sp, color = TextHint)
            Text(
                text = request.reason,
                fontFamily = Poppins,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 2,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

private fun formatDate(isoString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(isoString) ?: return isoString
        val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        isoString
    }
}
