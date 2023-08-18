package com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse


class CheckTmsResponseData: TmsResponseData(){
    var data: CheckTmsResponseBody? = null
    @Transient override var checkTmsResponseData: CheckTmsResponseData? = null
    @Transient override var status: String? = null
    @Transient override var errorMessage: String? = null
    @Transient override var exceptionMessage: String? = null

    override fun setData(any: Any) {
        this.checkTmsResponseData = any as CheckTmsResponseData
        this.data = checkTmsResponseData!!.data
    }
}

data class CheckTmsResponseBody(
    var trxId: String? = null
)
