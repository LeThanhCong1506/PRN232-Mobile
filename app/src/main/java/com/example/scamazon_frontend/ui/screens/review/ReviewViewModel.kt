package com.example.scamazon_frontend.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.review.ReviewResponse
import com.example.scamazon_frontend.data.models.review.ProductReviewSummaryResponse
import com.example.scamazon_frontend.data.models.review.CreateReviewRequest
import com.example.scamazon_frontend.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val reviewRepo: ReviewRepository
) : ViewModel() {

    private val _reviewsState = MutableStateFlow<Resource<ProductReviewSummaryResponse>?>(null)
    val reviewsState: StateFlow<Resource<ProductReviewSummaryResponse>?> = _reviewsState.asStateFlow()

    private val _createReviewState = MutableStateFlow<Resource<Unit>?>(null)
    val createReviewState: StateFlow<Resource<Unit>?> = _createReviewState.asStateFlow()

    private val _selectedRating = MutableStateFlow(0)
    val selectedRating: StateFlow<Int> = _selectedRating.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment.asStateFlow()

    fun loadReviews(productId: Int, page: Int = 1, limit: Int = 10) {
        viewModelScope.launch {
            _reviewsState.value = Resource.Loading()
            val result = reviewRepo.getProductReviews(productId, page, limit)
            when (result) {
                is Resource.Success -> {
                    _reviewsState.value = Resource.Success(result.data!!)
                }
                is Resource.Error -> {
                    _reviewsState.value = Resource.Error(result.message ?: "Failed to load reviews")
                }
                is Resource.Loading -> { }
            }
        }
    }

    fun submitReview(productId: Int) {
        if (_selectedRating.value == 0) return
        viewModelScope.launch {
            _createReviewState.value = Resource.Loading()
            val request = CreateReviewRequest(rating = _selectedRating.value, comment = _comment.value.ifBlank { null })
            val result = reviewRepo.createReview(productId, request)
            _createReviewState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to submit review")
                is Resource.Loading -> Resource.Loading()
            }
            if (result is Resource.Success) {
                loadReviews(productId)
            }
        }
    }

    fun setRating(rating: Int) {
        _selectedRating.value = rating
    }

    fun setComment(text: String) {
        _comment.value = text
    }

    fun deleteReview(productId: Int, reviewId: Int) {
        viewModelScope.launch {
            val result = reviewRepo.deleteReview(reviewId)
            if (result is Resource.Success) {
                loadReviews(productId)
            }
        }
    }

    fun resetCreateState() {
        _createReviewState.value = null
        _selectedRating.value = 0
        _comment.value = ""
    }
}
