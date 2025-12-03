package com.kwonps.mtouchpos.viewmodel

import app.cash.turbine.test
import com.google.gson.Gson
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
import com.kwonps.mtouchpos.MainDispatcherRule
import com.kwonps.mtouchpos.fakes.FakeCardReaderCommunicateRepository
import com.kwonps.mtouchpos.fakes.FakeDeviceRepository
import com.kwonps.mtouchpos.fakes.FakeOfflinePaymentRepository
import com.kwonps.mtouchpos.fakes.FakeUserRepository
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
    private val gson = Gson()
    private lateinit var viewModel: OfflinePaymentVM

    private val cardReaderData = CardReaderData.Bluetooth(deviceInformation = "device", deviceName = "reader")

    @Before
    fun setup() {
        val deviceRepository = FakeDeviceRepository(gson.toJson(cardReaderData))
        val userRepository = FakeUserRepository().apply {
            currentUserJson = gson.toJson(
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
                )
            )
        }

        val fetchConnectedDeviceInfo = FetchConnectedDeviceInfo(gson, deviceRepository)
        val fetchConnectedUserInfo = FetchConnectedUserInfo(userRepository)
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
            if (state is PaymentProcessState.Complete) {
                assert(state.data.vanTrxId == "trx")
            }
        }
    }

    @Test
    fun `processVanCommunication surfaces errors`() = runTest {
        offlineRepository.communicateResponses = listOf(PaymentResult.Failure(PaymentError.Communication("error")))

        flow {
            viewModel.processVanCommunication(
                vanData = VanData(dptId = "dpt"),
                serialResult = CardReaderStatus.Communication.result(
                    trackII = byteArrayOf(),
                    readerModelNum = byteArrayOf(),
                    encryptInfo = byteArrayOf(),
                    reqEMVData = byteArrayOf(),
                    cardNumber = ""
                ),
                paymentData = OfflinePaymentData.Approve(
                    amountData = AmountData(totalAmount = 10_000, freeAmount = 0, serviceAmount = 0),
                    installment = Installment("일시불"),
                    trackId = "track"
                )
            )
        }.test {
            val errorState = awaitItem() as PaymentProcessState.Error
            assert(errorState.message.contains("error"))
            awaitComplete()
        }
    }

    @Test
    fun `pushOfflinePayment emits completion and errors through state flow`() = runTest {
        val approveStateField = OfflinePaymentVM::class.java.getDeclaredField("_paymentProcessState").apply { isAccessible = true }
        val offlineInfoField = OfflinePaymentVM::class.java.getDeclaredField("_offlinePaymentInfo").apply { isAccessible = true }
        val paymentState = approveStateField.get(viewModel) as MutableStateFlow<PaymentProcessState>
        val offlineInfo = offlineInfoField.get(viewModel) as MutableStateFlow<OfflinePaymentVM.OfflinePaymentInfo>

        paymentState.value = PaymentProcessState.Approve(vanTrackId = "VAN-TRACK")
        offlineInfo.value = OfflinePaymentVM.OfflinePaymentInfo.Approve(
            totalAmount = "11000",
            trackId = "track",
            freeAmount = 1000,
            serviceAmount = 0,
            installment = "일시불"
        )

        val successDetail = PaymentDetailData(
            amount = AmountData(totalAmount = 11000, freeAmount = 1000, serviceAmount = 0),
            installment = Installment("일시불"),
            approval = PaymentDetailData.ApprovalInfo(authCode = "1111", authDate = "240101"),
            tracking = PaymentDetailData.TrackingInfo(trackId = "track", trxId = "trx", trxResult = "0000"),
            card = PaymentDetailData.CardInfo(cardNumber = "1234", cardType = "IC"),
            remainAmount = null
        )
        offlineRepository.pushResponses = listOf(PaymentResult.Success(successDetail))

        val payload = ApprovedPaymentType.CompletePaymentViewInfo(
            purchaseType = com.kwonps.mtouchpos.vo.type.PurchaseType.PAYMENT,
            totalAmount = "11000",
            freeAmount = "1000",
            serviceAmount = "0",
            remainAmount = null,
            installment = "일시불",
            trackId = "track",
            authDate = "240101",
            authCode = "1111",
            trxId = "trx",
            cardType = "IC",
            issuer = "issuer",
            acquirer = "acquirer",
            cardNumber = "1234"
        )

        viewModel.paymentProcessState.test {
            viewModel.pushOfflinePayment(payload)
            assert(awaitItem() is PaymentProcessState.Init)
            assert(awaitItem() is PaymentProcessState.Complete)
        }

        offlineRepository.pushResponses = listOf(PaymentResult.Failure(PaymentError.Communication("sync-fail")))

        viewModel.paymentProcessState.test {
            viewModel.pushOfflinePayment(payload)
            assert(awaitItem() is PaymentProcessState.Init)
            val error = awaitItem() as PaymentProcessState.Error
            assert(error.message == "sync-fail")
        }
    }
}
