package com.example.domain.repositoryInterface

import com.mtouch.domain.model.tmsApiRequest.CheckTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.KeyTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.ListTmsRequestData
import com.mtouch.domain.model.tmsApiRequest.StatisticsTmsRequestData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.CheckTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.KeyTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.ListTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.StatisticsTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.SummaryTmsResponseData
import io.reactivex.rxjava3.core.Single

interface GetPaymentInfomationRepositoryImpl {
    suspend fun getPaymentListByPeriod(key: String, listTmsRequestData: ListTmsRequestData): Single<ListTmsResponseData>
    suspend fun getPaymentStatisticsByPeriod(key: String, statisticsTmsRequestData: StatisticsTmsRequestData): Single<StatisticsTmsResponseData>
}