package com.example.scamazon_frontend.ui.screens.admin.warranty

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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

@Composable
fun AdminWarrantyClaimsScreen(
    viewModel: AdminWarrantyViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val claimsState by viewModel.claimsState.collectAsStateWithLifecycle()
    val resolveState by viewModel.resolveState.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    var showResolveDialog by remember { mutableStateOf<AdminWarrantyClaimDto?>(null) }
    var selectedResolution by remember { mutableStateOf("APPROVED") }
    var resolutionNote by remember { mutableStateOf("") }

    LaunchedEffect(resolveState) {
        when (resolveState) {
            is Resource.Success -> {
                Toast.makeText(context, "Claim resolved successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetResolveState()
                showResolveDialog = null
            }
            is Resource.Error -> {
                Toast.makeText(context, resolveState?.message ?: "Failed to resolve", Toast.LENGTH_SHORT).show()
                viewModel.resetResolveState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        LafyuuTopAppBar(title = "Warranty Claims", onBackClick = onNavigateBack)

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundWhite)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("all" to "All", "submitted" to "Submitted", "approved" to "Approved", "rejected" to "Rejected", "resolved" to "Resolved").forEach { (value, label) ->
                val isSelected = selectedFilter == value
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onFilterChange(value) },
                    label = {
                        Text(
                            label,
                            fontFamily = Poppins,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = White,
                        containerColor = BackgroundLight,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = BorderLight,
                        selectedBorderColor = PrimaryBlue,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        when (claimsState) {
            is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ErrorState(
                    message = claimsState.message ?: "Error loading claims",
                    onRetry = { viewModel.loadClaims() }
                )
            }
            is Resource.Success -> {
                val claims = claimsState.data ?: emptyList()
                if (claims.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            title = "No Claims",
                            message = "No warranty claims found"
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(claims, key = { it.claimId }) { claim ->
                            AdminClaimCard(
                                claim = claim,
                                onResolve = {
                                    showResolveDialog = claim
                                    selectedResolution = "APPROVED"
                                    resolutionNote = ""
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    // Resolve Dialog
    showResolveDialog?.let { claim ->
        AlertDialog(
            onDismissRequest = { showResolveDialog = null },
            title = { Text("Resolve Claim #${claim.claimId}", fontFamily = Poppins, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Customer: ${claim.customer.fullName}", fontFamily = Poppins, fontSize = 13.sp)
                    Text("Issue: ${claim.issueDescription.take(100)}...", fontFamily = Poppins, fontSize = 12.sp, color = TextSecondary)

                    Text("Resolution:", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    listOf("APPROVED", "REJECTED", "RESOLVED").forEach { res ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedResolution == res,
                                onClick = { selectedResolution = res },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                            )
                            Text(res, fontFamily = Poppins, fontSize = 13.sp)
                        }
                    }

                    OutlinedTextField(
                        value = resolutionNote,
                        onValueChange = { resolutionNote = it },
                        label = { Text("Note (optional)", fontFamily = Poppins) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderDefault,
                            cursorColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.resolveClaim(claim.claimId, selectedResolution, resolutionNote.ifBlank { null }) },
                    enabled = resolveState !is Resource.Loading
                ) {
                    if (resolveState is Resource.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PrimaryBlue, strokeWidth = 2.dp)
                    } else {
                        Text("Confirm", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = null }) {
                    Text("Cancel", fontFamily = Poppins, color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun AdminClaimCard(claim: AdminWarrantyClaimDto, onResolve: () -> Unit) {
    val (statusColor, statusLabel) = when (claim.status.uppercase()) {
        "SUBMITTED" -> SecondaryYellow to "Submitted"
        "APPROVED" -> StatusSuccess to "Approved"
        "REJECTED" -> StatusError to "Rejected"
        "RESOLVED" -> PrimaryBlue to "Resolved"
        else -> TextSecondary to claim.status
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Claim #${claim.claimId}", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(statusLabel, fontFamily = Poppins, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Product: ${claim.product.name}", fontFamily = Poppins, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text("Customer: ${claim.customer.fullName}", fontFamily = Poppins, fontSize = 12.sp, color = TextSecondary)
            claim.customer.phone?.let { Text("Phone: $it", fontFamily = Poppins, fontSize = 12.sp, color = TextSecondary) }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Issue:", fontFamily = Poppins, fontSize = 12.sp, color = TextHint, fontWeight = FontWeight.SemiBold)
            Text(
                text = if (claim.issueDescription.length > 120) claim.issueDescription.take(120) + "..." else claim.issueDescription,
                fontFamily = Poppins, fontSize = 13.sp, color = TextPrimary
            )

            claim.resolutionNote?.let { note ->
                Spacer(modifier = Modifier.height(4.dp))
                Text("Resolution note: $note", fontFamily = Poppins, fontSize = 12.sp, color = PrimaryBlue)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(claim.submittedAt.take(10), fontFamily = Poppins, fontSize = 11.sp, color = TextHint)
                if (claim.status.uppercase() == "SUBMITTED") {
                    LafyuuSmallButton(
                        text = "Resolve",
                        onClick = onResolve
                    )
                }
            }
        }
    }
}
