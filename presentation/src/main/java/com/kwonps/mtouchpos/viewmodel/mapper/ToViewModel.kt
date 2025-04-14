package com.kwonps.mtouchpos.viewmodel.mapper

import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.model.paymentHistory.PaymentHistoryData
import com.kwonps.domain.model.paymentHistory.PaymentStatisticData
import com.kwonps.domain.model.user.UserDetailData
import com.kwonps.mtouchpos.viewmodel.PaymentHistoryVM
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.info.PaymentProcessState
import com.kwonps.mtouchpos.vo.info.UserInfo
import com.kwonps.mtouchpos.vo.type.PurchaseType
import com.kwonps.mtouchpos.vo.type.UseCaseResult

fun <T, R> ApiResult<T>.toUseCaseResult(transform: (T) -> R): UseCaseResult<R> =
    when (this) {
        is ApiResult.Error -> UseCaseResult.Error(message)
        is ApiResult.Exception -> UseCaseResult.Exception(exception)
        is ApiResult.Success -> UseCaseResult.Success(transform(value))
    }

fun PaymentDetailData.toCompletePaymentInfo(vat: String?) = ApprovedPaymentType.CompletePaymentViewInfo(
    purchaseType = if(trxResult == PurchaseType.APPROVE.code) PurchaseType.APPROVE else PurchaseType.REFUND,
    totalAmount = totalAmount,
    freeAmount = freeAmount ?: if(vat == null || vat == "N") "0" else totalAmount,
    serviceAmount = serviceAmount ?: "0",
    installment = installment,
    trackId = trackId,
    authDate = authDate,
    authCode = authCode,
    trxId = trxId,
    cardNumber = cardNumber,
    issuer = issuerName ?: "",
    acquirer = purchaseName ?: "",
    cardType = cardType,
    remainAmount = remainAmount
)

fun PaymentHistoryData.toPaymentHistoryInfo(vat: String?) = ApprovedPaymentType.PaymentHistoryViewInfo(
    resultMsg = resultMsg,
    purchaseType = if(trxResult == PurchaseType.APPROVE.description) PurchaseType.APPROVE else PurchaseType.REFUND,
    amount = amount,
    taxAmount = if(vat == null || vat == "N") (amount.toInt() / 11).toString() else "0",
    freeAmount = if(vat == null || vat == "N") "0" else amount,
    installment = installment,
    van = van,
    vanId = vanId,
    vanTrxId = vanTrxId,
    authCd = authCd,
    tmnId = tmnId,
    mchtId = mchtId,
    trxId = trxId,
    trackId = trackId,
    bin = bin,
    cardType = cardType,
    issuer = issuer,
    number = number,
    regDay = regDay,
    regTime = regTime,
    brand = brand,
    rfdId = rfdId,
    rfdDay = rfdDay,
    rfdTime = rfdTime
)

fun PaymentStatisticData.toPaymentStatisticInfo() =
    HashMap<PaymentHistoryVM.StatisticType, PaymentHistoryVM.PaymentStatisticInfo>().apply {
        put(
            PaymentHistoryVM.StatisticType.APPROVE,
            PaymentHistoryVM.PaymentStatisticInfo(
                approveAmount,
                approveCount
            )
        )
        put(
            PaymentHistoryVM.StatisticType.CANCEL,
            PaymentHistoryVM.PaymentStatisticInfo(
                cancelAmount,
                cancelCount
            )
        )
        put(
            PaymentHistoryVM.StatisticType.TOTAL,
            PaymentHistoryVM.PaymentStatisticInfo(
                (approveAmount.toIntWithoutCommas() + cancelAmount.toIntWithoutCommas()).toString(),
                (approveCount.toIntWithoutCommas() + cancelCount.toIntWithoutCommas()).toString()
            )
        )
    }

fun CardReaderStatus.Connection.toDeviceConnectState() = when(this) {
    CardReaderStatus.Connection.Active -> PaymentProcessState.CommunicateCardReader.Active
    is CardReaderStatus.Connection.Establishing -> PaymentProcessState.CommunicateCardReader.Connecting(attempts)
    is CardReaderStatus.Connection.Failure -> PaymentProcessState.CommunicateCardReader.Error(error)
    CardReaderStatus.Connection.Inactive -> PaymentProcessState.CommunicateCardReader.Inactive
}

fun UserDetailData.toUserInfo() = UserInfo(
    tmnId = tmnId,
    serial = serial,
    mchtId = mchtId,
    mchtName = mchtName,
    telNo = telNo,
    ceoName = ceoName,
    address = address,
    identity = identity,
)

fun String.toIntWithoutCommas(): Int = this.replace(",", "").toInt()