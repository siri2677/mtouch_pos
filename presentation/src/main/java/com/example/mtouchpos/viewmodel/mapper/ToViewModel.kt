package com.example.mtouchpos.viewmodel.mapper

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.paymentHistory.PaymentStatisticData
import com.example.mtouchpos.viewmodel.DirectPaymentViewModel
import com.example.mtouchpos.viewmodel.OfflinePaymentViewModel
import com.example.mtouchpos.viewmodel.PaymentHistoryViewModel
import com.example.mtouchpos.vo.data.CompletePaymentInfo
import com.example.mtouchpos.vo.type.PaymentType
import com.example.mtouchpos.vo.type.TransactionType
import com.example.mtouchpos.vo.type.UseCaseResult

fun <T, R> ApiResult<T>.toUseCaseResult(transform: (T) -> R): UseCaseResult<R> =
    when (this) {
        is ApiResult.Error -> UseCaseResult.Error(message)
        is ApiResult.Exception -> UseCaseResult.Exception(exception)
        is ApiResult.Success -> UseCaseResult.Success(transform(value))
    }

fun CompletePaymentInfo.toDirectCancelPaymentInfo() =
    DirectPaymentViewModel.DirectCancelPaymentInfo(
        amount = amount,
        installment = installment,
        trackId = trackId,
        trxId = trxId,
        authCode = authCode,
        authDate = authDate,
        issuer = issuer,
        cardNumber = cardNumber,
    )

fun CompletePaymentInfo.toOfflineCancelPaymentInfo(
    freeAmount: Int,
    serviceAmount: Int
) = OfflinePaymentViewModel.OfflineCancelPaymentInfo(
    amount = amount,
    installment = installment,
    trackId = trackId,
    trxId = trxId,
    authCode = authCode,
    authDate = authDate,
    freeAmount = freeAmount,
    serviceAmount = serviceAmount,
)

fun PaymentHistoryViewModel.PaymentHistoryInfo.toOfflineCancelPaymentInfo(
    freeAmount: Int,
    serviceAmount: Int
) = OfflinePaymentViewModel.OfflineCancelPaymentInfo(
    amount = amount,
    installment = installment,
    trackId = trackId,
    trxId = trxId,
    authCode = authCode,
    authDate = regDate,
    freeAmount = freeAmount,
    serviceAmount = serviceAmount,
)


fun PaymentDetailData.toCompletePaymentInfo(transactionType: TransactionType) = CompletePaymentInfo(
    transactionType = transactionType,
    paymentType = if(trxResult == PaymentType.APPROVE.code) PaymentType.APPROVE else PaymentType.REFUND,
    amount = amount,
    installment = installment,
    trackId = trackId,
    cardNumber = cardNumber,
    issuer = issuerName,
    authDate = authDate,
    authCode = authCode,
    trxId = trxId
)

fun PaymentDetailData.toPaymentHistoryInfo() = PaymentHistoryViewModel.PaymentHistoryInfo(
    paymentType = if(trxResult == PaymentType.APPROVE.code) PaymentType.APPROVE else PaymentType.REFUND,
    trackId = trackId,
    cardNumber = cardNumber,
    issuer = issuerName,
    amount = amount,
    installment = installment,
    regDate = authDate,
    authCode = authCode,
    trxId = trxId,
    rootRegDate = rootRegData
)

fun PaymentStatisticData.toPaymentStatisticInfo() =
    HashMap<PaymentHistoryViewModel.StatisticType, PaymentHistoryViewModel.PaymentStatisticInfo>().apply {
        put(
            PaymentHistoryViewModel.StatisticType.APPROVE,
            PaymentHistoryViewModel.PaymentStatisticInfo(
                approveAmount.toInt(),
                approveCount.toInt()
            )
        )
        put(
            PaymentHistoryViewModel.StatisticType.CANCEL,
            PaymentHistoryViewModel.PaymentStatisticInfo(cancelAmount.toInt(), cancelCount.toInt())
        )
        put(
            PaymentHistoryViewModel.StatisticType.TOTAL,
            PaymentHistoryViewModel.PaymentStatisticInfo(
                approveAmount.toInt() + cancelAmount.toInt(),
                approveCount.toInt() + cancelCount.toInt()
            )
        )
    }

fun ApiResult<PaymentProcessStatus>.toPaymentProcessState(): OfflinePaymentViewModel.PaymentProcessState = when(this) {
    is ApiResult.Success -> {
        when(val status = this.value) {
            is PaymentProcessStatus.CompletePayment-> {
                OfflinePaymentViewModel.PaymentProcessState.CompletePayment(
                    status.data.toCompletePaymentInfo(TransactionType.OFFLINE)
                )
            }
            is PaymentProcessStatus.DeviceCommunication.FallBack -> {
                OfflinePaymentViewModel.PaymentProcessState.Fallback(status.description)
            }
            PaymentProcessStatus.DeviceCommunication.InsertIC -> {
                OfflinePaymentViewModel.PaymentProcessState.InsertIC
            }
            PaymentProcessStatus.DeviceCommunication.ReadingIC -> {
                OfflinePaymentViewModel.PaymentProcessState.ReadingIC
            }
            else -> throw Exception("not support type: ${this::class.java.simpleName}")
        }
    }
    is ApiResult.Error -> {
        OfflinePaymentViewModel.PaymentProcessState.Error(this.message)
    }
    is ApiResult.Exception -> {
        OfflinePaymentViewModel.PaymentProcessState.Error(this.exception.message.toString())
    }
}