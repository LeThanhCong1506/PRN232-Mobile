package com.example.scamazon_frontend.ui.screens.map

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.store.StoreBranchDto
import com.example.scamazon_frontend.ui.components.LafyuuTopAppBar
import com.example.scamazon_frontend.ui.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val branchesState by viewModel.branchesState.collectAsState()

    // Configure osmdroid user agent
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Track selected branch for bottom card
    var selectedBranch by remember { mutableStateOf<StoreBranchDto?>(null) }

    Scaffold(
        topBar = {
            LafyuuTopAppBar(
                title = "Store Location",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = branchesState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is Resource.Error -> {
                    // Fallback to hardcoded location on error
                    val fallbackLat = 10.8447
                    val fallbackLng = 106.7801
                    MapContent(
                        branches = listOf(
                            StoreBranchDto(
                                name = "Scamazon Flagship Store",
                                address = "123-125 Đ. Lê Văn Việt, Thủ Đức, TP HCM",
                                latitude = fallbackLat,
                                longitude = fallbackLng,
                                phone = "(123) 456-7890",
                                isMainBranch = true
                            )
                        ),
                        selectedBranch = selectedBranch,
                        onBranchSelected = { selectedBranch = it }
                    )
                    // Show bottom card
                    val displayBranch = selectedBranch ?: StoreBranchDto(
                        name = "Scamazon Flagship Store",
                        address = "123-125 Đ. Lê Văn Việt, Thủ Đức, TP HCM",
                        latitude = fallbackLat, longitude = fallbackLng,
                        phone = "(123) 456-7890", isMainBranch = true
                    )
                    BranchInfoCard(
                        branch = displayBranch,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
                is Resource.Success -> {
                    val branches = state.data ?: emptyList()
                    if (branches.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No store branches found", color = TextSecondary)
                        }
                    } else {
                        // Auto-select main branch or first branch
                        LaunchedEffect(branches) {
                            if (selectedBranch == null) {
                                selectedBranch = branches.find { it.isMainBranch } ?: branches.first()
                            }
                        }

                        MapContent(
                            branches = branches,
                            selectedBranch = selectedBranch,
                            onBranchSelected = { selectedBranch = it }
                        )

                        // Bottom Info Card
                        selectedBranch?.let { branch ->
                            BranchInfoCard(
                                branch = branch,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapContent(
    branches: List<StoreBranchDto>,
    selectedBranch: StoreBranchDto?,
    onBranchSelected: (StoreBranchDto) -> Unit
) {
    val centerBranch = selectedBranch ?: branches.firstOrNull()
    val centerLat = centerBranch?.latitude ?: 10.8447
    val centerLng = centerBranch?.longitude ?: 106.7801

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(centerLat, centerLng))

                // Add markers for all branches
                branches.forEach { branch ->
                    val marker = Marker(this)
                    marker.position = GeoPoint(branch.latitude, branch.longitude)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = branch.name
                    marker.snippet = branch.address
                    marker.setOnMarkerClickListener { _, _ ->
                        onBranchSelected(branch)
                        true
                    }
                    overlays.add(marker)
                }
            }
        },
        update = { mapView ->
            mapView.controller.setCenter(GeoPoint(centerLat, centerLng))
        }
    )
}

@Composable
private fun BranchInfoCard(
    branch: StoreBranchDto,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        shape = LafyuuShapes.CardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = branch.name,
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            val info = buildString {
                append(branch.address)
                branch.phone?.let { append("\nPhone: $it") }
                if (branch.isMainBranch) append("\n⭐ Main Branch")
            }
            Text(
                text = info,
                style = Typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val uri = Uri.parse("google.navigation:q=${branch.latitude},${branch.longitude}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.google.android.apps.maps")
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${branch.latitude},${branch.longitude}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Get Directions", color = White)
            }
        }
    }
}
