package com.example.scamazon_frontend.ui.screens.checkout

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.RingtoneManager
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.core.utils.formatPrice
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.components.*
import com.example.scamazon_frontend.ui.theme.*

@Composable
fun PaymentQRScreen(
    orderId: Int,
    viewModel: PaymentQRViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val qrState by viewModel.qrState.collectAsStateWithLifecycle()
    val qrBitmap by viewModel.qrBitmap.collectAsStateWithLifecycle()
    val paymentStatus by viewModel.paymentStatus.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(orderId) {
        viewModel.createPaymentQR(orderId)
    }

    // When payment is confirmed, play a notification sound so the user is alerted.
    LaunchedEffect(paymentStatus) {
        if (paymentStatus == "success") {
            try {
                val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, notificationUri)
                ringtone?.play()
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        LafyuuTopAppBar(title = "Payment", onBackClick = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (paymentStatus == "success") {
                // ==================== SUCCESS STATE ====================
                Spacer(modifier = Modifier.height(32.dp))

                val scale = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale.value)
                        .background(StatusSuccess.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(StatusSuccess, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Payment Successful!",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = StatusSuccess,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Order Code: ${viewModel.orderNumber}",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )

                Text(
                    text = "Total: ${formatPrice(viewModel.orderAmount)}đ",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = PrimaryBlue
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Your payment has been confirmed and your order is being processed.",
                    style = Typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                LafyuuPrimaryButton(
                    text = "View My Orders",
                    onClick = onNavigateToOrders,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LafyuuOutlinedButton(
                    text = "Continue Shopping",
                    onClick = onNavigateToHome,
                    modifier = Modifier.fillMaxWidth()
                )

            } else {
                // ==================== QR PAYMENT STATE ====================

                when (qrState) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = PrimaryBlue)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Loading payment info...",
                                    style = Typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    is Resource.Error -> {
                        ErrorState(
                            message = qrState.message ?: "Failed to generate QR code",
                            onRetry = { viewModel.createPaymentQR(orderId) }
                        )
                    }

                    is Resource.Success -> {
                        // -- Countdown Timer Bar --
                        val minutes = remainingSeconds / 60
                        val seconds = remainingSeconds % 60
                        val isUrgent = remainingSeconds < 120

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUrgent) StatusError.copy(alpha = 0.1f) else SecondaryYellow.copy(alpha = 0.1f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (isUrgent) StatusError else SecondaryYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Complete payment within ",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = if (isUrgent) StatusError else TextPrimary
                                )
                                Text(
                                    text = "%02d:%02d".format(minutes, seconds),
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (isUrgent) StatusError else PrimaryBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // -- Transfer Amount --
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PrimaryBlueSoft),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Transfer Amount",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${formatPrice(viewModel.orderAmount)}đ",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = PrimaryBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // -- QR Code Card --
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Scan QR to Pay",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val bitmap = qrBitmap
                                if (bitmap != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(260.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                                            .background(White)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Payment QR Code",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(260.dp)
                                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.QrCode,
                                                contentDescription = "QR Error",
                                                tint = TextHint,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Failed to generate QR code",
                                                style = Typography.bodySmall,
                                                color = TextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LafyuuPrimaryButton(
                                                text = "Retry",
                                                onClick = { viewModel.createPaymentQR(orderId) },
                                                modifier = Modifier.width(120.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Open your banking app and scan this QR code",
                                    style = Typography.bodySmall,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // -- Bank Account Info --
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Or Transfer Manually",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                PaymentInfoRow(
                                    label = "Bank",
                                    value = "VietinBank",
                                    context = context
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = BorderLight
                                )

                                PaymentInfoRow(
                                    label = "Account Number",
                                    value = "102876493175",
                                    copyable = true,
                                    context = context
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = BorderLight
                                )

                                PaymentInfoRow(
                                    label = "Account Name",
                                    value = "NGUYEN VIET HOANG",
                                    context = context
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = BorderLight
                                )

                                PaymentInfoRow(
                                    label = "Amount",
                                    value = "${formatPrice(viewModel.orderAmount)}đ",
                                    copyable = true,
                                    context = context,
                                    valueColor = PrimaryBlue,
                                    valueBold = true
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = BorderLight
                                )

                                val paymentRef = viewModel.paymentReference
                                PaymentInfoRow(
                                    label = "Transfer Content",
                                    value = paymentRef,
                                    copyable = true,
                                    context = context,
                                    valueColor = StatusError,
                                    valueBold = true
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // -- Important Notice --
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = StatusError.copy(alpha = 0.08f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = StatusError,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Please enter the transfer content exactly as shown above to ensure your payment is recognized automatically.",
                                    style = Typography.bodySmall,
                                    color = StatusError,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // -- Polling Indicator --
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse_alpha"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = StatusSuccess
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Waiting for payment confirmation...",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = StatusSuccess.copy(alpha = alpha)
                            )
                        }

                        // -- Help Info --
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PrimaryBlueSoft),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "This screen will update automatically when your payment is confirmed. Please do not close this screen.",
                                    style = Typography.bodySmall,
                                    color = PrimaryBlue,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentInfoRow(
    label: String,
    value: String,
    copyable: Boolean = false,
    context: Context,
    valueColor: Color = TextPrimary,
    valueBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.weight(0.4f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.6f),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = value,
                fontFamily = Poppins,
                fontWeight = if (valueBold) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = valueColor,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (copyable) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = PrimaryBlue,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val plainValue = value.replace("đ", "").replace(",", "").replace(".", "").trim()
                            clipboard.setPrimaryClip(ClipData.newPlainText(label, plainValue))
                            Toast.makeText(context, "Copied: $plainValue", Toast.LENGTH_SHORT).show()
                        }
                )
            }
        }
    }
}
