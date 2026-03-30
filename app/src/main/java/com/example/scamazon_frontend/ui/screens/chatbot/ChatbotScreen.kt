package com.example.scamazon_frontend.ui.screens.chatbot

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.scamazon_frontend.data.network.api.ProductSuggestionDto
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.theme.*

@Composable
fun ChatbotScreen(
    viewModel: ChatbotViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {},
    onSearchProduct: (String) -> Unit = {}
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val addToCartEvent by viewModel.addToCartEvent.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val suggestions = listOf(
        "How do I place an order?",
        "What payment methods are available?",
        "Tell me about Arduino products",
        "What is the shipping fee?",
        "How does the warranty work?",
        "Do you have any coupons?"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    LaunchedEffect(addToCartEvent) {
        addToCartEvent?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearAddToCartEvent()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundWhite)) {

        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().background(PrimaryBlue).statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
            }
            Box(
                modifier = Modifier.size(40.dp).background(White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = null, tint = White, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("STEM AI Assistant", color = White, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).background(StatusSuccess, CircleShape))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Online · Powered by Groq AI", color = White.copy(alpha = 0.85f), fontFamily = Poppins, fontSize = 11.sp)
                }
            }
        }

        // ── Messages ─────────────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).background(Color(0xFFF5F7FA)),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                FullScreenMessageBubble(
                    message = msg,
                    onSearchProduct = onSearchProduct,
                    onAddToCart = { viewModel.addToCart(it) }
                )
            }

            if (messages.size == 1 && !isLoading) {
                item {
                    Column(modifier = Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Quick questions:", fontFamily = Poppins, fontSize = 12.sp, color = TextSecondary)
                        suggestions.forEach { suggestion ->
                            SuggestionChip(
                                onClick = { viewModel.sendMessage(suggestion) },
                                label = { Text(suggestion, fontFamily = Poppins, fontSize = 12.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = White, labelColor = PrimaryBlue),
                                border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = PrimaryBlue.copy(alpha = 0.4f))
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)).background(White).padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = PrimaryBlue)
                            Text("Thinking...", fontSize = 13.sp, color = TextSecondary, fontFamily = Poppins)
                        }
                    }
                }
            }
        }

        // ── Input bar ────────────────────────────────────────────────────────
        Surface(modifier = Modifier.fillMaxWidth(), color = White, shadowElevation = 8.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val canSend = inputText.isNotBlank() && !isLoading
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask me anything...", fontSize = 14.sp, color = TextHint) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (canSend) { viewModel.sendMessage(inputText.trim()); inputText = ""; keyboardController?.hide() }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderDefault)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { if (canSend) { viewModel.sendMessage(inputText.trim()); inputText = ""; keyboardController?.hide() } },
                    modifier = Modifier.size(48.dp).background(if (canSend) PrimaryBlue else BorderDefault, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = if (canSend) White else TextHint, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

// ── Full-screen message bubble ────────────────────────────────────────────────

@Composable
private fun FullScreenMessageBubble(
    message: ChatbotMessage,
    onSearchProduct: (String) -> Unit = {},
    onAddToCart: (Int) -> Unit = {}
) {
    val searchKeyword = if (!message.isUser) extractSearchKeyword(message.content) else null
    val isError = message.source == "error"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier.size(32.dp).background(PrimaryBlue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            // Bubble — widthIn on Box ensures text wraps correctly
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        if (message.isUser) RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
                        else RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
                    )
                    .background(
                        when {
                            message.isUser -> PrimaryBlue
                            isError        -> Color(0xFFFFEBEB)
                            else           -> White
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = parseMarkdownText(message.content),
                    color = if (message.isUser) White else if (isError) Color(0xFFB00020) else TextPrimary,
                    fontSize = 14.sp,
                    fontFamily = Poppins,
                    lineHeight = 20.sp,
                    softWrap = true
                )
            }

            // Timestamp + source badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 3.dp)
            ) {
                if (message.timestamp.isNotEmpty()) {
                    Text(message.timestamp, fontSize = 10.sp, color = TextHint, fontFamily = Poppins)
                }
                if (!message.isUser && message.source != null) {
                    val (label, color) = sourceBadge(message.source)
                    Text(label, fontSize = 10.sp, color = color, fontFamily = Poppins, fontWeight = FontWeight.Medium)
                }
            }

            // Product search chip (only if no product cards)
            if (searchKeyword != null && (message.products == null || message.products.isEmpty())) {
                Spacer(modifier = Modifier.height(4.dp))
                SuggestionChip(
                    onClick = { onSearchProduct(searchKeyword) },
                    label = { Text("🔍 Search: $searchKeyword", fontSize = 12.sp, fontFamily = Poppins) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = White, labelColor = PrimaryBlue),
                    border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = PrimaryBlue.copy(alpha = 0.4f))
                )
            }

            // Product cards from BE
            if (!message.products.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    message.products.forEach { product ->
                        FullScreenProductCard(product = product, onAddToCart = onAddToCart)
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenProductCard(product: ProductSuggestionDto, onAddToCart: (Int) -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            // Product image
            AsyncImage(
                model = ImageRequest.Builder(context).data(product.imageUrl).crossfade(true).build(),
                contentDescription = product.name,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontSize = 13.sp, fontFamily = Poppins, fontWeight = FontWeight.Medium, maxLines = 2, lineHeight = 17.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(formatVND(product.price), fontSize = 14.sp, fontFamily = Poppins, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Add to cart
            Button(
                onClick = { onAddToCart(product.productId) },
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp), tint = White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 11.sp, color = White, fontFamily = Poppins)
            }
        }
    }
}
