package com.kwonps.mtouchpos

import app.cash.turbine.test
import com.kwonps.domain.model.cardreader.CardReaderData
import com.kwonps.domain.model.payment.PaymentResult
import com.kwonps.domain.model.payment.VanData
import com.kwonps.domain.usecase.cardreader.CommunicateKsnetCardReader
import com.kwonps.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.kwonps.domain.usecase.cardreader.PrintCompletedTransaction
import com.kwonps.domain.usecase.offlinePayment.ProcessOfflinePayment
import com.kwonps.domain.usecase.offlinePayment.RequestOfflinePayment
import com.kwonps.domain.usecase.offlinePayment.SyncReceipt
import com.kwonps.domain.usecase.user.FetchConnectedUserInfo
import com.kwonps.mtouchpos.viewmodel.OfflinePaymentVM
import com.kwonps.mtouchpos.viewmodel.mapper.toPaymentProcessState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
    private lateinit var fetchConnectedUserInfo: FetchConnectedUserInfo
    private lateinit var offlinePayment: RequestOfflinePayment
    private lateinit var processOfflinePayment: ProcessOfflinePayment
    private lateinit var syncReceipt: SyncReceipt
    private lateinit var communicateKsnetCardReader: CommunicateKsnetCardReader
    private lateinit var printCompletedTransaction: PrintCompletedTransaction

    @Before
    fun setup() {
        fetchConnectedDeviceInfo = mockk(relaxed = true)
        fetchConnectedUserInfo = mockk(relaxed = true)
        offlinePayment = mockk(relaxed = true)
        processOfflinePayment = mockk(relaxed = true)
        syncReceipt = mockk(relaxed = true)
        communicateKsnetCardReader = mockk(relaxed = true)
        printCompletedTransaction = mockk(relaxed = true)

        every { fetchConnectedDeviceInfo.getCurrentCardReaderData() } returns CardReaderData.Bluetooth(
            deviceInformation = "f0:00:00:00:00:00",
            deviceName = "ksr03"
        )

        viewModel = OfflinePaymentVM(
            fetchConnectedDeviceInfoUseCase = fetchConnectedDeviceInfo,
            fetchConnectedUserInfo = fetchConnectedUserInfo,
            requestOfflinePaymentUseCase = offlinePayment,
            communicateKsnetCardReader = communicateKsnetCardReader,
            processOfflinePayment = processOfflinePayment,
            syncReceipt = syncReceipt,
            printCompletedTransaction = printCompletedTransaction,
        )
    }

    @Test
    fun `updateOfflinePaymentInfo correctly`() = runTest {
        val sampleOfflinePaymentInfo = OfflinePaymentVM.OfflinePaymentInfo.Approve(
            freeAmount = 10,
            installment = "02",
            totalAmount = "1004",
            serviceAmount = 100
        )

        viewModel.updateOfflinePaymentInfo(sampleOfflinePaymentInfo)

        with(viewModel.offlinePaymentInfo.value) {
            assertEquals(sampleOfflinePaymentInfo.trackId, trackId)
            assertEquals(sampleOfflinePaymentInfo.installment, installment)
            assertEquals(sampleOfflinePaymentInfo.freeAmount, freeAmount)
            assertEquals(sampleOfflinePaymentInfo.serviceAmount, serviceAmount)
            assertEquals(sampleOfflinePaymentInfo.totalAmount, totalAmount)
        }
    }

    @Test
    fun `requestOfflinePayment success emits paymentProcessState result`() = runTest {
        val apiResult = PaymentResult.Success(VanData())
        val paymentProcessState = apiResult.toPaymentProcessState()

        coEvery { offlinePayment(any()) } returns flowOf(apiResult)

        viewModel.paymentProcessState.test {
            viewModel.requestOfflinePayment(null)
            assertEquals(awaitItem(), paymentProcessState)
        }
    }
}
