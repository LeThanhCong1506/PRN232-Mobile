package com.example.scamazon_frontend.ui.screens.warranty

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
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
import com.example.scamazon_frontend.data.models.warranty.MyWarrantyDto
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarrantyListScreen(
    viewModel: WarrantyViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {},
    onWarrantyClick: (MyWarrantyDto) -> Unit = {}
) {
    val warrantiesState by viewModel.warrantiesState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Warranties",
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
    ) { innerPadding ->
        when (warrantiesState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = PrimaryBlue) }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(warrantiesState.message ?: "Error", color = StatusError, fontFamily = Poppins)
                        Button(
                            onClick = { viewModel.loadMyWarranties() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) { Text("Retry", color = White, fontFamily = Poppins) }
                    }
                }
            }
            is Resource.Success -> {
                val warranties = warrantiesState.data ?: emptyList()
                if (warranties.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = TextHint, modifier = Modifier.size(64.dp))
                            Text("No warranties found", fontFamily = Poppins, color = TextSecondary, fontSize = 16.sp)
                            Text("Warranties are created when orders are delivered", fontFamily = Poppins, color = TextHint, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(innerPadding).background(BackgroundLight),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(warranties) { warranty ->
                            WarrantyCard(warranty = warranty, onClick = { onWarrantyClick(warranty) })
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun WarrantyCard(warranty: MyWarrantyDto, onClick: () -> Unit) {
    val statusColor = when (warranty.status.uppercase()) {
        "ACTIVE" -> StatusSuccess
        "IN_REPAIR" -> SecondaryYellow
        "REPAIRED" -> PrimaryBlue
        "VOID", "EXPIRED" -> StatusError
        else -> TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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
                    text = warranty.product.name,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(
                        text = warranty.status,
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
                    Text("Serial", fontFamily = Poppins, fontSize = 11.sp, color = TextHint)
                    Text(warranty.serialNumber, fontFamily = Poppins, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Policy", fontFamily = Poppins, fontSize = 11.sp, color = TextHint)
                    Text(warranty.policyName, fontFamily = Poppins, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Purchase Date", fontFamily = Poppins, fontSize = 11.sp, color = TextHint)
                    Text(warranty.purchaseDate, fontFamily = Poppins, fontSize = 12.sp, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Expiry Date", fontFamily = Poppins, fontSize = 11.sp, color = TextHint)
                    Text(warranty.expiryDate, fontFamily = Poppins, fontSize = 12.sp, color = if (warranty.monthsRemaining <= 3) StatusError else TextSecondary)
                }
            }

            if (warranty.monthsRemaining > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${warranty.monthsRemaining} month(s) remaining",
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = if (warranty.monthsRemaining <= 3) StatusError else StatusSuccess,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
