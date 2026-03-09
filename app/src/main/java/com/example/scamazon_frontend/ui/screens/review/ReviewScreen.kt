package com.example.scamazon_frontend.ui.screens.review

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.review.ReviewResponse
import com.example.scamazon_frontend.data.models.review.ProductReviewSummaryResponse
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.components.*
import com.example.scamazon_frontend.ui.theme.*

@Composable
fun ReviewScreen(
    productId: Int,
    canWrite: Boolean = false,
    viewModel: ReviewViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    onNavigateBack: () -> Unit = {}
) {
    val reviewsState by viewModel.reviewsState.collectAsStateWithLifecycle()
    val createReviewState by viewModel.createReviewState.collectAsStateWithLifecycle()
    val selectedRating by viewModel.selectedRating.collectAsStateWithLifecycle()
    val comment by viewModel.comment.collectAsStateWithLifecycle()

    LaunchedEffect(productId) {
        viewModel.loadReviews(productId)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(createReviewState) {
        when (createReviewState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Đánh giá thành công!")
                viewModel.resetCreateState()
                viewModel.loadReviews(productId)
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(createReviewState?.message ?: "Lỗi gửi đánh giá")
                viewModel.resetCreateState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundWhite)
        ) {
            LafyuuTopAppBar(title = "Đánh giá sản phẩm", onBackClick = onNavigateBack)

            when (val state = reviewsState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorState(
                            message = state.message ?: "Lỗi tải đánh giá",
                            onRetry = { viewModel.loadReviews(productId) }
                        )
                    }
                }
                is Resource.Success -> {
                    val data = state.data!!
                    ReviewContent(
                        data = data,
                        canWrite = canWrite,
                        selectedRating = selectedRating,
                        comment = comment,
                        isSubmitting = createReviewState is Resource.Loading,
                        onRatingChange = { viewModel.setRating(it) },
                        onCommentChange = { viewModel.setComment(it) },
                        onSubmit = { viewModel.submitReview(productId) },
                        onDeleteReview = { reviewId -> viewModel.deleteReview(productId, reviewId) }
                    )
                }
                null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewContent(
    data: ProductReviewSummaryResponse,
    canWrite: Boolean,
    selectedRating: Int,
    comment: String,
    isSubmitting: Boolean,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDeleteReview: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rating Summary
        item {
            RatingSummarySection(data)
        }

        // Write Review Section
        if (canWrite) {
            item {
                WriteReviewSection(
                    selectedRating = selectedRating,
                    comment = comment,
                    isSubmitting = isSubmitting,
                    onRatingChange = onRatingChange,
                    onCommentChange = onCommentChange,
                    onSubmit = onSubmit
                )
            }
        }

        // Reviews Header
        item {
            Text(
                text = "Tất cả đánh giá (${data.totalReviews})",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
        }

        // Review List
        val reviewList = data.reviews.orEmpty()
        if (reviewList.isEmpty()) {
            item {
                EmptyState(
                    title = "Chưa có đánh giá",
                    message = "Chưa có đánh giá nào"
                )
            }
        } else {
            items(reviewList) { review ->
                ReviewCard(
                    review = review,
                    onDelete = { onDeleteReview(review.reviewId) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun RatingSummarySection(data: ProductReviewSummaryResponse) {
    val totalReviews = data.totalReviews
    val avgRating = data.averageRating.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Đánh giá tổng quan",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f", avgRating),
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        color = PrimaryBlue
                    )
                    RatingBar(rating = avgRating)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalReviews đánh giá",
                        style = Typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun WriteReviewSection(
    selectedRating: Int,
    comment: String,
    isSubmitting: Boolean,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlueSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Viết đánh giá",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Star rating
            Text(
                text = "Chọn số sao:",
                style = Typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                InteractiveRatingBar(
                    currentRating = selectedRating,
                    onRatingChange = onRatingChange
                )
            }

            if (selectedRating > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (selectedRating) {
                        1 -> "Rất tệ"
                        2 -> "Tệ"
                        3 -> "Bình thường"
                        4 -> "Tốt"
                        5 -> "Tuyệt vời"
                        else -> ""
                    },
                    style = Typography.bodySmall,
                    color = AccentGold,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comment
            OutlinedTextField(
                value = comment,
                onValueChange = { if (it.length <= 1000) onCommentChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nhận xét (tùy chọn)") },
                placeholder = { Text("Chia sẻ trải nghiệm của bạn về sản phẩm...") },
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderDefault,
                    cursorColor = PrimaryBlue,
                    focusedLabelColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Text(
                text = "${comment.length}/1000",
                style = Typography.bodySmall,
                color = TextHint,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Submit button
            LafyuuPrimaryButton(
                text = if (isSubmitting) "Đang gửi..." else "Gửi đánh giá",
                onClick = onSubmit,
                enabled = selectedRating > 0 && !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReviewCard(review: ReviewResponse, onDelete: () -> Unit = {}) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xoá đánh giá", fontFamily = Poppins, fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn xoá đánh giá này?", fontFamily = Poppins) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("Xoá", color = StatusError, fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Huỷ", color = TextSecondary, fontFamily = Poppins)
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // User info row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avatar placeholder (backend doesn't provide avatar)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PrimaryBlueSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.reviewer ?: "Anonymous",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = review.createdAt?.take(10) ?: "",
                        style = Typography.bodySmall,
                        color = TextHint
                    )
                }

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xoá đánh giá",
                        tint = TextHint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rating
            RatingBar(rating = review.rating.toFloat())

            // Comment
            if (!review.comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = review.comment,
                    style = Typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
