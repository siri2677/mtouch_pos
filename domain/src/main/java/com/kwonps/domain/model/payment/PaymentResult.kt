package com.kwonps.domain.model.payment

sealed class PaymentResult<out T> {
    data class Success<out T>(val value: T) : PaymentResult<T>()
    data class Failure(val error: PaymentError) : PaymentResult<Nothing>()
}

sealed class PaymentError {
    data class Validation(val reason: String) : PaymentError()
    data class Communication(val message: String, val code: String? = null) : PaymentError()
    data class Conflict(val reason: String) : PaymentError()
    data class Unknown(val throwable: Throwable) : PaymentError()
}
