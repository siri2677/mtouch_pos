package com.example.mtouchpos.viewmodel.mapper

import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.paymentHistory.PaymentStatisticData
import com.example.mtouchpos.viewmodel.OfflinePaymentViewModel
import com.example.mtouchpos.viewmodel.PaymentHistoryViewModel
import com.example.mtouchpos.vo.data.ApprovedPaymentType
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

fun ApiResult<PaymentProcessStatus>.toPaymentProcessState() = when(this) {
    is ApiResult.Success -> {
        when(val status = this.value) {
            is PaymentProcessStatus.CompletePayment-> {
                OfflinePaymentViewModel.PaymentProcessState.CompletePayment(
                    status.data.toCompletePaymentInfo()
                )
            }
            is PaymentProcessStatus.ApprovePayment -> {
                OfflinePaymentViewModel.PaymentProcessState.Loading
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