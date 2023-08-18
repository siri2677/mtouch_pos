package com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse


class KeyTmsResponseData: TmsResponseData() {
    var data: KeyTmsResponseBody? = null
    @Transient override var keyTmsResponseData: KeyTmsResponseData? = null
    @Transient override var status: String? = null
    @Transient override var errorMessage: String? = null
    @Transient override var exceptionMessage: String? = null

    override fun setData(any: Any) {
        this.keyTmsResponseData = any as KeyTmsResponseData
        this.data = keyTmsResponseData!!.data
    }
}

data class KeyTmsResponseBody(
    var tmnId: String? = null,
    var bankName: String? = null,
    var phoneNo: String? = null,
    var result: String? = null,
    var Authorization: String? = null,
    var semiAuth: String? = null,
    var identity: String? = null,
    var accntHolder: String? = null,
    var appDirect: String? = null,
    var ceoName: String? = null,
    var addr: String? = null,
    var key: String? = null,
    var agencyEmail: String? = null,
    var distEmail: String? = null,
    var vat: String? = null,
    var agencyTel: String? = null,
    var agencyName: String? = null,
    var telNo: String? = null,
    var apiMaxInstall: String? = null,
    var distTel: String? = null,
    var name: String? = null,
    var distName: String? = null,
    var payKey: String? = null,
    var account: String? = null
)
