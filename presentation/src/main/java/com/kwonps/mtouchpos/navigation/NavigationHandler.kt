package com.kwonps.mtouchpos.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.kwonps.mtouchpos.view.navgraph.NavigationBundleKey
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState
import com.kwonps.mtouchpos.view.ui.navigate
import com.kwonps.mtouchpos.view.util.LoadingDialog
import com.kwonps.mtouchpos.view.util.SelectDialog
import com.kwonps.mtouchpos.vo.type.UseCaseResult

@Stable
class NavigationHandler(
    val navController: NavController
) {
    fun navigate(
        route: String,
        bundle: Bundle? = null,
        navOptions: NavOptions? = null
    ) {
        navController.navigate(route, bundle, navOptions)
    }

    fun showErrorDialog(message: String?) {
        navigate(
            route = NavigationGraphState.CommonView.MessageDialog.name,
            bundle = Bundle().apply {
                putParcelable(
                    NavigationBundleKey.MESSAGE,
                    SelectDialog(initValue = message)
                )
            },
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
        )
    }

    @Composable
    fun <T> UseCaseResult<T>.handleResult(
        onSuccess: (UseCaseResult.Success<T>) -> Unit = {}
    ) {
        LaunchedEffect(this) {
            when (this@handleResult) {
                is UseCaseResult.Success -> onSuccess(this@handleResult)
                is UseCaseResult.Error -> showErrorDialog(this@handleResult.message)
                is UseCaseResult.Exception -> showErrorDialog(this@handleResult.exception.message)
                else -> {}
            }
        }

        if (this is UseCaseResult.Loading) LoadingDialog(navController)
    }
}

@Composable
fun rememberNavigationHandler(navController: NavController): NavigationHandler {
    return remember(navController) { NavigationHandler(navController) }
}
