package com.example.scamazon_frontend.data.repository

import android.content.Context
import android.net.Uri
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.admin.CreateProductRequest
import com.example.scamazon_frontend.data.models.admin.UpdateProductRequest
import com.example.scamazon_frontend.data.models.product.PaginationMetadata
import com.example.scamazon_frontend.data.models.product.ProductDto
import com.example.scamazon_frontend.data.models.product.ProductPaginationResponse
import com.example.scamazon_frontend.data.network.api.AdminProductApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class AdminProductRepository(private val api: AdminProductApi) {

    suspend fun getAdminProducts(
        page: Int = 1,
        pageSize: Int = 20,
        search: String? = null,
        categoryId: Int? = null,
        brandId: Int? = null,
        productType: String? = null,
        lowStock: Boolean? = null
    ): Resource<ProductPaginationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAdminProducts(page, pageSize, search, categoryId, brandId, productType, lowStock)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val pagedData = body.data
                        val products = pagedData.items.map { admin ->
                            ProductDto(
                                id = admin.productId,
                                name = admin.name,
                                slug = admin.productId.toString(),
                                description = null,
                                detailDescription = null,
                                price = admin.price,
                                salePrice = null,
                                stockQuantity = admin.stockQuantity,
                                categoryId = null,
                                brandId = null,
                                primaryImage = admin.primaryImage,
                                images = null,
                                avgRating = null,
                                soldCount = null,
                                isActive = admin.isActive,
                                isFeatured = false,
                                createdAt = null
                            )
                        }
                        val pagination = PaginationMetadata(
                            currentPage = pagedData.pagination.currentPage,
                            totalPages = pagedData.pagination.totalPages,
                            totalItems = pagedData.pagination.totalItems
                        )
                        Resource.Success(ProductPaginationResponse(items = products, pagination = pagination))
                    } else {
                        Resource.Error(body?.message ?: "Failed to load products")
                    }
                } else {
                    Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun createProduct(request: CreateProductRequest): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createProduct(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to create product")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun updateProduct(id: Int, request: UpdateProductRequest): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.updateProduct(id, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to update product")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun deleteProduct(id: Int): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.deleteProduct(id)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to delete product")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun uploadImages(context: Context, productId: Int, uris: List<Uri>, primaryIndex: Int = 0): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val parts = uris.mapIndexed { index, uri ->
                    val stream = context.contentResolver.openInputStream(uri)
                    val bytes = stream?.readBytes() ?: return@mapIndexed null
                    stream.close()
                    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val requestBody = bytes.toRequestBody(mimeType.toMediaType())
                    MultipartBody.Part.createFormData("files", "image_$index.jpg", requestBody)
                }.filterNotNull()

                val primaryIndexBody = primaryIndex.toString()
                    .toRequestBody("text/plain".toMediaType())

                val response = api.uploadImages(productId, parts, primaryIndexBody)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to upload images")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun deleteImage(productId: Int, imageId: Int): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.deleteImage(productId, imageId)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to delete image")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }
}
