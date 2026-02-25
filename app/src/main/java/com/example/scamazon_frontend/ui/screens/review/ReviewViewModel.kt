package com.example.scamazon_frontend.ui.screens.review

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.review.ReviewDto
import com.example.scamazon_frontend.data.models.review.ReviewListDataDto
import com.example.scamazon_frontend.data.models.review.ReviewUserDto
import com.example.scamazon_frontend.data.models.review.ReviewPaginationDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReviewViewModel : ViewModel() {

    private val _reviewsState = MutableStateFlow<Resource<ReviewListDataDto>?>(null)
    val reviewsState: StateFlow<Resource<ReviewListDataDto>?> = _reviewsState.asStateFlow()

    private val _createReviewState = MutableStateFlow<Resource<ReviewDto>?>(null)
    val createReviewState: StateFlow<Resource<ReviewDto>?> = _createReviewState.asStateFlow()

    private val _selectedRating = MutableStateFlow(0)
    val selectedRating: StateFlow<Int> = _selectedRating.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment.asStateFlow()

    fun loadReviews(productId: Int, page: Int = 1, limit: Int = 10, rating: Int? = null) {
        _reviewsState.value = Resource.Loading()
        val filtered = if (rating != null) {
            MockData.reviews.copy(reviews = MockData.reviews.reviews.filter { it.rating == rating })
        } else MockData.reviews
        _reviewsState.value = Resource.Success(filtered)
    }

    fun submitReview(productId: Int) {
        if (_selectedRating.value == 0) return
        _createReviewState.value = Resource.Loading()
        _createReviewState.value = Resource.Success(
            ReviewDto(
                id = 99,
                rating = _selectedRating.value,
                comment = _comment.value.ifBlank { null },
                user = ReviewUserDto(1, "demo_user", "Nguyễn Văn A", null),
                createdAt = "2025-02-25"
            )
        )
    }

    fun setRating(rating: Int) {
        _selectedRating.value = rating
    }

    fun setComment(text: String) {
        _comment.value = text
    }

    fun resetCreateState() {
        _createReviewState.value = null
        _selectedRating.value = 0
        _comment.value = ""
    }
}
