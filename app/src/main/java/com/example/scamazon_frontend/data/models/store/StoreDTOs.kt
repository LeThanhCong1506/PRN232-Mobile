package com.example.scamazon_frontend.data.models.store

import com.google.gson.annotations.SerializedName

data class StoreLocationDto(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("phone") val phone: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("openingHours") val openingHours: Map<String, String>?
)

data class StoreBranchDto(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("phone") val phone: String?,
    @SerializedName("isMainBranch") val isMainBranch: Boolean
)
