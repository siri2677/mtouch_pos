package com.example.domain.dto.response.tms

import java.io.Serializable

data class ResponseGetPaymentListDto(
    var result: String?,
    var list: ArrayList<ResponseGetPaymentListBody>
)

data class ResponseGetPaymentListBody(
    var rfdTime: String?,
    var amount: String,
    var van: String,
    var vanTrxId: String,
    var authCd: String,
    var tmnId: String,
    var trackId: String,
    var bin: String,
    var cardType: String,
    var trxId: String,
    var issuer: String,
    var regDay: String,
    var resultMsg: String,
    var number: String,
    var trxResult: String,
    var regTime: String,
    var vanId: String,
    var _idx: String,
    var installment: String,
    var rfdDay: String?,
    var mchtId: String,
    var brand: String,
    var rfdId: String
): Serializable
