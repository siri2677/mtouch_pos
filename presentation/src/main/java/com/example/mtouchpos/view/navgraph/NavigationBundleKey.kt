package com.example.mtouchpos.view.navgraph

sealed interface NavigationBundleKey {
    companion object {
        const val reDirectPage = "reDirectPage"
        const val MESSAGE = "message"
        const val ITEM_LIST = "itemList"
        const val RESULT_DATA = "resultData"
        const val onDismiss = "onDismiss"

        const val beforeState = "beforeState"
        const val SEARCH_PERIOD = "searchPeriod"

        const val responseDTO = "responseDTO"
        const val RESPONSE_TMS_API = "responseTmsAPI"
        const val RESPONSE_GET_PAYMENT_LIST = "responseGetPaymentListBody"

        const val responsePayAPI = "responsePayAPI"
        const val saveState = "saveState"
        const val pagerState = "pagerState"
    }
}