package com.kwonps.mtouchpos.navigation

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.core.os.bundleOf
import androidx.core.util.Consumer
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.kwonps.mtouchpos.view.navgraph.NavigationBundleKey
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState
import com.kwonps.mtouchpos.view.util.SelectDialog
import com.kwonps.mtouchpos.viewmodel.OfflinePaymentVM
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.info.PaymentProcessState
import com.kwonps.mtouchpos.vo.type.PurchaseType
import java.net.URLDecoder

private data class ExternalPaymentResult(
    val isSuccess: Boolean,
    val message: String,
    val completePayment: ApprovedPaymentType.CompletePaymentViewInfo?
)

class OfflinePaymentRouter(
    private val navigator: NavigationHandler,
    private val componentActivity: ComponentActivity,
    private val offlinePaymentViewModel: OfflinePaymentVM,
    private val route: String,
) {
    private val paymentInfo get() = offlinePaymentViewModel.offlinePaymentInfo.value

    fun configureIntent(
        isSuccess: Boolean,
        message: String,
        merchantUrl: String,
        paymentProcessState: PaymentProcessState.Complete? = null
    ) {
        val queryParameter = StringBuilder().apply {
            append(merchantUrl)
            append("?isSuccess=$isSuccess")
            append("&resultMsg=$message")
            paymentProcessState?.let {
                append("&purchaseType=${it.data.purchaseType.code}")
                append("&amount=${it.data.totalAmount}")
                append("&installment=${it.data.installment}")
                it.data.trackId?.let { trackId -> append("&trackId=${trackId}") }
                it.data.trxId?.let { trxId -> append("&trxId=${trxId}") }
                append("&authDate=${it.data.authDate}")
                append("&authCode=${it.data.authCode}")
                append("&cardNumber=${it.data.cardNumber}")
            }
        }.toString()
        componentActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(queryParameter)))
        componentActivity.finish()
    }

    fun navigateToDeviceDialog() {
        navigator.navigate(
            route = "${NavigationGraphState.CreditPaymentView.PaymentProcessDialog.name}/$route"
        )
    }

    fun navigateToErrorDialog(message: String) {
        navigator.navigate(
            route = NavigationGraphState.CommonView.MessageDialog.name,
            bundle = bundleOf(
                NavigationBundleKey.MESSAGE to SelectDialog(
                    initValue = message
                )
            ),
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(route, false).build()
        )
    }

    @Composable
    fun cardTerminalActivityResult() = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleExternalPayment(
            isPG = offlinePaymentViewModel.offlinePaymentInfo.value.dptId == null,
            paymentResult = parseCardReaderResult(result)
        )
    }

    @Composable
    fun cardTerminalNewIntent() {
        Consumer<Intent> {
            parseNewIntentResult(it)?.let { parsed ->
                handleExternalPayment(
                    isPG = offlinePaymentViewModel.offlinePaymentInfo.value.dptId == null,
                    paymentResult = parsed
                )
            }
        }.let { listener ->
            DisposableEffect(componentActivity, navigator.navController) {
                componentActivity.addOnNewIntentListener(listener)
                onDispose { componentActivity.removeOnNewIntentListener(listener) }
            }
        }
    }

    private fun parseCardReaderResult(result: ActivityResult): ExternalPaymentResult {
        val responseUri = result.data?.data
        val resultMessage = when (responseUri?.getQueryParameter("response_msg")) {
            "RDI_FAIL_MAKE_PACKET" -> "리더기 패킷 생성 오류"
            "RDI_FAIL_READ_DATA" -> "리더기 데이터 읽기 오류"
            "RDI_FAIL_READ_TIMEOUT" -> "리더기 타임아웃 오류"
            "VAN_FAIL_MAKE_PACKET" -> "VAN 패킷 생성 오류"
            "VAN_FAIL_READ_DATA" -> "VAN 데이터 읽기 오류"
            "VAN_FAIL_READ_TIMEOUT" -> "VAN 타임아웃 오류"
            "VAN_FAIL_SERVER_ERROR" -> "VAN 서버 오류"
            "PAYMENT_CANCEL" -> "거래 취소"
            "DEVICE_ROOTING" -> "루팅된 디바이스"
            "ISNOT_DEVICE_DOWNLOAD" -> "단말기 다운로드 오류"
            else -> "성공"
        }

        val isSuccess = responseUri?.getQueryParameter("response_code") == "0000"
        val completePaymentViewInfo = if (isSuccess) {
            responseUri?.let { uri ->
                val installment = uri.getQueryParameter("installment") ?: return@let null
                val authDate = uri.getQueryParameter("approval_date")?.trim() ?: return@let null
                val authCode = uri.getQueryParameter("approval_no")?.trim() ?: return@let null
                val issuer = uri.getQueryParameter("issuer_name")?.trim() ?: return@let null
                val acquirer = uri.getQueryParameter("acquirer_name")?.trim() ?: return@let null
                val cardNumber = uri.getQueryParameter("card_no") ?: return@let null
                val purchaseType = if (uri.getQueryParameter("trdtype") == "F1") {
                    PurchaseType.APPROVE
                } else {
                    PurchaseType.REFUND
                }

                buildCompletePaymentInfo(
                    purchaseType = purchaseType,
                    installment = installment,
                    authDate = authDate,
                    authCode = authCode,
                    issuer = issuer,
                    acquirer = acquirer,
                    cardNumber = cardNumber
                )
            }
        } else {
            null
        }

        return ExternalPaymentResult(
            isSuccess = isSuccess,
            message = resultMessage,
            completePayment = completePaymentViewInfo
        )
    }

    private fun parseNewIntentResult(intent: Intent): ExternalPaymentResult? {
        val data = intent.data ?: return null

        val settleResult = data.getQueryParameter("setleSuccesAt")
        if (settleResult != null) {
            val isSuccess = settleResult == "O"
            val completePaymentViewInfo = if (isSuccess) {
                buildCompletePaymentInfo(
                    purchaseType = if (data.getQueryParameter("delngSe") == "1") PurchaseType.APPROVE else PurchaseType.REFUND,
                    installment = data.getQueryParameter("instlmtMonth") ?: return null,
                    authDate = data.getQueryParameter("confmDe")?.plus(data.getQueryParameter("confmTime") ?: "") ?: return null,
                    authCode = data.getQueryParameter("confmNo") ?: return null,
                    issuer = data.getQueryParameter("issuCmpnyNm") ?: return null,
                    acquirer = data.getQueryParameter("puchasCmpnyNm") ?: return null,
                    cardNumber = data.getQueryParameter("cardNo")?.replace("-", "") ?: return null,
                )
            } else {
                null
            }

            val message = URLDecoder.decode(data.getQueryParameter("setleMssage"), "UTF-8")
            return ExternalPaymentResult(
                isSuccess = isSuccess,
                message = message,
                completePayment = completePaymentViewInfo
            )
        }

        val approvalNumber = data.getQueryParameter("approvalNo")
        if (approvalNumber != null) {
            val isSuccess = approvalNumber != "X"
            val completePaymentViewInfo = if (isSuccess) {
                buildCompletePaymentInfo(
                    purchaseType = if (data.getQueryParameter("cancelApprovalNo") == null) PurchaseType.APPROVE else PurchaseType.REFUND,
                    installment = data.getQueryParameter("monthVal") ?: return null,
                    authDate = data.getQueryParameter("approvalDate")?.substring(2) ?: return null,
                    authCode = data.getQueryParameter("approvalNo") ?: return null,
                    issuer = data.getQueryParameter("issuerName") ?: return null,
                    acquirer = data.getQueryParameter("acquirerName") ?: return null,
                    cardNumber = data.getQueryParameter("cardNo")?.replace("-", "") ?: return null,
                )
            } else {
                null
            }

            val message = data.getQueryParameter("message1").orEmpty()
            return ExternalPaymentResult(
                isSuccess = isSuccess,
                message = message,
                completePayment = completePaymentViewInfo
            )
        }

        return null
    }

    private fun handleExternalPayment(
        isPG: Boolean,
        paymentResult: ExternalPaymentResult
    ) {
        val merchantUrl = paymentInfo.merchantUrl
        val shouldReturnToMerchant = !isPG && paymentResult.completePayment != null

        when {
            paymentResult.completePayment == null -> {
                merchantUrl?.let {
                    configureIntent(
                        isSuccess = false,
                        message = paymentResult.message,
                        merchantUrl = it
                    )
                } ?: navigator.showErrorDialog(paymentResult.message)
            }

            isPG -> offlinePaymentViewModel.pushOfflinePayment(paymentResult.completePayment)

            shouldReturnToMerchant -> merchantUrl?.let {
                configureIntent(
                    isSuccess = true,
                    message = paymentResult.message,
                    merchantUrl = it,
                    paymentProcessState = PaymentProcessState.Complete(paymentResult.completePayment)
                )
            } ?: navigator.showErrorDialog(paymentResult.message)
        }
    }

    private fun buildCompletePaymentInfo(
        purchaseType: PurchaseType,
        installment: String,
        authDate: String,
        authCode: String,
        issuer: String,
        acquirer: String,
        cardNumber: String
    ): ApprovedPaymentType.CompletePaymentViewInfo {
        return ApprovedPaymentType.CompletePaymentViewInfo(
            purchaseType = purchaseType,
            trackId = null,
            trxId = null,
            totalAmount = paymentInfo.totalAmount,
            freeAmount = paymentInfo.freeAmount.toString(),
            serviceAmount = paymentInfo.serviceAmount.toString(),
            installment = installment,
            authDate = authDate,
            authCode = authCode,
            issuer = issuer,
            acquirer = acquirer,
            cardNumber = cardNumber,
            cardType = null,
            remainAmount = null,
        )
    }
}

@Composable
fun rememberOfflinePaymentRouter(
    navController: NavController,
    componentActivity: ComponentActivity,
    offlinePaymentViewModel: OfflinePaymentVM,
    route: String,
): OfflinePaymentRouter {
    val navigator = rememberNavigationHandler(navController)
    return remember(navigator, componentActivity, offlinePaymentViewModel, route) {
        OfflinePaymentRouter(navigator, componentActivity, offlinePaymentViewModel, route)
    }
}
