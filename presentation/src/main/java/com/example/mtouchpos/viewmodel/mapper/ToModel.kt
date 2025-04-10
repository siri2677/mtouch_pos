package com.example.mtouchpos.viewmodel.mapper

import com.example.domain.model.payment.AmountData
import com.example.domain.model.payment.DirectPaymentData
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.paymentHistory.PeriodData
import com.example.domain.model.user.UserData
import com.example.mtouchpos.viewmodel.DirectPaymentVM
import com.example.mtouchpos.viewmodel.LoginVM
import com.example.mtouchpos.viewmodel.OfflinePaymentVM
import com.example.mtouchpos.viewmodel.PaymentHistoryVM
import com.example.mtouchpos.vo.info.ApprovedPaymentType
import com.example.mtouchpos.vo.info.UserInfo

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
    installment = String.format("%02d", if (installment == "일시불") 0 else Integer.parseInt(installment.replace("개월", ""))),
    trackId = trackId
)

fun OfflinePaymentVM.OfflinePaymentInfo.Cancel.toCancelPaymentData() = OfflinePaymentData.Cancel(
    amountData = AmountData(
        totalAmount = totalAmount.toInt(),
        freeAmount = freeAmount,
        serviceAmount = serviceAmount,
    ),
    installment = installment,
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