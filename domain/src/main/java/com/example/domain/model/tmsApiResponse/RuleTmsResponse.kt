package com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse

class RuleTmsResponseData: TmsResponseData(){
    var data: RuleTmsResponseBody? = null
    @Transient override var ruleTmsResponseData: RuleTmsResponseData? = null
    @Transient override var status: String? = null
    @Transient override var errorMessage: String? = null
    @Transient override var exceptionMessage: String? = null

    override fun setData(any: Any) {
        this.ruleTmsResponseData = any as RuleTmsResponseData
        this.data = ruleTmsResponseData!!.data
    }
}

data class RuleTmsResponseBody(
    var result: String? = null,
    var van: String? = null,
    var vanId: String? = null,
    var trackId: String? = null,
    var secondKey: String? = null
)