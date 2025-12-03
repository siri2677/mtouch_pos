package com.kwonps.mtouchpos.viewmodel

import app.cash.turbine.test
import com.kwonps.domain.model.cardreader.CardReaderData
import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.model.payment.AmountData
import com.kwonps.domain.model.payment.Installment
import com.kwonps.domain.model.payment.OfflinePaymentData
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.model.payment.PaymentError
import com.kwonps.domain.model.payment.PaymentResult
import com.kwonps.domain.model.payment.VanData
import com.kwonps.domain.model.user.UserDetailData
import com.kwonps.domain.usecase.cardreader.CommunicateKsnetCardReader
import com.kwonps.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.kwonps.domain.usecase.cardreader.PrintCompletedTransaction
import com.kwonps.domain.usecase.offlinePayment.ProcessOfflinePayment
import com.kwonps.domain.usecase.offlinePayment.RequestOfflinePayment
import com.kwonps.domain.usecase.offlinePayment.SyncReceipt
import com.kwonps.domain.usecase.user.FetchConnectedUserInfo
import com.kwonps.data.parser.GsonUserDetailParser
import com.kwonps.mtouchpos.MainDispatcherRule
import com.kwonps.mtouchpos.fakes.FakeCardReaderCommunicateRepository
import com.kwonps.mtouchpos.fakes.FakeDeviceRepository
import com.kwonps.mtouchpos.fakes.FakeOfflinePaymentRepository
import com.kwonps.mtouchpos.fakes.FakeUserRepository
import com.kwonps.mtouchpos.fakes.TestCoroutineDispatcherProvider
import com.kwonps.mtouchpos.fakes.TestJsonAdapter
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.info.PaymentProcessState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OfflinePaymentVMTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val offlineRepository = FakeOfflinePaymentRepository()
    private val cardReaderCommunicateRepository = FakeCardReaderCommunicateRepository()
    private val dispatcherProvider = TestCoroutineDispatcherProvider()
    private val jsonAdapter = TestJsonAdapter()
    private lateinit var viewModel: OfflinePaymentVM

    private val cardReaderData = CardReaderData.Bluetooth(deviceInformation = "device", deviceName = "reader")

    @Before
    fun setup() {
        val deviceRepository = FakeDeviceRepository(jsonAdapter.toJson(cardReaderData, CardReaderData::class.java))
        val userRepository = FakeUserRepository().apply {
            currentUserJson = jsonAdapter.toJson(
                UserDetailData(
                    tmnId = "tmn",
                    serial = "serial",
                    mchtId = "mcht",
                    mchtName = "merchant",
                    telNo = "010",
                    ceoName = "ceo",
                    address = "address",
                    identity = "id",
                    semiAuth = "Y",
                    appDirect = "N",
                    van = "KSPAY",
                    key = "key",
                    vat = "Y",
                    apiMaxInstall = "12",
                    payKey = "payKey"
                ),
                UserDetailData::class.java
            )
        }

        val fetchConnectedDeviceInfo = FetchConnectedDeviceInfo(jsonAdapter, deviceRepository, dispatcherProvider)
        val fetchConnectedUserInfo = FetchConnectedUserInfo(userRepository, GsonUserDetailParser())
        val requestOfflinePayment = RequestOfflinePayment(offlineRepository)
        val processOfflinePayment = ProcessOfflinePayment(offlineRepository)
        val syncReceipt = SyncReceipt(offlineRepository)
        val communicateKsnetCardReader = CommunicateKsnetCardReader(
            cardReaderCommunicateRepository,
            com.kwonps.domain.model.cardreader.KsnetCardReaderResponseBuilder(),
            com.kwonps.domain.model.cardreader.KsnetCardReaderRequestBuilder()
        )
        val printCompletedTransaction = PrintCompletedTransaction(
            cardReaderCommunicateRepository,
            com.kwonps.domain.model.cardreader.KsnetCardReaderRequestBuilder()
        )

        viewModel = OfflinePaymentVM(
            fetchConnectedDeviceInfo,
            fetchConnectedUserInfo,
            requestOfflinePayment,
            communicateKsnetCardReader,
            processOfflinePayment,
            syncReceipt,
            printCompletedTransaction
        )
    }

    @Test
    fun `processVanCommunication completes payment`() = runTest {
        val paymentData = OfflinePaymentData.Approve(
            amountData = AmountData(totalAmount = 11_000, freeAmount = 1_100, serviceAmount = 0),
            installment = Installment("일시불"),
            trackId = "track"
        )
        val vanData = VanData(van = "van", vanTrackId = "VAN-TRACK", vanId = "1", dptId = "dpt")
        val detail = PaymentDetailData(
            amount = paymentData.amountData,
            installment = paymentData.installment,
            approval = PaymentDetailData.ApprovalInfo("1111", "240101"),
            tracking = PaymentDetailData.TrackingInfo(trackId = paymentData.trackId, trxId = "trx", trxResult = "0000"),
            card = PaymentDetailData.CardInfo(cardNumber = "1234", cardType = "IC")
        )
        offlineRepository.communicateResponses = listOf(PaymentResult.Success(detail))

        flow {
            viewModel.processVanCommunication(
                vanData = vanData,
                serialResult = CardReaderStatus.Communication.result(
                    trackII = byteArrayOf(),
                    readerModelNum = "reader".toByteArray(),
                    encryptInfo = byteArrayOf(),
                    reqEMVData = byteArrayOf(),
                    cardNumber = "1234"
                ),
                paymentData = paymentData
            )
        }.collect { state ->
            if(state !is PaymentProcessState.Complete) throw AssertionError()
        }
    }

    @Test
    fun `syncReceipt emits complete info`() = runTest {
        val paymentInfo = ApprovedPaymentType.CompletePaymentViewInfo(
            totalAmount = "100",
            freeAmount = "0",
            serviceAmount = "0",
            van = "KSPAY",
            vanId = "van",
            vanTrackId = "van-track",
            trackId = "track",
            trxId = "trx",
            trxResult = "0000",
            installment = "일시불",
            approvalCode = "code",
            approvalTime = "time",
            cardNumber = "1234",
            cardType = "IC",
            issuer = "issuer",
            acquirer = "acquirer",
            merchantName = "name",
            merchantNumber = "number",
            terminalNumber = "terminal",
            sign = "sign",
            payType = "card"
        )

        offlineRepository.syncReceiptResult = PaymentResult.Success(
            PaymentDetailData(
                amount = AmountData(totalAmount = 100),
                installment = Installment("0"),
                approval = PaymentDetailData.ApprovalInfo("0", "0"),
                tracking = PaymentDetailData.TrackingInfo(trackId = "track", trxId = "trx", trxResult = "trxResult"),
                card = PaymentDetailData.CardInfo(cardNumber = "1234", cardType = "IC")
            )
        )

        viewModel.paymentProcessState.test {
            viewModel.syncReceipt(paymentInfo)
            val result = awaitItem()
            assert(result is PaymentProcessState.Complete)
        }
    }

    @Test
    fun `updateOfflinePaymentInfo normalizes amounts`() = runTest {
        val paymentInfo = OfflinePaymentVM.OfflinePaymentInfo.Approve(
            totalAmount = "10000",
            freeAmount = 500,
            serviceAmount = 100,
            installment = "일시불"
        )

        viewModel.updateOfflinePaymentInfo(paymentInfo)

        val updatedInfo = viewModel.offlinePaymentInfo.value as OfflinePaymentVM.OfflinePaymentInfo.Approve
        assert(updatedInfo.freeAmount == paymentInfo.freeAmount)
    }
}
