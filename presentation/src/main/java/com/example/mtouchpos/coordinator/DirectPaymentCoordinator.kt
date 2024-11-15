package com.example.mtouchpos.coordinator

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.mtouchpos.vo.data.ApprovedPaymentType
import com.example.mtouchpos.vo.type.UseCaseResult

class DirectPaymentCoordinator(
    override val navController: NavController
) : CommonCoordinator(navController) {
    @Composable
    fun observeResultPaymentData(
        reactDirectPaymentInfo: UseCaseResult<ApprovedPaymentType.CompletePaymentViewInfo>
    ) {
        reactDirectPaymentInfo.navigateForUseCaseResult { completePaymentViewInfo ->
            completePaymentViewInfo.value.navigateToCompletePaymentView(navController)
        }
    }
}