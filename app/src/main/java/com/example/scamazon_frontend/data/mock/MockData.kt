package com.example.scamazon_frontend.data.mock

import com.example.scamazon_frontend.data.models.admin.*
import com.example.scamazon_frontend.data.models.auth.*
import com.example.scamazon_frontend.data.models.cart.*
import com.example.scamazon_frontend.data.models.category.CategoryDto
import com.example.scamazon_frontend.data.models.chat.*
import com.example.scamazon_frontend.data.models.favorite.FavoriteItemDto
import com.example.scamazon_frontend.data.models.notification.NotificationDto
import com.example.scamazon_frontend.data.models.order.*
import com.example.scamazon_frontend.data.models.product.*
import com.example.scamazon_frontend.data.models.profile.ProfileDataDto
import com.example.scamazon_frontend.data.models.review.*

/**
 * Central mock data provider - replaces all API calls with static data.
 * Edit these objects to change what appears on screen.
 */
object MockData {

    // ==================== CATEGORIES ====================
    val categories = listOf(
        CategoryDto(id = 1, name = "Điện thoại", slug = "dien-thoai", description = "Smartphones", imageUrl = "https://picsum.photos/seed/cat1/200", parentId = null, isActive = true, createdAt = "2025-01-01"),
        CategoryDto(id = 2, name = "Laptop", slug = "laptop", description = "Laptops", imageUrl = "https://picsum.photos/seed/cat2/200", parentId = null, isActive = true, createdAt = "2025-01-02"),
        CategoryDto(id = 3, name = "Phụ kiện", slug = "phu-kien", description = "Accessories", imageUrl = "https://picsum.photos/seed/cat3/200", parentId = null, isActive = true, createdAt = "2025-01-03"),
        CategoryDto(id = 4, name = "Đồng hồ", slug = "dong-ho", description = "Watches", imageUrl = "https://picsum.photos/seed/cat4/200", parentId = null, isActive = true, createdAt = "2025-01-04"),
        CategoryDto(id = 5, name = "Âm thanh", slug = "am-thanh", description = "Audio", imageUrl = "https://picsum.photos/seed/cat5/200", parentId = null, isActive = true, createdAt = "2025-01-05"),
    )

    // ==================== BRANDS ====================
    val brands = listOf(
        BrandDto(id = 1, name = "Apple", slug = "apple", logoUrl = "https://picsum.photos/seed/apple/100", description = "Apple Inc."),
        BrandDto(id = 2, name = "Samsung", slug = "samsung", logoUrl = "https://picsum.photos/seed/samsung/100", description = "Samsung Electronics"),
        BrandDto(id = 3, name = "Xiaomi", slug = "xiaomi", logoUrl = "https://picsum.photos/seed/xiaomi/100", description = "Xiaomi Corp."),
    )

    // ==================== PRODUCTS ====================
    private fun makeProduct(id: Int, name: String, price: Double, salePrice: Double?, catId: Int, brandId: Int, rating: Float = 4.5f, sold: Int = 100) = ProductDto(
        id = id, name = name, slug = name.lowercase().replace(" ", "-"),
        description = "Mô tả sản phẩm $name", detailDescription = "Chi tiết $name",
        price = price, salePrice = salePrice, stockQuantity = 50,
        categoryId = catId, brandId = brandId,
        primaryImage = "https://picsum.photos/seed/p$id/400/400",
        images = null, avgRating = rating, soldCount = sold,
        isActive = true, isFeatured = id % 2 == 0, createdAt = "2025-02-0${(id % 9) + 1}"
    )

    val products = listOf(
        makeProduct(1, "iPhone 16 Pro Max", 34990000.0, 32490000.0, 1, 1, 4.8f, 520),
        makeProduct(2, "Samsung Galaxy S24 Ultra", 31990000.0, 28990000.0, 1, 2, 4.7f, 310),
        makeProduct(3, "MacBook Air M3", 27990000.0, 25990000.0, 2, 1, 4.9f, 200),
        makeProduct(4, "Xiaomi 14 Ultra", 22990000.0, 19990000.0, 1, 3, 4.5f, 180),
        makeProduct(5, "AirPods Pro 2", 5990000.0, 4990000.0, 3, 1, 4.6f, 800),
        makeProduct(6, "Galaxy Watch 6", 6990000.0, 5490000.0, 4, 2, 4.4f, 150),
        makeProduct(7, "MacBook Pro 14 M3", 49990000.0, null, 2, 1, 4.9f, 90),
        makeProduct(8, "Galaxy Buds FE", 1990000.0, 1490000.0, 5, 2, 4.3f, 600),
        makeProduct(9, "Apple Watch Ultra 2", 18990000.0, 17490000.0, 4, 1, 4.7f, 120),
        makeProduct(10, "Redmi Buds 5 Pro", 990000.0, 790000.0, 5, 3, 4.2f, 950),
    )

    fun getProductsPaginated(page: Int = 1, limit: Int = 10, categoryId: Int? = null, sort: String? = null): ProductPaginationResponse {
        val filtered = if (categoryId != null) products.filter { it.categoryId == categoryId } else products
        val sorted = when (sort) {
            "price_asc" -> filtered.sortedBy { it.salePrice ?: it.price }
            "popular" -> filtered.sortedByDescending { it.soldCount ?: 0 }
            "newest" -> filtered.sortedByDescending { it.id }
            else -> filtered
        }
        val start = ((page - 1) * limit).coerceAtMost(sorted.size)
        val end = (start + limit).coerceAtMost(sorted.size)
        val items = sorted.subList(start, end)
        return ProductPaginationResponse(
            items = items,
            pagination = PaginationMetadata(
                currentPage = page,
                totalPages = ((sorted.size + limit - 1) / limit).coerceAtLeast(1),
                totalItems = sorted.size
            )
        )
    }

    // ==================== PRODUCT DETAIL ====================
    fun getProductDetail(slug: String): ProductDetailDto {
        val p = products.find { it.slug == slug } ?: products.first()
        return ProductDetailDto(
            id = p.id, name = p.name, slug = p.slug,
            description = p.description, detailDescription = p.detailDescription,
            specifications = mapOf("Bảo hành" to "12 tháng", "Xuất xứ" to "Chính hãng"),
            price = p.price, salePrice = p.salePrice,
            discountPercent = if (p.salePrice != null) ((1 - p.salePrice / p.price) * 100).toInt() else null,
            stockQuantity = p.stockQuantity, stockStatus = "in_stock",
            category = CategoryInfoDto(p.categoryId ?: 1, categories.find { it.id == p.categoryId }?.name ?: "", categories.find { it.id == p.categoryId }?.slug ?: ""),
            brand = BrandInfoDto(p.brandId ?: 1, brands.find { it.id == p.brandId }?.name ?: "", brands.find { it.id == p.brandId }?.slug ?: "", brands.find { it.id == p.brandId }?.logoUrl),
            images = listOf(
                ProductImageDto(1, p.primaryImage, true, 0),
                ProductImageDto(2, "https://picsum.photos/seed/p${p.id}b/400/400", false, 1)
            ),
            ratingSummary = RatingSummaryDto(p.avgRating ?: 4.5f, 25, RatingBreakdownDto(15, 5, 3, 1, 1)),
            viewCount = 1200, soldCount = p.soldCount, isFeatured = p.isFeatured,
            createdAt = p.createdAt, updatedAt = p.createdAt
        )
    }

    // ==================== CART ====================
    val cartData = CartDataDto(
        cartId = 1,
        items = listOf(
            CartItemDto(1, 1, "iPhone 16 Pro Max", "https://picsum.photos/seed/p1/400/400", 34990000.0, 32490000.0, 1, 32490000.0, 50, true),
            CartItemDto(2, 5, "AirPods Pro 2", "https://picsum.photos/seed/p5/400/400", 5990000.0, 4990000.0, 2, 9980000.0, 50, true),
        ),
        subtotal = 42470000.0,
        totalItems = 3
    )

    // ==================== AUTH ====================
    val mockUser = UserDto(
        id = 1, username = "demo_user", email = "demo@example.com",
        fullName = "Nguyễn Văn A", role = "Customer",
        avatarUrl = "https://picsum.photos/seed/avatar/200",
        phone = "0901234567", address = "123 Đường ABC",
        city = "Hồ Chí Minh", district = "Quận 1", ward = "Phường Bến Nghé",
        createdAt = "2025-01-01"
    )

    val mockAdminUser = UserDto(
        id = 2, username = "admin", email = "admin@example.com",
        fullName = "Admin", role = "Admin",
        avatarUrl = "https://picsum.photos/seed/admin/200",
        phone = "0909999999", address = "Admin Office",
        city = "Hồ Chí Minh", district = "Quận 1", ward = "Phường Bến Nghé",
        createdAt = "2025-01-01"
    )

    val mockAuthResponse = AuthResponse(user = mockUser, token = "mock-jwt-token-12345")

    // ==================== PROFILE ====================
    val profileData = ProfileDataDto(
        id = 1, username = "demo_user", email = "demo@example.com",
        phone = "0901234567", fullName = "Nguyễn Văn A",
        avatarUrl = "https://picsum.photos/seed/avatar/200",
        role = "Customer", address = "123 Đường ABC",
        city = "Hồ Chí Minh", district = "Quận 1", ward = "Phường Bến Nghé",
        createdAt = "2025-01-01"
    )

    // ==================== ORDERS ====================
    val orderSummaries = listOf(
        OrderSummaryDto(1, "ORD-001", 42470000.0, "delivered", 3, "https://picsum.photos/seed/p1/400/400", "2025-02-20"),
        OrderSummaryDto(2, "ORD-002", 19990000.0, "shipping", 1, "https://picsum.photos/seed/p4/400/400", "2025-02-22"),
        OrderSummaryDto(3, "ORD-003", 5490000.0, "confirmed", 1, "https://picsum.photos/seed/p6/400/400", "2025-02-24"),
    )

    fun getOrderDetail(id: Int) = OrderDetailDataDto(
        id = id, orderCode = "ORD-00$id",
        shippingName = "Nguyễn Văn A", shippingPhone = "0901234567",
        shippingAddress = "123 Đường ABC", shippingCity = "Hồ Chí Minh",
        shippingDistrict = "Quận 1", shippingWard = "Phường Bến Nghé",
        subtotal = 42470000.0, shippingFee = 30000.0, discount = 0.0, total = 42500000.0,
        status = "delivered", note = null, createdAt = "2025-02-20",
        items = listOf(
            OrderItemDto(1, 1, "iPhone 16 Pro Max", "https://picsum.photos/seed/p1/400/400", 32490000.0, 1, 32490000.0),
            OrderItemDto(2, 5, "AirPods Pro 2", "https://picsum.photos/seed/p5/400/400", 4990000.0, 2, 9980000.0),
        ),
        payment = PaymentInfoDto("vnpay", 42500000.0, "success", "TXN123456", "2025-02-20T10:30:00")
    )

    // ==================== ADMIN ORDERS ====================
    val adminOrders = AdminOrderListDataDto(
        orders = listOf(
            AdminOrderSummaryDto(1, "ORD-001", "Nguyễn Văn A", "0901234567", 42500000.0, "delivered", "vnpay", "success", 3, "2025-02-20"),
            AdminOrderSummaryDto(2, "ORD-002", "Trần Thị B", "0902345678", 19990000.0, "shipping", "cod", "pending", 1, "2025-02-22"),
            AdminOrderSummaryDto(3, "ORD-003", "Lê Văn C", "0903456789", 5490000.0, "pending", "vnpay", "pending", 1, "2025-02-24"),
        ),
        totalCount = 3, page = 1, limit = 20
    )

    // ==================== DASHBOARD ====================
    val dashboardStats = DashboardStatsDto(
        customers = CustomerStatsDto(total = 150, new7Days = 12),
        products = ProductStatsDto(total = 45, lowStock = 3),
        orders = OrderStatsDto(pending = 5, confirmed = 8, shipping = 3, delivered = 120, today = 4),
        revenue = RevenueStatsDto(today = 15000000.0, week = 85000000.0, month = 350000000.0),
        chats = ChatStatsDto(active = 2)
    )

    // ==================== REVIEWS ====================
    val reviews = ReviewListDataDto(
        reviews = listOf(
            ReviewDto(1, 5, "Sản phẩm rất tốt!", ReviewUserDto(1, "user1", "Nguyễn A", null), "2025-02-18"),
            ReviewDto(2, 4, "Giao hàng nhanh, đóng gói cẩn thận", ReviewUserDto(2, "user2", "Trần B", null), "2025-02-17"),
            ReviewDto(3, 5, "Chất lượng xứng đáng với giá tiền", ReviewUserDto(3, "user3", "Lê C", null), "2025-02-15"),
        ),
        pagination = ReviewPaginationDto(1, 1, 3, 10, false, false)
    )

    // ==================== FAVORITES ====================
    val favorites = listOf(
        FavoriteItemDto(1, 1, "iPhone 16 Pro Max", "iphone-16-pro-max", "https://picsum.photos/seed/p1/400/400", 34990000.0, 32490000.0, "2025-02-20"),
        FavoriteItemDto(2, 3, "MacBook Air M3", "macbook-air-m3", "https://picsum.photos/seed/p3/400/400", 27990000.0, 25990000.0, "2025-02-21"),
    )
    val favoriteProductIds = setOf(1, 3)

    // ==================== NOTIFICATIONS ====================
    val notifications = listOf(
        NotificationDto(1, "order", "Đơn hàng đã giao", "Đơn hàng ORD-001 đã được giao thành công", null, false, "2025-02-24"),
        NotificationDto(2, "promo", "Flash Sale!", "Giảm giá đến 50% cho tất cả phụ kiện", null, true, "2025-02-23"),
        NotificationDto(3, "order", "Đơn hàng đang giao", "Đơn hàng ORD-002 đang trên đường giao", null, false, "2025-02-22"),
    )

    // ==================== CHAT ====================
    val chatRooms = listOf(
        ChatRoomSummaryDto(1, 1, "Nguyễn Văn A", null, null, "Scamazon Store", "active", "Cảm ơn bạn!", "2025-02-24T10:30:00", 0, "2025-02-20"),
    )

    val chatMessages = listOf(
        ChatMessageDto(1, 1, 1, "Nguyễn Văn A", "text", "Xin chào, tôi muốn hỏi về sản phẩm", null, null, null, false, true, "2025-02-24T10:00:00"),
        ChatMessageDto(2, 1, null, "Store", "text", "Chào bạn! Mình có thể giúp gì cho bạn?", null, null, null, true, true, "2025-02-24T10:05:00"),
        ChatMessageDto(3, 1, 1, "Nguyễn Văn A", "text", "Cảm ơn bạn!", null, null, null, false, true, "2025-02-24T10:30:00"),
    )
}
