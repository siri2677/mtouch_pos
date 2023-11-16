package com.example.domain.dto.request.tms

data class RequestInsertPaymentDataDTO(
    val amount: Int,
    val installment: String,
    val type: String,
    val token: String,
    val trxId: String?,
    val walletSettle: String = "N",
    val resultMsg: String?,

    val prodQty: String?,
    val prodName: String?,
    val prodPrice: String?,
    val payerTel: String?,
    val payerName: String?,
    val payerEmail: String?,
    val dealerRate: String?,
    val distRate: String?,
    var number: String?,
    var van: String?,
    var vanId: String?,
    var vanTrxId: String?,
    var trackId: String?,
    var authCd: String?,
    var regDate: String?,
    var issuerCode: String?,
    var acquirerCode: String?
)
