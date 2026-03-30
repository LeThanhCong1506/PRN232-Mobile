package com.example.scamazon_frontend.ui.screens.warranty

import androidx.compose.foundation.background
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
import com.example.scamazon_frontend.data.models.warranty.AdminWarrantyClaimDto
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.components.*
import com.example.scamazon_frontend.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyClaimsScreen(
    viewModel: WarrantyViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {}
) {
    val claimsState by viewModel.myClaimsState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadMyClaims()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        LafyuuTopAppBar(title = "My Warranty Claims", onBackClick = onNavigateBack)

        when (claimsState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(
                        message = claimsState.message ?: "Failed to load claims",
                        onRetry = { viewModel.loadMyClaims() }
                    )
                }
            }
            is Resource.Success -> {
                val claims = claimsState.data?.items ?: emptyList()
                if (claims.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            title = "No Warranty Claims",
                            message = "You haven't submitted any warranty claims yet."
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
                        items(claims) { claim ->
                            ClaimCard(claim = claim)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClaimCard(claim: AdminWarrantyClaimDto) {
    val statusColor = when (claim.status.uppercase()) {
        "RESOLVED", "APPROVED" -> StatusSuccess
        "REJECTED" -> StatusError
        else -> SecondaryYellow // SUBMITTED / PENDING
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = "Claim #${claim.claimId}",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(
                        text = claim.status,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = claim.product.name,
                fontFamily = Poppins,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Submitted", fontFamily = Poppins, fontSize = 11.sp, color = TextHint)
                    Text(formatDate(claim.submittedAt), fontFamily = Poppins, fontSize = 13.sp, color = TextPrimary)
                }
                if (claim.resolvedDate != null) {
                    Column {
                        Text("Resolved", fontFamily = Poppins, fontSize = 11.sp, color = TextHint)
                        Text(claim.resolvedDate, fontFamily = Poppins, fontSize = 13.sp, color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Issue Description", fontFamily = Poppins, fontSize = 11.sp, color = TextHint)
            Text(
                text = claim.issueDescription,
                fontFamily = Poppins,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 3,
                modifier = Modifier.padding(top = 2.dp)
            )

            if (!claim.resolutionNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BackgroundLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Admin Resolution Note", fontFamily = Poppins, fontSize = 11.sp, color = TextHint, fontWeight = FontWeight.Bold)
                        Text(
                            text = claim.resolutionNote,
                            fontFamily = Poppins,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(isoString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(isoString) ?: return isoString
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        isoString
    }
}
