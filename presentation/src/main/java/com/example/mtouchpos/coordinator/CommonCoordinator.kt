package com.example.mtouchpos.coordinator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.mtouchpos.view.navgraph.NavigationBundleKey
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.navigate
import com.example.mtouchpos.view.util.LoadingDialog
import com.example.mtouchpos.vo.type.UseCaseResult

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
}