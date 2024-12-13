package com.example.data.repositoryImpl

import com.example.data.dto.request.RequestOffPayment
import com.example.data.dto.response.ResponseOffPayment
import com.example.data.remote.DataFormat
import com.example.data.remote.apiservice.TmsAPIService
import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.OfflinePaymentPushData
import com.example.domain.model.payment.VanData
import com.example.domain.repository.OfflinePaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OfflinePaymentRepositoryImpl @Inject constructor(
    private val apiService: TmsAPIService,
    private val token: String
): OfflinePaymentRepository {
    override suspend fun invoke(
        offlinePaymentData: OfflinePaymentData
    ): Flow<ApiResult<VanData>> = flow {
        fun OfflinePaymentData.toRequestPaymentModel() = RequestOffPayment.Payment(
            amount = amountData.totalAmount.toString(),
            installment = installment,
            trackId = trackId
        )

        fun OfflinePaymentData.toRequestCancelPaymentModel(
            rootTrxId: String
        ) = RequestOffPayment.CancelPayment(
            amount = amountData.totalAmount.toString(),
            installment = installment,
            trxId = rootTrxId,
            trackId = trackId
        )

        fun ResponseOffPayment.Payment.toPaymentVanInfo() = VanData(
            van = van,
            vanId = vanId,
            vanTrackId = trackId,
            dptId = secondKey
        )

        when(offlinePaymentData) {
            is OfflinePaymentData.Approve -> apiService.rule(
                token, DataFormat(offlinePaymentData.toRequestPaymentModel())
            )
            is OfflinePaymentData.Cancel -> apiService.crule(
                token, DataFormat(offlinePaymentData.toRequestCancelPaymentModel(offlinePaymentData.rootTrxId))
            )
        }.let { emit(it.handleApiResult { response -> response.data.toPaymentVanInfo() }) }
    }.catch { e -> emit(ApiResult.Exception(e)) }

    override suspend fun ksnetSocketCommunicate(
        resultCommunicateData: PaymentProcessStatus.CompleteDeviceCommunication,
        offlinePaymentData: OfflinePaymentData,
        paymentVanInfo: VanData,
    ): Flow<ApiResult<PaymentDetailData>> = flow {
        fun VanData.mappingKsnetSocketCommunicateModel(
            resultCommunicateData: PaymentProcessStatus.CompleteDeviceCommunication,
            offlinePaymentData: OfflinePaymentData
        ) = RequestOffPayment.KsnetSocketCommunicate(
            RequestOffPayment.KsnetSocketCommunicateTms(
                toRequestKsnetSocketCommunicateDataModel(
                    cardNumber = resultCommunicateData.cardNumber,
                    offlinePaymentData = offlinePaymentData,
                    paymentVanInfo = paymentVanInfo
                ),
                toRequestKsnetSocketCommunicateSocketModel(
                    offlinePaymentData = offlinePaymentData,
                    responseSerialData = resultCommunicateData
                )
            )
        )

        fun ResponseOffPayment.KsnetSocketCommunicate.toPaymentDetailInfo(
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
            trxResult = resultData!!.telegramType
        )

        apiService.socketKsnet(
            token,
            paymentVanInfo.mappingKsnetSocketCommunicateModel(
                resultCommunicateData = resultCommunicateData,
                offlinePaymentData = offlinePaymentData
            )
        ).handleApiResultDetail {
            if(it.data.result == "오류") {
                ApiResult.Error(it.data.resultMsg!!)
            } else {
                ApiResult.Success(
                    it.data.toPaymentDetailInfo(
                        trackId = paymentVanInfo.vanTrackId,
                        installment = offlinePaymentData.installment
                    )
                )
            }
        }.let { emit(it) }
    }.catch { e -> emit(ApiResult.Exception(e)) }

    override suspend fun push(
        offlinePaymentPushData: OfflinePaymentPushData
    ): Flow<ApiResult<PaymentDetailData>> = flow {
        fun OfflinePaymentPushData.toPushCompletedPaymentModel() = RequestOffPayment.Push(
            amount = amount,
            installment = installment,
            trackId = trackId,
            rootTrxId = rootTrxId,
            vanTrxId = vanTrxId,
            authCd = authCode,
            regDate = authDate,
            number = cardNumber
        )

        fun ResponseOffPayment.PushResultData.toPaymentDetailData(trxId: String) = PaymentDetailData(
            amount = amount,
            installment = installment,
            authCode = authCode,
            authDate = authDate,
            trackId = trackId,
            trxId = trxId,
            trxResult = trxResult,
            cardNumber = cardNumber,
            issuerName = issuerName
        )

        apiService.push(
            token = token,
            body = DataFormat(offlinePaymentPushData.toPushCompletedPaymentModel())
        ).handleApiResultDetail {
            if(it.data.resultCd == "0000") {
                it.data.result?.let {
                    pushResultData -> ApiResult.Success(pushResultData.toPaymentDetailData(it.data.trxId))
                } ?: ApiResult.Error("서버 응답을 받는 중 오류가 발생 하였습니다.")
            } else {
                ApiResult.Error(it.data.resultMsg)
            }
        }.let { emit(it) }
    }.catch { e ->
        emit(ApiResult.Exception(e))
    }

    private fun VanData.toRequestKsnetSocketCommunicateDataModel(
        cardNumber: String,
        offlinePaymentData: OfflinePaymentData,
        paymentVanInfo: VanData
    ) = RequestOffPayment.KsnetSocketCommunicateData(
        van = van,
        vanId = vanId,
        trackId = paymentVanInfo.vanTrackId,
        trxId = when(offlinePaymentData) {
            is OfflinePaymentData.Approve -> null
            is OfflinePaymentData.Cancel -> offlinePaymentData.rootTrxId
        },
        walletSettle = "N",
        vanPayment = "false",
        cardNumber = cardNumber
    )

    private fun VanData.toRequestKsnetSocketCommunicateSocketModel(
        offlinePaymentData: OfflinePaymentData,
        responseSerialData: PaymentProcessStatus.CompleteDeviceCommunication
    ) = RequestOffPayment.KsnetSocketCommunicateSocket(
        transType = "IC".toByteArray(),
        swModelNumber = "######MTOUCH1101".toByteArray(),
        receiptNo = "".toByteArray(),
        workType = "01".toByteArray(),
        posEntry = "S".toByteArray(),
        filler = "".toByteArray(),
        signData = "".toByteArray(),
        telegramType = when(offlinePaymentData) {
            is OfflinePaymentData.Approve -> "0200"
            is OfflinePaymentData.Cancel -> "0420"
        }.toByteArray(),
        dptId = dptId.toByteArray(),
        payType = offlinePaymentData.installment.toByteArray(),
        totalAmount = offlinePaymentData.amountData.totalAmount.toAmountByteArray(),
        amount = offlinePaymentData.amountData.totalAmount.setSupplyAmount(),
        serviceAmount = offlinePaymentData.amountData.serviceAmount.toAmountByteArray(),
        taxAmount = (offlinePaymentData.amountData.totalAmount - offlinePaymentData.amountData.freeAmount).setTaxAmount(),
        freeAmount = offlinePaymentData.amountData.freeAmount.toAmountByteArray(),
        signTran = offlinePaymentData.amountData.totalAmount.setSignTran(),
        readerModelNum = responseSerialData.readerModelNum,
        encryptInfo = responseSerialData.encryptInfo,
        reqEMVData = responseSerialData.reqEMVData,
        trackII = responseSerialData.trackII,
        rootAuthCode = when(offlinePaymentData) {
            is OfflinePaymentData.Approve -> null
            is OfflinePaymentData.Cancel -> offlinePaymentData.authCode.toByteArray()
        },
        rootRegDate = when(offlinePaymentData) {
            is OfflinePaymentData.Approve -> null
            is OfflinePaymentData.Cancel -> offlinePaymentData.authDate.substring(0, 6).toByteArray()
        }
    )

    private fun Int.toAmountByteArray() = String.format("%012d", this).toByteArray()
    private fun Int.setSupplyAmount() = (this - (this / 11)).toAmountByteArray()
    private fun Int.setTaxAmount() = (this / 11).toAmountByteArray()
    private fun Int.setSignTran() = (if (this > 50000) "S" else "N").toByteArray()
}