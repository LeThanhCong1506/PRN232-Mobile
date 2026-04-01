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
    onNavigateBack: () -> Unit = {}
) {
    val returnState by viewModel.returnListState.collectAsStateWithLifecycle()
    val processState by viewModel.processState.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf<ReturnRequestResponse?>(null) }
    var showDetailDialog by remember { mutableStateOf<ReturnRequestResponse?>(null) }
    var adminNote by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadAllReturnRequests()
    }

    LaunchedEffect(processState) {
        if (processState is Resource.Success) {
            snackbarHostState.showSnackbar("Return processed successfully!")
            showDialog = null
            adminNote = ""
            viewModel.resetProcessState()
        } else if (processState is Resource.Error) {
            snackbarHostState.showSnackbar(processState?.message ?: "Processing failed")
            viewModel.resetProcessState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.background(BackgroundLight).fillMaxSize()
                        ) {
                            items(requests) { req ->
                                AdminReturnCard(request = req, onClick = {
                                    val status = req.status.uppercase()
                                    if (status == "PENDING" || status == "APPROVED") {
                                        showDialog = req
                                    } else {
                                        // COMPLETED / REJECTED / SUBMITTED -> xem detail
                                        showDetailDialog = req
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    // Process Dialog (PENDING / APPROVED)
    if (showDialog != null) {
        AlertDialog(
            onDismissRequest = { showDialog = null; adminNote = "" },
            title = { Text("Process Return #${showDialog?.returnRequestId}", fontFamily = Poppins, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Type: ${showDialog?.type}", style = Typography.bodyMedium, color = PrimaryBlue)
                    Text("Order: #${showDialog?.orderNumber} by ${showDialog?.userName}", style = Typography.bodySmall, color = TextSecondary)
                    Text("Reason: ${showDialog?.reason}", style = Typography.bodySmall)
                    OutlinedTextField(
                        value = adminNote,
                        onValueChange = { adminNote = it },
                        label = { Text("Admin Note") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (showDialog?.status?.uppercase() == "PENDING") {
                        TextButton(onClick = { viewModel.processReturnRequest(showDialog!!.returnRequestId, "APPROVED", adminNote) }) {
                            Text("Approve", color = StatusSuccess)
                        }
                        TextButton(onClick = { viewModel.processReturnRequest(showDialog!!.returnRequestId, "REJECTED", adminNote) }) {
                            Text("Reject", color = StatusError)
                        }
                    } else if (showDialog?.status?.uppercase() == "APPROVED") {
                        TextButton(onClick = { viewModel.processReturnRequest(showDialog!!.returnRequestId, "COMPLETED", adminNote) }) {
                            Text("Complete", color = PrimaryBlue)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = null; adminNote = "" }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Detail Dialog (COMPLETED / REJECTED / SUBMITTED)
    showDetailDialog?.let { req ->
        val statusColor = when (req.status.uppercase()) {
            "APPROVED", "COMPLETED" -> StatusSuccess
            "REJECTED" -> StatusError
            else -> SecondaryYellow
        }
        AlertDialog(
            onDismissRequest = { showDetailDialog = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Request #${req.returnRequestId}",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Surface(shape = RoundedCornerShape(12.dp), color = statusColor.copy(alpha = 0.15f)) {
                        Text(
                            text = req.status,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(label = "Type", value = req.type)
                    DetailRow(label = "Order", value = "#${req.orderNumber}")
                    DetailRow(label = "Customer", value = req.userName)
                    DetailRow(label = "Reason", value = req.reason)
                    if (!req.adminNote.isNullOrBlank()) {
                        HorizontalDivider()
                        DetailRow(label = "Admin Note", value = req.adminNote)
                    }
                    req.createdAt?.let {
                        HorizontalDivider()
                        DetailRow(
                            label = "Created At",
                            value = it.take(10)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDetailDialog = null }) {
                    Text("Close", color = PrimaryBlue, fontFamily = Poppins)
                }
            }
        )
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

            // Hint bấm vào xem chi tiết với COMPLETED/SUBMITTED
            val status = request.status.uppercase()
            if (status != "PENDING" && status != "APPROVED") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to view details",
                    fontFamily = Poppins,
                    fontSize = 10.sp,
                    color = TextHint,
                    fontStyle = FontStyle.Italic
                )
            }

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

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$label:",
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            fontFamily = Poppins,
            fontSize = 13.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}
