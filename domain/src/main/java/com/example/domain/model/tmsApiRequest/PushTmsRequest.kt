package com.mtouch.domain.model.tmsApiRequest

data class PushTmsRequestData (
    var data: PushTmsRequestBody
)

data class PushTmsRequestBody(
    var amount: Int? = null,
    var van: String? = null,
    var vanTrxId: String? = null,
    var walletSettle: String? = null,
    var authCd: String? = null,
    var trackId: String? = null,
    var regDate: String? = null,
    var type: String? = null,
    var resultMsg: String? = null,
    var number: String? = null,
    var vanId: String? = null,
    var installment: String? = null,
    var issuerCode: String? = null,
    var acquirerCode: String? = null,
    var payerTel: String? = null,
    var prodPrice: String? = null,
    var payerName: String? = null,
    var payerEmail: String? = null,
    var dealerRate: String? = null,
    var distRate: String? = null
)
