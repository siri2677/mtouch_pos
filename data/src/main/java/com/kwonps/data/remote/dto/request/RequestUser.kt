package com.kwonps.data.remote.dto.request

import com.google.gson.annotations.SerializedName

sealed interface RequestUser {
    data class Login(
        val tmnId: String,
        val serial: String,
        @SerializedName("mchtId")
        val merchantId: String,
        val appId: String?,
        val version: String?,
        val telNo: String?
    ): RequestUser

    data class SearchMerchantId(
        val mchtId: String
    ): RequestUser
}