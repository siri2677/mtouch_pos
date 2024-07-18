package com.example.data.repository

import com.example.data.dto.request.RequestDirectPayment
import com.example.data.dto.response.ResponseDirectPayment
import com.example.data.remote.apiservice.PayAPIService
import com.example.domain.model.ApiResult
import com.example.domain.model.payment.CardData
import com.example.domain.model.payment.DirectPaymentData
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.RootPaymentData
import com.example.domain.repositoryInterface.DirectPaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DirectPaymentRepositoryImpl @Inject constructor(
    private val apiService: PayAPIService,
    private val payKey: String
): DirectPaymentRepository {
    override suspend fun approve(
        paymentInfo: OfflinePaymentData,
        directPaymentInfo: DirectPaymentData
    ): Flow<ApiResult<PaymentDetailData>> = flow {
        val response = apiService.sendDirectPayment(
            payKey,
            directPaymentInfo.toRequestDirectPayment(paymentInfo)
        ).handleApiResultDetail {
            if (it.result.resultCd == "0000") {
                ApiResult.Success(it.toPaymentDetailInfo())
            } else {
                ApiResult.Error(it.result.advanceMsg)
            }
        }
        emit(response)
    }.catch { e -> emit(ApiResult.Exception(e)) }

    override suspend fun cancel(
        paymentInfo: OfflinePaymentData,
        rootPaymentInfo: RootPaymentData,
        cardInfo: CardData
    ): Flow<ApiResult<PaymentDetailData>> = flow {
        val response = apiService.sendDirectRefund(
            payKey,
            paymentInfo.toRequestDirectCancelPayment(rootPaymentInfo)
        ).handleApiResultDetail {
            if (it.result.resultCd == "0000") {
                ApiResult.Success(it.toPaymentDetailInfo(paymentInfo, cardInfo))
            } else {
                ApiResult.Error(it.result.advanceMsg)
            }
        }
        emit(response)
    }.catch { e -> emit(ApiResult.Exception(e)) }

    private fun DirectPaymentData.toRequestDirectPayment(
        paymentInfo: OfflinePaymentData
    ): RequestDirectPayment.DirectPayment {
        val directPaymentPayCard = RequestDirectPayment.DirectPaymentPayCard(
            number = cardNumber,
            expiry = expiry,
            installment = paymentInfo.installment,
            cvv = null,
            cardId = null,
            last4 = null,
            issuer = null,
            cardType = null
        )

        val directPaymentPayProduct = RequestDirectPayment.DirectPaymentPayProduct(
            name = productName,
            qty = null,
            price = null,
            desc = null
        )

        val directPaymentPayMetadata = RequestDirectPayment.DirectPaymentPayMetadata(
            cardAuth = cardAuth,
            authPw = authPw,
            authDob = authDob
        )

        return RequestDirectPayment.DirectPayment(
            RequestDirectPayment.DirectPaymentPay(
                trxType = "ONTR",
                trackId = paymentInfo.trackId,
                amount = paymentInfo.totalAmount.toString(),
                payerName = payerName,
                payerTel = payerTel,
                card = directPaymentPayCard,
                product = directPaymentPayProduct,
                payerEmail = null,
                fillerAmt = null,
                udf1 = null,
                udf2 = null,
                metadata = directPaymentPayMetadata,
                trxId = null,
                authCd = null,
                settle = null,
            )
        )
    }

    private fun OfflinePaymentData.toRequestDirectCancelPayment(
        rootPaymentInfo: RootPaymentData
    ) = RequestDirectPayment.DirectCancelPayment(
        RequestDirectPayment.DirectCancelPaymentRefund(
            trxType = "ONTR",
            trackId = trackId,
            amount = totalAmount.toString(),
            rootTrxId = rootPaymentInfo.trxId,
            rootTrxDay = rootPaymentInfo.regDate,
            udf1 = null,
            udf2 = null,
            rootTrackId = null,
            trxId = null,
            authCd = null,
            settle = null
        )
    )

    private fun ResponseDirectPayment.DirectPayment.toPaymentDetailInfo() = PaymentDetailData(
        amount = pay!!.amount.toString(),
        installment = pay!!.card.installment.toString(),
        trackId = pay!!.trackId,
        cardNumber = "${pay!!.card.bin}${"**********"}${pay!!.card.last4}",
        issuerName = pay!!.card.issuer,
        authDate = result.create,
        authCode = pay!!.authCd!!,
        trxId = pay!!.trxId,
        trxResult = pay.trxType
    )

    private fun ResponseDirectPayment.DirectCancelPayment.toPaymentDetailInfo(
        paymentInfo: OfflinePaymentData,
        cardInfo: CardData
    ) = PaymentDetailData(
        amount = refund!!.amount,
        installment = paymentInfo.installment,
        trackId = refund!!.trackId,
        cardNumber = cardInfo.number,
        issuerName = cardInfo.issuer,
        authDate = result.create,
        authCode = refund!!.authCd!!,
        trxId = refund!!.trxId!!,
        trxResult = refund.trxType
    )
}