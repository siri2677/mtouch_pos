package com.mtouch.domain.usecase

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.domain.repositoryInterface.LoginRelatedRepositoryImpl
import com.example.domain.service.BluetoothConnectService
import com.example.domain.service.ForecdTerminationService
import com.mtouch.domain.model.tmsApiRequest.KeyTmsRequestData
import com.mtouch.domain.useCaseInterface.LoginUseCaseImpl
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.KeyTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.SummaryTmsResponseData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class LoginUseCase @Inject constructor(val context: Context, private var loginRelatedRepositoryImpl: LoginRelatedRepositoryImpl)
    : LoginUseCaseImpl {

    override suspend fun getLoginToken(keyTmsRequestData: KeyTmsRequestData): Single<KeyTmsResponseData> {
        return loginRelatedRepositoryImpl.getLoginToken(keyTmsRequestData)
    }

    override fun getPaymentSummaryAboutTerminalId(token: String): Single<SummaryTmsResponseData> {
        return loginRelatedRepositoryImpl.getPaymentSummaryAboutTerminalId(token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun checkAppDestloy() {
        val intent = Intent(context, ForecdTerminationService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

}