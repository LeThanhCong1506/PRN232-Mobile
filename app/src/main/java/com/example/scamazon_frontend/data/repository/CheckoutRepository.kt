package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.checkout.PaymentMethodDto
import com.example.scamazon_frontend.data.models.checkout.ShippingInfoResponse
import com.example.scamazon_frontend.data.models.checkout.ValidateCheckoutRequest
import com.example.scamazon_frontend.data.models.checkout.ValidateCheckoutResponse
import com.example.scamazon_frontend.data.network.api.CheckoutApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CheckoutRepository(
    private val checkoutApi: CheckoutApi
) {
    suspend fun validateCheckout(request: ValidateCheckoutRequest): Resource<ValidateCheckoutResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = checkoutApi.validateCheckout(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Validation failed without a message")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(errorBody ?: "Validation request failed (${response.code()})")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred during checkout validation")
            }
        }
    }

    suspend fun getShippingInfo(): Resource<ShippingInfoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = checkoutApi.getShippingInfo()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to get shipping info")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(errorBody ?: "Failed to get shipping info (${response.code()})")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred fetching shipping info")
            }
        }
    }

    suspend fun getPaymentMethods(): Resource<List<PaymentMethodDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = checkoutApi.getPaymentMethods()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to get payment methods")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(errorBody ?: "Failed to get payment methods (${response.code()})")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred fetching payment methods")
            }
        }
    }
}
