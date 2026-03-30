package com.example.scamazon_frontend.ui.screens.chatbot

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.theme.*

@Composable
fun ChatbotScreen(
    viewModel: ChatbotViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {}
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val suggestions = listOf(
        "How do I place an order?",
        "What payment methods are available?",
        "Tell me about Arduino products",
        "What is the shipping fee?",
        "How does the warranty work?"
    )

    // Auto-scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundWhite)) {

        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryBlue)
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "STEM AI Assistant",
                    color = White,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(StatusSuccess, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Online · Powered by Groq AI",
                        color = White.copy(alpha = 0.85f),
                        fontFamily = Poppins,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // ── Messages ─────────────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFF5F7FA)),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                FullScreenMessageBubble(message = msg)
            }

            // Quick suggestion chips — shown only before first user message
            if (messages.size == 1 && !isLoading) {
                item {
                    Column(
                        modifier = Modifier.padding(top = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Quick questions:",
                            fontFamily = Poppins,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        suggestions.forEach { suggestion ->
                            SuggestionChip(
                                onClick = { viewModel.sendMessage(suggestion) },
                                label = {
                                    Text(
                                        text = suggestion,
                                        fontFamily = Poppins,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = White,
                                    labelColor = PrimaryBlue
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = PrimaryBlue.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                }
            }

            // Typing indicator
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                                .background(White)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryBlue
                            )
                            Text(
                                text = "Thinking...",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                fontFamily = Poppins
                            )
                        }
                    }
                }
            }
        }

        // ── Input bar ────────────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val canSend = inputText.isNotBlank() && !isLoading
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text("Ask me anything...", fontSize = 14.sp, color = TextHint)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (canSend) {
                                viewModel.sendMessage(inputText.trim())
                                inputText = ""
                                keyboardController?.hide()
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderDefault
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (canSend) {
                            viewModel.sendMessage(inputText.trim())
                            inputText = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(if (canSend) PrimaryBlue else BorderDefault, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (canSend) White else TextHint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// Full-screen bubble with source badge for bot messages
@Composable
private fun FullScreenMessageBubble(message: ChatbotMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    if (message.isUser)
                        RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
                    else
                        RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
                )
                .background(if (message.isUser) PrimaryBlue else White)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.content,
                color = if (message.isUser) White else TextPrimary,
                fontSize = 14.sp,
                fontFamily = Poppins,
                lineHeight = 20.sp
            )
        }
        // Source badge for bot messages
        if (!message.isUser && message.source != null && message.source != "error") {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when (message.source) {
                    "faq"      -> "FAQ"
                    "ai"       -> "AI · Groq"
                    "fallback" -> "Fallback"
                    else       -> ""
                },
                fontSize = 10.sp,
                color = TextHint,
                fontFamily = Poppins
            )
        }
    }
}
