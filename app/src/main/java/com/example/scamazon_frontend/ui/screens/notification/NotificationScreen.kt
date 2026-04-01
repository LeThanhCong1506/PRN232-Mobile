package com.example.scamazon_frontend.ui.screens.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scamazon_frontend.ui.components.LafyuuTopAppBar
import com.example.scamazon_frontend.ui.theme.*

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.scamazon_frontend.data.models.notification.NotificationDto

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToOrderDetail: (Int) -> Unit = {}
) {
    val state by viewModel.notificationsState.collectAsState()

    // Reload every time this screen is opened (ViewModel is NavGraph-scoped and may
    // have been created before the user was logged in, leaving state as Error/stale).
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        topBar = {
            LafyuuTopAppBar(
                title = "Notifications",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Mark all as read", tint = TextSecondary)
                    }
                }
            )
        }
    ) { paddingValues ->
        when (state) {
            is com.example.scamazon_frontend.core.utils.Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            is com.example.scamazon_frontend.core.utils.Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(text = state.message ?: "Error", color = StatusError)
                }
            }
            is com.example.scamazon_frontend.core.utils.Resource.Success -> {
                val notifications = state.data ?: emptyList()
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No notifications yet", color = TextSecondary, fontFamily = Poppins)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(BackgroundWhite),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(notifications) { notif ->
                            NotificationItem(
                                notif = notif,
                                onMarkAsRead = { viewModel.markAsRead(notif.id) },
                                onNavigateToOrderDetail = onNavigateToOrderDetail
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notif: NotificationDto,
    onMarkAsRead: () -> Unit,
    onNavigateToOrderDetail: (Int) -> Unit = {}
) {
    val unread = notif.isRead == false
    val orderId = extractOrderId(notif.data)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (unread) onMarkAsRead()
                if (notif.type == "order_status" && orderId != null) {
                    onNavigateToOrderDetail(orderId)
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (unread) PrimaryBlueSoft else White
        ),
        shape = LafyuuShapes.CardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = if (unread) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon Background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when(notif.type) {
                    "order_status" -> Icons.Default.ShoppingBag
                    "chat" -> Icons.Default.Notifications
                    else -> Icons.Default.Notifications
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notif.title,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                val bodyText = when {
                    !notif.body.isNullOrBlank() -> notif.body
                    notif.type == "order_status" -> "Tap to view your order details"
                    notif.type == "payment" -> "Your payment has been processed"
                    notif.type == "chat" -> "You have a new message"
                    else -> "Tap to view details"
                }
                Text(
                    text = bodyText,
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
                if (notif.type == "order_status" && extractOrderId(notif.data) != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View order →",
                        fontFamily = Poppins,
                        fontSize = 11.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatNotifDate(notif.createdAt),
                    fontFamily = Poppins,
                    fontSize = 10.sp,
                    color = TextHint
                )
            }
            
            if (unread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(StatusError)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

private fun extractOrderId(data: String?): Int? {
    if (data.isNullOrBlank()) return null
    return try {
        val json = org.json.JSONObject(data)
        val id = json.optInt("orderId", 0).takeIf { it > 0 }
            ?: json.optInt("order_id", 0).takeIf { it > 0 }
            ?: json.optInt("OrderId", 0).takeIf { it > 0 }
        id
    } catch (e: Exception) {
        // Fallback: regex extract first number
        Regex("\\d+").find(data)?.value?.toIntOrNull()
    }
}

private fun formatNotifDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return try {
        val input = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        val output = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val date = input.parse(raw.take(19))
        if (date != null) output.format(date) else raw
    } catch (e: Exception) {
        raw
    }
}

data class NotificationMock(
    val id: Int,
    val title: String,
    val body: String,
    val type: String,
    val date: String,
    val unread: Boolean
)
