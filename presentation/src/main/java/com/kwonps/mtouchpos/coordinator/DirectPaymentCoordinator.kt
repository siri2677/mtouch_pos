package com.kwonps.mtouchpos.coordinator

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.kwonps.mtouchpos.view.navgraph.NavigationBundleKey
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState
import com.kwonps.mtouchpos.view.ui.navigate
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType

class DirectPaymentCoordinator(
    override val navController: NavController
) : CommonCoordinator(navController) {
    fun navigateToCompletePaymentView(completePaymentViewInfo: ApprovedPaymentType.CompletePaymentViewInfo) {
        navController.navigate(
            route = "${NavigationGraphState.CommonView.CompletePayment.name}${NavigationGraphState.DirectPaymentView.DirectPayment.name}",
            bundle = bundleOf(
                NavigationBundleKey.RESULT_DATA to completePaymentViewInfo,
                NavigationBundleKey.BEFORE_NAVGRAPH to navController.currentBackStackEntry!!.destination.route!!
            ),
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(
                NavigationGraphState.HomeView.Home.name, false).build()
        )
    }
}