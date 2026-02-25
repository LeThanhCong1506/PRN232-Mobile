package com.example.scamazon_frontend.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.scamazon_frontend.core.utils.TokenManager
import com.example.scamazon_frontend.ui.screens.admin.category.AdminCategoryViewModel
import com.example.scamazon_frontend.ui.screens.admin.chat.AdminChatDetailViewModel
import com.example.scamazon_frontend.ui.screens.admin.chat.AdminChatListViewModel
import com.example.scamazon_frontend.ui.screens.admin.dashboard.AdminDashboardViewModel
import com.example.scamazon_frontend.ui.screens.admin.order.AdminOrderViewModel
import com.example.scamazon_frontend.ui.screens.admin.product.AdminProductViewModel
import com.example.scamazon_frontend.ui.screens.auth.AuthViewModel
import com.example.scamazon_frontend.ui.screens.cart.CartViewModel
import com.example.scamazon_frontend.ui.screens.chat.ChatViewModel
import com.example.scamazon_frontend.ui.screens.checkout.CheckoutViewModel
import com.example.scamazon_frontend.ui.screens.checkout.PaymentQRViewModel
import com.example.scamazon_frontend.ui.screens.favorite.FavoriteViewModel
import com.example.scamazon_frontend.ui.screens.home.HomeViewModel
import com.example.scamazon_frontend.ui.screens.notification.NotificationViewModel
import com.example.scamazon_frontend.ui.screens.order.OrderHistoryViewModel
import com.example.scamazon_frontend.ui.screens.product.ProductDetailViewModel
import com.example.scamazon_frontend.ui.screens.product.ProductListViewModel
import com.example.scamazon_frontend.ui.screens.profile.ProfileViewModel
import com.example.scamazon_frontend.ui.screens.review.ReviewViewModel
import com.example.scamazon_frontend.ui.screens.search.SearchViewModel

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val tokenManager = TokenManager(context)
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(tokenManager) as T
        }

        // All other ViewModels no longer need constructor arguments
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel()
            modelClass.isAssignableFrom(ProductListViewModel::class.java) -> ProductListViewModel()
            modelClass.isAssignableFrom(ProductDetailViewModel::class.java) -> ProductDetailViewModel()
            modelClass.isAssignableFrom(CartViewModel::class.java) -> CartViewModel()
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel()
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel()
            modelClass.isAssignableFrom(CheckoutViewModel::class.java) -> CheckoutViewModel()
            modelClass.isAssignableFrom(OrderHistoryViewModel::class.java) -> OrderHistoryViewModel()
            modelClass.isAssignableFrom(AdminDashboardViewModel::class.java) -> AdminDashboardViewModel()
            modelClass.isAssignableFrom(AdminProductViewModel::class.java) -> AdminProductViewModel()
            modelClass.isAssignableFrom(AdminCategoryViewModel::class.java) -> AdminCategoryViewModel()
            modelClass.isAssignableFrom(AdminOrderViewModel::class.java) -> AdminOrderViewModel()
            modelClass.isAssignableFrom(PaymentQRViewModel::class.java) -> PaymentQRViewModel()
            modelClass.isAssignableFrom(ReviewViewModel::class.java) -> ReviewViewModel()
            modelClass.isAssignableFrom(FavoriteViewModel::class.java) -> FavoriteViewModel()
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> ChatViewModel()
            modelClass.isAssignableFrom(AdminChatListViewModel::class.java) -> AdminChatListViewModel()
            modelClass.isAssignableFrom(AdminChatDetailViewModel::class.java) -> AdminChatDetailViewModel()
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> NotificationViewModel()
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        } as T
    }
}
