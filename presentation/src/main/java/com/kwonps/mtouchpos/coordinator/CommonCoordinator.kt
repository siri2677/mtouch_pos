package com.kwonps.mtouchpos.coordinator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.kwonps.mtouchpos.view.navgraph.NavigationBundleKey
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState
import com.kwonps.mtouchpos.view.ui.navigate
import com.kwonps.mtouchpos.view.util.LoadingDialog
import com.kwonps.mtouchpos.view.util.SelectDialog
import com.kwonps.mtouchpos.vo.type.UseCaseResult

abstract class CommonCoordinator(open val navController: NavController) {
    @Composable
    fun <T> UseCaseResult<T>.NavigateForUseCaseResult(
        successProcess: (UseCaseResult.Success<T>) -> Unit = {}
    ) {
        LaunchedEffect(this) {
            when(this@NavigateForUseCaseResult) {
                is UseCaseResult.Success -> successProcess(this@NavigateForUseCaseResult)
                is UseCaseResult.Error -> navigateToErrorDialog(navController)
                is UseCaseResult.Exception -> navigateToErrorDialog(navController)
                else -> {}
            }
        }
        if(this is UseCaseResult.Loading) LoadingDialog(navController)
    }

    fun UseCaseResult.Error.navigateToErrorDialog(navController: NavController) {
        navController.navigate(
            route = NavigationGraphState.CommonView.MessageDialog.name,
            bundle = bundleOf(NavigationBundleKey.MESSAGE to SelectDialog(initValue = this.message)),
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
        )
    }

    fun UseCaseResult.Exception.navigateToErrorDialog(navController: NavController) {
        navController.navigate(
            route = NavigationGraphState.CommonView.MessageDialog.name,
            bundle = bundleOf(NavigationBundleKey.MESSAGE to this.exception.message),
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
        )
    }
}