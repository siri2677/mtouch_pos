package com.kwonps.mtouchpos.view

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
import com.kwonps.mtouchpos.coordinator.OfflinePaymentCoordinator
import com.kwonps.mtouchpos.view.ExternalActivity.Action
import com.kwonps.mtouchpos.view.ExternalActivity.OfflinePaymentAction
import com.kwonps.mtouchpos.view.navgraph.CommonViewNavGraph
import com.kwonps.mtouchpos.view.ui.theme.MtouchPos
import com.kwonps.mtouchpos.viewmodel.LoginVM
import com.kwonps.mtouchpos.viewmodel.OfflinePaymentVM
import com.kwonps.mtouchpos.viewmodel.OfflinePaymentVM.OfflinePaymentInfo.*
import com.kwonps.mtouchpos.viewmodel.PaymentHistoryVM
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType.PaymentHistoryViewInfo
import com.kwonps.mtouchpos.vo.info.UserInfo
import com.kwonps.mtouchpos.vo.type.UseCaseResult
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Type

@AndroidEntryPoint
class ExternalActivity : ComponentActivity() {
    enum class OfflinePaymentAction(val value: String) {
        PAYMENT("payment"),
        CANCEL_PAYMENT("cancelPayment"),
        VAN_PAYMENT("vanPayment"),
        VAN_CANCEL_PAYMENT("vanCancelPayment");
    }

    enum class Action(val value: String) {
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

        val action = intent.data?.getQueryParameter("action")
        val merchantUrl = intent.data?.getQueryParameter("callbackAppUrl")
        val isOfflinePaymentAction = OfflinePaymentAction.entries.toTypedArray().none { it.value == action }
        val isAction = Action.entries.toTypedArray().none { it.value == action }

        if (!isOfflinePaymentAction && !isAction) {
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
                            NavigateToActionView(
                                navController = navController,
                                intent = intent,
                                merchantUrl = merchantUrl!!,
                                action = action!!
                            )
                        }

                        OfflinePaymentAction.entries.toTypedArray().forEach { action ->
                            navigation(
                                startDestination = action.value,
                                route = "${action.value}graph"
                            ) {
                                composable(action.value) {
                                    PaymentView(
                                        intent = intent,
                                        navController = navController,
                                        route = action,
                                        offlinePaymentViewModel = it.sharedViewModel<OfflinePaymentVM>(navController),
                                    )
                                }
                                CommonViewNavGraph(navController, this).paymentProcessDialog(this, action.value)
                            }
                        }

                        composable(Action.STATISTICS.value) {
                            StatisticsView(
                                intent = intent,
                                merchantUrl = merchantUrl!!,
                                paymentHistoryVM = it.sharedViewModel<PaymentHistoryVM>(navController),
                            )
                        }

                        composable(Action.LIST.value) {
                            ListView(
                                intent = intent,
                                merchantUrl = merchantUrl!!,
                                paymentHistoryVM = it.sharedViewModel<PaymentHistoryVM>(navController),
                            )
                        }
//                        composable(Action.REFUND.value) { PaymentScreen() }
//                        composable(Action.DIRECT_PAYMENT.value) { ProfileScreen() }
//                        composable(Action.DIRECT_REFUND.value) { PaymentScreen() }
//                        composable(Action.CASH_PAYMENT.value) { ProfileScreen() }
//                        composable(Action.CASH_REFUND.value) { PaymentScreen() }
//                        composable(Action.CASH_INFO.value) { ProfileScreen() }
//                        composable(Action.PRINT_RECEIPT.value) { PaymentScreen() }
//                        composable(Action.CHECK_INSTALLMENT.value) { ProfileScreen() }
//                        composable(NavigationGraphState.CommonView.ItemListDialog.name) { HomeScreen() }
                        with(CommonViewNavGraph(navController, this)){
                            itemListDialog()
                            messageDialog()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavigateToActionView(
    navController: NavController,
    intent: Intent,
    merchantUrl: String,
    action: String,
    loginViewModel: LoginVM = hiltViewModel()
) {
    if(action == OfflinePaymentAction.VAN_PAYMENT.value || action == OfflinePaymentAction.VAN_CANCEL_PAYMENT.value) {
        navController.navigate(action)
    } else {
        val context = LocalContext.current as ComponentActivity
        val reactLogin = loginViewModel.reactLogin.collectAsStateWithLifecycle(UseCaseResult.Init).value

        LaunchedEffect(intent) {
            with(loginViewModel) {
                UserInfo(
                    tmnId = intent.data?.getQueryParameter("tmnId") ?: "",
                    serial = intent.data?.getQueryParameter("serial") ?: "",
                    mchtId = intent.data?.getQueryParameter("mchtId") ?: ""
                ).let { updateUserInfo(it) }
                login()
            }
        }

        LaunchedEffect(reactLogin) {
            when(reactLogin) {
                is UseCaseResult.Success -> navController.navigate(action)

                is UseCaseResult.Error -> configureIntent(
                    isSuccess = false,
                    componentActivity = context,
                    message = reactLogin.message,
                    merchantUrl = merchantUrl
                )

                is UseCaseResult.Exception -> configureIntent(
                    isSuccess = false,
                    componentActivity = context,
                    message = reactLogin.exception.message.toString(),
                    merchantUrl = merchantUrl
                )

                else -> {}
            }
        }
    }
}

@Composable
fun PaymentView(
    intent: Intent,
    route: OfflinePaymentAction,
    navController: NavController,
    offlinePaymentViewModel: OfflinePaymentVM
) {
    val context = LocalContext.current as ComponentActivity
    val offlinePaymentCoordinator = OfflinePaymentCoordinator(
        navController = navController,
        componentActivity = context,
        offlinePaymentViewModel = offlinePaymentViewModel,
        route = route.value
    )

    offlinePaymentCoordinator.CardTerminalNewIntent()

    LaunchedEffect(Unit) {
        val offlinePaymentInfo = when(route) {
            OfflinePaymentAction.PAYMENT, OfflinePaymentAction.VAN_PAYMENT -> {
                Approve(
                    installment = intent.data?.getQueryParameter("installment") ?: "",
                    totalAmount = intent.data?.getQueryParameter("totalAmount") ?: "",
                    trackId = intent.data?.getQueryParameter("trackId") ?: "",
                    freeAmount = intent.data?.getQueryParameter("freeAmount")?.toInt() ?: 0,
                    serviceAmount = intent.data?.getQueryParameter("serviceAmount")?.toInt() ?: 0,
                    merchantUrl = intent.data?.getQueryParameter("callbackAppUrl"),
                    dptId = if(route == OfflinePaymentAction.VAN_PAYMENT) intent.data?.getQueryParameter("dptId") else null
                )
            }

            OfflinePaymentAction.CANCEL_PAYMENT, OfflinePaymentAction.VAN_CANCEL_PAYMENT -> {
                Cancel(
                    installment = intent.data?.getQueryParameter("installment") ?: "",
                    totalAmount = intent.data?.getQueryParameter("totalAmount") ?: "0",
                    trackId = intent.data?.getQueryParameter("trackId") ?: "",
                    rootTrxId = intent.data?.getQueryParameter("rootTrxId") ?: "",
                    authCode = intent.data?.getQueryParameter("authCode") ?: "",
                    authDate = intent.data?.getQueryParameter("authDate") ?: "",
                    freeAmount = intent.data?.getQueryParameter("freeAmount")?.toInt() ?: 0,
                    serviceAmount = intent.data?.getQueryParameter("serviceAmount")?.toInt() ?: 0,
                    merchantUrl = intent.data?.getQueryParameter("callbackAppUrl"),
                    dptId = if(route == OfflinePaymentAction.VAN_CANCEL_PAYMENT) intent.data?.getQueryParameter("dptId") else null
                )
            }
        }

        offlinePaymentViewModel.updateOfflinePaymentInfo(offlinePaymentInfo)
        offlinePaymentCoordinator.navigateToDeviceDialog()
    }
}

@Composable
fun StatisticsView(
    intent: Intent,
    merchantUrl: String,
    paymentHistoryVM: PaymentHistoryVM
) {
    val context = LocalContext.current as ComponentActivity
    val paymentStatisticInfo = paymentHistoryVM.paymentStatisticInfo.collectAsStateWithLifecycle(UseCaseResult.Init).value
    val periodInfo = PaymentHistoryVM.PeriodInfo(
        startDay = intent.data?.getQueryParameter("startDay") ?: "",
        endDay = intent.data?.getQueryParameter("endDay") ?: ""
    )

    LaunchedEffect(Unit) {
        paymentHistoryVM.updatePeriodInfoAndFetchPaymentList(periodInfo)
        paymentHistoryVM.fetchPaymentStatistic()
    }

    LaunchedEffect(paymentStatisticInfo) {
        when(paymentStatisticInfo) {
            is UseCaseResult.Success -> {
                fun createCustomJson(
                    data: HashMap<PaymentHistoryVM.StatisticType, PaymentHistoryVM.PaymentStatisticInfo>
                ) = JsonObject().apply {
                    data.forEach { (key, value) ->
                        when (key) {
                            PaymentHistoryVM.StatisticType.APPROVE -> {
                                addProperty("payAmt", value.amount)
                                addProperty("count", value.count)
                            }
                            PaymentHistoryVM.StatisticType.CANCEL -> {
                                addProperty("rfdAmt", value.amount)
                                addProperty("count", value.count)
                            }
                            PaymentHistoryVM.StatisticType.TOTAL -> {
                                addProperty("totalAmount", value.amount)
                                addProperty("totalCount", value.count)
                            }
                        }
                        addProperty("result", "조회성공")
                        addProperty("startDay", periodInfo.startDay)
                        addProperty("endDay", periodInfo.endDay)
                    }
                }.toString()

                configureIntent(
                    isSuccess = true,
                    componentActivity = context,
                    message = "성공",
                    merchantUrl = merchantUrl,
                    contents = "&resultData=${createCustomJson(paymentStatisticInfo.value)}"
                )
            }

            is UseCaseResult.Error -> configureIntent(
                isSuccess = false,
                componentActivity = context,
                message = paymentStatisticInfo.message,
                merchantUrl = merchantUrl
            )

            is UseCaseResult.Exception -> configureIntent(
                isSuccess = false,
                componentActivity = context,
                message = paymentStatisticInfo.exception.message.toString(),
                merchantUrl = merchantUrl
            )

            else -> {}
        }
    }
}

@Composable
fun ListView(
    intent: Intent,
    merchantUrl: String,
    paymentHistoryVM: PaymentHistoryVM
) {
    val context = LocalContext.current as ComponentActivity
    val paymentHistoryInfo = paymentHistoryVM.paymentHistoryInfo.collectAsStateWithLifecycle(UseCaseResult.Init).value

    LaunchedEffect(Unit) {
        val periodInfo = PaymentHistoryVM.PeriodInfo(
            startDay = intent.data?.getQueryParameter("startDay") ?: "",
            endDay = intent.data?.getQueryParameter("endDay") ?: ""
        )
        paymentHistoryVM.updatePeriodInfoAndFetchPaymentList(periodInfo)
        paymentHistoryVM.fetchPaymentList()
    }

    LaunchedEffect(paymentHistoryInfo) {
        when(paymentHistoryInfo) {
            is UseCaseResult.Success -> {
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                val paymentHistoryViewInfoSerializer = object: JsonSerializer<PaymentHistoryViewInfo> {
                    override fun serialize(
                        src: PaymentHistoryViewInfo,
                        typeOfSrc: Type,
                        context: JsonSerializationContext
                    ): JsonElement? = gson.toJsonTree(src).asJsonObject.apply {
                        addProperty("trxResult", src.purchaseType.description)
                    }
                }

                val json = GsonBuilder().apply {
                    registerTypeAdapter(PaymentHistoryViewInfo::class.java, paymentHistoryViewInfoSerializer)
                }.create().toJson(paymentHistoryInfo.value)

                configureIntent(
                    isSuccess = true,
                    componentActivity = context,
                    message = "성공",
                    merchantUrl = merchantUrl,
                    contents = "&resultData=$json"
                )
            }

            is UseCaseResult.Error -> configureIntent(
                isSuccess = false,
                componentActivity = context,
                message = paymentHistoryInfo.message,
                merchantUrl = merchantUrl
            )

            is UseCaseResult.Exception -> configureIntent(
                isSuccess = false,
                componentActivity = context,
                message = paymentHistoryInfo.exception.message.toString(),
                merchantUrl = merchantUrl
            )

            else -> {}
        }
    }
}

fun configureIntent(
    isSuccess: Boolean,
    componentActivity: ComponentActivity,
    message: String,
    merchantUrl: String,
    contents: String? = null,
) {
    val queryParameter = StringBuilder().apply {
        append(merchantUrl)
        append("?isSuccess=$isSuccess")
        append("&resultMsg=$message")
        contents?.let { append(it) }
    }.toString()
    componentActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(queryParameter)))
    componentActivity.finish()
}