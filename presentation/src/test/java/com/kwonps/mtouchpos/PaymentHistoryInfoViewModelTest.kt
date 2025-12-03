package com.kwonps.mtouchpos

import app.cash.turbine.test
import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.payment.AmountData
import com.kwonps.domain.model.payment.Installment
import com.kwonps.domain.model.payment.PaymentDetailData
import com.kwonps.domain.model.paymentHistory.PaymentStatisticData
import com.kwonps.domain.usecase.paymentHistory.FetchPaymentHistoryList
import com.kwonps.domain.usecase.paymentHistory.FetchPaymentHistoryStatistics
import com.kwonps.mtouchpos.viewmodel.PaymentHistoryVM
import com.kwonps.mtouchpos.viewmodel.mapper.toPaymentHistoryInfo
import com.kwonps.mtouchpos.viewmodel.mapper.toPaymentStatisticInfo
import com.kwonps.mtouchpos.viewmodel.mapper.toUseCaseResult
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
class PaymentHistoryInfoViewModelTest {
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PaymentHistoryVM
    private lateinit var getPaymentHistoryList: FetchPaymentHistoryList
    private lateinit var getPaymentHistoryStatistics: FetchPaymentHistoryStatistics

    private val periodInfo = PaymentHistoryVM.PeriodInfo(
        "20240101",
        "20240101"
    )

    private val samplePaymentDetailData = PaymentDetailData(
        amount = AmountData(totalAmount = 10000),
        installment = Installment("12"),
        approval = PaymentDetailData.ApprovalInfo(
            authCode = "AUTH12345",
            authDate = "20230715"
        ),
        tracking = PaymentDetailData.TrackingInfo(
            trackId = "TRCK12345678",
            trxId = "TRX987654321",
            trxResult = "승인"
        ),
        card = PaymentDetailData.CardInfo(
            cardNumber = "1234-5678-1234-5678",
            cardType = null,
            issuerName = "Bank of Example",
            purchaseName = null
        )
    )

    private val samplePaymentStatisticData = PaymentStatisticData(
        approveAmount = "200000",
        approveCount = "10",
        cancelAmount = "10000",
        cancelCount = "2"
    )

    @Before
    fun setup() {
        getPaymentHistoryList = mockk<FetchPaymentHistoryList>()
        getPaymentHistoryStatistics = mockk<FetchPaymentHistoryStatistics>()

        viewModel = PaymentHistoryVM(
            fetchPaymentHistoryListUseCase = getPaymentHistoryList,
            fetchPaymentHistoryStatisticsUseCase = getPaymentHistoryStatistics
        )
    }

    @Test
    fun `fetchPaymentList success emits PaymentHistoryInfoList`() = runTest {
        val apiResult = ApiResult.Success(
            listOf(samplePaymentDetailData)
        )
        val useCaseResult = apiResult.toUseCaseResult {
            it.map { list -> list.toPaymentHistoryInfo() }
        }

        coEvery { getPaymentHistoryList(any()) } returns flow { emit(apiResult) }

        viewModel.paymentHistoryInfo.test {
            viewModel.fetchPaymentList(periodInfo)
            assertEquals(awaitItem(), useCaseResult)
        }

        coVerify(exactly = 1) { getPaymentHistoryList(any()) }
    }

    @Test
    fun `fetchPaymentStatistic success emits PaymentStatisticInfoMap`() = runTest {
        val apiResult = ApiResult.Success(samplePaymentStatisticData)
        val useCaseResult = apiResult.toUseCaseResult {
            it.toPaymentStatisticInfo()
        }

        coEvery { getPaymentHistoryStatistics(any()) } returns flow { emit(apiResult) }

        viewModel.paymentStatisticInfo.test {
            viewModel.fetchPaymentStatistic(periodInfo)
            assertEquals(awaitItem(), useCaseResult)
        }

        coVerify(exactly = 1) { getPaymentHistoryStatistics(any()) }
    }
}