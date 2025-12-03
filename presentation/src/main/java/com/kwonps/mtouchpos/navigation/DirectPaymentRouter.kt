package com.kwonps.mtouchpos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.kwonps.mtouchpos.view.navgraph.NavigationBundleKey
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.type.UseCaseResult

class DirectPaymentRouter(
    private val navigator: NavigationHandler
) {
    fun navigateToCompletePaymentView(completePaymentViewInfo: ApprovedPaymentType.CompletePaymentViewInfo) {
        navigator.navigate(
            route = "${NavigationGraphState.CommonView.CompletePayment.name}${NavigationGraphState.DirectPaymentView.DirectPayment.name}",
            bundle = bundleOf(
                NavigationBundleKey.RESULT_DATA to completePaymentViewInfo,
                NavigationBundleKey.BEFORE_NAVGRAPH to navigator.navController.currentBackStackEntry!!.destination.route!!
            ),
            navOptions = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(NavigationGraphState.HomeView.Home.name, false)
                .build()
        )
    }

    @Composable
    fun observeResultPaymentData(
        result: UseCaseResult<ApprovedPaymentType.CompletePaymentViewInfo>
    ) {
        navigator.run {
            result.handleResult {
                navigateToCompletePaymentView(it.data)
            }
        }
    }
}

@Composable
fun rememberDirectPaymentRouter(navController: NavController): DirectPaymentRouter {
    val navigator = rememberNavigationHandler(navController)
    return remember(navigator) { DirectPaymentRouter(navigator) }
}
