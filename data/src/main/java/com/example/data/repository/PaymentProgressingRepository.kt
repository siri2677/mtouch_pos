package com.example.data.repository

import com.example.data.apiService.PaymentProgressingAPIService
import com.example.data.apiSetting.APISetting
import com.example.domain.repositoryInterface.PaymentProgressingRepositoryImpl
import com.mtouch.domain.model.tmsApiRequest.CRuleTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.CheckTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.PushTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.RuleTmsRequestData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.CRuleTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.CheckTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.PushTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.RuleTmsResponseData
import io.reactivex.rxjava3.core.Single

class PaymentProgressingRepository: PaymentProgressingRepositoryImpl {
    override suspend fun requestPayment(
        key: String,
        ruleTmsRequestData: RuleTmsRequestData
    ): Single<RuleTmsResponseData> {
        return APISetting().getAPIService<PaymentProgressingAPIService>()?.rule(key, ruleTmsRequestData)!!
    }

    override suspend fun savePaymentInformation(
        key: String,
        pushTmsRequestData: PushTmsRequestData
    ): Single<PushTmsResponseData> {
        return APISetting().getAPIService<PaymentProgressingAPIService>()?.push(key, pushTmsRequestData)!!
    }

    override suspend fun requestPaymentCancellation(
        key: String,
        cRuleTmsRequestData: CRuleTmsRequestData
    ): Single<CRuleTmsResponseData> {
        return APISetting().getAPIService<PaymentProgressingAPIService>()?.crule(key, cRuleTmsRequestData)!!
    }

    override suspend fun checkPaymentType(
        key: String,
        checkTmsResponseData: CheckTmsRequestData
    ): Single<CheckTmsResponseData> {
        return APISetting().getAPIService<PaymentProgressingAPIService>()?.check(key, checkTmsResponseData)!!
    }
}