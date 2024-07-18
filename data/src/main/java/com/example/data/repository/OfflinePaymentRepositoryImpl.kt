package com.example.data.repository

import com.example.data.dto.request.RequestOffPayment
import com.example.data.dto.response.ResponseOffPayment
import com.example.data.remote.DataFormat
import com.example.data.remote.apiservice.TmsAPIService
import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.PaymentVanData
import com.example.domain.model.payment.RootPaymentData
import com.example.domain.repositoryInterface.OfflinePaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OfflinePaymentRepositoryImpl @Inject constructor(
    private val apiService: TmsAPIService,
    private val token: String
): OfflinePaymentRepository {
    override suspend fun invoke(
        offlinePaymentData: OfflinePaymentData,
        rootPaymentInfo: RootPaymentData?
    ): Flow<ApiResult<PaymentVanData>> = flow {
        val response = rootPaymentInfo?.let {
            apiService.crule(
                token, DataFormat(offlinePaymentData.toRequestCancelPaymentModel(it.trxId))
            )
        } ?: apiService.rule(
            token, DataFormat(offlinePaymentData.toRequestPaymentModel())
        )
        emit(response.handleApiResult { it.data.toPaymentVanInfo() })
    }.catch { e -> emit(ApiResult.Exception(e)) }

    override suspend fun ksnetSocketCommunicate(
        resultCommunicateData: PaymentProcessStatus.CompleteDeviceCommunication,
        rootPaymentInfo: RootPaymentData?,
        offlinePaymentData: OfflinePaymentData,
        paymentVanInfo: PaymentVanData,
    ): Flow<ApiResult<PaymentDetailData>> = flow {
        val response = apiService.socketKsnet(
            token,
            paymentVanInfo.mappingKsnetSocketCommunicateModel(
                resultCommunicateData = resultCommunicateData,
                rootPaymentInfo = rootPaymentInfo,
                offlinePaymentData = offlinePaymentData
            )
        ).handleApiResultDetail {
            if(it.data.result == "오류") {
                ApiResult.Error(it.data.resultMsg!!)
            } else {
                ApiResult.Success(
                    it.data.toPaymentDetailInfo(
                        trackId = offlinePaymentData.trackId,
                        installment = offlinePaymentData.installment
                    )
                )
            }
        }
        emit(response)
    }.catch { e -> emit(ApiResult.Exception(e)) }

    private fun OfflinePaymentData.toRequestPaymentModel() = RequestOffPayment.Payment(
        amount = totalAmount.toString(),
        installment = installment
    )

    private fun OfflinePaymentData.toRequestCancelPaymentModel(
        rootTrxId: String
    ) = RequestOffPayment.CancelPayment(
        amount = totalAmount.toString(),
        installment = installment,
        trxId = rootTrxId,
    )

    private fun ResponseOffPayment.Payment.toPaymentVanInfo() = PaymentVanData(
        van = van,
        vanId = vanId,
        dptId = secondKey
    )

    private fun PaymentVanData.mappingKsnetSocketCommunicateModel(
        resultCommunicateData: PaymentProcessStatus.CompleteDeviceCommunication,
        rootPaymentInfo: RootPaymentData?,
        offlinePaymentData: OfflinePaymentData
    ) = RequestOffPayment.KsnetSocketCommunicate(
        RequestOffPayment.KsnetSocketCommunicateTms(
            toRequestKsnetSocketCommunicateDataModel(
                cardNumber = resultCommunicateData.cardNumber,
                trxId = rootPaymentInfo?.trxId,
                trackId = offlinePaymentData.trackId
            ),
            toRequestKsnetSocketCommunicateSocketModel(
                paymentData = offlinePaymentData,
                rootPaymentInfo = rootPaymentInfo,
                responseSerialData = resultCommunicateData
            )
        )
    )

    private fun PaymentVanData.toRequestKsnetSocketCommunicateDataModel(
        cardNumber: String,
        trackId: String,
        trxId: String?
    ) = RequestOffPayment.KsnetSocketCommunicateData(
        van = van,
        vanId = vanId,
        trackId = trackId,
        trxId = trxId,
        walletSettle = "N",
        vanPayment = "false",
        cardNumber = cardNumber
    )

    private fun PaymentVanData.toRequestKsnetSocketCommunicateSocketModel(
        paymentData: OfflinePaymentData,
        rootPaymentInfo: RootPaymentData?,
        responseSerialData: PaymentProcessStatus.CompleteDeviceCommunication
    ) = RequestOffPayment.KsnetSocketCommunicateSocket(
        transType = "IC".toByteArray(),
        swModelNumber = "######MTOUCH1101".toByteArray(),
        receiptNo = "".toByteArray(),
        workType = "01".toByteArray(),
        posEntry = "S".toByteArray(),
        filler = "".toByteArray(),
        signData = "".toByteArray(),
        telegramType = (rootPaymentInfo?.let { "0420" } ?: "0200").toByteArray(),
        dptId = dptId.toByteArray(),
        payType = paymentData.installment.toByteArray(),
        totalAmount = paymentData.totalAmount.toAmountByteArray(),
        amount = paymentData.totalAmount.setSupplyAmount(),
        serviceAmount = paymentData.serviceAmount?.toAmountByteArray() ?: "".toByteArray(),
        taxAmount = (paymentData.totalAmount - paymentData.freeAmount!! ?: 0).setTaxAmount(),
        freeAmount = paymentData.freeAmount?.toAmountByteArray() ?: "".toByteArray(),
        signTran = paymentData.totalAmount.setSignTran(),
        readerModelNum = responseSerialData.readerModelNum,
        encryptInfo = responseSerialData.encryptInfo,
        reqEMVData = responseSerialData.reqEMVData,
        trackII = responseSerialData.trackII,
        rootAuthCode = rootPaymentInfo?.authCode?.let { it.toByteArray() } ?: null,
        rootRegDate = rootPaymentInfo?.regDate?.let { it.toByteArray() } ?: null
    )

    private fun ResponseOffPayment.KsnetSocketCommunicate.toPaymentDetailInfo(
        trackId: String,
        installment: String
    ) = PaymentDetailData(
        amount = resultData!!.totalAmount,
        installment = installment,
        authCode = resultData!!.authNum,
        authDate = resultData!!.authDate,
        issuerName = resultData!!.issuerName,
        cardNumber = resultData!!.cardNum,
        trackId = trackId,
        trxId = trxId!!,
        trxResult = resultData.telegramType
    )

    private fun Int.toAmountByteArray() = String.format("%012d", this).toByteArray()
    private fun Int.setSupplyAmount() = (this - (this / 11)).toAmountByteArray()
    private fun Int.setTaxAmount() = (this / 11).toAmountByteArray()
    private fun Int.setSignTran() = (if (this > 50000) "S" else "N").toByteArray()
}