package com.example.data.entity.api.response.tms


data class KeyTmsResponseData(
    var data: KeyTmsResponseBody? = null
)

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
