package com.example.scamazon_frontend.ui.navigation

/**
 * Navigation Routes - Lafyuu E-commerce App
 * Defines all screen routes in the application
 */
sealed class Screen(val route: String) {

    // ==========================================
    // AUTH SCREENS
    // ==========================================
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password/{email}") {
        fun createRoute(email: String) = "reset_password/$email"
    }

    // ==========================================
    // MAIN SCREENS (Bottom Navigation)
    // ==========================================
    object Home : Screen("home")
    object Explore : Screen("explore") {
        fun createRoute(query: String) = "explore?query=${android.net.Uri.encode(query)}"
    }
    object Cart : Screen("cart")
    object Account : Screen("account")

    // ==========================================
    // PRODUCT SCREENS
    // ==========================================
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }

    object ProductList : Screen("product_list/{categoryId}") {
        fun createRoute(categoryId: String) = "product_list/$categoryId"
    }

    object Search : Screen("search")
    object SearchResult : Screen("search_result/{query}") {
        fun createRoute(query: String) = "search_result/$query"
    }

    // ==========================================
    // CART & CHECKOUT SCREENS
    // ==========================================
    object Checkout : Screen("checkout")
    object ShippingAddress : Screen("shipping_address")
    object PaymentMethod : Screen("payment_method")
    object OrderSuccess : Screen("order_success/{orderId}?orderCode={orderCode}&total={total}&paymentMethod={paymentMethod}") {
        fun createRoute(orderId: String, orderCode: String = "", total: String = "0", paymentMethod: String = "cod"): String {
            val encodedOrderCode = android.net.Uri.encode(orderCode) ?: ""
            return "order_success/$orderId?orderCode=$encodedOrderCode&total=$total&paymentMethod=$paymentMethod"
        }
    }

    // ==========================================
    // ACCOUNT SCREENS
    // ==========================================
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object OrderHistory : Screen("order_history")
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
    object Wishlist : Screen("wishlist")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")

    // ==========================================
    // OTHER SCREENS
    // ==========================================
    object Map : Screen("map")
    object Chat : Screen("chat")
    object Chatbot : Screen("chatbot")
    object Review : Screen("review/{productId}/{canWrite}") {
        fun createRoute(productId: Int, canWrite: Boolean = false) = "review/$productId/$canWrite"
    }

    // ==========================================
    // ADMIN SCREENS
    // ==========================================
    object AdminDashboard : Screen("admin_dashboard")
    object AdminChatList : Screen("admin_chat_list")
    object AdminChatDetail : Screen("admin_chat_detail/{chatRoomId}") {
        fun createRoute(chatRoomId: Int) = "admin_chat_detail/$chatRoomId"
    }
    object AdminProducts : Screen("admin_products")
    object AdminProductAdd : Screen("admin_product_add")
    object AdminProductEdit : Screen("admin_product_edit/{productSlug}") {
        fun createRoute(productSlug: String) = "admin_product_edit/$productSlug"
    }
    object AdminCategories : Screen("admin_categories")
    object AdminCategoryAdd : Screen("admin_category_add")
    object AdminCategoryEdit : Screen("admin_category_edit/{categoryId}") {
        fun createRoute(categoryId: Int) = "admin_category_edit/$categoryId"
    }
    object AdminBrandAdd : Screen("admin_brand_add")
    object AdminBrandEdit : Screen("admin_brand_edit/{brandId}") {
        fun createRoute(brandId: Int) = "admin_brand_edit/$brandId"
    }
    object AdminAccount : Screen("admin_account")
    object AdminOrders : Screen("admin_orders")
    object AdminOrderDetail : Screen("admin_order_detail/{orderId}") {
        fun createRoute(orderId: Int) = "admin_order_detail/$orderId"
    }
    object PaymentQR : Screen("payment_qr/{orderId}") {
        fun createRoute(orderId: String) = "payment_qr/$orderId"
    }
    object PaymentResult : Screen("payment_result/{resultType}") {
        fun createRoute(
            resultType: String,
            status: String? = null,
            orderId: String? = null,
            orderNumber: String? = null,
            message: String? = null
        ): String {
            return "payment_result/$resultType?" +
                   listOfNotNull(
                       status?.let { "status=$it" },
                       orderId?.let { "orderId=$it" },
                       orderNumber?.let { "orderNumber=$it" },
                       message?.let { "message=$it" }
                   ).joinToString("&")
        }
    }

    // ==========================================
    // WARRANTY SCREENS
    // ==========================================
    object WarrantyList : Screen("warranty_list")
    object WarrantyClaim : Screen("warranty_claim/{warrantyId}") {
        fun createRoute(warrantyId: Int) = "warranty_claim/$warrantyId"
    }
    object AdminWarrantyClaims : Screen("admin_warranty_claims")
    object MyClaims : Screen("my_claims")
    object AdminWarrantyList : Screen("admin_warranty_list")
    object AdminWarrantyAdd : Screen("admin_warranty_add")
    object AdminWarrantyEdit : Screen("admin_warranty_edit/{warrantyId}") {
        fun createRoute(warrantyId: Int) = "admin_warranty_edit/$warrantyId"
    }

    // ==========================================
    // RETURN REQUEST SCREENS
    // ==========================================
    object ReturnList : Screen("return_list")
    object ReturnDetail : Screen("return_detail/{returnId}") {
        fun createRoute(returnId: Int) = "return_detail/$returnId"
    }
    object ReturnCreate : Screen("return_create/{orderId}") {
        fun createRoute(orderId: Int) = "return_create/$orderId"
    }
    object AdminReturns : Screen("admin_returns")
    object AdminUserList : Screen("admin_users")
}

/**
 * Navigation Arguments
 */
object NavArgs {
    const val PRODUCT_ID = "productId"
    const val CATEGORY_ID = "categoryId"
    const val ORDER_ID = "orderId"
    const val QUERY = "query"
}
