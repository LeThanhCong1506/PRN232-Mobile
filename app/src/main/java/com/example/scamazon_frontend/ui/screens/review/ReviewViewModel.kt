package com.example.scamazon_frontend.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.review.BackendProductReviewsDto
import com.example.scamazon_frontend.data.models.review.ReviewDto
import com.example.scamazon_frontend.data.models.review.ReviewListDataDto
import com.example.scamazon_frontend.data.models.review.ReviewRequestDto
import com.example.scamazon_frontend.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val reviewRepo: ReviewRepository
) : ViewModel() {

    private val _reviewsState = MutableStateFlow<Resource<ReviewListDataDto>?>(null)
    val reviewsState: StateFlow<Resource<ReviewListDataDto>?> = _reviewsState.asStateFlow()

    private val _reviewSummary = MutableStateFlow<BackendProductReviewsDto?>(null)
    val reviewSummary: StateFlow<BackendProductReviewsDto?> = _reviewSummary.asStateFlow()

    private val _createReviewState = MutableStateFlow<Resource<ReviewDto>?>(null)
    val createReviewState: StateFlow<Resource<ReviewDto>?> = _createReviewState.asStateFlow()

    private val _selectedRating = MutableStateFlow(0)
    val selectedRating: StateFlow<Int> = _selectedRating.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment.asStateFlow()

    fun loadReviews(productId: Int, page: Int = 1, limit: Int = 10, rating: Int? = null) {
        viewModelScope.launch {
            _reviewsState.value = Resource.Loading()
            val result = reviewRepo.getProductReviews(productId, page, limit, rating)
            when (result) {
                is Resource.Success -> {
                    result.data?.let { (summary, list) ->
                        _reviewSummary.value = summary
                        _reviewsState.value = Resource.Success(list)
                    }
                }
                is Resource.Error -> _reviewsState.value = Resource.Error(result.message ?: "Failed to load reviews")
                is Resource.Loading -> _reviewsState.value = Resource.Loading()
            }
        }
    }

    fun submitReview(productId: Int) {
        if (_selectedRating.value == 0) return
        viewModelScope.launch {
            _createReviewState.value = Resource.Loading()
            val request = ReviewRequestDto(rating = _selectedRating.value, comment = _comment.value.ifBlank { null })
            val result = reviewRepo.createReview(productId, request)
            _createReviewState.value = when (result) {
                is Resource.Success -> Resource.Success(result.data!!)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to submit review")
                is Resource.Loading -> Resource.Loading()
            }
        }
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
