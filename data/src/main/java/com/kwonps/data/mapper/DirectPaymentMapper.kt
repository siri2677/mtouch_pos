package com.kwonps.data.mapper

import com.kwonps.data.remote.dto.request.RequestDirectPayment
import com.kwonps.data.remote.dto.response.ResponseDirectPayment
import com.kwonps.domain.model.payment.AmountData
import com.kwonps.domain.model.payment.DirectPaymentData
import com.kwonps.domain.model.payment.Installment
import com.kwonps.domain.model.payment.PaymentDetailData
import javax.inject.Inject

class DirectPaymentMapper @Inject constructor() {
    fun toApproveRequest(directPaymentData: DirectPaymentData.Approve): RequestDirectPayment.DirectPayment {
        val directPaymentPayCard = RequestDirectPayment.DirectPaymentPayCard(
            number = directPaymentData.cardNumber,
            expiry = directPaymentData.expiry,
            installment = directPaymentData.installment,
            cvv = null,
            cardId = null,
            last4 = null,
            issuer = null,
            cardType = null
        )

        val directPaymentPayProduct = RequestDirectPayment.DirectPaymentPayProduct(
            name = directPaymentData.productName,
            qty = null,
            price = null,
            desc = null
        )

        val directPaymentPayMetadata = RequestDirectPayment.DirectPaymentPayMetadata(
            cardAuth = directPaymentData.cardAuth,
            authPw = directPaymentData.authPw,
            authDob = directPaymentData.authDob
        )

        return RequestDirectPayment.DirectPayment(
            RequestDirectPayment.DirectPaymentPay(
                trxType = "ONTR",
                trackId = directPaymentData.trackId,
                amount = directPaymentData.totalAmount.toString(),
                payerName = directPaymentData.payerName,
                payerTel = directPaymentData.payerTel,
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

    fun toCancelRequest(directPaymentData: DirectPaymentData.Cancel) = RequestDirectPayment.DirectCancelPayment(
        RequestDirectPayment.DirectCancelPaymentRefund(
            trxType = "ONTR",
            trackId = directPaymentData.trackId,
            amount = directPaymentData.totalAmount.toString(),
            rootTrxId = directPaymentData.rootTrxId,
            rootTrxDay = null,
            udf1 = null,
            udf2 = null,
            rootTrackId = null,
            trxId = null,
            authCd = null,
            tmnId = null
        )
    )

    fun toPaymentDetail(response: ResponseDirectPayment.DirectPayment) = PaymentDetailData(
        amount = AmountData(totalAmount = response.pay!!.amount),
        installment = Installment(response.pay.card.installment.toString()),
        approval = PaymentDetailData.ApprovalInfo(
            authCode = response.pay.authCd!!,
            authDate = response.result.create
        ),
        tracking = PaymentDetailData.TrackingInfo(
            trackId = response.pay.trackId,
            trxId = response.pay.trxId,
            trxResult = response.pay.trxType,
        ),
        card = PaymentDetailData.CardInfo(
            cardNumber = "${response.pay.card.bin}${"**********"}${response.pay.card.last4}",
            cardType = null,
            issuerName = response.pay.card.issuer,
            purchaseName = response.pay.product.name,
        ),
        remainAmount = null,
    )

    fun toCancelledPaymentDetail(
        response: ResponseDirectPayment.DirectCancelPayment,
        directPaymentData: DirectPaymentData.Cancel
    ) = PaymentDetailData(
        amount = AmountData(totalAmount = response.refund!!.amount.toInt()),
        installment = Installment(directPaymentData.installment),
        approval = PaymentDetailData.ApprovalInfo(
            authCode = response.refund!!.authCd!!,
            authDate = response.result.create
        ),
        tracking = PaymentDetailData.TrackingInfo(
            trackId = response.refund!!.trackId,
            trxId = response.refund!!.trxId!!,
            trxResult = response.refund.trxType
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
