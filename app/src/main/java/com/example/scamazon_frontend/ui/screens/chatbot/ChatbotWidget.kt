package com.example.scamazon_frontend.ui.screens.chatbot

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.scamazon_frontend.data.network.api.ProductSuggestionDto
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.theme.*

// ── Shared utilities ──────────────────────────────────────────────────────────

internal val productKeywords = listOf(
    "arduino", "esp32", "esp8266", "raspberry", "sensor", "module", "kit",
    "servo", "motor", "led", "display", "lcd", "oled", "relay", "shield",
    "breadboard", "resistor", "capacitor", "transistor", "microcontroller"
)

internal fun extractSearchKeyword(text: String): String? {
    val lower = text.lowercase()
    return productKeywords.firstOrNull { lower.contains(it) }
}

internal fun parseMarkdownText(text: String) = buildAnnotatedString {
    val parts = text.split("**")
    parts.forEachIndexed { index, part ->
        if (index % 2 == 0) append(part)
        else withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(part) }
    }
}

internal fun sourceBadge(source: String): Pair<String, Color> = when (source) {
    "faq"      -> "📚 FAQ"      to Color(0xFF2E7D32)
    "ai"       -> "🤖 AI·Groq"  to Color(0xFF1565C0)
    "fallback" -> "💡 Fallback" to Color(0xFFE65100)
    "error"    -> "⚠️ Error"    to Color(0xFFB00020)
    else       -> source        to Color(0xFF757575)
}

internal fun formatVND(price: Double): String {
    val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
    return "${formatter.format(price)}₫"
}

// ── ChatbotWidget ─────────────────────────────────────────────────────────────

@Composable
fun ChatbotWidget(
    onSearchProduct: (String) -> Unit = {},
    viewModel: ChatbotViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    var isExpanded by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val addToCartEvent by viewModel.addToCartEvent.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val widgetSuggestions = listOf(
        "How do I place an order?",
        "Payment methods?",
        "Shipping fee?"
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

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 80.dp, end = 16.dp)
        ) {
            Card(
                modifier = Modifier.width(320.dp).height(480.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth().background(PrimaryBlue).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(28.dp).background(White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SmartToy, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("STEM AI Assistant", color = White, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(StatusSuccess, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Online · Groq AI", color = White.copy(alpha = 0.8f), fontFamily = Poppins, fontSize = 10.sp)
                                }
                            }
                        }
                        IconButton(onClick = { isExpanded = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = White)
                        }
                    }

                    // Messages
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).background(Color(0xFFF5F7FA)).padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(messages) { msg ->
                            ChatbotMessageBubble(message = msg, onSearchProduct = onSearchProduct, onAddToCart = { viewModel.addToCart(it) })
                        }

                        if (messages.size == 1 && !isLoading) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    widgetSuggestions.forEach { s ->
                                        SuggestionChip(
                                            onClick = { viewModel.sendMessage(s) },
                                            label = { Text(s, fontFamily = Poppins, fontSize = 11.sp) },
                                            modifier = Modifier.height(28.dp),
                                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = White, labelColor = PrimaryBlue),
                                            border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = PrimaryBlue.copy(alpha = 0.35f))
                                        )
                                    }
                                }
                            }
                        }

                        if (isLoading) {
                            item {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                    Box(
                                        modifier = Modifier.clip(RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)).background(White).padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            CircularProgressIndicator(modifier = Modifier.size(13.dp), strokeWidth = 2.dp, color = PrimaryBlue)
                                            Text("Thinking...", fontSize = 12.sp, color = TextSecondary, fontFamily = Poppins)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Input
                    Row(
                        modifier = Modifier.fillMaxWidth().background(White).padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val canSend = inputText.isNotBlank() && !isLoading
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Ask me anything...", fontSize = 12.sp, color = TextHint) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (canSend) { viewModel.sendMessage(inputText.trim()); inputText = "" }
                            }),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderDefault)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { if (canSend) { viewModel.sendMessage(inputText.trim()); inputText = "" } },
                            modifier = Modifier.size(36.dp).background(if (canSend) PrimaryBlue else BorderDefault, CircleShape)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = if (canSend) White else TextHint, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.padding(bottom = 16.dp, end = 16.dp).size(56.dp),
            containerColor = PrimaryBlue,
            contentColor = White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.SmartToy,
                contentDescription = "Chatbot",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ── ChatbotMessageBubble (Widget) ─────────────────────────────────────────────

@Composable
internal fun ChatbotMessageBubble(
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
                modifier = Modifier.size(26.dp).background(PrimaryBlue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(5.dp))
        }

        Column(horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start) {
            // Bubble — widthIn on the Box directly ensures text wraps
            Box(
                modifier = Modifier
                    .widthIn(max = 220.dp)
                    .clip(
                        if (message.isUser) RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
                        else RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp)
                    )
                    .background(
                        when {
                            message.isUser -> PrimaryBlue
                            isError        -> Color(0xFFFFEBEB)
                            else           -> White
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text(
                    text = parseMarkdownText(message.content),
                    color = if (message.isUser) White else if (isError) Color(0xFFB00020) else TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = Poppins,
                    lineHeight = 17.sp,
                    softWrap = true
                )
            }

            // Timestamp + source
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                if (message.timestamp.isNotEmpty()) {
                    Text(message.timestamp, fontSize = 9.sp, color = TextHint, fontFamily = Poppins)
                }
                if (!message.isUser && message.source != null) {
                    val (label, color) = sourceBadge(message.source)
                    Text(label, fontSize = 9.sp, color = color, fontFamily = Poppins, fontWeight = FontWeight.Medium)
                }
            }

            // Product search chip
            if (searchKeyword != null && (message.products == null || message.products.isEmpty())) {
                Spacer(modifier = Modifier.height(3.dp))
                SuggestionChip(
                    onClick = { onSearchProduct(searchKeyword) },
                    label = { Text("🔍 Search: $searchKeyword", fontSize = 10.sp, fontFamily = Poppins) },
                    modifier = Modifier.height(26.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = White, labelColor = PrimaryBlue),
                    border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = PrimaryBlue.copy(alpha = 0.4f))
                )
            }

            // Product cards
            if (!message.products.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(message.products) { product ->
                        WidgetProductCard(product = product, onAddToCart = onAddToCart)
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetProductCard(product: ProductSuggestionDto, onAddToCart: (Int) -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.width(130.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Image area — shows placeholder when imageUrl is null or empty
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(Color(0xFFEEF2FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = PrimaryBlue.copy(alpha = 0.20f),
                    modifier = Modifier.size(28.dp)
                )
                if (!product.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(product.name, fontSize = 10.sp, fontFamily = Poppins, fontWeight = FontWeight.Medium, maxLines = 2, lineHeight = 13.sp)
                Text(formatVND(product.price), fontSize = 11.sp, fontFamily = Poppins, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                Button(
                    onClick = { onAddToCart(product.productId) },
                    modifier = Modifier.fillMaxWidth().height(26.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(12.dp), tint = White)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Add", fontSize = 10.sp, color = White, fontFamily = Poppins)
                }
            }
        }
    }
}
