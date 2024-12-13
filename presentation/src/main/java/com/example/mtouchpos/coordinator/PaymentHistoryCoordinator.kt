package com.example.mtouchpos.coordinator

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.mtouchpos.vo.info.ApprovedPaymentType
import com.example.mtouchpos.vo.type.UseCaseResult

class PaymentHistoryCoordinator(
    override val navController: NavController
) : CommonCoordinator(navController) {
    @Composable
    fun observePaymentHistoryInfoList(
        paymentHistoryViewInfo: UseCaseResult<List<ApprovedPaymentType.PaymentHistoryViewInfo>>
    ) { paymentHistoryViewInfo.NavigateForUseCaseResult() }
}