package com.example.scamazon_frontend.ui.screens.admin.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.admin.AdminUserDto
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.components.*
import com.example.scamazon_frontend.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminUserListScreen(
    viewModel: AdminUserViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {}
) {
    val usersState by viewModel.usersState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredUsers by viewModel.filteredUsers.collectAsStateWithLifecycle()
    val updateStatusState by viewModel.updateStatusState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(updateStatusState) {
        if (updateStatusState is Resource.Success) {
            snackbarHostState.showSnackbar("User status updated successfully")
            viewModel.resetUpdateStatus()
        } else if (updateStatusState is Resource.Error) {
            snackbarHostState.showSnackbar(updateStatusState?.message ?: "Update failed")
            viewModel.resetUpdateStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(BackgroundWhite)) {
                LafyuuTopAppBar(title = "User Management", onBackClick = onNavigateBack)
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                    placeholder = { Text("Search by name, email, or role...", fontFamily = Poppins, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextHint) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = TextHint)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderLight,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundLight)
        ) {
            when (usersState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ErrorState(
                            message = usersState.message ?: "Failed to load users",
                            onRetry = { viewModel.loadUsers() }
                        )
                    }
                }
                is Resource.Success -> {
                    if (filteredUsers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                title = if (searchQuery.isNotEmpty()) "No results found" else "No users yet",
                                message = if (searchQuery.isNotEmpty()) "Try a different search term." else "Registration is required first."
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredUsers) { user ->
                                UserCard(user = user, onToggleStatus = { viewModel.toggleUserStatus(user) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCard(user: AdminUserDto, onToggleStatus: () -> Unit) {
    val roleColor = if (user.roleName.equals("Admin", ignoreCase = true)) PrimaryBlue else StatusSuccess
    val statusColor = if (user.isActive == false) StatusError else StatusSuccess

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(roleColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username.firstOrNull()?.uppercase() ?: "?",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = roleColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = user.email,
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Role Badge
                    Surface(shape = RoundedCornerShape(4.dp), color = roleColor.copy(alpha = 0.1f)) {
                        Text(
                            text = user.roleName,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = roleColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    
                    // Status Badge
                    Surface(shape = RoundedCornerShape(4.dp), color = statusColor.copy(alpha = 0.1f)) {
                        Text(
                            text = if (user.isActive == false) "Inactive" else "Active",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Status Toggle Button
            IconButton(onClick = onToggleStatus) {
                Icon(
                    imageVector = if (user.isActive == false) Icons.Default.LockOpen else Icons.Default.Block,
                    contentDescription = "Toggle Status",
                    tint = if (user.isActive == false) statusColor else StatusError
                )
            }
        }
    }
}
