package com.example.scamazon_frontend.ui.screens.favorite

import androidx.lifecycle.ViewModel
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.favorite.FavoriteItemDto
import com.example.scamazon_frontend.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoriteViewModel : ViewModel() {

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
        _favoritesState.value = Resource.Success(MockData.favorites)
    }

    fun loadFavoriteIds() {
        _favoriteIds.value = MockData.favoriteProductIds
    }

    fun toggleFavorite(productId: Int) {
        val currentIds = _favoriteIds.value.toMutableSet()
        if (currentIds.contains(productId)) currentIds.remove(productId) else currentIds.add(productId)
        _favoriteIds.value = currentIds
    }

    fun isFavorited(productId: Int): Boolean {
        return _favoriteIds.value.contains(productId)
    }
}
