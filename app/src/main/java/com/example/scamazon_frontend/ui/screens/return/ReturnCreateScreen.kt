package com.example.scamazon_frontend.ui.screens.`return`

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.components.*
import com.example.scamazon_frontend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnCreateScreen(
    orderId: Int,
    viewModel: ReturnViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    val createState by viewModel.createReturnState.collectAsStateWithLifecycle()

    var reason by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("RETURN") }
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("RETURN", "EXCHANGE")

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(createState) {
        when (createState) {
            is Resource.Success -> {
                viewModel.resetCreateState()
                onSuccess()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(createState?.message ?: "Failed to submit request")
                viewModel.resetCreateState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { LafyuuTopAppBar(title = "Request Return/Exchange", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundWhite)
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Order ID: #$orderId",
                style = Typography.titleMedium,
                color = TextPrimary
            )

            // Dropdown for Type
            Text("Request Type", style = Typography.labelLarge, color = TextPrimary)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderLight
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedType = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Reason Text Field
            Text("Reason", style = Typography.labelLarge, color = TextPrimary)
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("Please describe the issue with minimum 10 characters...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderLight
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            LafyuuPrimaryButton(
                text = if (createState is Resource.Loading) "Submitting..." else "Submit Request",
                onClick = { viewModel.submitReturnRequest(orderId, selectedType, reason) },
                enabled = reason.length >= 10 && createState !is Resource.Loading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
