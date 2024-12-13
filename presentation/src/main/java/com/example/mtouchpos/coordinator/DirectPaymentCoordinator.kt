package com.example.mtouchpos.coordinator

import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.mtouchpos.view.navgraph.NavigationBundleKey
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.navigate
import com.example.mtouchpos.vo.info.ApprovedPaymentType
import com.example.mtouchpos.vo.type.UseCaseResult

class DirectPaymentCoordinator(
    override val navController: NavController
) : CommonCoordinator(navController) {
    private fun ApprovedPaymentType.CompletePaymentViewInfo.navigateToCompletePaymentView() {
        navController.navigate(
            route = "${NavigationGraphState.CommonView.CompletePayment.name}${NavigationGraphState.DirectPaymentView.DirectPayment.name}",
            bundle = bundleOf(
                NavigationBundleKey.RESULT_DATA to this,
                NavigationBundleKey.BEFORE_NAVGRAPH to navController.currentBackStackEntry!!.destination.route!!
            ),
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(
                NavigationGraphState.HomeView.Home.name, false).build()
        )
    }

    @Composable
    fun observeResultPaymentData(
        reactDirectPaymentInfo: UseCaseResult<ApprovedPaymentType.CompletePaymentViewInfo>
    ) {
        reactDirectPaymentInfo.NavigateForUseCaseResult { completePaymentViewInfo ->
            completePaymentViewInfo.value.navigateToCompletePaymentView()
        }
    }
}