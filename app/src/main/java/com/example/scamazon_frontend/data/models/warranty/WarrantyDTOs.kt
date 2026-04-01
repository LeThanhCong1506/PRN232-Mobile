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

// ==================== Admin Warranty CRUD ====================
// Matches backend WarrantyResponse DTO exactly

data class AdminWarrantyDto(
    @SerializedName("warrantyId") val warrantyId: Int,
    @SerializedName("serialNumber") val serialNumber: String,
    @SerializedName("warrantyPolicyId") val warrantyPolicyId: Int,
    @SerializedName("warrantyPolicyName") val policyName: String,
    @SerializedName("durationMonths") val durationMonths: Int?,
    @SerializedName("startDate") val startDate: String,   // format: "yyyy-MM-dd"
    @SerializedName("endDate") val endDate: String,       // format: "yyyy-MM-dd"
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("notes") val notes: String?,
    @SerializedName("productId") val productId: Int?,
    @SerializedName("productName") val productName: String?,
    @SerializedName("productSku") val productSku: String?
)

// Create: POST /api/warranty
data class CreateWarrantyRequest(
    @SerializedName("serialNumber") val serialNumber: String,
    @SerializedName("warrantyPolicyId") val warrantyPolicyId: Int,
    @SerializedName("startDate") val startDate: String,   // "yyyy-MM-dd"
    @SerializedName("endDate") val endDate: String,       // "yyyy-MM-dd"
    @SerializedName("notes") val notes: String? = null
)

// Update: PUT /api/warranty/{id}
data class UpdateWarrantyRequest(
    @SerializedName("endDate") val endDate: String,       // "yyyy-MM-dd"
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("notes") val notes: String? = null
)
