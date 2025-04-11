package com.example.mtouchpos.view.navgraph

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.mtouchpos.view.navgraph.NavigationBundleKey.Companion.RESPONSE_GET_PAYMENT_LIST
import com.example.mtouchpos.view.sharedViewModel
import com.example.mtouchpos.view.ui.CalendarView
import com.example.mtouchpos.view.ui.CardReaderConnectDialog
import com.example.mtouchpos.view.ui.CardReaderSettingView
import com.example.mtouchpos.view.ui.DirectPaymentView
import com.example.mtouchpos.view.ui.MainView
import com.example.mtouchpos.view.ui.ManifestPermissionRequestView
import com.example.mtouchpos.view.ui.OfflinePaymentView
import com.example.mtouchpos.view.ui.PaymentHistoryDetailView
import com.example.mtouchpos.view.ui.PaymentHistoryView
import com.example.mtouchpos.view.ui.PaymentStatisticsView
import com.example.mtouchpos.view.ui.PgIdLoginDialog
import com.example.mtouchpos.view.ui.RegisteredIdDialog
import com.example.mtouchpos.view.ui.navigate
import com.example.mtouchpos.viewmodel.BluetoothCardReaderSettingVM
import com.example.mtouchpos.viewmodel.OfflinePaymentVM
import com.example.mtouchpos.viewmodel.PaymentHistoryVM
import com.example.mtouchpos.viewmodel.UsbCardReaderSettingVM
import java.time.format.DateTimeFormatter

class MainViewNavGraph(
    override val navController: NavController,
    override val navGraphBuilder: NavGraphBuilder
): CommonViewNavGraph(navController, navGraphBuilder) {
    fun mainViewGraph() {
        navGraphBuilder.composable(NavigationGraphState.HomeView.Home.name) {
            MainView(navController = navController)
        }
        navGraphBuilder.dialog(NavigationGraphState.HomeView.PgIdLogin.name) {
            PgIdLoginDialog(navController = navController)
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
            messageDialog()
            completePaymentPage(this)
            composable(NavigationGraphState.DirectPaymentView.DirectPayment.name) {
                DirectPaymentView(navController = navController)
            }
        }
    }

    fun deviceConnectGraph() {
        navGraphBuilder.navigation(
            startDestination = NavigationGraphState.DeviceSettingView.Bluetooth.name,
            route = "DeviceConnectGraph"
        ) {
            messageDialog()
            composable(NavigationGraphState.DeviceSettingView.Bluetooth.name) { backStackEntry ->
                CardReaderSettingView(navController, hiltViewModel<BluetoothCardReaderSettingVM>())
            }
            composable(NavigationGraphState.DeviceSettingView.USB.name) {
                CardReaderSettingView(navController, hiltViewModel<UsbCardReaderSettingVM>())
            }
            dialog(
                route = "connectScreen/{listData}",
                arguments = listOf(navArgument("listData") { type = NavType.StringType })
            ) { backStackEntry ->
                ManifestPermissionRequestView(
                    navController = navController,
                    permissionJson = backStackEntry.arguments?.getString("listData") ?: ""
                )
            }
            dialog(NavigationGraphState.DeviceSettingView.BluetoothConnectDialog.name) {
                CardReaderConnectDialog(
                    navController,
                    it.sharedViewModel<BluetoothCardReaderSettingVM>(navController)
                )
            }
            dialog(NavigationGraphState.DeviceSettingView.USBConnectDialog.name) {
                CardReaderConnectDialog(
                    navController,
                    it.sharedViewModel<UsbCardReaderSettingVM>(navController)
                )
            }
        }
    }

    fun paymentHistoryGraph() {
        navGraphBuilder.navigation(
            startDestination = NavigationGraphState.PaymentHistoryView.PaymentHistory.name,
            route = "paymentHistoryGraph"
        ) {
            completePaymentPage(this, NavigationGraphState.PaymentHistoryView.PaymentHistoryDetail.name)
            paymentProcessDialog(this, NavigationGraphState.PaymentHistoryView.PaymentHistoryDetail.name)
            messageDialog()
            composable(NavigationGraphState.PaymentHistoryView.PaymentHistory.name) {
                PaymentHistoryView(
                    navController = navController,
                    customPaymentPeriod = it.getSerializableArgument(NavigationBundleKey.SEARCH_PERIOD)
                )
            }
            composable(NavigationGraphState.PaymentHistoryView.PaymentHistoryDetail.name) {
                PaymentHistoryDetailView(
                    navController = navController,
                    offlinePaymentViewModel = it.sharedViewModel<OfflinePaymentVM>(navController),
                    paymentHistoryInfo = it.getSerializableArgument(RESPONSE_GET_PAYMENT_LIST)!!
                )
            }
            composable(NavigationGraphState.PaymentHistoryView.PaymentStatistic.name) {
                PaymentStatisticsView(
                    navController = navController,
                    customPaymentPeriod = it.getSerializableArgument(NavigationBundleKey.SEARCH_PERIOD)
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