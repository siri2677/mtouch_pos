package com.kwonps.data.remote.dto.response

import com.google.gson.annotations.SerializedName

sealed interface ResponseUser {

    data class Login(
        val tmnId: String,
        val bankName: String,
        val phoneNo: String,
        val result: String,
        @SerializedName("Authorization") val authorization: String,
        val semiAuth: String,
        val identity: String,
        val van: String,
        val accntHolder: String,
        val appDirect: String,
        val ceoName: String,
        val addr: String,
        val key: String,
        val agencyEmail: String,
        val distEmail: String,
        val vat: String,
        val agencyTel: String,
        val agencyName: String,
        val telNo: String,
        val apiMaxInstall: String,
        val distTel: String,
        val name: String,
        val distName: String,
        val payKey: String,
        val account: String
    ) : ResponseUser

    data class SearchMerchantId(
        val idType: String,
        val name: String,
        val mchtId: String
    ) : ResponseUser
}

