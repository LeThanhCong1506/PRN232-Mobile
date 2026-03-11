package com.example.scamazon_frontend.core.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton to manage cart badge count globally across the app.
 * This allows the cart icon badge to update immediately when items are added.
 */
object CartBadgeManager {
    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount.asStateFlow()

    private val _triggerAnimation = MutableStateFlow(false)
    val triggerAnimation: StateFlow<Boolean> = _triggerAnimation.asStateFlow()

    fun updateCount(count: Int) {
        _cartItemCount.value = count
    }

    fun incrementCount(by: Int = 1) {
        _cartItemCount.value += by
        // Trigger animation
        _triggerAnimation.value = true
    }

    fun resetAnimation() {
        _triggerAnimation.value = false
    }

    fun getCount(): Int = _cartItemCount.value
}
