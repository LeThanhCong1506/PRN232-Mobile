package com.example.scamazon_frontend.data.repository

import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.payment.PaymentStatusResponse
import com.example.scamazon_frontend.data.network.api.PaymentApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository(
    private val paymentApi: PaymentApi
) {
    suspend fun verifyPaymentManually(orderId: Int): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = paymentApi.verifyPaymentManually(orderId)
                if (response.isSuccessful && response.body()?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(response.body()?.message ?: "Failed to verify payment (${response.code()})")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred verifying payment")
            }
        }
    }

    suspend fun getPaymentStatus(orderId: Int): Resource<PaymentStatusResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = paymentApi.getPaymentStatus(orderId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to get payment status")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(errorBody ?: "Failed to get payment status (${response.code()})")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred fetching payment status")
            }
        }
    }
}
