package com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse

class CRuleTmsResponseData: TmsResponseData(){
    var data: CRuleTmsResponseBody? = null
    @Transient override var cRuleTmsResponseData: CRuleTmsResponseData? = null
    @Transient override var status: String? = null
    @Transient override var errorMessage: String? = null
    @Transient override var exceptionMessage: String? = null

    override fun setData(any: Any) {
        this.cRuleTmsResponseData = any as CRuleTmsResponseData
        this.data = cRuleTmsResponseData!!.data
    }
}

data class CRuleTmsResponseBody(
    var result: String? = null,
    var amount: String? = null,
    var van: String? = null,
    var vanId: String? = null,
    var authCd: String? = null,
    var trackId: String? = null,
    var regDay: String? = null,
    var secondKey: String? = null
)
