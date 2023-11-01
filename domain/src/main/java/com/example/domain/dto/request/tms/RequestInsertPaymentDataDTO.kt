package com.example.domain.dto.request.tms

data class RequestInsertPaymentDataDTO(
    var amount: Int,
    var van: String,
    var vanId: String,
    var vanTrxId: String,
    var walletSettle: String = "N",
    var type: String,
    var installment: String,
    var number: String? = null,
    var authCd: String? = null,
    var trackId: String? = null,
    var regDate: String? = null,
    var issuerCode: String? = null,
    var acquirerCode: String? = null,
    var resultMsg: String? = null,
    var prodQty: String? = null,
    var prodName: String? = null,
    var prodPrice: String? = null,
    var payerTel: String? = null,
    var payerName: String? = null,
    var payerEmail: String? = null,
    var dealerRate: String? = null,
    var distRate: String? = null,
    var token: String,
)
