package com.example.scamazon_frontend.ui.screens.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamazon_frontend.core.utils.Resource
import com.example.scamazon_frontend.data.models.checkout.PaymentMethodDto
import com.example.scamazon_frontend.data.models.checkout.ValidateCheckoutRequest
import com.example.scamazon_frontend.data.models.order.CheckoutResponse
import com.example.scamazon_frontend.data.models.order.CreateOrderRequest
import com.example.scamazon_frontend.data.repository.AuthRepository
import com.example.scamazon_frontend.data.repository.CheckoutRepository
import com.example.scamazon_frontend.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val authRepository: AuthRepository,
    private val checkoutRepository: CheckoutRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    // Form fields
    private val _shippingName = MutableStateFlow("")
    val shippingName: StateFlow<String> = _shippingName.asStateFlow()

    private val _shippingEmail = MutableStateFlow("")
    val shippingEmail: StateFlow<String> = _shippingEmail.asStateFlow()

    private val _shippingPhone = MutableStateFlow("")
    val shippingPhone: StateFlow<String> = _shippingPhone.asStateFlow()

    private val _shippingAddress = MutableStateFlow("")
    val shippingAddress: StateFlow<String> = _shippingAddress.asStateFlow()

    private val _shippingCity = MutableStateFlow("")
    val shippingCity: StateFlow<String> = _shippingCity.asStateFlow()

    private val _shippingDistrict = MutableStateFlow("")
    val shippingDistrict: StateFlow<String> = _shippingDistrict.asStateFlow()

    private val _shippingWard = MutableStateFlow("")
    val shippingWard: StateFlow<String> = _shippingWard.asStateFlow()

    private val _paymentMethod = MutableStateFlow("COD")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _couponCode = MutableStateFlow("")
    val couponCode: StateFlow<String> = _couponCode.asStateFlow()

    private val _orderState = MutableStateFlow<Resource<CheckoutResponse>?>(null)
    val orderState: StateFlow<Resource<CheckoutResponse>?> = _orderState.asStateFlow()

    // Payment methods list
    private val _paymentMethodsList = MutableStateFlow<Resource<List<PaymentMethodDto>>>(Resource.Loading())
    val paymentMethodsList: StateFlow<Resource<List<PaymentMethodDto>>> = _paymentMethodsList.asStateFlow()

    init {
        loadShippingInfo()
        loadPaymentMethods()
    }

    private fun loadShippingInfo() {
        viewModelScope.launch {
            val result = checkoutRepository.getShippingInfo()
            if (result is Resource.Success) {
                result.data?.let { shipping ->
                    _shippingName.value = shipping.username ?: ""
                    _shippingEmail.value = shipping.email ?: ""
                    _shippingPhone.value = shipping.phone ?: ""
                    _shippingAddress.value = shipping.address ?: ""
                }
            } else {
                // Backend shipping info failed, fallback to profile.
                val profileResult = authRepository.getProfile()
                if (profileResult is Resource.Success) {
                    profileResult.data?.let { profile ->
                        _shippingName.value = profile.fullName ?: ""
                        _shippingEmail.value = profile.email ?: ""
                        _shippingPhone.value = profile.phone ?: ""
                        _shippingAddress.value = profile.address ?: ""
                        _shippingCity.value = profile.city ?: ""
                        _shippingDistrict.value = profile.district ?: ""
                        _shippingWard.value = profile.ward ?: ""
                    }
                }
            }
        }
    }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            val result = checkoutRepository.getPaymentMethods()
            _paymentMethodsList.value = result
            if (result is Resource.Success && result.data?.isNotEmpty() == true) {
                _paymentMethod.value = result.data.first().code
            }
        }
    }

    fun onShippingNameChange(value: String) { _shippingName.value = value }
    fun onShippingEmailChange(value: String) { _shippingEmail.value = value }
    fun onShippingPhoneChange(value: String) { _shippingPhone.value = value }
    fun onShippingAddressChange(value: String) { _shippingAddress.value = value }
    fun onShippingCityChange(value: String) { _shippingCity.value = value }
    fun onShippingDistrictChange(value: String) { _shippingDistrict.value = value }
    fun onShippingWardChange(value: String) { _shippingWard.value = value }
    fun onPaymentMethodChange(value: String) { _paymentMethod.value = value }
    fun onNoteChange(value: String) { _note.value = value }
    fun onCouponCodeChange(value: String) { _couponCode.value = value }

    fun placeOrder() {
        if (_shippingName.value.isBlank() || _shippingPhone.value.isBlank() || _shippingAddress.value.isBlank()) {
            _orderState.value = Resource.Error("Please fill in all required fields (Name, Phone, Street Address)")
            return
        }

        if (_shippingEmail.value.isBlank()) {
            _orderState.value = Resource.Error("Please fill in your email address")
            return
        }

        if (_shippingCity.value.isBlank() || _shippingDistrict.value.isBlank() || _shippingWard.value.isBlank()) {
            _orderState.value = Resource.Error("Please fill in City, District, and Ward")
            return
        }

        _orderState.value = Resource.Loading()

        viewModelScope.launch {
            // 1. Validate the cart and items before placing the order
            val coupon = _couponCode.value.ifBlank { null }
            val validateRequest = ValidateCheckoutRequest(couponCode = coupon)
            val validationResult = checkoutRepository.validateCheckout(validateRequest)

            if (validationResult is Resource.Error) {
                _orderState.value = Resource.Error(validationResult.message ?: "Validation failed.")
                return@launch
            }

            if (validationResult is Resource.Success && validationResult.data?.isValid == false) {
                val stockErrors = validationResult.data.stockErrors
                    ?.joinToString(", ") { "${it.productName}: requested ${it.requestedQuantity}, available ${it.availableQuantity}" }
                    ?: "Invalid cart items."
                _orderState.value = Resource.Error("Validation failed: $stockErrors")
                return@launch
            }

            // 2. If valid, proceed to place order
            val request = CreateOrderRequest(
                paymentMethod = _paymentMethod.value,
                customerName = _shippingName.value,
                customerEmail = _shippingEmail.value,
                customerPhone = _shippingPhone.value,
                province = _shippingCity.value,
                district = _shippingDistrict.value,
                ward = _shippingWard.value,
                streetAddress = _shippingAddress.value,
                couponCode = coupon,
                notes = _note.value.ifBlank { null }
            )

            val result = orderRepository.checkout(request)
            _orderState.value = result
        }
    }

    fun resetOrderState() {
        _orderState.value = null
    }
}
