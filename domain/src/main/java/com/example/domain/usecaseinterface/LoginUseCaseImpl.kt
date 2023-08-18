package com.mtouch.domain.useCaseInterface

import com.mtouch.domain.model.tmsApiRequest.KeyTmsRequestData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.KeyTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.SummaryTmsResponseData
import io.reactivex.rxjava3.core.Single

interface LoginUseCaseImpl {
    suspend fun getLoginToken(keyTmsRequestData: KeyTmsRequestData): Single<KeyTmsResponseData>
    fun getPaymentSummaryAboutTerminalId(key: String): Single<SummaryTmsResponseData>
    fun checkAppDestloy()
}