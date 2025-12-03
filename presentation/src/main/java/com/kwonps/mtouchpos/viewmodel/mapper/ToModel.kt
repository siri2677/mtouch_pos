package com.kwonps.mtouchpos.viewmodel.mapper

import com.kwonps.domain.model.payment.AmountData
import com.kwonps.domain.model.payment.DirectPaymentData
import com.kwonps.domain.model.payment.Installment
import com.kwonps.domain.model.payment.OfflinePaymentData
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.model.paymentHistory.PeriodData
import com.kwonps.domain.model.user.UserData
import com.kwonps.mtouchpos.viewmodel.DirectPaymentVM
import com.kwonps.mtouchpos.viewmodel.OfflinePaymentVM
import com.kwonps.mtouchpos.viewmodel.PaymentHistoryVM
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.info.UserInfo
import com.kwonps.mtouchpos.vo.type.PurchaseType
import kotlin.String

fun DirectPaymentVM.DirectPaymentInfo.toApproveDirectPaymentData() = DirectPaymentData.Approve(
    totalAmount = amount.toInt(),
    installment = String.format("%02d", if (installment == "일시불") 0 else installment.replace("개월", "").toInt()),
    trackId = trackId,
    productName = productName,
    cardNumber = cardNumber,
    expiry = expiry,
    cardAuth = cardAuth,
    payerName = payerName,
    payerTel = payerTel,
    authPw = authPw,
    authDob = authDob
)

fun DirectPaymentVM.DirectCancelPaymentInfo.toCancelDirectPaymentData() = DirectPaymentData.Cancel(
    totalAmount = amount.toInt(),
    installment = installment,
    trackId = trackId,
    rootTrxId = trxId,
    cardNumber = cardNumber
)

fun OfflinePaymentVM.OfflinePaymentInfo.Approve.toApprovePaymentData() = OfflinePaymentData.Approve(
    amountData = AmountData(
        totalAmount = totalAmount.toInt(),
        freeAmount = freeAmount,
        serviceAmount = serviceAmount,
    ),
    installment = Installment(String.format("%02d", if (installment == "일시불") 0 else Integer.parseInt(installment.replace("개월", "")))),
    trackId = trackId
)

fun OfflinePaymentVM.OfflinePaymentInfo.Cancel.toCancelPaymentData() = OfflinePaymentData.Cancel(
    amountData = AmountData(
        totalAmount = totalAmount.toInt(),
        freeAmount = freeAmount,
        serviceAmount = serviceAmount,
    ),
    installment = Installment(installment),
    trackId = trackId,
    rootTrxId = rootTrxId,
    authCode = authCode,
    authDate = authDate,
)

fun UserInfo.toUserData() = UserData(
    tmnId = tmnId,
    serial = serial,
    mchtId = mchtId
)

fun PaymentHistoryVM.PeriodInfo.toPeriodData() = PeriodData(
    first = startDay,
    last = endDay,
)

fun ApprovedPaymentType.CompletePaymentViewInfo.toDirectCancelPaymentInfo() = DirectPaymentVM.DirectCancelPaymentInfo(
    amount = totalAmount,
    trackId = trackId!!,
    trxId = trxId!!,
)

fun ApprovedPaymentType.CompletePaymentViewInfo.toPaymentDetailData() = PaymentDetailData(
    amount = AmountData(
        totalAmount = totalAmount.toIntOrZero(),
        freeAmount = freeAmount?.toIntOrZero() ?: 0,
        serviceAmount = serviceAmount?.toIntOrZero() ?: 0,
    ),
    installment = Installment(installment),
    approval = PaymentDetailData.ApprovalInfo(
        authCode = authCode,
        authDate = authDate
    ),
    tracking = PaymentDetailData.TrackingInfo(
        trackId = trackId,
        trxId = trxId,
        trxResult = if(purchaseType == PurchaseType.APPROVE) "0200" else "0420"
    ),
    card = PaymentDetailData.CardInfo(
        cardNumber = cardNumber,
        cardType = cardType,
        issuerName = issuer,
        purchaseName = acquirer,
    ),
    remainAmount = remainAmount
)

private fun String.toIntOrZero(): Int = toIntOrNull() ?: 0