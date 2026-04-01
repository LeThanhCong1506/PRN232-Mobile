package com.example.scamazon_frontend.ui.screens.admin.warranty

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.scamazon_frontend.data.models.warranty.CreateWarrantyRequest
import com.example.scamazon_frontend.data.models.warranty.UpdateWarrantyRequest
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.screens.admin.product.AdminProductViewModel
import com.example.scamazon_frontend.ui.theme.*

/**
 * Form tạo / cập nhật warranty (Admin only).
 * warrantyId == null  →  Create mode
 * warrantyId != null  →  Edit mode (load from ViewModel.editingWarranty)
 *
 * Backend Create: POST /api/warranty
 *   Body: { serialNumber, warrantyPolicyId, startDate, endDate, notes? }
 *
 * Backend Update: PUT /api/warranty/{id}
 *   Body: { endDate, isActive, notes? }
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWarrantyFormScreen(
    warrantyId: Int? = null,
    viewModel: AdminWarrantyManagementViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    productViewModel: AdminProductViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val editingWarranty by viewModel.editingWarranty.collectAsStateWithLifecycle()
    val policiesState by productViewModel.policiesState.collectAsStateWithLifecycle()

    val isEdit = warrantyId != null

    // ---- Create mode fields ----
    var serialNumber by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }

    // ---- Shared fields ----
    var endDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    // ---- Policy selection ----
    var selectedPolicyId by remember { mutableStateOf<Int?>(null) }
    var selectedPolicyName by remember { mutableStateOf("Chọn chính sách") }
    var policyDropdownExpanded by remember { mutableStateOf(false) }

    // When editingWarranty loads, pre-populate edit fields
    LaunchedEffect(editingWarranty) {
        if (isEdit && editingWarranty != null) {
            val w = editingWarranty!!
            endDate = w.endDate.take(10)
            notes = w.notes ?: ""
            isActive = w.isActive
            // Pre-select policy by id
            selectedPolicyId = w.warrantyPolicyId
            selectedPolicyName = w.policyName
        }
    }

    // When policies load, sync selectedPolicyName if id already set
    LaunchedEffect(policiesState, selectedPolicyId) {
        if (policiesState is Resource.Success && selectedPolicyId != null) {
            val match = (policiesState as Resource.Success).data
                ?.find { it.policyId == selectedPolicyId }
            if (match != null) selectedPolicyName = match.policyName
        }
    }

    // Handle save result
    LaunchedEffect(saveState) {
        when (saveState) {
            is Resource.Success -> {
                Toast.makeText(
                    context,
                    if (isEdit) "Cập nhật warranty thành công" else "Tạo warranty thành công",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetSaveState()
                viewModel.clearEditingWarranty()
                onNavigateBack()
            }
            is Resource.Error -> {
                Toast.makeText(context, (saveState as Resource.Error).message ?: "Thất bại", Toast.LENGTH_LONG).show()
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEdit) "Sửa Warranty" else "Tạo Warranty",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearEditingWarranty()
                        onNavigateBack()
                    }) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ==================== READ-ONLY INFO (Edit mode) ====================
            if (isEdit && editingWarranty != null) {
                val w = editingWarranty!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Thông tin hiện tại", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                        Text("Sản phẩm: ${w.productName ?: "-"}", fontFamily = Poppins, fontSize = 12.sp, color = TextSecondary)
                        Text("Serial: ${w.serialNumber}", fontFamily = Poppins, fontSize = 12.sp, color = TextSecondary)
                        Text("Ngày bắt đầu: ${w.startDate.take(10)}", fontFamily = Poppins, fontSize = 12.sp, color = TextSecondary)
                        Text("Chính sách: ${w.policyName}", fontFamily = Poppins, fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            // ==================== CREATE MODE ONLY ====================
            if (!isEdit) {
                OutlinedTextField(
                    value = serialNumber,
                    onValueChange = { serialNumber = it },
                    label = { Text("Serial Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderLight
                    )
                )

                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Ngày bắt đầu bảo hành * (yyyy-MM-dd)") },
                    placeholder = { Text("2024-01-15", color = TextHint) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderLight
                    )
                )

                // Policy dropdown (Create mode)
                ExposedDropdownMenuBox(
                    expanded = policyDropdownExpanded,
                    onExpandedChange = { policyDropdownExpanded = !policyDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedPolicyName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Chính sách bảo hành *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = policyDropdownExpanded,
                        onDismissRequest = { policyDropdownExpanded = false }
                    ) {
                        when (val ps = policiesState) {
                            is Resource.Success -> ps.data?.forEach { policy ->
                                DropdownMenuItem(
                                    text = { Text("${policy.policyName} (${policy.durationMonths} tháng)", fontFamily = Poppins) },
                                    onClick = {
                                        selectedPolicyId = policy.policyId
                                        selectedPolicyName = policy.policyName
                                        // Auto-fill end date based on policy duration
                                        if (startDate.isNotBlank()) {
                                            runCatching {
                                                val start = java.time.LocalDate.parse(startDate.trim())
                                                endDate = start.plusMonths(policy.durationMonths.toLong()).toString()
                                            }
                                        }
                                        policyDropdownExpanded = false
                                    }
                                )
                            }
                            is Resource.Loading -> DropdownMenuItem(
                                text = { Text("Đang tải...", fontFamily = Poppins) }, onClick = {}
                            )
                            else -> DropdownMenuItem(
                                text = { Text("Không có chính sách", fontFamily = Poppins) }, onClick = {}
                            )
                        }
                    }
                }
            }

            // ==================== SHARED: end date ====================
            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text(if (isEdit) "Ngày hết hạn mới * (yyyy-MM-dd)" else "Ngày kết thúc bảo hành * (yyyy-MM-dd)") },
                placeholder = { Text("2026-01-15", color = TextHint) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderLight
                )
            )

            // ==================== EDIT MODE: isActive toggle ====================
            if (isEdit) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Còn hiệu lực", fontFamily = Poppins, fontSize = 14.sp, color = TextPrimary)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = White,
                            checkedTrackColor = StatusSuccess
                        )
                    )
                }
            }

            // ==================== SHARED: notes ====================
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Ghi chú (tùy chọn)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderLight
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            val isSaving = saveState is Resource.Loading
            Button(
                onClick = {
                    if (endDate.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập ngày hết hạn", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (isEdit && warrantyId != null) {
                        viewModel.updateWarranty(
                            warrantyId,
                            UpdateWarrantyRequest(
                                endDate = endDate.trim(),
                                isActive = isActive,
                                notes = notes.ifBlank { null }
                            )
                        )
                    } else {
                        if (serialNumber.isBlank() || startDate.isBlank() || selectedPolicyId == null) {
                            Toast.makeText(context, "Vui lòng điền đầy đủ: serial, ngày bắt đầu, chính sách", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.createWarranty(
                            CreateWarrantyRequest(
                                serialNumber = serialNumber.trim(),
                                warrantyPolicyId = selectedPolicyId!!,
                                startDate = startDate.trim(),
                                endDate = endDate.trim(),
                                notes = notes.ifBlank { null }
                            )
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (isEdit) "Cập nhật" else "Tạo Warranty",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                }
            }
        }
    }
}
