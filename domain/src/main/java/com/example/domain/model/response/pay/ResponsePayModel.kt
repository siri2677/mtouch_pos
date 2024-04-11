package com.example.domain.model.response.pay

import com.example.domain.model.request.tms.RequestTmsModel
import com.example.domain.model.response.ResponseModel

sealed interface ResponsePayModel: ResponseModel {
    object Init: ResponsePayModel

    data class DirectCancelPayment(
        val result: DirectPaymentResult,
        val refund: DirectCancelPaymentRefund?
    ): ResponsePayModel

    data class DirectCancelPaymentRefund(
        val trxType: String,
        val trackId: String,
        val amount: String,
        val udf1: String?,
        val udf2: String?,
        val rootTrxId: String,
        val rootTrackId: String?,
        val rootTrxDay: String,
        val trxId: String?,
        val authCd: String?,
        val settle: String?
    )

    data class DirectPayment(
        val result: DirectPaymentResult,
        val pay: DirectPaymentPay?
    ): ResponsePayModel

    data class DirectPaymentResult(
        val resultCd: String,
        val resultMsg: String,
        val advanceMsg: String,
        val create: String
    )

    data class DirectPaymentPay(
        val authCd: String?,
        val card: DirectPaymentCard,
        val product: DirectPaymentProduct,
        val trxId: String,
        val trxType: String,
        val tmnId: String,
        val trackId: String,
        val amount: Int,
        val metadata: DirectPaymentMetaData?
    )

    data class DirectPaymentCard(
        val installment: Int,
        val bin: String,
        val last4: String,
        val issuer: String,
        val cardType: String,
        val acquirer: String,
        val issuerCode: String?,
        val acquirerCode: String?
    )

    data class DirectPaymentProduct(
        val name: String,
        val qty: Int,
        val price: Int,
        val desc: String?
    )

    data class DirectPaymentMetaData(
        val cardAuth: String,
        val authPw: String?,
        val authDob: String?
    )
}