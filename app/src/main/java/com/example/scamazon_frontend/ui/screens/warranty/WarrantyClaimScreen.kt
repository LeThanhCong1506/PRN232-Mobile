package com.example.scamazon_frontend.ui.screens.warranty

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarrantyClaimScreen(
    warrantyId: Int,
    productName: String = "",
    viewModel: WarrantyViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val submitClaimState by viewModel.submitClaimState.collectAsStateWithLifecycle()
    val issueDescription by viewModel.issueDescription.collectAsStateWithLifecycle()
    val contactPhone by viewModel.contactPhone.collectAsStateWithLifecycle()
    val isSubmitting = submitClaimState is Resource.Loading

    LaunchedEffect(submitClaimState) {
        when (submitClaimState) {
            is Resource.Success -> {
                Toast.makeText(context, "Warranty claim submitted successfully!", Toast.LENGTH_LONG).show()
                viewModel.resetSubmitState()
                onNavigateBack()
            }
            is Resource.Error -> {
                Toast.makeText(context, submitClaimState?.message ?: "Failed to submit claim", Toast.LENGTH_LONG).show()
                viewModel.resetSubmitState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Submit Warranty Claim", fontFamily = Poppins, fontWeight = FontWeight.Bold, color = TextPrimary)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Product info
            if (productName.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlueSoft)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Product", fontFamily = Poppins, fontSize = 11.sp, color = TextHint)
                        Text(productName, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = PrimaryBlue)
                    }
                }
            }

            // Issue Description
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Issue Description *", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = issueDescription,
                        onValueChange = { viewModel.setIssueDescription(it) },
                        placeholder = { Text("Describe the issue in detail (min 10 characters)...", fontFamily = Poppins, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderDefault
                        )
                    )
                    if (issueDescription.isNotEmpty() && issueDescription.length < 10) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Minimum 10 characters required", fontFamily = Poppins, fontSize = 11.sp, color = StatusError)
                    }
                }
            }

            // Contact Phone
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Contact Phone (optional)", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { viewModel.setContactPhone(it) },
                        placeholder = { Text("Your phone number...", fontFamily = Poppins, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderDefault
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.submitClaim(warrantyId) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = issueDescription.length >= 10 && !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, disabledContainerColor = TextHint)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = White, strokeWidth = 2.dp)
                } else {
                    Text("Submit Claim", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, color = White, fontSize = 16.sp)
                }
            }
        }
    }
}
