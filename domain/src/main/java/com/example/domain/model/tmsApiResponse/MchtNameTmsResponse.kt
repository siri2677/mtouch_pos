package com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse

class MchtNameTmsResponseData: TmsResponseData(){
    var data: MchtNameTmsResponseBody? = null
    @Transient override var mchtNameTmsResponseData: MchtNameTmsResponseData? = null
    @Transient override var status: String? = null
    @Transient override var errorMessage: String? = null
    @Transient override var exceptionMessage: String? = null

    override fun setData(any: Any) {
        this.mchtNameTmsResponseData = any as MchtNameTmsResponseData
        this.data = mchtNameTmsResponseData!!.data
    }
}

data class MchtNameTmsResponseBody(
    var idType: String? = null,
    var name: String? = null,
    var mchtId: String? = null
)
