package com.example.mtouchpos

import app.cash.turbine.test
import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.usecase.directPayment.RequestDirectCancelPayment
import com.example.domain.usecase.directPayment.RequestDirectPayment
import com.example.mtouchpos.viewmodel.DirectPaymentVM
import com.example.mtouchpos.viewmodel.mapper.toCompletePaymentInfo
import com.example.mtouchpos.viewmodel.mapper.toUseCaseResult
import com.example.mtouchpos.vo.type.PaymentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.Date

@ExperimentalCoroutinesApi
class RequestDirectPaymentViewModelTest {
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: DirectPaymentVM
    private lateinit var directPayment: RequestDirectPayment
    private lateinit var directCancelPayment: RequestDirectCancelPayment

    private val directPaymentInfo = DirectPaymentVM.DirectPaymentInfo(
        amount = "15000",
        installment = "03",
        trackId = "AXD_${Date().time}",
        productName = "High-Quality Headphones",
        cardNumber = "1234-5678-9101-1121",
        expirationYear = Calendar.getInstance().get(Calendar.YEAR).toString(),
        expirationMonth = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString(),
        expiry = "${Calendar.getInstance().get(Calendar.YEAR)}/" +
                "${Calendar.getInstance().get(Calendar.MONTH) + 1}",
        cardAuth = "123",
        payerName = "John Doe",
        payerTel = "010-1234-5678",
        authPw = "9876",
        authDob = "1985-04-12"
    )

    private val directCancelPaymentInfo = DirectPaymentVM.DirectCancelPaymentInfo(
        amount = "12000",
        installment = "6",
        trackId = "TRCK20230712XYZ",
        trxId = "TRX1234567890",
        authCode = "AUTH098765",
        authDate = "2024-01-15",
        issuer = "Example Bank",
        cardNumber = "4111-1111-1111-1111"
    )

    private val paymentDetailData = PaymentDetailData(
        amount = "10000",
        installment = "12",
        trackId = "TRCK12345678",
        cardNumber = "1234-5678-1234-5678",
        issuerName = "Bank of Example",
        trxResult = "승인",
        authDate = "20230715",
        authCode = "AUTH12345",
        trxId = "TRX987654321"
    )

    @Before
    fun setup() {
        directPayment = mockk<RequestDirectPayment>()
        directCancelPayment = mockk<RequestDirectCancelPayment>()

        viewModel = DirectPaymentVM(
            directPayment = directPayment,
            directCancelPayment = directCancelPayment
        )
    }

    @Test
    fun `updatesDirectPaymentInfo correctly`() = runTest {
        viewModel.updateDirectPaymentInfo(directPaymentInfo)

        with(viewModel.directPaymentInfo.value) {
            assertEquals(amount, directPaymentInfo.amount)
            assertEquals(installment, directPaymentInfo.installment)
            assertEquals(trackId, directPaymentInfo.trackId)
            assertEquals(productName, directPaymentInfo.productName)
            assertEquals(cardNumber, directPaymentInfo.cardNumber)
            assertEquals(expirationYear, directPaymentInfo.expirationYear)
            assertEquals(expirationMonth, directPaymentInfo.expirationMonth)
            assertEquals(expiry, directPaymentInfo.expiry)
            assertEquals(cardAuth, directPaymentInfo.cardAuth)
            assertEquals(payerName, directPaymentInfo.payerName)
            assertEquals(payerTel, directPaymentInfo.payerTel)
            assertEquals(authPw, directPaymentInfo.authPw)
            assertEquals(authDob, directPaymentInfo.authDob)
        }
    }


    @Test
    fun `updateDirectCancelPaymentInfo correctly`() = runTest {
        viewModel.updateDirectCancelPaymentInfo(directCancelPaymentInfo)

        with(viewModel.directCancelPaymentInfo.value) {
            assertEquals(amount, directCancelPaymentInfo.amount)
            assertEquals(installment, directCancelPaymentInfo.installment)
            assertEquals(trackId, directCancelPaymentInfo.trackId)
            assertEquals(trxId, directCancelPaymentInfo.trxId)
            assertEquals(authCode, directCancelPaymentInfo.authCode)
            assertEquals(authDate, directCancelPaymentInfo.authDate)
            assertEquals(issuer, directCancelPaymentInfo.issuer)
            assertEquals(cardNumber, directCancelPaymentInfo.cardNumber)
        }
    }

    @Test
    fun `directPayment success emits completePaymentInfo result`() = runTest {
        val apiResult = ApiResult.Success(paymentDetailData)
        val useCaseResult = apiResult.toUseCaseResult {
            it.toCompletePaymentInfo(PaymentType.DIRECT)
        }

        coEvery { directPayment(any(), any()) } returns flow { emit(apiResult) }

        viewModel.reactDirectPaymentInfo.test {
            viewModel.requestDirectPayment()
            assertEquals(awaitItem(), useCaseResult)
        }

        coVerify(exactly = 1) { directPayment(any(), any()) }
    }

    @Test
    fun `directCancelPayment success emits completePaymentInfo result`() = runTest {
        val apiResult = ApiResult.Success(paymentDetailData)
        val useCaseResult = apiResult.toUseCaseResult {
            it.toCompletePaymentInfo(PaymentType.DIRECT)
        }

        coEvery { directCancelPayment(any(), any(), any()) } returns flow { emit(apiResult) }

        viewModel.reactDirectPaymentInfo.test {
            viewModel.requestDirectCancelPayment()
            assertEquals(awaitItem(), useCaseResult)
        }

        coVerify(exactly = 1) { directCancelPayment(any(), any(), any()) }
    }
}