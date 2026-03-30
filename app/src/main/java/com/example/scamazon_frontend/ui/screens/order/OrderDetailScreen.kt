package com.example.scamazon_frontend.ui.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.core.utils.formatPrice
import com.example.scamazon_frontend.data.models.order.OrderDetailResponse
import com.example.scamazon_frontend.data.models.order.OrderItemResponse
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.components.*
import com.example.scamazon_frontend.ui.theme.*

@Composable
fun OrderDetailScreen(
    orderId: Int,
    viewModel: OrderHistoryViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {},
    onNavigateToReview: (Int) -> Unit = {},
    onNavigateToReturn: (Int) -> Unit = {}
) {
    val orderDetailState by viewModel.orderDetailState.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) {
        viewModel.fetchOrderDetail(orderId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        LafyuuTopAppBar(title = "Order Detail", onBackClick = onNavigateBack)

        when (val state = orderDetailState) {
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
                    ErrorState(
                        message = state.message ?: "Error loading order",
                        onRetry = { viewModel.fetchOrderDetail(orderId) }
                    )
                }
            }
            is Resource.Success -> {
                val order = state.data!!
                val scaffoldState = remember { SnackbarHostState() }
                
                Scaffold(
                    snackbarHost = { SnackbarHost(scaffoldState) },
                    topBar = { },
                    containerColor = Color.Transparent
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        OrderDetailContent(
                            order = order,
                            onNavigateToReview = onNavigateToReview,
                            onNavigateToReturn = { onNavigateToReturn(orderId) },
                            onCancelOrder = { reason ->
                                viewModel.cancelOrder(
                                    id = orderId,
                                    reason = reason,
                                    onSuccess = {},
                                    onError = {}
                                )
                            }
                        )
                    }
                }
            }
            null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
        }
    }
}

@Composable
private fun OrderDetailContent(
    order: OrderDetailResponse,
    onNavigateToReview: (Int) -> Unit = {},
    onNavigateToReturn: () -> Unit = {},
    onCancelOrder: (String) -> Unit = {}
) {
    val isDelivered = order.status.lowercase() == "delivered"
    val canCancel = order.status.lowercase() == "pending"
    var showCancelDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Order header
        item {
            OrderHeaderSection(order)
        }

        // Order status timeline
        item {
            OrderStatusSection(order.status ?: "pending")
        }

        // Shipping info
        item {
            ShippingInfoSection(order)
        }

        // Order items
        item {
            Text(
                text = "Order Items",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
        }
        items(order.items) { item ->
            OrderItemCard(
                item = item,
                showReviewButton = isDelivered,
                onReviewClick = { onNavigateToReview(item.productId) }
            )
        }

        // Review section for delivered orders
        if (isDelivered) {
            item {
                ReviewPromptSection(
                    items = order.items,
                    onReviewClick = onNavigateToReview
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onNavigateToReturn() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Request Return / Exchange",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Payment summary
        item {
            PaymentSummarySection(order)
        }

        // Payment info
        if (order.payment != null) {
            item {
                PaymentInfoSection(order)
            }
        }

        // Note
        if (!order.notes.isNullOrBlank()) {
            item {
                NoteSection(order.notes)
            }
        }

        if (canCancel) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (order.payment?.status?.lowercase() == "pending" && order.payment.paymentMethod.lowercase() == "sepay") {
                        val context = LocalContext.current
                        LafyuuPrimaryButton(
                            text = "Pay Now",
                            onClick = {
                                try {
                                    val url = "https://prn232-backend-production.up.railway.app/api/Payment/${order.orderId}/checkout?successUrl=myapp://payment/success&errorUrl=myapp://payment/error"
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        )
                    }
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = StatusError,
                            containerColor = Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusError)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = StatusError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cancel Order",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = StatusError
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showCancelDialog) {
        var cancelReason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Order") },
            text = {
                Column {
                    Text("Are you sure you want to cancel this order? This action cannot be undone.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Reason (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        onCancelOrder(cancelReason.ifBlank { "User requested cancellation" })
                    }
                ) {
                    Text("Confirm", color = StatusError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun OrderHeaderSection(order: OrderDetailResponse) {
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
                    text = order.orderNumber,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PrimaryBlue
                )
                OrderStatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            order.createdAt?.let { date ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = date.take(10),
                        style = Typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderStatusSection(currentStatus: String) {
    val statuses = listOf("pending", "confirmed", "shipping", "delivered")
    val currentIndex = statuses.indexOf(currentStatus.lowercase())
    val isCancelled = currentStatus.lowercase() == "cancelled"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order Status",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isCancelled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = StatusError,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Order Cancelled",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = StatusError
                    )
                }
            } else {
                statuses.forEachIndexed { index, status ->
                    val isCompleted = index <= currentIndex
                    val isActive = index == currentIndex

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dot indicator
                        Box(
                            modifier = Modifier
                                .size(if (isActive) 16.dp else 12.dp)
                                .background(
                                    color = when {
                                        isActive -> PrimaryBlue
                                        isCompleted -> StatusSuccess
                                        else -> BorderDefault
                                    },
                                    shape = RoundedCornerShape(50)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted && !isActive) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = White,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = status.replaceFirstChar { it.uppercase() },
                            fontFamily = Poppins,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (isCompleted) TextPrimary else TextHint
                        )
                    }

                    // Connector line
                    if (index < statuses.lastIndex) {
                        Box(
                            modifier = Modifier
                                .padding(start = 5.5.dp)
                                .width(1.dp)
                                .height(16.dp)
                                .background(
                                    if (index < currentIndex) StatusSuccess else BorderDefault
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShippingInfoSection(order: OrderDetailResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Shipping Information",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            ShippingRow(icon = Icons.Default.Person, value = order.customerName ?: "")
            ShippingRow(icon = Icons.Default.Phone, value = order.customerPhone ?: "")
            ShippingRow(icon = Icons.Default.LocationOn, value = order.shippingAddress)
        }
    }
}

@Composable
private fun ShippingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = Typography.bodyMedium,
            color = TextPrimary
        )
    }
}

@Composable
private fun OrderItemCard(
    item: OrderItemResponse,
    showReviewButton: Boolean = false,
    onReviewClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product Image
                if (!item.productImageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.productImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.productName ?: "Product Image",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BackgroundLight),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(BackgroundLight, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = TextHint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName ?: "",
                        style = Typography.titleSmall,
                        color = TextPrimary,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${formatPrice(item.unitPrice)}đ × ${item.quantity}",
                            style = Typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "${formatPrice(item.subtotal)}đ",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = PrimaryBlue
                        )
                    }
                }
            }

            // Review button for delivered orders
            if (showReviewButton) {
                HorizontalDivider(color = BorderLight)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onReviewClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AccentGold
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Đánh giá",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewPromptSection(
    items: List<OrderItemResponse>,
    onReviewClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AccentGoldSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.RateReview,
                contentDescription = null,
                tint = AccentGold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Đánh giá sản phẩm",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Đơn hàng đã giao. Hãy chia sẻ trải nghiệm của bạn!",
                    style = Typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PaymentSummarySection(order: OrderDetailResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Payment Summary",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummaryRow("Subtotal", formatPrice(order.subtotalAmount) + "đ")
            SummaryRow("Shipping Fee", formatPrice(order.shippingFee ?: 0.0) + "đ")
            if (order.discountAmount != null && order.discountAmount > 0) {
                SummaryRow("Discount", "-${formatPrice(order.discountAmount)}đ", StatusSuccess)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${formatPrice(order.totalAmount)}đ",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PrimaryBlue
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = Typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = Typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun PaymentInfoSection(order: OrderDetailResponse) {
    val payment = order.payment ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Payment Info",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Payment method
            SummaryRow(
                "Method",
                when (payment.paymentMethod.uppercase()) {
                    "VNPAY"         -> "VNPay"
                    "ZALOPAY"       -> "ZaloPay"
                    "SEPAY"         -> "SePay"
                    "BANK_TRANSFER" -> "Bank Transfer"
                    "COD"           -> "COD"
                    else            -> payment.paymentMethod.replaceFirstChar { it.uppercase() }
                }
            )

            // Amount actually charged (from payment record, not order total)
            SummaryRow("Amount", "${formatPrice(payment.amount)}đ")

            // Payment status
            SummaryRow(
                "Status",
                payment.status.replaceFirstChar { it.uppercase() }
            )

            // Received amount (e.g. for COD partial payment)
            payment.receivedAmount?.let { received ->
                if (received > 0) {
                    SummaryRow("Received", "${formatPrice(received)}đ")
                }
            }

            // Payment reference (e.g. SePay reference code)
            payment.paymentReference?.let { ref ->
                SummaryRow("Reference", ref)
            }

            // Transaction ID
            payment.transactionId?.let { txId ->
                SummaryRow("Transaction", txId)
            }

            // Paid date
            payment.paymentDate?.let { date ->
                SummaryRow("Paid At", date.take(19).replace("T", " "))
            }

            // Expiry date (for pending payments like SePay QR)
            if (payment.status.lowercase() == "pending") {
                payment.expiredAt?.let { exp ->
                    SummaryRow("Expires At", exp.take(19).replace("T", " "))
                }
            }
        }
    }
}

@Composable
private fun NoteSection(note: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AccentGoldSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Notes,
                contentDescription = null,
                tint = AccentGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Note",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Text(
                    text = note,
                    style = Typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

// Re-use from OrderHistoryScreen
@Composable
private fun OrderStatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "pending" -> Triple(SecondaryYellow.copy(alpha = 0.15f), SecondaryYellow, "Pending")
        "confirmed" -> Triple(PrimaryBlue.copy(alpha = 0.15f), PrimaryBlue, "Confirmed")
        "shipping" -> Triple(PrimaryBlue.copy(alpha = 0.15f), PrimaryBlue, "Shipping")
        "delivered" -> Triple(StatusSuccess.copy(alpha = 0.15f), StatusSuccess, "Delivered")
        "cancelled" -> Triple(StatusError.copy(alpha = 0.15f), StatusError, "Cancelled")
        else -> Triple(BackgroundLight, TextSecondary, status.replaceFirstChar { it.uppercase() })
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            style = Typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
