package com.example.mtouchpos

import app.cash.turbine.test
import com.example.domain.manager.cardreader.CardReaderCommunicateManager
import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentProcessStatus
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.example.domain.usecase.offlinePayment.RequestOfflineCancelPayment
import com.example.domain.usecase.offlinePayment.RequestOfflinePayment
import com.example.mtouchpos.viewmodel.CardReaderConnectVM
import com.example.mtouchpos.viewmodel.OfflinePaymentVM
import com.example.mtouchpos.viewmodel.mapper.toPaymentProcessState
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

@ExperimentalCoroutinesApi
class RequestOfflinePaymentViewModelTest {
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: OfflinePaymentVM
    private lateinit var fetchConnectedDeviceInfo: FetchConnectedDeviceInfo
    private lateinit var offlinePayment: RequestOfflinePayment
    private lateinit var offlineCancelPayment: RequestOfflineCancelPayment
    private lateinit var deviceCommunicateManager: CardReaderCommunicateManager

    private val sampleOfflinePaymentInfo = OfflinePaymentVM.OfflinePaymentInfo(
        freeAmount = 10,
        installment = "02",
        totalAmount = 1004,
        serviceAmount = 100
    )

    private val sampleOfflineCancelPaymentInfo = OfflinePaymentVM.OfflineCancelPaymentInfo(
        amount = "5000",          // 취소 금액
        installment = "3",        // 할부 개월 수
        trackId = "TRCK20230810XYZ", // 거래 추적 ID
        trxId = "TRX9876543210",  // 거래 ID
        authCode = "AUTH543210",  // 승인 코드
        authDate = "2024-02-20",  // 승인 날짜
        freeAmount = 500,         // 비과세 금액
        serviceAmount = 50        // 서비스 수수료
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

    private val bluetoothDeviceInfo = CardReaderConnectVM.BluetoothDeviceInfo(
        deviceInformation = "f0:00:00:00:00:00",
        deviceName = "ksr03"
    )

    @Before
    fun setup() {
        fetchConnectedDeviceInfo = mockk<FetchConnectedDeviceInfo>()
        offlinePayment = mockk<RequestOfflinePayment>()
        offlineCancelPayment = mockk<RequestOfflineCancelPayment>()
        deviceCommunicateManager = mockk<CardReaderCommunicateManager>()

        viewModel = OfflinePaymentVM(
            fetchConnectedDeviceInfoUseCase = fetchConnectedDeviceInfo,
            offlinePaymentUseCase = offlinePayment,
            offlineCancelPaymentUseCase = offlineCancelPayment,
        )
    }

    @Test
    fun `updateOfflinePaymentInfo correctly`() = runTest {
        viewModel.updateOfflinePaymentInfo(sampleOfflinePaymentInfo)

        with(viewModel.offlinePaymentInfo.value) {
            assertEquals(trackId, sampleOfflinePaymentInfo.trackId)
            assertEquals(installment, sampleOfflinePaymentInfo.installment)
            assertEquals(freeAmount, sampleOfflinePaymentInfo.freeAmount)
            assertEquals(serviceAmount, sampleOfflinePaymentInfo.serviceAmount)
            assertEquals(totalAmount, sampleOfflinePaymentInfo.totalAmount)
        }
    }

    @Test
    fun `updateOfflineCancelPaymentInfo correctly`() = runTest {
        viewModel.updateOfflineCancelPaymentInfo(sampleOfflineCancelPaymentInfo)

        with(viewModel.offlineCancelPaymentInfo.value) {
            assertEquals(amount, sampleOfflineCancelPaymentInfo.amount)
            assertEquals(installment, sampleOfflineCancelPaymentInfo.installment)
            assertEquals(trackId, sampleOfflineCancelPaymentInfo.trackId)
            assertEquals(trxId, sampleOfflineCancelPaymentInfo.trxId)
            assertEquals(authCode, sampleOfflineCancelPaymentInfo.authCode)
            assertEquals(authDate, sampleOfflineCancelPaymentInfo.authDate)
            assertEquals(freeAmount, sampleOfflineCancelPaymentInfo.freeAmount)
            assertEquals(serviceAmount, sampleOfflineCancelPaymentInfo.serviceAmount)
        }
    }

    @Test
    fun `fetchConnectedDeviceInfo correctly`() = runTest {
        coEvery { fetchConnectedDeviceInfo() } returns bluetoothDeviceInfo

        viewModel.fetchConnectedDeviceInfo()

        coVerify(exactly = 1) { fetchConnectedDeviceInfo() }
    }

    @Test
    fun `requestOfflinePayment success emits paymentProcessState result`() = runTest {
        val apiResult = ApiResult.Success(
            PaymentProcessStatus.CompletePayment(paymentDetailData)
        )
        val paymentProcessState = apiResult.toPaymentProcessState()

        coEvery { offlinePayment(any(), any()) } returns flow { emit(apiResult) }

        viewModel.paymentProcessState.test {
            viewModel.requestOfflinePayment(null)
            assertEquals(awaitItem(), paymentProcessState)
        }

        coVerify(exactly = 1) { offlinePayment(any(), any()) }
    }

    @Test
    fun `requestOfflineCancelPayment success emits paymentProcessState result`() = runTest {
        val apiResult = ApiResult.Success(
            PaymentProcessStatus.CompletePayment(paymentDetailData)
        )
        val paymentProcessState = apiResult.toPaymentProcessState()

        coEvery { offlineCancelPayment(any(), any(), any()) } returns flow { emit(apiResult) }

        viewModel.paymentProcessState.test {
            viewModel.requestOfflinePayment(null)
            assertEquals(awaitItem(), paymentProcessState)
        }

        coVerify(exactly = 1) { offlineCancelPayment(any(), any(), any()) }
    }
}