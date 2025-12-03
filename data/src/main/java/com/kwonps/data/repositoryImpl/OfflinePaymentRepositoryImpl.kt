package com.kwonps.data.repositoryImpl

import com.kwonps.data.remote.dto.request.RequestOffPayment
import com.kwonps.data.remote.dto.response.ResponseOffPayment
import com.kwonps.data.remote.DataFormat
import com.kwonps.data.remote.apiservice.TmsAPIService
import com.kwonps.data.remote.handleApiResult
import com.kwonps.data.remote.handleApiResultDetail
import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.model.payment.PaymentError
import com.kwonps.domain.model.payment.PaymentResult
import com.kwonps.domain.model.payment.OfflinePaymentData
import com.kwonps.domain.model.payment.OfflinePaymentPushData
import com.kwonps.domain.model.payment.VanData
import com.kwonps.domain.repository.OfflinePaymentRepository
import com.kwonps.domain.service.payment.PaymentAmountCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OfflinePaymentRepositoryImpl @Inject constructor(
    private val apiService: TmsAPIService,
    private val token: String
) : OfflinePaymentRepository {
    override suspend fun requestPayment(
        offlinePaymentData: OfflinePaymentData
    ): Flow<PaymentResult<VanData>> = flow {
        fun OfflinePaymentData.toRequestPaymentModel() = RequestOffPayment.Payment(
            amount = amountData.totalAmount.toString(),
            installment = installment.value,
            trackId = trackId
        )

        fun OfflinePaymentData.toRequestCancelPaymentModel(
            rootTrxId: String
        ) = RequestOffPayment.CancelPayment(
            amount = amountData.totalAmount.toString(),
            installment = installment.value,
            trxId = rootTrxId,
            trackId = trackId
        )

        fun ResponseOffPayment.Payment.toPaymentVanInfo() = VanData(
            van = van,
            vanId = vanId,
            vanTrackId = trackId,
            dptId = secondKey
        )

        val apiResult = when (offlinePaymentData) {
            is OfflinePaymentData.Approve -> apiService.rule(
                token, DataFormat(offlinePaymentData.toRequestPaymentModel())
            )

            is OfflinePaymentData.Cancel -> apiService.crule(
                token, DataFormat(offlinePaymentData.toRequestCancelPaymentModel(offlinePaymentData.rootTrxId!!))
            )
        }.handleApiResult { response -> response.data.toPaymentVanInfo() }

        emit(apiResult.toPaymentResult())
    }.catch { e -> emit(PaymentResult.Failure(PaymentError.Unknown(e))) }

    override suspend fun communicateWithVan(
        resultCommunicateData: CardReaderStatus.Communication.result,
        offlinePaymentData: OfflinePaymentData,
        paymentVanInfo: VanData,
    ): Flow<PaymentResult<PaymentDetailData>> = flow {
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
        ): PaymentDetailData {
            val amountData = com.kwonps.domain.model.payment.AmountData(
                totalAmount = resultData?.totalAmount?.toIntOrZero() ?: 0,
                freeAmount = resultData?.freeAmount?.toIntOrZero() ?: 0,
                serviceAmount = resultData?.serviceAmount?.toIntOrZero() ?: 0
            )

            return PaymentDetailData(
                amount = amountData,
                installment = com.kwonps.domain.model.payment.Installment(installment),
                approval = PaymentDetailData.ApprovalInfo(
                    authCode = resultData!!.authNum,
                    authDate = resultData.authDate,
                    rootRegDate = resultData.rootRegDate
                ),
                tracking = PaymentDetailData.TrackingInfo(
                    trackId = trackId,
                    trxId = trxId,
                    trxResult = resultData.telegramType
                ),
                card = PaymentDetailData.CardInfo(
                    cardNumber = resultData.cardNum,
                    cardType = resultData.cardType,
                    issuerName = resultData.issuerName,
                    purchaseName = resultData.purchaseName
                ),
                remainAmount = null
            )
        }

        apiService.socketKsnet(
            token,
            mappingKsnetSocketCommunicateModel()
        ).handleApiResultDetail {
            if (it.data.result == "오류") {
                PaymentResult.Failure(PaymentError.Communication(it.data.resultMsg!!))
            } else {
                PaymentResult.Success(
                    it.data.toPaymentDetailInfo(
                        trackId = paymentVanInfo.vanTrackId,
                        installment = offlinePaymentData.installment.value
                    )
                )
            }
        }.let { emit(it) }
    }.catch { e -> emit(PaymentResult.Failure(PaymentError.Unknown(e))) }

    override suspend fun pushReceipt(
        offlinePaymentPushData: OfflinePaymentPushData
    ): Flow<PaymentResult<PaymentDetailData>> = flow {
        fun OfflinePaymentPushData.toPushCompletedPaymentModel() = RequestOffPayment.Push(
            amount = amount.totalAmount.toString(),
            installment = installment.value,
            trackId = identifiers.trackId,
            rootTrxId = identifiers.rootTrxId,
            vanTrxId = identifiers.vanTrxId,
            authCd = approval.authCode,
            regDate = approval.authDate,
            number = cardNumber
        )

        fun ResponseOffPayment.PushResultData.toPaymentDetailData(trxId: String, pushData: OfflinePaymentPushData) =
            PaymentDetailData(
                amount = com.kwonps.domain.model.payment.AmountData(totalAmount = amount.toIntOrZero()),
                installment = pushData.installment,
                approval = PaymentDetailData.ApprovalInfo(
                    authCode = authCode,
                    authDate = authDate.substring(2)
                ),
                tracking = PaymentDetailData.TrackingInfo(
                    trackId = trackId,
                    trxId = trxId,
                    trxResult = trxResult
                ),
                card = PaymentDetailData.CardInfo(
                    cardNumber = cardNumber,
                    cardType = null,
                    issuerName = issuerName,
                    purchaseName = null
                ),
                remainAmount = null
            )

        apiService.push(
            token = token,
            body = DataFormat(offlinePaymentPushData.toPushCompletedPaymentModel())
        ).handleApiResultDetail {
            if (it.data.resultCd == "0000") {
                it.data.result?.let { pushResultData ->
                    PaymentResult.Success(
                        pushResultData.toPaymentDetailData(
                            trxId = it.data.trxId,
                            pushData = offlinePaymentPushData
                        )
                    )
                } ?: PaymentResult.Failure(PaymentError.Communication("서버 응답을 받는 중 오류가 발생 하였습니다."))
            } else {
                PaymentResult.Failure(PaymentError.Communication(it.data.resultMsg))
            }
        }.let { emit(it) }
    }.catch { e ->
        emit(PaymentResult.Failure(PaymentError.Unknown(e)))
    }

    private fun toRequestKsnetSocketCommunicateDataModel(
        cardNumber: String,
        offlinePaymentData: OfflinePaymentData,
        paymentVanInfo: VanData
    ) = RequestOffPayment.KsnetSocketCommunicateData(
        van = paymentVanInfo.van,
        vanId = paymentVanInfo.vanId,
        trackId = paymentVanInfo.vanTrackId,
        trxId = when (offlinePaymentData) {
            is OfflinePaymentData.Approve -> null
            is OfflinePaymentData.Cancel -> offlinePaymentData.rootTrxId
        },
        walletSettle = "N",
        vanPayment = if (paymentVanInfo.van == null) "true" else "false",
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
        telegramType = when (offlinePaymentData) {
            is OfflinePaymentData.Approve -> "0200"
            is OfflinePaymentData.Cancel -> "0420"
        }.toByteArray(),
        dptId = paymentVanInfo.dptId.toByteArray(),
        payType = offlinePaymentData.installment.value.toByteArray(),
        totalAmount = offlinePaymentData.amountData.totalAmount.toAmountByteArray(),
        amount = offlinePaymentData.amountData.supplyAmount.toAmountByteArray(),
        serviceAmount = offlinePaymentData.amountData.serviceAmount.toAmountByteArray(),
        taxAmount = offlinePaymentData.amountData.vatAmount.toAmountByteArray(),
        freeAmount = offlinePaymentData.amountData.freeAmount.toAmountByteArray(),
        signTran = PaymentAmountCalculator.signTranFlag(offlinePaymentData.amountData.totalAmount).toByteArray(),
        readerModelNum = resultCommunicateData.readerModelNum,
        encryptInfo = resultCommunicateData.encryptInfo,
        reqEMVData = resultCommunicateData.reqEMVData,
        trackII = resultCommunicateData.trackII,
        rootAuthCode = when (offlinePaymentData) {
            is OfflinePaymentData.Approve -> null
            is OfflinePaymentData.Cancel -> offlinePaymentData.authCode.toByteArray()
        },
        rootRegDate = when (offlinePaymentData) {
            is OfflinePaymentData.Approve -> null
            is OfflinePaymentData.Cancel -> offlinePaymentData.authDate.substring(0, 6).toByteArray()
        }
    )

    private fun Int.toAmountByteArray() = String.format("%012d", this).toByteArray()

    private fun String.toIntOrZero(): Int = toIntOrNull() ?: 0

    private fun <T> com.kwonps.domain.model.ApiResult<T>.toPaymentResult(): PaymentResult<T> = when (this) {
        is com.kwonps.domain.model.ApiResult.Error -> PaymentResult.Failure(PaymentError.Communication(message))
        is com.kwonps.domain.model.ApiResult.Exception -> PaymentResult.Failure(PaymentError.Unknown(exception))
        is com.kwonps.domain.model.ApiResult.Success -> PaymentResult.Success(value)
    }
}
