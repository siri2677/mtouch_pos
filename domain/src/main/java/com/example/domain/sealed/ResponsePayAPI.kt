package com.example.domain.sealed

import com.example.domain.dto.response.pay.ResponseDirectCancelPaymentDto
import com.example.domain.dto.response.pay.ResponseDirectPaymentDto
import java.io.Serializable
import kotlin.coroutines.CoroutineContext

sealed class ResponsePayAPI: Serializable{
    data class DirectPaymentContent(val responseDirectPaymentDto: ResponseDirectPaymentDto): ResponsePayAPI()
    data class DirectCancelPaymentContent(val responseDirectCancelPaymentDto: ResponseDirectCancelPaymentDto): ResponsePayAPI()
    data class ErrorMessage(val message: String): ResponsePayAPI()

   fun test() {
   }
}
