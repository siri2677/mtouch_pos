package com.kwonps.mtouchpos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.kwonps.mtouchpos.viewmodel.PaymentHistoryVM.PaymentStatisticInfo
import com.kwonps.mtouchpos.viewmodel.PaymentHistoryVM.StatisticType
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.type.UseCaseResult

class PaymentHistoryRouter(
    private val navigator: NavigationHandler
) {
    @Composable
    fun observePaymentHistoryInfoList(
        paymentHistoryViewInfo: UseCaseResult<List<ApprovedPaymentType.PaymentHistoryViewInfo>>
    ) {
        navigator.run { paymentHistoryViewInfo.handleResult() }
    }

    @Composable
    fun observePaymentStatisticInfoList(
        paymentStatisticViewInfo: UseCaseResult<HashMap<StatisticType, PaymentStatisticInfo>>
    ) {
        navigator.run { paymentStatisticViewInfo.handleResult() }
    }
}

@Composable
fun rememberPaymentHistoryRouter(navController: NavController): PaymentHistoryRouter {
    val navigator = rememberNavigationHandler(navController)
    return remember(navigator) { PaymentHistoryRouter(navigator) }
}
