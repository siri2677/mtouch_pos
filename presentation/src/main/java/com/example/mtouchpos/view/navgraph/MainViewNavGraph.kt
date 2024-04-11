package com.example.mtouchpos.view.navgraph

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import com.example.mtouchpos.dto.PaymentPeriod
import com.example.mtouchpos.dto.ResponseFlowData
import com.example.mtouchpos.view.ui.BluetoothDevicePaymentDialog
import com.example.mtouchpos.view.ui.CreditPaymentView
import com.example.mtouchpos.view.ui.DeviceSettingView
import com.example.mtouchpos.view.ui.DirectPaymentView
import com.example.mtouchpos.view.ui.LoginDialog
import com.example.mtouchpos.view.ui.MainView
import com.example.mtouchpos.view.ui.PaymentHistoryView
import com.example.mtouchpos.view.ui.PgIdLoginDialog
import com.example.mtouchpos.view.ui.RegisteredIdDialog
import com.example.mtouchpos.view.ui.UsbDevicePaymentDialog
import com.example.mtouchpos.view.ui.VanIdLoginDialog
import com.example.mtouchpos.view.ui.navigate
import com.example.mtouchpos.view.ui.PaymentHistoryDetailView
import com.example.mtouchpos.view.ui.PaymentHistoryViewTab
import com.example.mtouchpos.view.ui.theme.NavigationBundleKey
import com.example.mtouchpos.viewmodel.PaymentHistoryViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.kizitonwose.calendar.sample.compose.CalendarView
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class MainViewNavGraph(
    private val commonViewNavGraph: CommonViewNavGraph
) {
    fun mainViewGraph() {
        commonViewNavGraph.run {
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
    }

    fun creditPaymentGraph() {
        commonViewNavGraph.run {
            navGraphBuilder.navigation(
                startDestination = NavigationGraphState.CreditPaymentView.CreditPayment.name,
                route = "creditPaymentGraph"
            ) {
                completePaymentPage()
                composable(NavigationGraphState.CreditPaymentView.CreditPayment.name) {
                    CreditPaymentView(navController = navController)
                }
            }
        }
    }

//    fun completePaymentGraph() {
//        commonViewNavGraph.run {
//            navGraphBuilder.navigation(
//                startDestination = NavigationGraphState.CommonView.CompletePayment.name,
//                route = "completePaymentGraph"
//            ) {
//                completePaymentPage()
//            }
//        }
//    }

    fun directPaymentGraph() {
        commonViewNavGraph.run {
            navGraphBuilder.navigation(
                startDestination = NavigationGraphState.DirectPaymentView.DirectPayment.name,
                route = "directPaymentGraph"
            ) {
                itemListDialog()
                errorDialog()
                completePaymentPage()
                composable(NavigationGraphState.DirectPaymentView.DirectPayment.name) {
                    DirectPaymentView(
                        navController = navController
                    )
                }
            }
        }
    }

    fun deviceConnectGraph() {
        commonViewNavGraph.run {
            navGraphBuilder.navigation(
                startDestination = NavigationGraphState.DeviceSettingView.Bluetooth.name,
                route = "DeviceConnectGraph"
            ) {
                composable(NavigationGraphState.DeviceSettingView.Bluetooth.name) { backStackEntry ->
                    DeviceSettingView().bluetoothDevice(
                        navHostController = navController
                    )
                }
                composable(NavigationGraphState.DeviceSettingView.USB.name) {
                    DeviceSettingView().usbDevice(
                        navHostController = navController
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    fun paymentHistoryGraph() {
        commonViewNavGraph.run {
            navGraphBuilder.navigation(
                startDestination = NavigationGraphState.PaymentHistoryView.PaymentHistory.name,
                route = "paymentHistoryGraph"
            ) {
                loadingDialog()
                completePaymentPage()
                composable(NavigationGraphState.PaymentHistoryView.PaymentHistory.name) { backStackEntry ->
                    fun getDay(days: Long): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LocalDateTime.now().minusDays(days).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    } else {
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_YEAR, -days.toInt())
                        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.time)
                    }
                    val pagerState = rememberPagerState()
                    val paymentHistoryViewModel: PaymentHistoryViewModel = hiltViewModel()
                    val pages = PaymentHistoryViewTab.values()
                    val paymentPeriod= when (pages[pagerState.currentPage]) {
                        PaymentHistoryViewTab.Today -> PaymentPeriod(getDay(0), getDay(0))
                        PaymentHistoryViewTab.Yesterday -> PaymentPeriod(getDay(1), getDay(0))
                        PaymentHistoryViewTab.Week -> PaymentPeriod(getDay(7), getDay(0))
                        PaymentHistoryViewTab.Custom -> backStackEntry.getSerializableArgument(
                            NavigationBundleKey.searchPeriod
                        )?: PaymentPeriod("","")
                    }

                    paymentHistoryViewModel.also {
                        val isDiffBeforeSearchPeriod = it.paymentPeriod != paymentPeriod

                        if(isDiffBeforeSearchPeriod) {
                            it.getPaymentList(paymentPeriod)
                            navController.navigate(
                                route = NavigationGraphState.CommonView.LoadingDialog.name,
                                bundle = bundleOf(
                                    NavigationBundleKey.responseTmsAPI to it
                                ),
                                navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(
                                    NavigationGraphState.PaymentHistoryView.PaymentHistory.name, inclusive = false, saveState = false).build()
                            )
                        }

                        PaymentHistoryView(
                            responseFlowData = if(isDiffBeforeSearchPeriod) ResponseFlowData.Init else backStackEntry.getSerializableArgument(
                                NavigationBundleKey.responseTmsAPI
                            )?: ResponseFlowData.Init,
                            navController = navController,
                            paymentPeriod = paymentPeriod,
                            pagerState = pagerState,
                            pages = pages
                        )
                    }
                }
                composable(NavigationGraphState.PaymentHistoryView.PaymentHistoryDetail.name) { backStackEntry ->
                    PaymentHistoryDetailView(
                        navController = navController,
                        creditPayment = backStackEntry.getSerializableArgument("responseGetPaymentListBody")!!
                    )
                }
                composable(NavigationGraphState.PaymentHistoryView.Calendar.name) {
                    CalendarView(
                        close = { navController.popBackStack() },
                        dateSelected = { startDate, endDate ->
                            navController?.navigate(
                                NavigationGraphState.PaymentHistoryView.PaymentHistory.name,
                                bundleOf(
                                    NavigationBundleKey.searchPeriod to (
                                        PaymentPeriod(
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
                dialog(NavigationGraphState.CreditPaymentView.BluetoothPaymentDialog.name) { backStackEntry ->
                    BluetoothDevicePaymentDialog(
                        navController = navController,
                        deviceSerialCommunicate = backStackEntry.getSerializableArgument(
                            NavigationBundleKey.ItemList
                        )!!
                    )
                }
                dialog(NavigationGraphState.CreditPaymentView.UsbPaymentDialog.name) { backStackEntry ->
                    UsbDevicePaymentDialog(
                        navController = navController,
                        deviceSerialCommunicate = backStackEntry.getSerializableArgument(
                            NavigationBundleKey.ItemList
                        )!!
                    )
                }
            }
        }
    }
}