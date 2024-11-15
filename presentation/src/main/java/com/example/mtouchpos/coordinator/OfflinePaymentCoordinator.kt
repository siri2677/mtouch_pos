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
import com.example.domain.usecase.device.manager.CommunicateCardTerminalManager
import com.example.mtouchpos.view.navgraph.NavigationBundleKey
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.navigate
import com.example.mtouchpos.view.util.ErrorDialogContent
import com.example.mtouchpos.view.util.LoadingDialogContent
import com.example.mtouchpos.viewmodel.OfflinePaymentViewModel
import com.example.mtouchpos.viewmodel.factory.CardTerminalFactory
import com.example.mtouchpos.vo.type.PurchaseType
import com.example.mtouchpos.vo.type.UseCaseResult
import java.io.Serializable
import java.net.URLDecoder

class OfflinePaymentCoordinator(
    override val navController: NavController,
    private val componentActivity: ComponentActivity,
    val offlinePaymentViewModel: OfflinePaymentViewModel
): CommonCoordinator(navController), Serializable {
    data class PaymentProcess(
        val merchantUrl: String?,
        val offlinePaymentInfo: OfflinePaymentViewModel.OfflinePaymentInfo
    ): Serializable

    fun configureIntent(
        isSuccess: Boolean,
        message: String,
        merchantUrl: String,
        paymentProcessState: OfflinePaymentViewModel.PaymentProcessState.CompletePayment? = null
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

    fun navigateToDeviceDialog(paymentProcess: PaymentProcess) {
        navController.navigate(
            route = NavigationGraphState.CreditPaymentView.PaymentProcessDialog.name,
            bundle = bundleOf(NavigationBundleKey.DIALOG_DATA to paymentProcess),
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
        )
    }

    private fun navigateToErrorDialog(message: String) {
        navController.navigate(
            route = NavigationGraphState.CommonView.ErrorDialog.name,
            bundle = bundleOf(NavigationBundleKey.MESSAGE to message),
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(NavigationGraphState.CreditPaymentView.CreditPayment.name, false).build()
        )
    }

    @Composable
    fun cardTerminalActivityResult(merchantUrl: String?) = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when(result.data?.data?.getQueryParameter("response_msg")) {
            "PAYMENT_CANCEL" -> {
                val message = "결제가 취소 되었습니다."
                merchantUrl?.let { merchantUrl ->
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
                    purchaseType = if(result.data?.data?.getQueryParameter("trdtype") == "1") PurchaseType.APPROVE
                    else PurchaseType.REFUND,
                    authCode = result.data?.data?.getQueryParameter("approval_no")!!,
                    authDate = result.data?.data?.getQueryParameter("approval_date")!!,
                    cardNumber = result.data?.data?.getQueryParameter("card_no")!!
                )
            }
        }
    }

    @Composable
    fun cardTerminalNewIntent(
        paymentProcessState: OfflinePaymentViewModel.PaymentProcessState,
        callBack: CardTerminalFactory.CallBack,
        merchantUrl: String?,
    ) {
        Consumer<Intent> {
            when (it.data?.getQueryParameter("setleSuccesAt")) {
                "X" -> {
                    val message = URLDecoder.decode(it.data?.getQueryParameter("setleMssage"), "UTF-8")
                    merchantUrl?.let { merchantUrl ->
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
                        purchaseType = if(it.data?.getQueryParameter("delngSe") == "1") PurchaseType.APPROVE else PurchaseType.REFUND,
                        authCode = it.data?.getQueryParameter("confmNo")!!,
                        authDate = it.data?.getQueryParameter("confmDe")!! + it.data?.getQueryParameter("confmTime")!!,
                        cardNumber = it.data?.getQueryParameter("cardNo")!!
                    )
                }
            }
        }.let {
            DisposableEffect(componentActivity, navController) {
                componentActivity.addOnNewIntentListener(it)
                onDispose { componentActivity.removeOnNewIntentListener(it) }
            }
        }

        paymentResult(
            paymentProcessState = paymentProcessState,
            communicateCardTerminalManager = CardTerminalFactory(
                context = componentActivity,
                launcher = cardTerminalActivityResult(merchantUrl),
                callBack = callBack
            ),
            merchantUrl = merchantUrl
        )
    }

    @Composable
    fun observeResultLogin(
        intent: Intent,
        reactLogin: UseCaseResult<String>,
        merchantUrl: String
    ) {
        LaunchedEffect(reactLogin) {
            when(reactLogin) {
                is UseCaseResult.Success -> {
                    val offlinePaymentInfo = OfflinePaymentViewModel.OfflinePaymentInfo.Approve(
                        installment = intent.data?.getQueryParameter("installment") ?: "",
                        totalAmount = intent.data?.getQueryParameter("totalAmount")?.toInt() ?: 0,
                        trackId = intent.data?.getQueryParameter("trackId") ?: "",
                        freeAmount = intent.data?.getQueryParameter("freeAmount")?.toInt() ?: 0,
                        serviceAmount = intent.data?.getQueryParameter("serviceAmount")?.toInt() ?: 0
                    )

                    with(offlinePaymentViewModel) {
                        updateOfflinePaymentInfo(offlinePaymentInfo)
                        PaymentProcess(
                            merchantUrl = merchantUrl,
                            offlinePaymentInfo = offlinePaymentInfo
                        ).let { navigateToDeviceDialog(it) }
                    }
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
    }

    @Composable
    fun paymentResult(
        paymentProcessState: OfflinePaymentViewModel.PaymentProcessState,
        communicateCardTerminalManager: CommunicateCardTerminalManager?,
        merchantUrl: String?,
        paymentProcess: @Composable () -> Unit = {}
    ) {
        when(paymentProcessState) {
            is OfflinePaymentViewModel.PaymentProcessState.Error -> {
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

            is OfflinePaymentViewModel.PaymentProcessState.CompletePayment -> {
                merchantUrl?.let {
                    configureIntent(
                        isSuccess = true,
                        message = "성공",
                        merchantUrl = it,
                        paymentProcessState = paymentProcessState
                    )
                } ?: paymentProcessState.data.navigateToCompletePaymentView(navController)
            }

            OfflinePaymentViewModel.PaymentProcessState.Loading -> LoadingDialogContent()

            else -> paymentProcess()
        }
    }
}