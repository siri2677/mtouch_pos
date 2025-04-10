package com.example.data.remote.dto.request

sealed interface RequestDirectPayment {
    data class DirectCancelPayment(
        val refund: DirectCancelPaymentRefund,
    )

    data class DirectPayment(
        val pay: DirectPaymentPay
    )

    data class DirectCancelPaymentRefund(
        val trxType: String = "ONTR",
        val trackId: String,
        val amount: String,
        val udf1: String?,
        val udf2: String?,
        val rootTrxId: String,
        val rootTrackId: String?,
        val rootTrxDay: String?,
        val trxId: String?,
        val authCd: String?,
        val tmnId: String?
    ): RequestDirectPayment

    data class DirectPaymentPay(
        val trxType: String = "ONTR",
        val trackId: String,
        val amount: String,
        val payerName: String,
        val payerTel: String,
        val card: DirectPaymentPayCard,
        val product: DirectPaymentPayProduct,
        val payerEmail: String?,
        val fillerAmt: Long?,
        val udf1: String?,
        val udf2: String?,
        val metadata: DirectPaymentPayMetadata?,
        val trxId: String?,
        val authCd: String?,
        val settle: String?
    ): RequestDirectPayment

    data class DirectPaymentPayProduct(
        val name: String,
        val qty: Int?,
        val price: String?,
        val desc: String?
    )

    data class DirectPaymentPayCard(
        val number: String,
        val expiry: String,
        val installment: String,
        val cvv: String?,
        val cardId: String?,
        val last4: String?,
        val issuer: String?,
        val cardType: String?
    )

    data class DirectPaymentPayMetadata(
        val cardAuth: String,
        val authPw: String?,
        val authDob: String?
    )
}