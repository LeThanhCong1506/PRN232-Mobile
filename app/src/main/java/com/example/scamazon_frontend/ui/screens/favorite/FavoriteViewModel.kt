package com.example.scamazon_frontend.ui.screens.favorite

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.favorite.FavoriteItemDto
import com.example.scamazon_frontend.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val context: Context,
    private val productRepository: ProductRepository
) : ViewModel() {

    companion object {
        private const val PREFS_NAME = "favorites_prefs"
        private const val KEY_FAVORITE_IDS = "favorite_ids"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _favoritesState = MutableStateFlow<Resource<List<FavoriteItemDto>>?>(null)
    val favoritesState: StateFlow<Resource<List<FavoriteItemDto>>?> = _favoritesState.asStateFlow()

    /** Set of product IDs currently favorited by this user */
    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    init {
        loadFavoriteIds()
    }

    fun loadFavorites() {
        _favoritesState.value = Resource.Loading()
        viewModelScope.launch {
            val ids = _favoriteIds.value
            if (ids.isEmpty()) {
                _favoritesState.value = Resource.Success(emptyList())
                return@launch
            }

            // Fetch all products and filter by favorite IDs
            val result = productRepository.getProducts(pageNumber = 1, pageSize = 100)
            when (result) {
                is Resource.Success -> {
                    val allProducts = result.data?.items ?: emptyList()
                    val favoriteItems = allProducts
                        .filter { ids.contains(it.id) }
                        .map { product ->
                            FavoriteItemDto(
                                id = product.id,
                                productId = product.id,
                                productName = product.name,
                                productSlug = product.id.toString(),
                                productImage = product.primaryImage,
                                price = product.price,
                                salePrice = product.salePrice,
                                createdAt = product.createdAt
                            )
                        }
                    _favoritesState.value = Resource.Success(favoriteItems)
                }
                is Resource.Error -> {
                    _favoritesState.value = Resource.Error(result.message ?: "Failed to load favorites")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadFavoriteIds() {
        val saved = prefs.getStringSet(KEY_FAVORITE_IDS, emptySet()) ?: emptySet()
        _favoriteIds.value = saved.mapNotNull { it.toIntOrNull() }.toSet()
    }

    private fun saveFavoriteIds(ids: Set<Int>) {
        prefs.edit().putStringSet(KEY_FAVORITE_IDS, ids.map { it.toString() }.toSet()).apply()
    }

    fun toggleFavorite(productId: Int) {
        val currentIds = _favoriteIds.value.toMutableSet()
        if (currentIds.contains(productId)) {
            currentIds.remove(productId)
        } else {
            currentIds.add(productId)
        }
        _favoriteIds.value = currentIds
        saveFavoriteIds(currentIds)
    }

    fun isFavorited(productId: Int): Boolean {
        return _favoriteIds.value.contains(productId)
    }
}
