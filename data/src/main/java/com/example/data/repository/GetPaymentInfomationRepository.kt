package com.example.data.repository

import com.example.data.apiService.GetPaymentInfomationAPIService
import com.example.data.apiSetting.APISetting
import com.example.domain.repositoryInterface.GetPaymentInfomationRepositoryImpl
import com.mtouch.domain.model.tmsApiRequest.ListTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.StatisticsTmsRequestData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.ListTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.StatisticsTmsResponseData
import io.reactivex.rxjava3.core.Single

class GetPaymentInfomationRepository: GetPaymentInfomationRepositoryImpl {
    override suspend fun getPaymentListByPeriod(
        key: String,
        listTmsRequestData: ListTmsRequestData
    ): Single<ListTmsResponseData> {
        return APISetting().getAPIService<GetPaymentInfomationAPIService>()?.list(key, listTmsRequestData)!!
    }

    override suspend fun getPaymentStatisticsByPeriod(
        key: String,
        statisticsTmsRequestData: StatisticsTmsRequestData
    ): Single<StatisticsTmsResponseData> {
        return APISetting().getAPIService<GetPaymentInfomationAPIService>()?.statistics(key, statisticsTmsRequestData)!!
    }
}