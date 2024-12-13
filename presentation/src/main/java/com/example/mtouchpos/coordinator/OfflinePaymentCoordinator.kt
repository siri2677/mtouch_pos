package com.example.mtouchpos.coordinator

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.core.os.bundleOf
import androidx.core.util.Consumer
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.domain.manager.cardterminal.CardTerminalCommunicateManager
import com.example.mtouchpos.view.navgraph.NavigationBundleKey
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.navigate
import com.example.mtouchpos.view.util.ErrorDialogContent
import com.example.mtouchpos.view.util.LoadingDialog
import com.example.mtouchpos.view.util.LoadingDialogContent
import com.example.mtouchpos.viewmodel.OfflinePaymentVM
import com.example.mtouchpos.vo.info.ApprovedPaymentType
import com.example.mtouchpos.vo.type.UseCaseResult
import java.io.Serializable
import java.net.URLDecoder

class OfflinePaymentCoordinator(
    override val navController: NavController,
    private val componentActivity: ComponentActivity,
    val offlinePaymentViewModel: OfflinePaymentVM,
    val route: String
): CommonCoordinator(navController), Serializable {
    fun configureIntent(
        isSuccess: Boolean,
        message: String,
        merchantUrl: String,
        paymentProcessState: OfflinePaymentVM.PaymentProcessState.CompletePayment? = null
    ) {
        val queryParameter = StringBuilder().apply {
            append(merchantUrl)
            append("?isSuccess=$isSuccess")
            append("&resultMsg=$message")
            paymentProcessState?.let {
                append("&purchaseType=${it.data.purchaseType.code}")
                append("&amount=${it.data.amount}")
                append("&installment=${it.data.installment}")
                append("&trackId=${it.data.trackId}")
                append("&authDate=${it.data.authDate}")
                append("&authCode=${it.data.authCode}")
                append("&trxId=${it.data.trxId}")
                append("&cardNumber=${it.data.cardNumber}")
            }
        }.toString()
        componentActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(queryParameter)))
        componentActivity.finish()
    }

    fun navigateToDeviceDialog() {
        navController.navigate(
            route = "${NavigationGraphState.CreditPaymentView.PaymentProcessDialog.name}$route",
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(NavigationGraphState.CreditPaymentView.CreditPayment.name, false).build()
        )
    }

    private fun navigateToErrorDialog(message: String) {
        navController.navigate(
            route = NavigationGraphState.CommonView.ErrorDialog.name,
            bundle = bundleOf(NavigationBundleKey.MESSAGE to message),
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(NavigationGraphState.CreditPaymentView.CreditPayment.name, false).build()
        )
    }

    private fun ApprovedPaymentType.CompletePaymentViewInfo.navigateToCompletePaymentView() {
        navController.navigate(
            route = "${NavigationGraphState.CommonView.CompletePayment.name}$route",
            bundle = bundleOf(
                NavigationBundleKey.RESULT_DATA to this,
                NavigationBundleKey.BEFORE_NAVGRAPH to navController.currentBackStackEntry!!.destination.route!!
            ),
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(
                NavigationGraphState.HomeView.Home.name, false).build()
        )
    }

    @Composable
    fun CardTerminalActivityResult() = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when(result.data?.data?.getQueryParameter("response_msg")) {
            "PAYMENT_CANCEL" -> {
                val message = "결제가 취소 되었습니다."
                offlinePaymentViewModel.offlinePaymentInfo.value.merchantUrl?.let { merchantUrl ->
                    configureIntent(
                        isSuccess = false,
                        message = message,
                        merchantUrl = merchantUrl
                    )
                } ?: navigateToErrorDialog(message)
            }

            else -> {
                offlinePaymentViewModel.pushOfflinePayment(
                    amount = null,
                    installment = result.data?.data?.getQueryParameter("installment")!!,
                    authCode = result.data?.data?.getQueryParameter("approval_no")!!.trim(),
                    authDate = result.data?.data?.getQueryParameter("approval_date")!!,
                    cardNumber = result.data?.data?.getQueryParameter("card_no")!!
                )
            }
        }
    }

    @Composable
    fun CardTerminalNewIntent() {
        Consumer<Intent> {
            when (it.data?.getQueryParameter("setleSuccesAt")) {
                "X" -> {
                    val message = URLDecoder.decode(it.data?.getQueryParameter("setleMssage"), "UTF-8")
                    offlinePaymentViewModel.offlinePaymentInfo.value.merchantUrl?.let { merchantUrl ->
                        configureIntent(
                            isSuccess = false,
                            message = message,
                            merchantUrl = merchantUrl
                        )
                    } ?: navigateToErrorDialog(message)
                }

                "O" -> {
                    offlinePaymentViewModel.pushOfflinePayment(
                        installment = it.data?.getQueryParameter("instlmtMonth")!!,
                        authCode = it.data?.getQueryParameter("confmNo")!!,
                        authDate = it.data?.getQueryParameter("confmDe")!! + it.data?.getQueryParameter("confmTime")!!,
                        cardNumber = it.data?.getQueryParameter("cardNo")!!.replace("-", "")
                    )
                }
            }
        }.let {
            DisposableEffect(componentActivity, navController) {
                componentActivity.addOnNewIntentListener(it)
                onDispose { componentActivity.removeOnNewIntentListener(it) }
            }
        }
    }

    @Composable
    fun ObserveResultLogin(
        intent: Intent,
        reactLogin: UseCaseResult<String>,
        merchantUrl: String
    ) {
        LaunchedEffect(reactLogin) {
            when(reactLogin) {
                is UseCaseResult.Success -> {
                    val offlinePaymentInfo = OfflinePaymentVM.OfflinePaymentInfo.Approve(
                        installment = intent.data?.getQueryParameter("installment") ?: "",
                        totalAmount = intent.data?.getQueryParameter("totalAmount")?.toInt() ?: 0,
                        trackId = intent.data?.getQueryParameter("trackId") ?: "",
                        freeAmount = intent.data?.getQueryParameter("freeAmount")?.toInt() ?: 0,
                        serviceAmount = intent.data?.getQueryParameter("serviceAmount")?.toInt() ?: 0,
                        merchantUrl = intent.data?.getQueryParameter("callbackAppUrl")
                    )

                    offlinePaymentViewModel.updateOfflinePaymentInfo(offlinePaymentInfo)
                    navigateToDeviceDialog()
                }

                is UseCaseResult.Error -> configureIntent(
                    isSuccess = false,
                    message = reactLogin.message,
                    merchantUrl = merchantUrl
                )

                is UseCaseResult.Exception -> configureIntent(
                    isSuccess = false,
                    message = reactLogin.exception.message.toString(),
                    merchantUrl = merchantUrl
                )

                else -> {}
            }
        }
        if(this is UseCaseResult.Loading) LoadingDialog(navController)
    }

    @Composable
    fun PaymentResult(
        paymentProcessState: OfflinePaymentVM.PaymentProcessState,
        communicateCardTerminalManager: CardTerminalCommunicateManager?,
        merchantUrl: String?,
        paymentProcess: @Composable () -> Unit = {}
    ) {
        when(paymentProcessState) {
            is OfflinePaymentVM.PaymentProcessState.Error -> {
                merchantUrl?.let {
                    configureIntent(
                        isSuccess = false,
                        message = paymentProcessState.message,
                        merchantUrl = it
                    )
                } ?: if (communicateCardTerminalManager == null) {
                    ErrorDialogContent(
                        navController = navController,
                        message = paymentProcessState.message
                    )
                } else {
                    navigateToErrorDialog(paymentProcessState.message)
                }
            }

            is OfflinePaymentVM.PaymentProcessState.CompletePayment -> {
                merchantUrl?.let {
                    configureIntent(
                        isSuccess = true,
                        message = "성공",
                        merchantUrl = it,
                        paymentProcessState = paymentProcessState
                    )
                } ?: paymentProcessState.data.navigateToCompletePaymentView()
            }

            OfflinePaymentVM.PaymentProcessState.Loading,
            is OfflinePaymentVM.PaymentProcessState.ApprovePayment -> LoadingDialogContent()

            OfflinePaymentVM.PaymentProcessState.Init -> {}

            else -> paymentProcess()
        }
    }
}