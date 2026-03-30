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
import com.example.scamazon_frontend.data.network.api.*
import com.example.scamazon_frontend.data.network.SignalRChatClient
import com.example.scamazon_frontend.data.network.SignalRNotificationClient
import com.example.scamazon_frontend.data.repository.*
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
import com.example.scamazon_frontend.ui.screens.`return`.ReturnViewModel
import com.example.scamazon_frontend.ui.screens.admin.`return`.AdminReturnViewModel
import com.example.scamazon_frontend.ui.screens.chatbot.ChatbotViewModel

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
        val adminCategoryApi = retrofit.create(AdminCategoryApi::class.java)
        val adminOrderApi = retrofit.create(AdminOrderApi::class.java)
        val warrantyApi = retrofit.create(WarrantyApi::class.java)
        val reviewApi = retrofit.create(ReviewApi::class.java)
        val storeApi = retrofit.create(StoreApi::class.java)
        val chatApi = retrofit.create(ChatApi::class.java)
        val notificationApi = retrofit.create(NotificationApi::class.java)
        val returnApi = retrofit.create(ReturnApi::class.java)
        val adminUserApi = retrofit.create(AdminUserApi::class.java)

        val productRepo = ProductRepository(productApi)
        val cartRepo = CartRepository(cartApi)
        val adminProductRepo = AdminProductRepository(adminProductApi)
        val adminCategoryRepo = AdminCategoryRepository(adminCategoryApi, productRepo)
        val adminOrderRepo = AdminOrderRepository(adminOrderApi)
        val warrantyRepo = WarrantyRepository(warrantyApi)
        val reviewRepo = ReviewRepository(reviewApi)
        val storeRepo = StoreRepository(storeApi)
        val chatRepo = ChatRepository(chatApi)
        val notificationRepo = NotificationRepository(notificationApi)
        val returnRepo = ReturnRepository(returnApi)
        val adminUserRepo = AdminUserRepository(adminUserApi)
        val signalRClient = SignalRChatClient()
        val signalRNotificationClient = SignalRNotificationClient()

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
                val checkoutApi = retrofit.create(CheckoutApi::class.java)
                val orderApi = retrofit.create(OrderApi::class.java)
                CheckoutViewModel(
                    AuthRepository(authApi),
                    CheckoutRepository(checkoutApi),
                    OrderRepository(orderApi)
                )
            }
            modelClass.isAssignableFrom(OrderHistoryViewModel::class.java) -> {
                val orderApi = retrofit.create(OrderApi::class.java)
                OrderHistoryViewModel(OrderRepository(orderApi))
            }
            modelClass.isAssignableFrom(AdminDashboardViewModel::class.java) -> AdminDashboardViewModel(adminOrderRepo)
            modelClass.isAssignableFrom(AdminProductViewModel::class.java) -> AdminProductViewModel(adminProductRepo, productRepo, warrantyRepo)
            modelClass.isAssignableFrom(AdminCategoryViewModel::class.java) -> AdminCategoryViewModel(adminCategoryRepo)
            modelClass.isAssignableFrom(AdminOrderViewModel::class.java) -> AdminOrderViewModel(adminOrderRepo, signalRNotificationClient, tokenManager)
            modelClass.isAssignableFrom(PaymentQRViewModel::class.java) -> {
                val paymentApi = retrofit.create(PaymentApi::class.java)
                PaymentQRViewModel(PaymentRepository(paymentApi))
            }
            modelClass.isAssignableFrom(ReviewViewModel::class.java) -> ReviewViewModel(reviewRepo)
            modelClass.isAssignableFrom(FavoriteViewModel::class.java) -> FavoriteViewModel(context, productRepo)
            modelClass.isAssignableFrom(MapViewModel::class.java) -> MapViewModel(storeRepo)
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> ChatViewModel(chatRepo, signalRClient, tokenManager)
            modelClass.isAssignableFrom(AdminChatListViewModel::class.java) -> AdminChatListViewModel(chatRepo)
            modelClass.isAssignableFrom(AdminChatDetailViewModel::class.java) -> AdminChatDetailViewModel(chatRepo, signalRClient, tokenManager)
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> NotificationViewModel(notificationRepo)
            modelClass.isAssignableFrom(WarrantyViewModel::class.java) -> WarrantyViewModel(warrantyRepo)
            modelClass.isAssignableFrom(AdminWarrantyViewModel::class.java) -> AdminWarrantyViewModel(warrantyRepo)
            modelClass.isAssignableFrom(ReturnViewModel::class.java) -> ReturnViewModel(returnRepo)
            modelClass.isAssignableFrom(AdminReturnViewModel::class.java) -> AdminReturnViewModel(returnRepo)
            modelClass.isAssignableFrom(com.example.scamazon_frontend.ui.screens.admin.user.AdminUserViewModel::class.java) -> com.example.scamazon_frontend.ui.screens.admin.user.AdminUserViewModel(adminUserRepo)
            modelClass.isAssignableFrom(ChatbotViewModel::class.java) -> {
                val chatbotApi = retrofit.create(ChatbotApi::class.java)
                ChatbotViewModel(ChatbotRepository(chatbotApi))
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        } as T
    }
}
