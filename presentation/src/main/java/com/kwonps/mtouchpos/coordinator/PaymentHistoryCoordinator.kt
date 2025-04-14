package com.kwonps.mtouchpos.coordinator

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.kwonps.mtouchpos.viewmodel.PaymentHistoryVM.PaymentStatisticInfo
import com.kwonps.mtouchpos.viewmodel.PaymentHistoryVM.StatisticType
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.type.UseCaseResult

class PaymentHistoryCoordinator(
    override val navController: NavController
) : CommonCoordinator(navController) {
    @Composable
    fun observePaymentHistoryInfoList(
        paymentHistoryViewInfo: UseCaseResult<List<ApprovedPaymentType.PaymentHistoryViewInfo>>
    ) { paymentHistoryViewInfo.NavigateForUseCaseResult() }

    @Composable
    fun observePaymentStatisticInfoList(
        paymentStatisticViewInfo: UseCaseResult<HashMap<StatisticType, PaymentStatisticInfo>>
    ) { paymentStatisticViewInfo.NavigateForUseCaseResult() }
}