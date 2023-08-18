package com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse

class StatisticsTmsResponseData: TmsResponseData(){
    var data: StatisticsTmsResponseBody? = null
    @Transient override var statisticsTmsResponseData: StatisticsTmsResponseData? = null
    @Transient override var status: String? = null
    @Transient override var errorMessage: String? = null
    @Transient override var exceptionMessage: String? = null

    override fun setData(any: Any) {
        this.statisticsTmsResponseData = any as StatisticsTmsResponseData
        this.data = statisticsTmsResponseData!!.data
    }
}

data class StatisticsTmsResponseBody(
    var amount: String? = null,
    var startDay: String? = null,
    var cnt: String? = null,
    var payCnt: String? = null,
    var rfdAmt: String? = null,
    var payAmt: String? = null,
    var rfdCnt: String? = null,
    var result: String? = null,
    var _idx: String? = null,
    var endDay: String? = null
)
