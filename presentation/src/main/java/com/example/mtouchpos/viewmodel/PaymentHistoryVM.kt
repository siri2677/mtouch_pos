package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.paymentHistory.FetchPaymentHistoryList
import com.example.domain.usecase.paymentHistory.FetchPaymentHistoryStatistics
import com.example.mtouchpos.viewmodel.mapper.toPaymentHistoryInfo
import com.example.mtouchpos.viewmodel.mapper.toPaymentStatisticInfo
import com.example.mtouchpos.viewmodel.mapper.toPeriodData
import com.example.mtouchpos.viewmodel.mapper.toUseCaseResult
import com.example.mtouchpos.vo.info.ApprovedPaymentType
import com.example.mtouchpos.vo.type.UseCaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PaymentHistoryVM @Inject constructor(
    private val fetchPaymentHistoryListUseCase: FetchPaymentHistoryList,
    private val fetchPaymentHistoryStatisticsUseCase: FetchPaymentHistoryStatistics
) : ViewModel(), Serializable {
    data class PaymentStatisticInfo(
        val amount: Int,
        val count: Int
    )

    data class PeriodInfo(
        val startDay: String = "",
        val endDay: String = ""
    ) : Serializable {
        companion object {
            private fun getDay(daysAgo: Long): String = SimpleDateFormat(
                "yyyyMMdd",
                Locale.getDefault()
            ).format(
                Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -daysAgo.toInt())
                }.time
            )

            fun createPeriod(startDaysAgo: Long, endDaysAgo: Long) = PeriodInfo(getDay(startDaysAgo), getDay(endDaysAgo))
        }
    }

    enum class StatisticType { APPROVE, CANCEL, TOTAL }

    private val _paymentHistoryInfo =
        MutableSharedFlow<UseCaseResult<List<ApprovedPaymentType.PaymentHistoryViewInfo>>>()
    val paymentHistoryInfo = _paymentHistoryInfo.asSharedFlow()

    private val _paymentStatisticInfo =
        MutableStateFlow<UseCaseResult<HashMap<StatisticType, PaymentStatisticInfo>>>(UseCaseResult.Init)
    val paymentStatisticInfo = _paymentStatisticInfo.asStateFlow()

    private val _periodInfo = MutableStateFlow(PeriodInfo())
    val periodInfo = _periodInfo.asStateFlow()

    fun fetchPaymentList() {
        viewModelScope.launch {
            _paymentHistoryInfo.emit(UseCaseResult.Loading)
            fetchPaymentHistoryListUseCase(periodInfo.value.toPeriodData()).map { apiResult ->
                apiResult.toUseCaseResult { list ->
                    list.map { it.toPaymentHistoryInfo() }
                }
            }.collect { _paymentHistoryInfo.emit(it) }
        }
    }

    fun fetchPaymentStatistic(periodInfo: PeriodInfo) {
        viewModelScope.launch {
            fetchPaymentHistoryStatisticsUseCase(periodInfo.toPeriodData()).map { apiResult ->
                apiResult.toUseCaseResult { it.toPaymentStatisticInfo() }
            }.collect { _paymentStatisticInfo.emit(it) }
        }
    }

//    fun initPaymentHistoryInfo() {
//        viewModelScope.launch {
//            _paymentHistoryInfo.resetReplayCache()
//            _paymentHistoryInfo.emit(UseCaseResult.Init)
//        }
////        _paymentHistoryInfo.value = UseCaseResult.Init
//    }

    fun updatePeriodInfoAndFetchPaymentList(days: Long) {
        _periodInfo.value = PeriodInfo.createPeriod(days, 0)
        fetchPaymentList()
    }

    fun updatePeriodInfoAndFetchPaymentList(periodInfo: PeriodInfo) {
        _periodInfo.value = periodInfo
        fetchPaymentList()
    }
}