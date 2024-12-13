package com.example.mtouchpos.viewmodel.mapper

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.paymentHistory.PaymentStatisticData
import com.example.mtouchpos.viewmodel.OfflinePaymentVM
import com.example.mtouchpos.viewmodel.PaymentHistoryVM
import com.example.mtouchpos.vo.info.ApprovedPaymentType
import com.example.mtouchpos.vo.type.PurchaseType
import com.example.mtouchpos.vo.type.UseCaseResult

fun <T, R> ApiResult<T>.toUseCaseResult(transform: (T) -> R): UseCaseResult<R> =
    when (this) {
        is ApiResult.Error -> UseCaseResult.Error(message)
        is ApiResult.Exception -> UseCaseResult.Exception(exception)
        is ApiResult.Success -> UseCaseResult.Success(transform(value))
    }

fun PaymentDetailData.toCompletePaymentInfo() = ApprovedPaymentType.CompletePaymentViewInfo(
    purchaseType = if(trxResult == PurchaseType.APPROVE.code) PurchaseType.APPROVE else PurchaseType.REFUND,
    amount = amount,
    installment = installment,
    trackId = trackId,
    authDate = authDate,
    authCode = authCode,
    trxId = trxId,
    cardNumber = cardNumber
)

fun PaymentDetailData.toPaymentHistoryInfo() = ApprovedPaymentType.PaymentHistoryViewInfo(
    purchaseType = if(trxResult == PurchaseType.APPROVE.code) PurchaseType.APPROVE else PurchaseType.REFUND,
    amount = amount,
    installment = installment,
    trackId = trackId,
    cardNumber = cardNumber,
    issuerName = issuerName!!,
    authDate = authDate,
    authCode = authCode,
    trxId = trxId,
    rootRegDate = rootRegDate
)

fun PaymentStatisticData.toPaymentStatisticInfo() =
    HashMap<PaymentHistoryVM.StatisticType, PaymentHistoryVM.PaymentStatisticInfo>().apply {
        put(
            PaymentHistoryVM.StatisticType.APPROVE,
            PaymentHistoryVM.PaymentStatisticInfo(
                approveAmount.toInt(),
                approveCount.toInt()
            )
        )
        put(
            PaymentHistoryVM.StatisticType.CANCEL,
            PaymentHistoryVM.PaymentStatisticInfo(cancelAmount.toInt(), cancelCount.toInt())
        )
        put(
            PaymentHistoryVM.StatisticType.TOTAL,
            PaymentHistoryVM.PaymentStatisticInfo(
                approveAmount.toInt() + cancelAmount.toInt(),
                approveCount.toInt() + cancelCount.toInt()
            )
        )
    }

fun ApiResult<PaymentProcessStatus>.toPaymentProcessState() = when(this) {
    is ApiResult.Success -> {
        when(val status = this.value) {
            is PaymentProcessStatus.CompletePayment-> {
                OfflinePaymentVM.PaymentProcessState.CompletePayment(
                    status.data.toCompletePaymentInfo()
                )
            }
            is PaymentProcessStatus.ApprovePayment -> {
                OfflinePaymentVM.PaymentProcessState.ApprovePayment(status.trackId)
            }
            is PaymentProcessStatus.CommunicateReader.FallBack -> {
                OfflinePaymentVM.PaymentProcessState.Fallback(status.description)
            }
            PaymentProcessStatus.CommunicateReader.InsertIC -> {
                OfflinePaymentVM.PaymentProcessState.InsertIC
            }
            PaymentProcessStatus.CommunicateReader.ReadingIC -> {
                OfflinePaymentVM.PaymentProcessState.ReadingIC
            }
            is PaymentProcessStatus.ConnectReader.Retry -> {
                OfflinePaymentVM.PaymentProcessState.Retry(status.count)
            }
            else -> throw Exception("not support type: ${this::class.java.simpleName}")
        }
    }
    is ApiResult.Error -> {
        OfflinePaymentVM.PaymentProcessState.Error(this.message)
    }
    is ApiResult.Exception -> {
        OfflinePaymentVM.PaymentProcessState.Error(this.exception.message.toString())
    }
}