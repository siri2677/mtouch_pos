package com.kwonps.mtouchpos.navigation

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState
import com.kwonps.mtouchpos.vo.type.UseCaseResult

class LoginRouter(private val navigator: NavigationHandler) {
    private fun navigateToHome() {
        navigator.navigate(
            route = NavigationGraphState.HomeView.Home.name,
            navOptions = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(NavigationGraphState.HomeView.Home.name, false)
                .build()
        )
    }

    @Composable
    fun observeResultLogin(
        context: Context,
        reactLogin: UseCaseResult<String>
    ) {
        navigator.run {
            reactLogin.handleResult {
                Toast.makeText(context, "로그인이 완료되었습니다", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
        }
    }
}

@Composable
fun rememberLoginRouter(navController: NavController): LoginRouter {
    val navigator = rememberNavigationHandler(navController)
    return remember(navigator) { LoginRouter(navigator) }
}
