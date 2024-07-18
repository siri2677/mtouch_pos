package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.paymentHistory.FetchPaymentHistoryList
import com.example.domain.usecase.paymentHistory.FetchPaymentHistoryStatistics
import com.example.mtouchpos.viewmodel.mapper.toPaymentHistoryInfo
import com.example.mtouchpos.viewmodel.mapper.toPaymentStatisticInfo
import com.example.mtouchpos.viewmodel.mapper.toPeriodData
import com.example.mtouchpos.viewmodel.mapper.toUseCaseResult
import com.example.mtouchpos.vo.type.PaymentType
import com.example.mtouchpos.vo.type.UseCaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    private val fetchPaymentHistoryListUseCase: FetchPaymentHistoryList,
    private val fetchPaymentHistoryStatisticsUseCase: FetchPaymentHistoryStatistics
) : ViewModel(), Serializable {
    data class PaymentHistoryInfo(
        val paymentType: PaymentType,
        val trackId: String,
        val cardNumber: String,
        val issuer: String,
        val amount: String,
        val installment: String,
        val regDate: String,
        val authCode: String,
        val trxId: String,
        val rootRegDate: String? = null
    )

    data class PaymentStatisticInfo(
        val amount: Int,
        val count: Int
    )

    data class PeriodInfo(
        val first: String,
        val last: String
    ): Serializable

    enum class StatisticType { APPROVE, CANCEL, TOTAL }

    private val _paymentHistoryInfo = MutableSharedFlow<UseCaseResult<List<PaymentHistoryInfo>>>()
    val paymentHistoryInfo = _paymentHistoryInfo.asSharedFlow()

    private val _paymentStatisticInfo = MutableSharedFlow<UseCaseResult<HashMap<StatisticType, PaymentStatisticInfo>>>()
    val paymentStatisticInfo = _paymentStatisticInfo.asSharedFlow()

    private val _savePeriodInfo = MutableStateFlow(PeriodInfo("",""))
    val savePeriodInfo = _savePeriodInfo.asStateFlow()

    fun fetchPaymentList(periodInfo: PeriodInfo) {
        viewModelScope.launch {
            fetchPaymentHistoryListUseCase(periodInfo.toPeriodData()).map { apiResult ->
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

    fun setSaveInstance(periodInfo: PeriodInfo) { _savePeriodInfo.value = periodInfo }
}