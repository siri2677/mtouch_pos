package com.example.domain.repositoryInterface

import com.mtouch.domain.model.tmsApiRequest.CRuleTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.CheckTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.PushTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.RuleTmsRequestData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.CRuleTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.CheckTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.PushTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.RuleTmsResponseData
import io.reactivex.rxjava3.core.Single

interface PaymentProgressingRepositoryImpl {
    suspend fun requestPayment(key: String, ruleTmsRequestData: RuleTmsRequestData): Single<RuleTmsResponseData>
    suspend fun savePaymentInformation(key: String, pushTmsRequestData: PushTmsRequestData): Single<PushTmsResponseData>
    suspend fun requestPaymentCancellation(key: String, cRuleTmsRequestData: CRuleTmsRequestData): Single<CRuleTmsResponseData>
    suspend fun checkPaymentType(key: String, checkTmsResponseData: CheckTmsRequestData): Single<CheckTmsResponseData>
}