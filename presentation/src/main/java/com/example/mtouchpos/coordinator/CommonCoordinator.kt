package com.example.mtouchpos.coordinator

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.mtouchpos.view.navgraph.NavigationBundleKey
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.navigate
import com.example.mtouchpos.view.util.LoadingDialog
import com.example.mtouchpos.vo.data.ApprovedPaymentType
import com.example.mtouchpos.vo.type.UseCaseResult

abstract class CommonCoordinator(open val navController: NavController) {
    @Composable
    fun <T> UseCaseResult<T>.navigateForUseCaseResult(
        successProcess: (UseCaseResult.Success<T>) -> Unit = {}
    ) {
        LaunchedEffect(this) {
            when(this@navigateForUseCaseResult) {
                is UseCaseResult.Success -> successProcess(this@navigateForUseCaseResult)
                is UseCaseResult.Error -> navigateToErrorDialog(navController)
                is UseCaseResult.Exception -> navigateToErrorDialog(navController)
                else -> {}
            }
        }
        if(this is UseCaseResult.Loading) LoadingDialog(navController)
    }

    fun UseCaseResult.Error.navigateToErrorDialog(navController: NavController) {
        navController.navigate(
            route = NavigationGraphState.CommonView.ErrorDialog.name,
            bundle = bundleOf(NavigationBundleKey.MESSAGE to this.message),
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
        )
    }

    fun UseCaseResult.Exception.navigateToErrorDialog(navController: NavController) {
        navController.navigate(
            route = NavigationGraphState.CommonView.ErrorDialog.name,
            bundle = bundleOf(NavigationBundleKey.MESSAGE to this.exception.message),
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
        )
    }

    fun ApprovedPaymentType.CompletePaymentViewInfo.navigateToCompletePaymentView(navController: NavController) {
        navController.navigate(
            route = NavigationGraphState.CommonView.CompletePayment.name,
            bundle = bundleOf(
                NavigationBundleKey.RESULT_DATA to this,
                NavigationBundleKey.BEFORE_NAVGRAPH to navController.currentBackStackEntry!!.destination.route!!
            ),
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(
                NavigationGraphState.HomeView.Home.name, false).build()
        )
    }
}