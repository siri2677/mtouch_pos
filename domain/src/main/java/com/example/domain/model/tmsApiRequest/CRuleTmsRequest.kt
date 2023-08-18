package com.mtouch.domain.model.tmsApiRequest

data class CRuleTmsRequestData(
    var data: CRuleTmsRequestBody
)

data class CRuleTmsRequestBody(
    var amount: String? = null,
    var installment: String? = null,
    var trxId: String? = null,
)

