package com.example.data.repository

import com.example.data.apiService.LoginRelatedAPIService
import com.example.data.apiSetting.APISetting
import com.example.domain.repositoryInterface.LoginRelatedRepositoryImpl
import com.mtouch.domain.model.tmsApiRequest.KeyTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.MchtNameTmsRequestData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.KeyTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.MchtNameTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.SummaryTmsResponseData
import io.reactivex.rxjava3.core.Single

class LoginRelatedRepository: LoginRelatedRepositoryImpl {
    override suspend fun getLoginToken(
        keyTmsRequestData: KeyTmsRequestData
    ): Single<KeyTmsResponseData> {
        return APISetting().getAPIService<LoginRelatedAPIService>()?.key(keyTmsRequestData)!!
    }

    override fun getPaymentSummaryAboutTerminalId(
        key: String
    ): Single<SummaryTmsResponseData> {
        return APISetting().getAPIService<LoginRelatedAPIService>()?.summary(key)!!
    }

    override suspend fun loginIDCheck(
        mchtNameTmsRequestData: MchtNameTmsRequestData
    ): Single<MchtNameTmsResponseData> {
        return APISetting().getAPIService<LoginRelatedAPIService>()?.mchtName(mchtNameTmsRequestData)!!
    }
}