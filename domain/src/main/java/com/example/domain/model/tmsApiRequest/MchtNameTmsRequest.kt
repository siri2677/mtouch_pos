package com.mtouch.domain.model.tmsApiRequest

data class MchtNameTmsRequestData(
    var data: MchtNameTmsRequestBody
)

data class MchtNameTmsRequestBody(
    var mchtId: String? = null
)
