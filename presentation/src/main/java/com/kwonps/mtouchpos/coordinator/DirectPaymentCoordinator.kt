package com.kwonps.mtouchpos.coordinator

import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.kwonps.mtouchpos.view.navgraph.NavigationBundleKey
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState
import com.kwonps.mtouchpos.view.ui.navigate
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.type.UseCaseResult

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