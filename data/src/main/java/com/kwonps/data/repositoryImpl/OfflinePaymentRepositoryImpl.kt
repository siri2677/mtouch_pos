package com.kwonps.data.repositoryImpl

import com.kwonps.data.remote.dto.request.RequestOffPayment
import com.kwonps.data.remote.dto.response.ResponseOffPayment
import com.kwonps.data.remote.DataFormat
import com.kwonps.data.remote.apiservice.TmsAPIService
import com.kwonps.data.remote.handleApiResult
import com.kwonps.data.remote.handleApiResultDetail
import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.model.payment.OfflinePaymentData
import com.kwonps.domain.model.payment.OfflinePaymentPushData
import com.kwonps.domain.model.payment.VanData
import com.kwonps.domain.repository.OfflinePaymentRepository
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
                token, DataFormat(offlinePaymentData.toRequestCancelPaymentModel(offlinePaymentData.rootTrxId!!))
            )
        }.let { emit(it.handleApiResult { response -> response.data.toPaymentVanInfo() }) }
    }.catch { e -> emit(ApiResult.Exception(e)) }

    override suspend fun ksnetSocketCommunicate(
        resultCommunicateData: CardReaderStatus.Communication.result,
        offlinePaymentData: OfflinePaymentData,
        paymentVanInfo: VanData,
    ): Flow<ApiResult<PaymentDetailData>> = flow {
        fun mappingKsnetSocketCommunicateModel() = RequestOffPayment.KsnetSocketCommunicate(
            RequestOffPayment.KsnetSocketCommunicateTms(
                toRequestKsnetSocketCommunicateDataModel(
                    offlinePaymentData = offlinePaymentData,
                    cardNumber = resultCommunicateData.cardNumber,
                    paymentVanInfo = paymentVanInfo
                ),
                toRequestKsnetSocketCommunicateSocketModel(
                    resultCommunicateData = resultCommunicateData,
                    offlinePaymentData = offlinePaymentData,
                    paymentVanInfo = paymentVanInfo
                )
            )
        )

        fun ResponseOffPayment.KsnetSocketCommunicate.toPaymentDetailInfo(
            trackId: String?,
            installment: String
        ) = PaymentDetailData(
            totalAmount = resultData!!.totalAmount,
            taxAmount = resultData.taxAmount,
            freeAmount = resultData.freeAmount,
            supplyAmount = resultData.supplyAmount,
            serviceAmount = resultData.serviceAmount,
            installment = installment,
            authCode = resultData.authNum,
            authDate = resultData.authDate,
            issuerName = resultData.issuerName,
            purchaseName = resultData.purchaseName,
            cardNumber = resultData.cardNum,
            trackId = trackId,
            trxId = trxId,
            trxResult = resultData.telegramType,
            cardType = resultData.cardType,
            remainAmount = null
        )

        apiService.socketKsnet(
            token,
            mappingKsnetSocketCommunicateModel()
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
            totalAmount = amount,
            taxAmount = null,
            freeAmount = null,
            supplyAmount = null,
            serviceAmount = null,
            installment = installment,
            authCode = authCode,
            authDate = authDate.substring(2),
            trackId = trackId,
            trxId = trxId,
            trxResult = trxResult,
            cardNumber = cardNumber,
            issuerName = issuerName,
            cardType = null,
            remainAmount = null
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

    private fun toRequestKsnetSocketCommunicateDataModel(
        cardNumber: String,
        offlinePaymentData: OfflinePaymentData,
        paymentVanInfo: VanData
    ) = RequestOffPayment.KsnetSocketCommunicateData(
        van = paymentVanInfo.van,
        vanId = paymentVanInfo.vanId,
        trackId = paymentVanInfo.vanTrackId,
        trxId = when(offlinePaymentData) {
            is OfflinePaymentData.Approve -> null
            is OfflinePaymentData.Cancel -> offlinePaymentData.rootTrxId
        },
        walletSettle = "N",
        vanPayment = if(paymentVanInfo.van == null) "true" else "false",
        cardNumber = cardNumber
    )

    private fun toRequestKsnetSocketCommunicateSocketModel(
        offlinePaymentData: OfflinePaymentData,
        resultCommunicateData: CardReaderStatus.Communication.result,
        paymentVanInfo: VanData
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
        dptId = paymentVanInfo.dptId.toByteArray(),
        payType = offlinePaymentData.installment.toByteArray(),
        totalAmount = offlinePaymentData.amountData.totalAmount.toAmountByteArray(),
        amount = offlinePaymentData.amountData.totalAmount.setSupplyAmount(),
        serviceAmount = offlinePaymentData.amountData.serviceAmount.toAmountByteArray(),
        taxAmount = (offlinePaymentData.amountData.totalAmount - offlinePaymentData.amountData.freeAmount).setTaxAmount(),
        freeAmount = offlinePaymentData.amountData.freeAmount.toAmountByteArray(),
        signTran = offlinePaymentData.amountData.totalAmount.setSignTran(),
        readerModelNum = resultCommunicateData.readerModelNum,
        encryptInfo = resultCommunicateData.encryptInfo,
        reqEMVData = resultCommunicateData.reqEMVData,
        trackII = resultCommunicateData.trackII,
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