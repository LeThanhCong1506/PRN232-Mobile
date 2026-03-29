package com.example.scamazon_frontend.ui.screens.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.admin.DashboardStatsDto
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.components.*
import com.example.scamazon_frontend.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateToChat: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {}
) {
    val statsState by viewModel.statsState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Admin Dashboard",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToChat) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Customer Chats",
                            tint = Navy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        }
    ) { innerPadding ->
        when (statsState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorState(
                        message = (statsState as Resource.Error).message ?: "Error loading stats",
                        onRetry = { viewModel.loadStats() }
                    )
                }
            }

            is Resource.Success -> {
                val stats = (statsState as Resource.Success<DashboardStatsDto>).data!!
                DashboardContent(
                    stats = stats,
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToUsers = { onNavigateToUsers() }
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    stats: DashboardStatsDto,
    modifier: Modifier = Modifier,
    onNavigateToUsers: () -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Customers Card
        item {
            StatsCard(
                title = "Customers",
                mainValue = stats.customers.total.toString(),
                subtitle = "+${stats.customers.new7Days} this month",
                icon = Icons.Filled.People,
                iconColor = PrimaryBlue,
                iconBgColor = PrimaryBlueSoft,
                onClick = onNavigateToUsers
            )
        }

        // Products Card
        item {
            StatsCard(
                title = "Products",
                mainValue = stats.products.total.toString(),
                subtitle = "${stats.products.lowStock} low stock",
                icon = Icons.Filled.Inventory2,
                iconColor = StatusSuccess,
                iconBgColor = Color(0xFFE8F8F5)
            )
        }

        // Orders Card
        item {
            StatsCard(
                title = "Total Orders",
                mainValue = stats.orders.today.toString(),
                subtitle = "${stats.orders.pending} pending",
                icon = Icons.Filled.ShoppingBag,
                iconColor = AccentGold,
                iconBgColor = AccentGoldSoft
            )
        }

        // Revenue Card
        item {
            StatsCard(
                title = "Monthly Revenue",
                mainValue = formatCurrency(stats.revenue.today),
                subtitle = "Total: ${formatCurrency(stats.revenue.month)}",
                icon = Icons.Filled.AttachMoney,
                iconColor = StatusSuccess,
                iconBgColor = Color(0xFFE8F8F5)
            )
        }

        // Orders Summary Card (full width)
        item(span = { GridItemSpan(maxLineSpan) }) {
            OrdersSummaryCard(stats)
        }

        // Revenue Summary Card (full width)
        item(span = { GridItemSpan(maxLineSpan) }) {
            RevenueSummaryCard(stats)
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    mainValue: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = mainValue,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextPrimary
            )

            Text(
                text = title,
                fontFamily = Poppins,
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontFamily = Poppins,
                fontSize = 11.sp,
                color = PrimaryBlueLight
            )
        }
    }
}

@Composable
private fun OrdersSummaryCard(stats: DashboardStatsDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Orders Overview",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bar Chart
            val chartData = listOf(
                "Pending" to stats.orders.pending.toFloat(),
                "Confirmed" to stats.orders.confirmed.toFloat(),
                "Shipping" to stats.orders.shipping.toFloat(),
                "Delivered" to stats.orders.delivered.toFloat()
            )
            SimpleBarChart(data = chartData, color = PrimaryBlue)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OrderStatusRow("Pending", stats.orders.pending, AccentGold)
            OrderStatusRow("Confirmed", stats.orders.confirmed, PrimaryBlue)
            OrderStatusRow("Shipping", stats.orders.shipping, StatusInfo)
            OrderStatusRow("Delivered", stats.orders.delivered, StatusSuccess)
        }
    }
}

@Composable
private fun OrderStatusRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontFamily = Poppins,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        Text(
            text = count.toString(),
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = TextPrimary
        )
    }
}

@Composable
private fun RevenueSummaryCard(stats: DashboardStatsDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Revenue",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bar Chart
            val chartData = listOf(
                "Monthly" to stats.revenue.today.toFloat(),
                "Total" to stats.revenue.month.toFloat()
            )
            SimpleBarChart(data = chartData, color = StatusSuccess)

            Spacer(modifier = Modifier.height(16.dp))

            RevenueRow("Monthly Revenue", stats.revenue.today)
            RevenueRow("Total Revenue", stats.revenue.month)
        }
    }
}

@Composable
private fun SimpleBarChart(
    data: List<Pair<String, Float>>,
    color: Color
) {
    val max = data.maxOfOrNull { it.second } ?: 1f
    val barHeight = 90.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        data.forEach { (label, value) ->
            val heightRatio = if (max == 0f) 0f else value / max
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Value count above bar
                Text(
                    text = value.toInt().toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(3.dp))
                // Fixed-height bar container, bar grows from bottom
                Box(
                    modifier = Modifier
                        .height(barHeight)
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(heightRatio.coerceAtLeast(0.04f))
                            .background(color, RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Label below bar – allow 2 lines so "Confirmed" fits
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RevenueRow(label: String, amount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = Poppins,
            fontSize = 13.sp,
            color = TextSecondary
        )
        Text(
            text = formatCurrency(amount),
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = StatusSuccess
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${formatter.format(amount.toLong())}₫"
}
