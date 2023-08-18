package com.mtouch.domain.model.tmsApiRequest

data class CheckTmsRequestData(
    var data: CheckTmsRequestBody
)

data class CheckTmsRequestBody(
    var trxId: String? = null
)
