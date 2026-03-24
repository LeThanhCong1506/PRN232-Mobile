package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.admin.BrandDto
import com.example.scamazon_frontend.data.models.admin.CreateBrandRequest
import com.example.scamazon_frontend.data.models.admin.CreateCategoryRequest
import com.example.scamazon_frontend.data.models.admin.UpdateBrandRequest
import com.example.scamazon_frontend.data.models.admin.UpdateCategoryRequest
import com.example.scamazon_frontend.data.models.category.CategoryDto
import com.example.scamazon_frontend.data.network.api.AdminCategoryApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminCategoryRepository(
    private val api: AdminCategoryApi,
    private val productRepo: ProductRepository
) {

    // Reuse existing product API endpoints for loading lists
    suspend fun getCategories(): Resource<List<CategoryDto>> = productRepo.getCategories()

    suspend fun getBrands(): Resource<List<BrandDto>> = productRepo.getBrands()

    // ===== Category CRUD =====

    suspend fun createCategory(request: CreateCategoryRequest): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createCategory(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to create category")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun updateCategory(id: Int, request: UpdateCategoryRequest): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.updateCategory(id, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to update category")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun deleteCategory(id: Int): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.deleteCategory(id)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to delete category")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    // ===== Brand CRUD =====

    suspend fun createBrand(request: CreateBrandRequest): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createBrand(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to create brand")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun updateBrand(id: Int, request: UpdateBrandRequest): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.updateBrand(id, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to update brand")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun deleteBrand(id: Int): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.deleteBrand(id)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to delete brand")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }
}
