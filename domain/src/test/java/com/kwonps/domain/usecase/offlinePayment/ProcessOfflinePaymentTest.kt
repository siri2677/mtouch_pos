package com.kwonps.domain.usecase.offlinePayment

import app.cash.turbine.test
import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.model.payment.AmountData
import com.kwonps.domain.model.payment.Installment
import com.kwonps.domain.model.payment.OfflinePaymentData
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.model.payment.PaymentError
import com.kwonps.domain.model.payment.PaymentResult
import com.kwonps.domain.model.payment.VanData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProcessOfflinePaymentTest {

    private val repository = FakeOfflinePaymentRepository()
    private val useCase = ProcessOfflinePayment(repository)

    private val payment = OfflinePaymentData.Approve(
        amountData = AmountData(totalAmount = 10_000, freeAmount = 0, serviceAmount = 0),
        installment = Installment("일시불"),
        trackId = "track"
    )
    private val readerResult = CardReaderStatus.Communication.result(
        trackII = byteArrayOf(),
        readerModelNum = "model".toByteArray(),
        encryptInfo = byteArrayOf(),
        reqEMVData = byteArrayOf(),
        cardNumber = "1234"
    )

    @Test
    fun `emits validation error for invalid amount`() = runTest {
        val invalidPayment = payment.copy(amountData = AmountData(totalAmount = 0, freeAmount = 0, serviceAmount = 0))

        useCase(ProcessOfflinePayment.Input(invalidPayment, readerResult)).test {
            val failure = awaitItem() as PaymentResult.Failure
            assertEquals(PaymentError.Validation("결제 금액은 0보다 커야 합니다."), failure.error)
            awaitComplete()
        }
    }

    @Test
    fun `propagates repository failures`() = runTest {
        repository.requestPaymentResponses = listOf(PaymentResult.Failure(PaymentError.Communication("network")))

        useCase(ProcessOfflinePayment.Input(payment, readerResult)).test {
            val failure = awaitItem() as PaymentResult.Failure
            assertEquals(PaymentError.Communication("network"), failure.error)
            awaitComplete()
        }
    }

    @Test
    fun `returns payment detail on success`() = runTest {
        val vanData = VanData(dptId = "dpt")
        val paymentDetail = PaymentDetailData(
            amount = payment.amountData,
            installment = payment.installment,
            approval = PaymentDetailData.ApprovalInfo(authCode = "auth", authDate = "20240101"),
            tracking = PaymentDetailData.TrackingInfo(trackId = payment.trackId, trxId = "trx", trxResult = "0000"),
            card = PaymentDetailData.CardInfo(cardNumber = "1111", cardType = "IC")
        )
        repository.requestPaymentResponses = listOf(PaymentResult.Success(vanData))
        repository.communicateResponses = listOf(PaymentResult.Success(paymentDetail))

        useCase(ProcessOfflinePayment.Input(payment, readerResult)).test {
            val success = awaitItem() as PaymentResult.Success
            assertEquals(paymentDetail, success.value.paymentDetail)
            assertEquals(vanData, success.value.vanData)
            awaitComplete()
        }
    }
}
