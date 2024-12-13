package com.example.mtouchpos.view.navgraph

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import com.example.mtouchpos.view.navgraph.NavigationBundleKey.Companion.RESPONSE_GET_PAYMENT_LIST
import com.example.mtouchpos.view.sharedViewModel
import com.example.mtouchpos.view.ui.CalendarView
import com.example.mtouchpos.view.ui.OfflinePaymentView
import com.example.mtouchpos.view.ui.DirectPaymentView
import com.example.mtouchpos.view.ui.LoginDialog
import com.example.mtouchpos.view.ui.MainView
import com.example.mtouchpos.view.ui.PaymentHistoryDetailView
import com.example.mtouchpos.view.ui.PaymentHistoryView
import com.example.mtouchpos.view.ui.PgIdLoginDialog
import com.example.mtouchpos.view.ui.RegisteredIdDialog
import com.example.mtouchpos.view.ui.VanIdLoginDialog
import com.example.mtouchpos.view.ui.BluetoothDevice
import com.example.mtouchpos.view.ui.navigate
import com.example.mtouchpos.view.ui.UsbDevice
import com.example.mtouchpos.viewmodel.OfflinePaymentVM
import com.example.mtouchpos.viewmodel.PaymentHistoryVM
import java.time.format.DateTimeFormatter

class MainViewNavGraph(
    override val navController: NavController,
    override val navGraphBuilder: NavGraphBuilder
): CommonViewNavGraph(navController, navGraphBuilder) {
    fun mainViewGraph() {
        navGraphBuilder.composable(NavigationGraphState.HomeView.Home.name) {
            MainView(navController = navController)
        }
        navGraphBuilder.dialog(NavigationGraphState.HomeView.Login.name) {
            LoginDialog(navController = navController)
        }
        navGraphBuilder.dialog(NavigationGraphState.HomeView.PgIdLogin.name) {
            PgIdLoginDialog(navController = navController)
        }
        navGraphBuilder.dialog(NavigationGraphState.HomeView.VanIdLogin.name) {
            VanIdLoginDialog(navController = navController)
        }
        navGraphBuilder.dialog(NavigationGraphState.HomeView.RegisteredId.name) {
            RegisteredIdDialog(navController = navController)
        }
    }

    fun creditPaymentGraph() {
        navGraphBuilder.navigation(
            startDestination = NavigationGraphState.CreditPaymentView.CreditPayment.name,
            route = "creditPaymentGraph"
        ) {
            completePaymentPage(this, NavigationGraphState.CreditPaymentView.CreditPayment.name)
            paymentProcessDialog(this, NavigationGraphState.CreditPaymentView.CreditPayment.name)
            composable(NavigationGraphState.CreditPaymentView.CreditPayment.name) {
                OfflinePaymentView(
                    navController = navController,
                    offlinePaymentViewModel = it.sharedViewModel<OfflinePaymentVM>(navController)
                )
            }
        }
    }

    fun directPaymentGraph() {
        navGraphBuilder.navigation(
            startDestination = NavigationGraphState.DirectPaymentView.DirectPayment.name,
            route = "directPaymentGraph"
        ) {
            itemListDialog()
            errorDialog()
            completePaymentPage(this)
            composable(NavigationGraphState.DirectPaymentView.DirectPayment.name) {
                DirectPaymentView(
                    navController = navController,

                )
            }
        }
    }

    fun deviceConnectGraph() {
        navGraphBuilder.navigation(
            startDestination = NavigationGraphState.DeviceSettingView.Bluetooth.name,
            route = "DeviceConnectGraph"
        ) {
            composable(NavigationGraphState.DeviceSettingView.Bluetooth.name) { backStackEntry ->
                BluetoothDevice(navHostController = navController)
            }
            composable(NavigationGraphState.DeviceSettingView.USB.name) {
                UsbDevice(navHostController = navController)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun paymentHistoryGraph() {
        navGraphBuilder.navigation(
            startDestination = NavigationGraphState.PaymentHistoryView.PaymentHistory.name,
            route = "paymentHistoryGraph"
        ) {
            completePaymentPage(this, NavigationGraphState.PaymentHistoryView.PaymentHistory.name)
            paymentProcessDialog(this, NavigationGraphState.PaymentHistoryView.PaymentHistory.name)
            errorDialog()
            composable(NavigationGraphState.PaymentHistoryView.PaymentHistory.name) { backStackEntry ->
                PaymentHistoryView(
                    navController = navController,
                    customPaymentPeriod = backStackEntry.getSerializableArgument(NavigationBundleKey.SEARCH_PERIOD)
                )
            }
            composable(NavigationGraphState.PaymentHistoryView.PaymentHistoryDetail.name) {
                PaymentHistoryDetailView(
                    navController = navController,
                    offlinePaymentViewModel = it.sharedViewModel<OfflinePaymentVM>(navController),
                    paymentHistoryInfo = it.getSerializableArgument(RESPONSE_GET_PAYMENT_LIST)!!
                )
            }
            composable(NavigationGraphState.PaymentHistoryView.Calendar.name) {
                CalendarView(
                    close = { navController.popBackStack() },
                    dateSelected = { startDate, endDate ->
                        navController.navigate(
                            NavigationGraphState.PaymentHistoryView.PaymentHistory.name,
                            bundleOf(
                                NavigationBundleKey.SEARCH_PERIOD to (
                                        PaymentHistoryVM.PeriodInfo(
                                            startDay = startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                                            endDay = endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                        )
                                )
                            ),
                            NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(
                                NavigationGraphState.PaymentHistoryView.Calendar.name, true).build()
                        )
                    }
                )
            }
        }
    }
}