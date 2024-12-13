package com.example.mtouchpos.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.mtouchpos.coordinator.OfflinePaymentCoordinator
import com.example.mtouchpos.view.ExternalActivity.Action
import com.example.mtouchpos.view.navgraph.CommonViewNavGraph
import com.example.mtouchpos.view.ui.theme.MtouchPos
import com.example.mtouchpos.viewmodel.LoginVM
import com.example.mtouchpos.viewmodel.OfflinePaymentVM
import com.example.mtouchpos.vo.type.UseCaseResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExternalActivity : ComponentActivity() {

    enum class Action(val value: String) {
        PAYMENT("payment"),
        REFUND("refund"),
        DIRECT_PAYMENT("directPayment"),
        DIRECT_REFUND("directRefund"),
        CASH_PAYMENT("cashPayment"),
        CASH_REFUND("cashRefund"),
        CASH_INFO("cashInfo"),
        PRINT_RECEIPT("printReceipt"),
        CHECK_INSTALLMENT("checkInstallment"),
        STATISTICS("statistics"),
        LIST("list");
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.data?.getQueryParameter("action")
        val merchantUrl = intent.data?.getQueryParameter("callbackAppUrl")!!

        if (Action.values().none { it.value == type }) {
            val queryParameter = StringBuilder().apply {
                append(merchantUrl)
                append("?isSuccess=false")
                append("&resultMsg=Invalid action")
            }.toString()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(queryParameter)))
            finish()
            return
        }

        setContent {
            MtouchPos {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            PaymentView(
                                navController = navController,
                                action = type!!
                            )
                        }

                        navigation(
                            startDestination = Action.PAYMENT.value,
                            route = "ActionPayment"
                        ) {
                            composable(Action.PAYMENT.value) {
                                PaymentView(
                                    intent = intent,
                                    navController = navController,
                                    merchantUrl = merchantUrl,
                                    route = Action.PAYMENT.value,
                                    offlinePaymentViewModel = it.sharedViewModel<OfflinePaymentVM>(navController),
                                )
                            }
                            CommonViewNavGraph(navController, this).paymentProcessDialog(this, Action.PAYMENT.value)
                        }

//                        composable(Action.REFUND.value) { PaymentScreen() }
//                        composable(Action.DIRECT_PAYMENT.value) { ProfileScreen() }
//                        composable(Action.DIRECT_REFUND.value) { PaymentScreen() }
//                        composable(Action.CASH_PAYMENT.value) { ProfileScreen() }
//                        composable(Action.CASH_REFUND.value) { PaymentScreen() }
//                        composable(Action.CASH_INFO.value) { ProfileScreen() }
//                        composable(Action.PRINT_RECEIPT.value) { PaymentScreen() }
//                        composable(Action.CHECK_INSTALLMENT.value) { ProfileScreen() }
//                        composable(Action.STATISTICS.value) { PaymentScreen() }
//                        composable(Action.LIST.value) { ProfileScreen() }
//                        composable(NavigationGraphState.CommonView.ItemListDialog.name) { HomeScreen() }
                        with(CommonViewNavGraph(navController, this)){
                            itemListDialog()
                            errorDialog()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentView(
    navController: NavController,
    action: String
) {
   when(action) {
       Action.PAYMENT.value -> navController.navigate(Action.PAYMENT.value)
       Action.REFUND.value -> TODO()
       Action.DIRECT_PAYMENT.value -> TODO()
       Action.DIRECT_REFUND.value -> TODO()
       Action.CASH_PAYMENT.value -> TODO()
       Action.CASH_REFUND.value -> TODO()
       Action.CASH_INFO.value -> TODO()
       Action.PRINT_RECEIPT.value -> TODO()
       Action.CHECK_INSTALLMENT.value -> TODO()
       Action.STATISTICS.value -> TODO()
       Action.LIST.value -> TODO()
   }
}

@Composable
fun PaymentView(
    intent: Intent,
    route: String,
    navController: NavController,
    merchantUrl: String,
    loginViewModel: LoginVM = hiltViewModel(),
    offlinePaymentViewModel: OfflinePaymentVM
) {
    val context = LocalContext.current as ComponentActivity
    val offlinePaymentCoordinator = OfflinePaymentCoordinator(
        navController = navController,
        componentActivity = context,
        offlinePaymentViewModel = offlinePaymentViewModel,
        route = route
    )

    LaunchedEffect(intent) {
        with(loginViewModel) {
            LoginVM.UserInfo(
                tmnId = intent.data?.getQueryParameter("tmnId") ?: "",
                serial = intent.data?.getQueryParameter("serial") ?: "",
                mchtId = intent.data?.getQueryParameter("mchtId") ?: ""
            ).let { updateUserInfo(it) }
            login()
        }
    }

    with(offlinePaymentCoordinator) {
        ObserveResultLogin(
            intent = intent,
            reactLogin = loginViewModel.reactLogin.collectAsStateWithLifecycle(UseCaseResult.Init).value,
            merchantUrl = merchantUrl
        )

        CardTerminalNewIntent()
    }
}

//class MainActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val type = intent.getStringExtra("type") ?: "default"
//
//        setContent {
//            val navController = rememberNavController()
//            NavHost(navController, startDestination = determineStartDestination(type)) {
//                composable("home") { HomeScreen() }
//                composable("payment") { PaymentScreen() }
//                composable("profile") { ProfileScreen() }
//                // 추가 경로 및 기능에 대한 Composable 구성
//            }
//        }
//    }
//
//    private fun determineStartDestination(type: String): String {
//        return when (type) {
//            "payment" -> "payment"
//            "profile" -> "profile"
//            else -> "home"
//        }
//    }
//}

//setContent {
//    MtouchPos {
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//            color = MaterialTheme.colorScheme.background
//        ) {
//            val resultProcess: (Int, Intent) -> Unit = { int, intent ->
//                setResult(ComponentActivity.RESULT_OK, intent)
//                finish()
//            }
//            val navController = rememberNavController()
//
//            NavHost(
//                navController = navController,
//                startDestination = NavigationGraphState.HomeView.Home.name
//            ) {
//                with(MainViewNavGraph(navController, this)) {
//                    creditPaymentGraph()
//                }
//            }
//        }
//    }
//}