package com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse

import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.*


abstract class TmsResponseData {
    open var status: String? = null
    open var errorMessage: String? = null
    open var exceptionMessage: String? = null

    open var checkTmsResponseData:      CheckTmsResponseData? = null
    open var cRuleTmsResponseData:      CRuleTmsResponseData? = null
    open var keyTmsResponseData:        KeyTmsResponseData? = null
    open var listTmsResponseData:       ListTmsResponseData? = null
    open var mchtNameTmsResponseData:   MchtNameTmsResponseData? = null
    open var pushTmsResponseData:       PushTmsResponseData? = null
    open var ruleTmsResponseData:       RuleTmsResponseData? = null
    open var statisticsTmsResponseData: StatisticsTmsResponseData? = null
    open var summaryTmsResponseData:    SummaryTmsResponseData? = null

    open fun setData(any: Any) {}
}