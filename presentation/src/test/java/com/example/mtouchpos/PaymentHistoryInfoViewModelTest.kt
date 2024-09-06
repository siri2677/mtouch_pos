package com.example.mtouchpos

import app.cash.turbine.test
import com.example.domain.model.ApiResult
import com.example.domain.model.payment.PaymentDetailData
import com.example.domain.model.paymentHistory.PaymentStatisticData
import com.example.domain.usecase.paymentHistory.FetchPaymentHistoryList
import com.example.domain.usecase.paymentHistory.FetchPaymentHistoryStatistics
import com.example.mtouchpos.viewmodel.PaymentHistoryViewModel
import com.example.mtouchpos.viewmodel.mapper.toPaymentHistoryInfo
import com.example.mtouchpos.viewmodel.mapper.toPaymentStatisticInfo
import com.example.mtouchpos.viewmodel.mapper.toUseCaseResult
import com.example.mtouchpos.vo.type.UseCaseResult
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

    private lateinit var viewModel: PaymentHistoryViewModel
    private lateinit var getPaymentHistoryList: FetchPaymentHistoryList
    private lateinit var getPaymentHistoryStatistics: FetchPaymentHistoryStatistics

    private val periodInfo = PaymentHistoryViewModel.PeriodInfo(
        "20240101",
        "20240101"
    )

    private val samplePaymentDetailData = PaymentDetailData(
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

        viewModel = PaymentHistoryViewModel(
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
            viewModel.updatePeriodInfoAndFetchPaymentList(periodInfo)
            assertEquals(UseCaseResult.Loading, awaitItem())
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