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
import com.example.scamazon_frontend.ui.screens.admin.warranty.AdminWarrantyViewModel
import com.example.scamazon_frontend.ui.screens.auth.AuthViewModel
import com.example.scamazon_frontend.data.network.ApiClient
import com.example.scamazon_frontend.data.network.api.AdminOrderApi
import com.example.scamazon_frontend.data.network.api.AdminProductApi
import com.example.scamazon_frontend.data.network.api.AuthApi
import com.example.scamazon_frontend.data.network.api.CartApi
import com.example.scamazon_frontend.data.network.api.ProductApi
import com.example.scamazon_frontend.data.network.api.ChatApi
import com.example.scamazon_frontend.data.network.api.ReviewApi
import com.example.scamazon_frontend.data.network.api.StoreApi
import com.example.scamazon_frontend.data.network.api.WarrantyApi
import com.example.scamazon_frontend.data.network.SignalRChatClient
import com.example.scamazon_frontend.data.repository.AdminOrderRepository
import com.example.scamazon_frontend.data.repository.ChatRepository
import com.example.scamazon_frontend.data.repository.StoreRepository
import com.example.scamazon_frontend.data.repository.AdminProductRepository
import com.example.scamazon_frontend.data.repository.AuthRepository
import com.example.scamazon_frontend.data.repository.CartRepository
import com.example.scamazon_frontend.data.repository.ProductRepository
import com.example.scamazon_frontend.data.repository.ReviewRepository
import com.example.scamazon_frontend.data.repository.WarrantyRepository
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
import com.example.scamazon_frontend.ui.screens.map.MapViewModel
import com.example.scamazon_frontend.ui.screens.search.SearchViewModel
import com.example.scamazon_frontend.ui.screens.warranty.WarrantyViewModel

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val tokenManager = TokenManager(context)
        val retrofit = ApiClient.getClient(tokenManager)

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val api = retrofit.create(AuthApi::class.java)
            val authRepo = AuthRepository(api)
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(tokenManager, authRepo) as T
        }

        // Create shared dependencies
        val productApi = retrofit.create(ProductApi::class.java)
        val cartApi = retrofit.create(CartApi::class.java)
        val adminProductApi = retrofit.create(AdminProductApi::class.java)
        val adminOrderApi = retrofit.create(AdminOrderApi::class.java)
        val warrantyApi = retrofit.create(WarrantyApi::class.java)
        val reviewApi = retrofit.create(ReviewApi::class.java)
        val storeApi = retrofit.create(StoreApi::class.java)
        val chatApi = retrofit.create(ChatApi::class.java)

        val productRepo = ProductRepository(productApi)
        val cartRepo = CartRepository(cartApi)
        val adminProductRepo = AdminProductRepository(adminProductApi)
        val adminOrderRepo = AdminOrderRepository(adminOrderApi)
        val warrantyRepo = WarrantyRepository(warrantyApi)
        val reviewRepo = ReviewRepository(reviewApi)
        val storeRepo = StoreRepository(storeApi)
        val chatRepo = ChatRepository(chatApi)
        val signalRClient = SignalRChatClient()

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(productRepo)
            modelClass.isAssignableFrom(ProductListViewModel::class.java) -> ProductListViewModel(productRepo)
            modelClass.isAssignableFrom(ProductDetailViewModel::class.java) -> ProductDetailViewModel(productRepo, cartRepo)
            modelClass.isAssignableFrom(CartViewModel::class.java) -> CartViewModel(cartRepo)
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                val authApi = retrofit.create(AuthApi::class.java)
                ProfileViewModel(AuthRepository(authApi))
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel(productRepo)
            modelClass.isAssignableFrom(CheckoutViewModel::class.java) -> {
                val authApi = retrofit.create(AuthApi::class.java)
                CheckoutViewModel(AuthRepository(authApi))
            }
            modelClass.isAssignableFrom(OrderHistoryViewModel::class.java) -> OrderHistoryViewModel()
            modelClass.isAssignableFrom(AdminDashboardViewModel::class.java) -> AdminDashboardViewModel(adminOrderRepo)
            modelClass.isAssignableFrom(AdminProductViewModel::class.java) -> AdminProductViewModel(adminProductRepo, productRepo)
            modelClass.isAssignableFrom(AdminCategoryViewModel::class.java) -> AdminCategoryViewModel()
            modelClass.isAssignableFrom(AdminOrderViewModel::class.java) -> AdminOrderViewModel(adminOrderRepo)
            modelClass.isAssignableFrom(PaymentQRViewModel::class.java) -> PaymentQRViewModel()
            modelClass.isAssignableFrom(ReviewViewModel::class.java) -> ReviewViewModel(reviewRepo)
            modelClass.isAssignableFrom(FavoriteViewModel::class.java) -> FavoriteViewModel(context, productRepo)
            modelClass.isAssignableFrom(MapViewModel::class.java) -> MapViewModel(storeRepo)
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> ChatViewModel(chatRepo, signalRClient, tokenManager)
            modelClass.isAssignableFrom(AdminChatListViewModel::class.java) -> AdminChatListViewModel(chatRepo)
            modelClass.isAssignableFrom(AdminChatDetailViewModel::class.java) -> AdminChatDetailViewModel(chatRepo, signalRClient, tokenManager)
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> NotificationViewModel()
            modelClass.isAssignableFrom(WarrantyViewModel::class.java) -> WarrantyViewModel(warrantyRepo)
            modelClass.isAssignableFrom(AdminWarrantyViewModel::class.java) -> AdminWarrantyViewModel(warrantyRepo)
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        } as T
    }
}
