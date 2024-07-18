package com.example.mtouchpos.viewmodel.mapper

import com.example.domain.model.payment.CardData
import com.example.domain.model.payment.DirectPaymentData
import com.example.domain.model.payment.OfflinePaymentData
import com.example.domain.model.payment.RootPaymentData
import com.example.domain.model.paymentHistory.PeriodData
import com.example.domain.model.user.UserData
import com.example.mtouchpos.viewmodel.DirectPaymentViewModel
import com.example.mtouchpos.viewmodel.LoginViewModel
import com.example.mtouchpos.viewmodel.OfflinePaymentViewModel
import com.example.mtouchpos.viewmodel.PaymentHistoryViewModel

fun DirectPaymentViewModel.DirectPaymentInfo.toPaymentData() = OfflinePaymentData(
    totalAmount = if(amount.isNotEmpty()) amount.toInt() else 0,
    installment = installment,
    trackId = trackId
)

fun DirectPaymentViewModel.DirectPaymentInfo.toDirectPaymentData() = DirectPaymentData(
    amount = amount.toInt(),
    installment = installment,
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

fun DirectPaymentViewModel.DirectCancelPaymentInfo.toPaymentData() = OfflinePaymentData(
    totalAmount = if(amount.isNotEmpty()) amount.toInt() else 0,
    installment = installment,
    trackId = trackId
)

fun DirectPaymentViewModel.DirectCancelPaymentInfo.toRootPaymentData() = RootPaymentData(
    trxId = trxId,
    authCode = authCode,
    regDate = authDate
)

fun DirectPaymentViewModel.DirectCancelPaymentInfo.toCardData() = CardData(
    issuer = issuer,
    number = cardNumber
)

fun OfflinePaymentViewModel.OfflinePaymentInfo.toPaymentData() = OfflinePaymentData(
    freeAmount = freeAmount,
    installment = installment,
    totalAmount = totalAmount,
    serviceAmount = serviceAmount,
    trackId = trackId
)

fun OfflinePaymentViewModel.OfflineCancelPaymentInfo.toPaymentData() = OfflinePaymentData(
    freeAmount = freeAmount,
    installment = installment,
    totalAmount = if(amount.isNotEmpty()) amount.toInt() else 0,
    serviceAmount = serviceAmount,
    trackId = trackId
)

fun OfflinePaymentViewModel.OfflineCancelPaymentInfo.toRootPaymentData() = RootPaymentData(
    trxId = trxId,
    authCode = authCode,
    regDate = authDate
)


fun LoginViewModel.UserInfo.toUserData() = UserData(
    tmnId = tmnId,
    serial = serial,
    mchtId = mchtId
)

fun PaymentHistoryViewModel.PeriodInfo.toPeriodData() = PeriodData(
    first = first,
    last = last,
)