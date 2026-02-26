package com.example.scamazon_frontend.data.models.common

import com.google.gson.annotations.SerializedName

data class BackendApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?,
    @SerializedName("errors") val errors: List<String>?
)

data class BackendPagedResponse<T>(
    @SerializedName("items") val items: List<T>,
    @SerializedName("pagination") val pagination: BackendPagination
)

data class BackendPagination(
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("hasPrevious") val hasPrevious: Boolean
)
