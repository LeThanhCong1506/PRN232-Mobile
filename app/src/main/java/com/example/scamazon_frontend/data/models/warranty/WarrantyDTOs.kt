package com.example.scamazon_frontend.data.models.warranty

import com.google.gson.annotations.SerializedName

// ==================== Customer Warranty ====================

data class MyWarrantyDto(
    @SerializedName("warrantyId") val warrantyId: Int,
    @SerializedName("product") val product: WarrantyProductInfoDto,
    @SerializedName("serialNumber") val serialNumber: String,
    @SerializedName("purchaseDate") val purchaseDate: String,
    @SerializedName("expiryDate") val expiryDate: String,
    @SerializedName("monthsRemaining") val monthsRemaining: Int,
    @SerializedName("status") val status: String,
    @SerializedName("policyName") val policyName: String
)

data class WarrantyProductInfoDto(
    @SerializedName("productId") val productId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String?
)

// ==================== Warranty Claims - Customer ====================

data class SubmitWarrantyClaimRequest(
    @SerializedName("issueDescription") val issueDescription: String,
    @SerializedName("contactPhone") val contactPhone: String? = null
)

data class SubmitWarrantyClaimResponseDto(
    @SerializedName("claimId") val claimId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("submittedAt") val submittedAt: String
)

// ==================== Admin Warranty Claims ====================

data class AdminWarrantyClaimDto(
    @SerializedName("claimId") val claimId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("customer") val customer: ClaimCustomerInfoDto,
    @SerializedName("product") val product: ClaimProductInfoDto,
    @SerializedName("issueDescription") val issueDescription: String,
    @SerializedName("contactPhone") val contactPhone: String?,
    @SerializedName("resolutionNote") val resolutionNote: String?,
    @SerializedName("submittedAt") val submittedAt: String,
    @SerializedName("resolvedDate") val resolvedDate: String?
)

data class ClaimCustomerInfoDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("phone") val phone: String?
)

data class ClaimProductInfoDto(
    @SerializedName("productId") val productId: Int,
    @SerializedName("name") val name: String
)

data class AdminWarrantyClaimPagedDto(
    @SerializedName("items") val items: List<AdminWarrantyClaimDto>,
    @SerializedName("page") val page: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class ResolveWarrantyClaimRequest(
    @SerializedName("resolution") val resolution: String,
    @SerializedName("resolutionNote") val resolutionNote: String? = null
)

data class ResolveWarrantyClaimResponseDto(
    @SerializedName("claimId") val claimId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("resolutionNote") val resolutionNote: String?,
    @SerializedName("resolvedDate") val resolvedDate: String
)
