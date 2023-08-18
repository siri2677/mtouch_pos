package com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse

class SummaryTmsResponseData: TmsResponseData(){
    var data: SummaryTmsResponseBody? = null
    @Transient override var summaryTmsResponseData: SummaryTmsResponseData? = null
    @Transient override var status: String? = null
    @Transient override var errorMessage: String? = null
    @Transient override var exceptionMessage: String? = null

    override fun setData(any: Any) {
        this.summaryTmsResponseData = any as SummaryTmsResponseData
        this.data = summaryTmsResponseData!!.data
    }
}

data class SummaryTmsResponseBody(
    var result: String? = null,
    var _idx: String? = null,
    var monthPayCnt: String? = null,
    var dayRef: String? = null,
    var monthPay: String? = null,
    var today: String? = null,
    var dayRefCnt: String? = null,
    var monthRefCnt: String? = null,
    var dayPay: String? = null,
    var dayPayCnt: String? = null,
    var monthRef: String? = null
)
