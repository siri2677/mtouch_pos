package com.example.mtouchpos.coordinator

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.vo.type.UseCaseResult

class LoginCoordinator(override val navController: NavController): CommonCoordinator(navController) {
    private fun navigateToHome() {
        navController.navigate(
            route = NavigationGraphState.HomeView.Home.name,
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(
                NavigationGraphState.HomeView.Home.name, false).build()
        )
    }

    @Composable
    fun ObserveResultLogin(
        context: Context,
        reactLogin: UseCaseResult<String>
    ) {
        reactLogin.NavigateForUseCaseResult {
            Toast.makeText(context, "로그인이 완료되었습니다", Toast.LENGTH_SHORT).show()
            navigateToHome()
        }
    }
}