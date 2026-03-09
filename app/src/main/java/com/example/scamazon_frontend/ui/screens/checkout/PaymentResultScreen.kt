package com.example.scamazon_frontend.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.scamazon_frontend.ui.components.LafyuuPrimaryButton
import com.example.scamazon_frontend.ui.components.LafyuuTopAppBar
import com.example.scamazon_frontend.ui.theme.BackgroundWhite
import com.example.scamazon_frontend.ui.theme.TextPrimary
import com.example.scamazon_frontend.ui.theme.Typography

@Composable
fun PaymentResultScreen(
    resultType: String,
    status: String?,
    orderId: String?,
    orderNumber: String?,
    message: String?,
    onNavigateHome: () -> Unit,
    onNavigateOrders: () -> Unit
) {
    val isSuccess = resultType == "success" || status == "success"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        LafyuuTopAppBar(
            title = "Payment Result",
            onBackClick = onNavigateHome
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = if (isSuccess) "Success" else "Error",
                tint = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isSuccess) "Payment Successful!" else "Payment Failed!",
                style = Typography.headlineMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!orderNumber.isNullOrBlank()) {
                Text(
                    text = "Order Number: $orderNumber",
                    style = Typography.bodyLarge,
                    color = TextPrimary
                )
            }

            if (!message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = Typography.bodyMedium,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            LafyuuPrimaryButton(
                text = "Back to Home",
                onClick = onNavigateHome,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isSuccess) {
                OutlinedButton(
                    onClick = onNavigateOrders,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("View My Orders")
                }
            }
        }
    }
}
