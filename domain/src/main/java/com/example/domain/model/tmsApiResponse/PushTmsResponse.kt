package com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse

class PushTmsResponseData: TmsResponseData(){
    var data: PushTmsResponseBody? = null
    @Transient override var pushTmsResponseData: PushTmsResponseData? = null
    @Transient override var status: String? = null
    @Transient override var errorMessage: String? = null
    @Transient override var exceptionMessage: String? = null

    override fun setData(any: Any) {
        this.pushTmsResponseData = any as PushTmsResponseData
        this.data = pushTmsResponseData!!.data
    }
}

data class PushTmsResponseBody(
    var result: String? = null,
    var trxId: String? = null
)

