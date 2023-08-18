package com.mtouch.domain.model.tmsApiRequest

data class RuleTmsRequestData (
    var data: RuleTmsRequestBody
)

data class RuleTmsRequestBody (
    var amount: String? = null,
    var installment: String? = null
)

