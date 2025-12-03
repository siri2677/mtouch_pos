package com.kwonps.data.repositoryImpl

import com.kwonps.data.remote.apiservice.PayAPIService
import com.kwonps.data.remote.dto.request.RequestDirectPayment
import com.kwonps.data.remote.dto.response.ResponseDirectPayment
import com.kwonps.data.remote.handleApiResultDetail
import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.payment.AmountData
import com.kwonps.domain.model.payment.DirectPaymentData
import com.kwonps.domain.model.payment.Installment
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.repository.DirectPaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DirectPaymentRepositoryImpl @Inject constructor(
    private val apiService: PayAPIService,
    private val payKey: String
): DirectPaymentRepository {
    override suspend fun approve(
        directPaymentInfo: DirectPaymentData.Approve
    ): Flow<ApiResult<PaymentDetailData>> = flow {
        val response = apiService.sendDirectPayment(
            payKey,
            directPaymentInfo.toRequestDirectPayment()
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
        directPaymentInfo: DirectPaymentData.Cancel
    ): Flow<ApiResult<PaymentDetailData>> = flow {
        val response = apiService.sendDirectRefund(
            payKey,
            directPaymentInfo.toRequestDirectCancelPayment()
        ).handleApiResultDetail {
            if (it.result.resultCd == "0000") {
                ApiResult.Success(it.toPaymentDetailInfo(directPaymentInfo))
            } else {
                ApiResult.Error(it.result.advanceMsg)
            }
        }
        emit(response)
    }.catch { e -> emit(ApiResult.Exception(e)) }

    private fun DirectPaymentData.Approve.toRequestDirectPayment(): RequestDirectPayment.DirectPayment {
        val directPaymentPayCard = RequestDirectPayment.DirectPaymentPayCard(
            number = cardNumber,
            expiry = expiry,
            installment = installment,
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
                trackId = trackId,
                amount = totalAmount.toString(),
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

    private fun DirectPaymentData.Cancel.toRequestDirectCancelPayment() = RequestDirectPayment.DirectCancelPayment(
        RequestDirectPayment.DirectCancelPaymentRefund(
            trxType = "ONTR",
            trackId = trackId,
            amount = totalAmount.toString(),
            rootTrxId = rootTrxId,
            rootTrxDay = null,
            udf1 = null,
            udf2 = null,
            rootTrackId = null,
            trxId = null,
            authCd = null,
            tmnId = null
        )
    )

    private fun ResponseDirectPayment.DirectPayment.toPaymentDetailInfo() = PaymentDetailData(
        amount = AmountData(totalAmount = pay!!.amount),
        installment = Installment(pay.card.installment.toString()),
        approval = PaymentDetailData.ApprovalInfo(
            authCode = pay.authCd!!,
            authDate = result.create
        ),
        tracking = PaymentDetailData.TrackingInfo(
            trackId = pay.trackId,
            trxId = pay.trxId,
            trxResult = pay.trxType,
        ),
        card = PaymentDetailData.CardInfo(
            cardNumber = "${pay.card.bin}${"**********"}${pay.card.last4}",
            cardType = null,
            issuerName = pay.card.issuer,
            purchaseName = pay.product.name,
        ),
        remainAmount = null,
    )

    private fun ResponseDirectPayment.DirectCancelPayment.toPaymentDetailInfo(
        directPaymentData: DirectPaymentData.Cancel
    ) = PaymentDetailData(
        amount = AmountData(totalAmount = refund!!.amount.toInt()),
        installment = Installment(directPaymentData.installment),
        approval = PaymentDetailData.ApprovalInfo(
            authCode = refund!!.authCd!!,
            authDate = result.create
        ),
        tracking = PaymentDetailData.TrackingInfo(
            trackId = refund!!.trackId,
            trxId = refund!!.trxId!!,
            trxResult = refund.trxType
        ),
        card = PaymentDetailData.CardInfo(
            cardNumber = directPaymentData.cardNumber,
            cardType = null,
            issuerName = null,
            purchaseName = null
        ),
        remainAmount = null,
    )
}