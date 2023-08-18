package com.example.domain.repositoryInterface

import com.mtouch.domain.model.tmsApiRequest.KeyTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.MchtNameTmsRequestData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.KeyTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.MchtNameTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.SummaryTmsResponseData
import io.reactivex.rxjava3.core.Single

interface LoginRelatedRepositoryImpl {
    suspend fun getLoginToken(keyTmsRequestData: KeyTmsRequestData): Single<KeyTmsResponseData>
    fun getPaymentSummaryAboutTerminalId(key: String): Single<SummaryTmsResponseData>
    suspend fun loginIDCheck(mchtNameTmsRequestData: MchtNameTmsRequestData): Single<MchtNameTmsResponseData>

}