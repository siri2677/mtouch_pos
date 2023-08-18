package com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse


class ListTmsResponseData: TmsResponseData(){
    var data: ListTmsResponseBody? = null
    @Transient override var listTmsResponseData: ListTmsResponseData? = null
    @Transient override var status: String? = null
    @Transient override var errorMessage: String? = null
    @Transient override var exceptionMessage: String? = null

    override fun setData(any: Any) {
        this.listTmsResponseData = any as ListTmsResponseData
        this.data = listTmsResponseData!!.data
    }
}

data class ListTmsResponseBody(
    var result: String? = null,
    var list: List<ListBody>
)

data class ListBody(
    var rfdTime: String? = null,
    var amount: String? = null,
    var van: String? = null,
    var vanTrxId: String? = null,
    var authCd: String? = null,
    var tmnId: String? = null,
    var trackId: String? = null,
    var bin: String? = null,
    var cardType: String? = null,
    var trxId: String? = null,
    var issuer: String? = null,
    var regDay: String? = null,
    var resultMsg: String? = null,
    var number: String? = null,
    var trxResult: String? = null,
    var regTime: String? = null,
    var vanId: String? = null,
    var _idx: String? = null,
    var installment: String? = null,
    var rfdDay: String? = null,
    var mchtId: String? = null,
    var brand: String? = null,
    var rfdId: String? = null
)
