package com.example.scamazon_frontend.ui.navigation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.NotificationCompat
import com.example.scamazon_frontend.MainActivity
import com.example.scamazon_frontend.R
import com.example.scamazon_frontend.core.utils.TokenManager
import com.example.scamazon_frontend.data.network.SignalRNotificationClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamazon_frontend.ui.screens.admin.account.AdminAccountScreen
import com.example.scamazon_frontend.ui.screens.admin.category.AdminCategoryFormScreen
import com.example.scamazon_frontend.ui.screens.admin.category.AdminCategoryListScreen
import com.example.scamazon_frontend.ui.screens.admin.dashboard.AdminDashboardScreen
import com.example.scamazon_frontend.ui.screens.admin.order.AdminOrderDetailScreen
import com.example.scamazon_frontend.ui.screens.admin.order.AdminOrderListScreen
import com.example.scamazon_frontend.ui.screens.admin.product.AdminProductFormScreen
import com.example.scamazon_frontend.ui.screens.admin.product.AdminProductListScreen
import com.example.scamazon_frontend.ui.screens.admin.warranty.AdminWarrantyClaimsScreen
import com.example.scamazon_frontend.ui.screens.warranty.WarrantyClaimScreen
import com.example.scamazon_frontend.ui.screens.warranty.WarrantyListScreen
import com.example.scamazon_frontend.ui.screens.auth.LoginScreen
import com.example.scamazon_frontend.ui.screens.auth.RegisterScreen
import com.example.scamazon_frontend.ui.screens.cart.CartScreen
import com.example.scamazon_frontend.ui.screens.checkout.CheckoutScreen
import com.example.scamazon_frontend.ui.screens.checkout.OrderSuccessScreen
import com.example.scamazon_frontend.ui.screens.checkout.PaymentQRScreen
import com.example.scamazon_frontend.ui.screens.checkout.PaymentResultScreen
import com.example.scamazon_frontend.ui.screens.home.HomeScreen
import com.example.scamazon_frontend.ui.screens.order.OrderDetailScreen
import com.example.scamazon_frontend.ui.screens.order.OrderHistoryScreen
import com.example.scamazon_frontend.ui.screens.product.ProductDetailScreen
import com.example.scamazon_frontend.ui.screens.product.ProductListScreen
import com.example.scamazon_frontend.ui.screens.profile.AccountScreen
import com.example.scamazon_frontend.ui.screens.profile.EditProfileScreen
import com.example.scamazon_frontend.ui.screens.review.ReviewScreen
import com.example.scamazon_frontend.ui.screens.favorite.FavoriteScreen
import com.example.scamazon_frontend.ui.screens.search.ExploreScreen
import com.example.scamazon_frontend.ui.screens.chatbot.ChatbotScreen
import com.example.scamazon_frontend.ui.screens.notification.NotificationViewModel
import com.example.scamazon_frontend.ui.screens.notification.NotificationScreen
import com.example.scamazon_frontend.ui.screens.chat.ChatViewModel
import com.example.scamazon_frontend.ui.screens.profile.ProfileViewModel
import com.example.scamazon_frontend.di.ViewModelFactory
import com.example.scamazon_frontend.ui.theme.TextSecondary
import com.example.scamazon_frontend.ui.theme.Typography
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scamazon_frontend.ui.screens.admin.user.AdminUserListScreen
import com.example.scamazon_frontend.ui.screens.warranty.MyClaimsScreen
import com.example.scamazon_frontend.ui.screens.`return`.ReturnListScreen
import com.example.scamazon_frontend.ui.screens.`return`.ReturnCreateScreen
import com.example.scamazon_frontend.ui.screens.`return`.ReturnDetailScreen
import com.example.scamazon_frontend.ui.screens.admin.`return`.AdminReturnScreen

/**
 * Main Navigation Graph
 * Defines all navigation routes and their composables
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notificationViewModel: NotificationViewModel = viewModel(factory = ViewModelFactory(context))
    val unreadCount by notificationViewModel.unreadCount.collectAsStateWithLifecycle()
    val profileViewModel: ProfileViewModel = viewModel(factory = ViewModelFactory(context))
    val sharedChatViewModel: ChatViewModel = viewModel(factory = ViewModelFactory(context))
    val chatUnreadCount by sharedChatViewModel.unreadCount.collectAsStateWithLifecycle()

    // Listen for SignalR OrderStatusChanged and show heads-up notification (Customer only)
    LaunchedEffect(Unit) {
        val tokenManager = TokenManager(context)
        var activeClient: SignalRNotificationClient? = null
        try {
            while (true) {
                val token = tokenManager.getToken()
                val userRole = tokenManager.getUserRole()
                if (token != null && userRole?.lowercase() != "admin") {
                    val client = SignalRNotificationClient()
                    activeClient = client
                    withContext(Dispatchers.IO) { client.connect(token) }
                    client.onOrderStatusChanged { data ->
                        val gson = Gson()
                        val json = try { gson.toJson(data) } catch (e: Exception) { "{}" }
                        @Suppress("UNCHECKED_CAST")
                        val map = try { gson.fromJson(json, Map::class.java) as? Map<String, Any> } catch (e: Exception) { null }
                        val title = map?.get("title")?.toString() ?: "Order Status Updated"
                        val body = map?.get("body")?.toString() ?: "Your order status has been updated."
                        Handler(Looper.getMainLooper()).post {
                            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                nm.createNotificationChannel(
                                    NotificationChannel("scamazon_notifications", "STEM Notifications", NotificationManager.IMPORTANCE_HIGH)
                                        .apply { description = "Thông báo từ STEM"; enableVibration(true) }
                                )
                            }
                            val tapIntent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
                            val pendingIntent = PendingIntent.getActivity(context, 0, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                            val notification = NotificationCompat.Builder(context, "scamazon_notifications")
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle(title)
                                .setContentText(body)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                                .setContentIntent(pendingIntent)
                                .build()
                            nm.notify(10002, notification)
                            notificationViewModel.loadNotifications()
                        }
                    }
                    client.onPaymentConfirmed { data ->
                        val gson = Gson()
                        val json = try { gson.toJson(data) } catch (e: Exception) { "{}" }
                        @Suppress("UNCHECKED_CAST")
                        val map = try { gson.fromJson(json, Map::class.java) as? Map<String, Any> } catch (e: Exception) { null }
                        val title = map?.get("title")?.toString() ?: "Payment Successful!"
                        val body = map?.get("message")?.toString() ?: "Your payment has been confirmed."
                        Handler(Looper.getMainLooper()).post {
                            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                nm.createNotificationChannel(
                                    NotificationChannel("scamazon_notifications", "STEM Notifications", NotificationManager.IMPORTANCE_HIGH)
                                        .apply { description = "Thông báo từ STEM"; enableVibration(true) }
                                )
                            }
                            val tapIntent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
                            val pendingIntent = PendingIntent.getActivity(context, 0, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                            val notification = NotificationCompat.Builder(context, "scamazon_notifications")
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle(title)
                                .setContentText(body)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                                .setContentIntent(pendingIntent)
                                .build()
                            nm.notify(10003, notification)
                            notificationViewModel.loadNotifications()
                        }
                    }
                    client.onPaymentExpired { data ->
                        val gson = Gson()
                        val json = try { gson.toJson(data) } catch (e: Exception) { "{}" }
                        @Suppress("UNCHECKED_CAST")
                        val map = try { gson.fromJson(json, Map::class.java) as? Map<String, Any> } catch (e: Exception) { null }
                        val title = map?.get("title")?.toString() ?: "Payment Expired"
                        val body = map?.get("message")?.toString() ?: "Your payment window has expired. Please place a new order."
                        Handler(Looper.getMainLooper()).post {
                            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                nm.createNotificationChannel(
                                    NotificationChannel("scamazon_notifications", "STEM Notifications", NotificationManager.IMPORTANCE_HIGH)
                                        .apply { description = "Thông báo từ STEM"; enableVibration(true) }
                                )
                            }
                            val tapIntent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
                            val pendingIntent = PendingIntent.getActivity(context, 0, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                            val notification = NotificationCompat.Builder(context, "scamazon_notifications")
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle(title)
                                .setContentText(body)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                                .setContentIntent(pendingIntent)
                                .build()
                            nm.notify(10004, notification)
                            notificationViewModel.loadNotifications()
                        }
                    }
                    // Stay connected while token is valid
                    while (tokenManager.getToken() != null) {
                        delay(5_000)
                    }
                    withContext(Dispatchers.IO) { client.disconnect() }
                    activeClient = null
                } else {
                    delay(2_000) // Wait for token (e.g., after fresh login)
                }
            }
        } finally {
            activeClient?.let { withContext(Dispatchers.IO) { it.disconnect() } }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ==========================================
        // AUTH SCREENS
        // ==========================================
        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToAdminDashboard = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // MAIN SCREENS (Bottom Navigation)
        // ==========================================
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                notificationBadge = unreadCount,
                onNavigateToWishlist = {
                    navController.navigate(Screen.Wishlist.route)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                onNavigateToChat = {
                    navController.navigate(Screen.Chat.route)
                },
                onNavigateToExplore = {
                    navController.navigate(Screen.Explore.route)
                },
                onNavigateToCategory = { categoryName ->
                    navController.navigate(Screen.Explore.createRoute(categoryName))
                }
            )
        }

        composable(route = Screen.Cart.route) {
            CartScreen(
                onNavigateToCheckout = {
                    navController.navigate(Screen.Checkout.route)
                },
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Account.route) {
            AccountScreen(
                viewModel = profileViewModel,
                onNavigateToProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.OrderHistory.route)
                },
                onNavigateToWishlist = {
                    navController.navigate(Screen.Wishlist.route)
                },
                onNavigateToChat = {
                    navController.navigate(Screen.Chat.route)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToWarranty = {
                    navController.navigate(Screen.WarrantyList.route)
                },
                onNavigateToReturns = {
                    navController.navigate(Screen.ReturnList.route)
                },
                onNavigateToClaims = {
                    navController.navigate(Screen.MyClaims.route)
                },
                onNavigateToChatbot = {
                    navController.navigate(Screen.Chatbot.route)
                },
                chatUnreadCount = chatUnreadCount
            )
        }

        // ==========================================
        // PRODUCT SCREENS
        // ==========================================
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(
                navArgument(NavArgs.PRODUCT_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString(NavArgs.PRODUCT_ID) ?: ""
            ProductDetailScreen(
                productId = productId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                },
                onNavigateToReview = { id ->
                    navController.navigate(Screen.Review.createRoute(id, canWrite = false))
                }
            )
        }

        composable(
            route = Screen.ProductList.route,
            arguments = listOf(
                navArgument(NavArgs.CATEGORY_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val catId = backStackEntry.arguments?.getString(NavArgs.CATEGORY_ID) ?: ""
            ProductListScreen(
                categoryId = catId,
                categoryName = null,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                }
            )
        }

        // ==========================================
        // SEARCH & EXPLORE
        // ==========================================
        composable(
            route = Screen.Explore.route + "?query={query}",
            arguments = listOf(
                navArgument("query") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query")
            ExploreScreen(
                initialQuery = query,
                onProductClick = { slug ->
                    navController.navigate(Screen.ProductDetail.createRoute(slug))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Search.route) {
            ExploreScreen(
                onProductClick = { slug ->
                    navController.navigate(Screen.ProductDetail.createRoute(slug))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.SearchResult.route,
            arguments = listOf(
                navArgument(NavArgs.QUERY) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString(NavArgs.QUERY) ?: ""
            PlaceholderScreen(screenName = "Results: \"$query\"")
        }

        // ==========================================
        // CHECKOUT & PAYMENT FLOW
        // ==========================================
        composable(route = Screen.Checkout.route) {
            CheckoutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOrderSuccess = { orderId, orderCode, total, paymentMethod, _, _ ->
                    if (paymentMethod.equals("SEPAY", ignoreCase = true)) {
                        // SEPAY: Navigate to PaymentQR screen which shows QR code and polls for payment
                        navController.navigate(Screen.PaymentQR.createRoute(orderId)) {
                            popUpTo(Screen.Cart.route) { inclusive = true }
                        }
                    } else if (paymentMethod.equals("COD", ignoreCase = true)) {
                        // COD: go directly to success
                        navController.navigate(
                            Screen.OrderSuccess.createRoute(orderId, orderCode, total, paymentMethod)
                        ) {
                            popUpTo(Screen.Cart.route) { inclusive = true }
                        }
                    } else {
                        // Other payment methods: navigate to PaymentQR screen
                        navController.navigate(Screen.PaymentQR.createRoute(orderId)) {
                            popUpTo(Screen.Cart.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.PaymentQR.route,
            arguments = listOf(
                navArgument(NavArgs.ORDER_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString(NavArgs.ORDER_ID)?.toIntOrNull() ?: 0
            PaymentQRScreen(
                orderId = orderId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.OrderHistory.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onNavigateToNotifications = {
                    // Reload notifications so the payment confirmation appears at the top
                    notificationViewModel.loadNotifications()
                    navController.navigate(Screen.Notifications.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.PaymentResult.route + "?status={status}&orderId={orderId}&orderNumber={orderNumber}&message={message}",
            deepLinks = listOf(
                androidx.navigation.navDeepLink { uriPattern = "myapp://payment/{resultType}" }
            ),
            arguments = listOf(
                navArgument("resultType") { type = NavType.StringType },
                navArgument("status") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("orderId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("orderNumber") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("message") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry -> 
            val resultType = backStackEntry.arguments?.getString("resultType") ?: "unknown"
            val status = backStackEntry.arguments?.getString("status")
            val orderId = backStackEntry.arguments?.getString("orderId")
            val orderNumber = backStackEntry.arguments?.getString("orderNumber")
            val message = backStackEntry.arguments?.getString("message")

            PaymentResultScreen(
                resultType = resultType,
                status = status,
                orderId = orderId,
                orderNumber = orderNumber,
                message = message,
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateOrders = {
                    navController.navigate(Screen.OrderHistory.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.OrderSuccess.route,
            arguments = listOf(
                navArgument(NavArgs.ORDER_ID) { type = NavType.StringType },
                navArgument("orderCode") { type = NavType.StringType; nullable = true; defaultValue = "" },
                navArgument("total") { type = NavType.StringType; nullable = true; defaultValue = "0" },
                navArgument("paymentMethod") { type = NavType.StringType; nullable = true; defaultValue = "cod" }
            )
        ) { backStackEntry ->
            val orderCode = backStackEntry.arguments?.getString("orderCode") ?: ""
            val total = backStackEntry.arguments?.getString("total")?.toDoubleOrNull() ?: 0.0
            val paymentMethod = backStackEntry.arguments?.getString("paymentMethod") ?: "cod"

            OrderSuccessScreen(
                orderCode = orderCode,
                totalAmount = total,
                paymentMethod = paymentMethod,
                onViewOrder = {
                    navController.navigate(Screen.OrderHistory.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onContinueShopping = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // ACCOUNT & PROFILE SCREENS
        // ==========================================
        composable(route = Screen.Profile.route) {
            EditProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.EditProfile.route) {
            EditProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ==========================================
        // ORDER SCREENS
        // ==========================================
        composable(route = Screen.OrderHistory.route) {
            OrderHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                }
            )
        }

        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(
                navArgument(NavArgs.ORDER_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString(NavArgs.ORDER_ID)?.toIntOrNull() ?: 0
            OrderDetailScreen(
                orderId = orderId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToReview = { productId ->
                    navController.navigate(Screen.Review.createRoute(productId, canWrite = true))
                },
                onNavigateToReturn = { orderIdParam ->
                    navController.navigate(Screen.ReturnCreate.createRoute(orderIdParam))
                }
            )
        }

        composable(route = Screen.Wishlist.route) {
            FavoriteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                }
            )
        }

        composable(route = Screen.Notifications.route) {
            NotificationScreen(
                viewModel = notificationViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Settings.route) {
            PlaceholderScreen(screenName = "Settings")
        }

        composable(route = Screen.Map.route) {
            val context = LocalContext.current
            val mapViewModel: com.example.scamazon_frontend.ui.screens.map.MapViewModel = viewModel(factory = com.example.scamazon_frontend.di.ViewModelFactory(context))
            com.example.scamazon_frontend.ui.screens.map.MapScreen(
                viewModel = mapViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Chat.route) {
            com.example.scamazon_frontend.ui.screens.chat.ChatScreen(
                viewModel = sharedChatViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Chatbot.route) {
            ChatbotScreen(
                onNavigateBack = { navController.popBackStack() },
                onSearchProduct = { keyword ->
                    navController.navigate("${Screen.Explore.route}?query=$keyword") {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            com.example.scamazon_frontend.ui.screens.auth.ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResetPassword = { email ->
                    navController.navigate(Screen.ResetPassword.createRoute(email))
                }
            )
        }

        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            com.example.scamazon_frontend.ui.screens.auth.ResetPasswordScreen(
                email = email,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Review.route,
            arguments = listOf(
                navArgument(NavArgs.PRODUCT_ID) { type = NavType.StringType },
                navArgument("canWrite") { type = NavType.StringType; defaultValue = "false" }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString(NavArgs.PRODUCT_ID)?.toIntOrNull() ?: 0
            val canWrite = backStackEntry.arguments?.getString("canWrite")?.toBoolean() ?: false
            ReviewScreen(
                productId = productId,
                canWrite = canWrite,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ==========================================
        // ADMIN SCREENS
        // ==========================================
        composable(route = Screen.AdminDashboard.route) {
            com.example.scamazon_frontend.ui.screens.admin.dashboard.AdminDashboardScreen(
                onNavigateToChat = {
                    navController.navigate(Screen.AdminChatList.route)
                },
                onNavigateToUsers = {
                    navController.navigate(Screen.AdminUserList.route)
                }
            )
        }

        composable(route = Screen.AdminChatList.route) {
            val context = LocalContext.current
            val viewModel: com.example.scamazon_frontend.ui.screens.admin.chat.AdminChatListViewModel = viewModel(factory = com.example.scamazon_frontend.di.ViewModelFactory(context))
            com.example.scamazon_frontend.ui.screens.admin.chat.AdminChatListScreen(
                viewModel = viewModel,
                onNavigateToChatDetail = { chatRoomId ->
                    navController.navigate(Screen.AdminChatDetail.createRoute(chatRoomId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AdminChatDetail.route,
            arguments = listOf(
                navArgument("chatRoomId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val chatRoomId = backStackEntry.arguments?.getInt("chatRoomId") ?: 0
            val context = LocalContext.current
            val viewModel: com.example.scamazon_frontend.ui.screens.admin.chat.AdminChatDetailViewModel = viewModel(factory = com.example.scamazon_frontend.di.ViewModelFactory(context))
            com.example.scamazon_frontend.ui.screens.admin.chat.AdminChatDetailScreen(
                chatRoomId = chatRoomId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.AdminProducts.route) {
            AdminProductListScreen(
                onNavigateToAddProduct = {
                    navController.navigate(Screen.AdminProductAdd.route)
                },
                onNavigateToEditProduct = { productSlug ->
                    navController.navigate(Screen.AdminProductEdit.createRoute(productSlug))
                }
            )
        }

        composable(route = Screen.AdminProductAdd.route) {
            AdminProductFormScreen(
                productSlug = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.AdminProductEdit.route,
            arguments = listOf(
                navArgument("productSlug") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productSlug = backStackEntry.arguments?.getString("productSlug")
            AdminProductFormScreen(
                productSlug = productSlug,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.AdminCategories.route) {
            AdminCategoryListScreen(
                onNavigateToAddCategory = {
                    navController.navigate(Screen.AdminCategoryAdd.route)
                },
                onNavigateToEditCategory = { categoryId ->
                    navController.navigate(Screen.AdminCategoryEdit.createRoute(categoryId))
                },
                onNavigateToAddBrand = {
                    navController.navigate(Screen.AdminBrandAdd.route)
                },
                onNavigateToEditBrand = { brandId ->
                    navController.navigate(Screen.AdminBrandEdit.createRoute(brandId))
                }
            )
        }

        composable(route = Screen.AdminCategoryAdd.route) {
            AdminCategoryFormScreen(
                isBrand = false,
                editId = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.AdminCategoryEdit.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")?.toIntOrNull()
            AdminCategoryFormScreen(
                isBrand = false,
                editId = categoryId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.AdminBrandAdd.route) {
            AdminCategoryFormScreen(
                isBrand = true,
                editId = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.AdminBrandEdit.route,
            arguments = listOf(
                navArgument("brandId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val brandId = backStackEntry.arguments?.getString("brandId")?.toIntOrNull()
            AdminCategoryFormScreen(
                isBrand = true,
                editId = brandId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Admin Order Management
        composable(route = Screen.AdminOrders.route) {
            AdminOrderListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOrderClick = { orderId ->
                    navController.navigate(Screen.AdminOrderDetail.createRoute(orderId))
                }
            )
        }

        composable(
            route = Screen.AdminOrderDetail.route,
            arguments = listOf(
                navArgument(NavArgs.ORDER_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString(NavArgs.ORDER_ID)?.toIntOrNull() ?: 0
            AdminOrderDetailScreen(
                orderId = orderId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.AdminAccount.route) {
            AdminAccountScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToWarrantyClaims = {
                    navController.navigate(Screen.AdminWarrantyClaims.route)
                },
                onNavigateToReturnRequests = {
                    navController.navigate(Screen.AdminReturns.route)
                },
                onNavigateToUserManagement = {
                    navController.navigate(Screen.AdminUserList.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // WARRANTY SCREENS
        // ==========================================
        composable(route = Screen.WarrantyList.route) {
            WarrantyListScreen(
                onWarrantyClick = { warranty ->
                    navController.navigate(Screen.WarrantyClaim.createRoute(warranty.warrantyId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.WarrantyClaim.route,
            arguments = listOf(
                navArgument("warrantyId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val warrantyId = backStackEntry.arguments?.getInt("warrantyId") ?: 0
            WarrantyClaimScreen(
                warrantyId = warrantyId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.MyClaims.route) {
            MyClaimsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.AdminWarrantyClaims.route) {
            AdminWarrantyClaimsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ==========================================
        // RETURN REQUEST SCREENS
        // ==========================================
        composable(route = Screen.ReturnList.route) {
            ReturnListScreen(
                onNavigateBack = { navController.popBackStack() },
                onReturnClick = { returnId ->
                    navController.navigate(Screen.ReturnDetail.createRoute(returnId))
                }
            )
        }

        composable(
            route = Screen.ReturnDetail.route,
            arguments = listOf(
                navArgument("returnId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val returnId = backStackEntry.arguments?.getInt("returnId") ?: 0
            ReturnDetailScreen(
                returnId = returnId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ReturnCreate.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            ReturnCreateScreen(
                orderId = orderId,
                onNavigateBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(route = Screen.AdminReturns.route) {
            AdminReturnScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.AdminUserList.route) {
            AdminUserListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Placeholder Screen for screens not yet implemented
 */
@Composable
private fun PlaceholderScreen(screenName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$screenName\n(Coming Soon)",
            style = Typography.headlineMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
