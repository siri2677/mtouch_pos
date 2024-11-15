package com.example.mtouchpos.viewmodel.mapper

import com.example.domain.model.payment.AmountData
import com.example.domain.model.payment.DirectPaymentData
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.paymentHistory.PeriodData
import com.example.domain.model.user.UserData
import com.example.mtouchpos.viewmodel.DirectPaymentViewModel
import com.example.mtouchpos.viewmodel.LoginViewModel
import com.example.mtouchpos.viewmodel.OfflinePaymentViewModel
import com.example.mtouchpos.viewmodel.PaymentHistoryViewModel
import com.example.mtouchpos.vo.data.ApprovedPaymentType
import java.util.Date

fun DirectPaymentViewModel.DirectPaymentInfo.toApproveDirectPaymentData() = DirectPaymentData.Approve(
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

fun DirectPaymentViewModel.DirectCancelPaymentInfo.toCancelDirectPaymentData() = DirectPaymentData.Cancel(
    totalAmount = amount.toInt(),
    installment = installment,
    trackId = trackId,
    rootTrxId = trxId,
    cardNumber = cardNumber
)

fun OfflinePaymentViewModel.OfflinePaymentInfo.Approve.toApprovePaymentData() = OfflinePaymentData.Approve(
    amountData = AmountData(
        totalAmount = totalAmount,
        freeAmount = freeAmount,
        serviceAmount = serviceAmount,
    ),
    installment = String.format("%02d", if (installment == "일시불") 0 else Integer.parseInt(installment.replace("개월", ""))),
    trackId = trackId
)

fun OfflinePaymentViewModel.OfflinePaymentInfo.Cancel.toCancelPaymentData() = OfflinePaymentData.Cancel(
    amountData = AmountData(
        totalAmount = amount.toInt(),
        freeAmount = freeAmount,
        serviceAmount = serviceAmount,
    ),
    installment = installment,
    trackId = trackId,
    rootTrxId = rootTrxId,
    authCode = authCode,
    authDate = authDate,
)

fun LoginViewModel.UserInfo.toUserData() = UserData(
    tmnId = tmnId,
    serial = serial,
    mchtId = mchtId
)

fun PaymentHistoryViewModel.PeriodInfo.toPeriodData() = PeriodData(
    first = startDay,
    last = endDay,
)

fun ApprovedPaymentType.toDirectCancelPaymentInfo() = DirectPaymentViewModel.DirectCancelPaymentInfo(
    amount = amount,
    trackId = trackId,
    trxId = trxId,
)