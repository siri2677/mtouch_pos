package com.kwonps.mtouchpos.coordinator

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.os.bundleOf
import androidx.core.util.Consumer
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.kwonps.mtouchpos.view.navgraph.NavigationBundleKey
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState
import com.kwonps.mtouchpos.view.ui.navigate
import com.kwonps.mtouchpos.view.util.SelectDialog
import com.kwonps.mtouchpos.viewmodel.OfflinePaymentVM
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.info.PaymentProcessState
import com.kwonps.mtouchpos.vo.type.PurchaseType
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
        navController.navigate(
            route = "${NavigationGraphState.CreditPaymentView.PaymentProcessDialog.name}/$route"
        )
    }

    fun navigateToErrorDialog(message: String) {
        navController.navigate(
            route = NavigationGraphState.CommonView.MessageDialog.name,
            bundle = bundleOf(NavigationBundleKey.MESSAGE to SelectDialog(
                initValue = message
            )),
            navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(route, false).build()
        )
    }

    @Composable
    fun cardTerminalActivityResult() = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultMessage = when(result.data?.data?.getQueryParameter("response_msg")) {
            "RDI_FAIL_MAKE_PACKET" -> "리더기 패킷 생성 오류"
            "RDI_FAIL_READ_DATA" -> "리더기 데이터 읽기 오류"
            "RDI_FAIL_READ_TIMEOUT" -> "리더기 타임아웃 오류"
            "VAN_FAIL_MAKE_PACKET" -> "VAN 패킷 생성 오류"
            "VAN_FAIL_READ_DATA" ->	"VAN 데이터 읽기 오류"
            "VAN_FAIL_READ_TIMEOUT" -> "VAN 타임아웃 오류"
            "VAN_FAIL_SERVER_ERROR" -> "VAN 서버 오류"
            "PAYMENT_CANCEL" ->	"거래 취소"
            "DEVICE_ROOTING" -> "루팅된 디바이스"
            "ISNOT_DEVICE_DOWNLOAD" -> "단말기 다운로드 오류"
            else -> "성공"
        }

        handleExternalAppPaymentResponse(
            isPG = offlinePaymentViewModel.offlinePaymentInfo.value.dptId == null,
            resultMessage = resultMessage,
            completePaymentViewInfo = if(result.data?.data?.getQueryParameter("response_code") == "0000") {
                ApprovedPaymentType.CompletePaymentViewInfo(
                    purchaseType = if(result.data?.data?.getQueryParameter("trdtype") == "F1") PurchaseType.APPROVE else PurchaseType.REFUND,
                    trackId = null,
                    trxId = null,
                    totalAmount = offlinePaymentViewModel.offlinePaymentInfo.value.totalAmount,
                    freeAmount = offlinePaymentViewModel.offlinePaymentInfo.value.freeAmount.toString(),
                    serviceAmount = offlinePaymentViewModel.offlinePaymentInfo.value.serviceAmount.toString(),
                    installment = result.data?.data?.getQueryParameter("installment")!!,
                    authDate = result.data?.data?.getQueryParameter("approval_date")!!.trim(),
                    authCode = result.data?.data?.getQueryParameter("approval_no")!!.trim(),
                    issuer = result.data?.data?.getQueryParameter("issuer_name")!!.trim(),
                    acquirer = result.data?.data?.getQueryParameter("acquirer_name")!!.trim(),
                    cardNumber = result.data?.data?.getQueryParameter("card_no")!!,
                    cardType = null,
                    remainAmount = null
                )
            } else null
        )
    }


    @Composable
    fun CardTerminalNewIntent() {
        Consumer<Intent> {
            responseIntent(it)
        }.let {
            DisposableEffect(componentActivity, navController) {
                componentActivity.addOnNewIntentListener(it)
                onDispose { componentActivity.removeOnNewIntentListener(it) }
            }
        }
    }

    private fun responseIntent(intent: Intent) {
        if (intent.data?.getQueryParameter("setleSuccesAt") != null) {
            handleExternalAppPaymentResponse(
                isPG = offlinePaymentViewModel.offlinePaymentInfo.value.dptId == null,
                resultMessage = URLDecoder.decode(intent.data?.getQueryParameter("setleMssage"), "UTF-8"),
                completePaymentViewInfo = if(intent.data?.getQueryParameter("setleSuccesAt") == "O") {
                    ApprovedPaymentType.CompletePaymentViewInfo(
                        purchaseType = if(intent.data?.getQueryParameter("delngSe") == "1") PurchaseType.APPROVE else PurchaseType.REFUND,
                        trackId = null,
                        trxId = null,
                        totalAmount = offlinePaymentViewModel.offlinePaymentInfo.value.totalAmount,
                        freeAmount = offlinePaymentViewModel.offlinePaymentInfo.value.freeAmount.toString(),
                        serviceAmount = offlinePaymentViewModel.offlinePaymentInfo.value.serviceAmount.toString(),
                        installment = intent.data?.getQueryParameter("instlmtMonth")!!,
                        authDate = intent.data?.getQueryParameter("confmDe")!! + intent.data?.getQueryParameter("confmTime")!!,
                        authCode = intent.data?.getQueryParameter("confmNo")!!,
                        issuer = intent.data?.getQueryParameter("issuCmpnyNm")!!,
                        acquirer = intent.data?.getQueryParameter("puchasCmpnyNm")!!,
                        cardNumber = intent.data?.getQueryParameter("cardNo")!!.replace("-", ""),
                        cardType = null,
                        remainAmount = null,
                    )
                } else null
            )
        } else if (intent.data?.getQueryParameter("approvalNo") != null) {
            handleExternalAppPaymentResponse(
                isPG = offlinePaymentViewModel.offlinePaymentInfo.value.dptId == null,
                resultMessage = intent.data?.getQueryParameter("message1").toString(),
                completePaymentViewInfo = if(intent.data?.getQueryParameter("approvalNo") != "X") {
                    ApprovedPaymentType.CompletePaymentViewInfo(
                        purchaseType = if(intent.data?.getQueryParameter("cancelApprovalNo") == null) PurchaseType.APPROVE else PurchaseType.REFUND,
                        trackId = null,
                        trxId = null,
                        totalAmount = offlinePaymentViewModel.offlinePaymentInfo.value.totalAmount,
                        freeAmount = offlinePaymentViewModel.offlinePaymentInfo.value.freeAmount.toString(),
                        serviceAmount = offlinePaymentViewModel.offlinePaymentInfo.value.serviceAmount.toString(),
                        installment = intent.data?.getQueryParameter("monthVal")!!,
                        authDate = intent.data?.getQueryParameter("approvalDate")!!.substring(2),
                        authCode = intent.data?.getQueryParameter("approvalNo")!!,
                        issuer = intent.data?.getQueryParameter("issuerName")!!,
                        acquirer = intent.data?.getQueryParameter("acquirerName")!!,
                        cardNumber = intent.data?.getQueryParameter("cardNo")!!.replace("-", ""),
                        cardType = null,
                        remainAmount = null,
                    )
                } else null
            )
        }
    }

    private fun handleExternalAppPaymentResponse(
        isPG: Boolean,
        resultMessage: String,
        completePaymentViewInfo: ApprovedPaymentType.CompletePaymentViewInfo?
    ) {
        val merchantUrl = offlinePaymentViewModel.offlinePaymentInfo.value.merchantUrl

        if(completePaymentViewInfo == null) {
            merchantUrl?.let {
                configureIntent(
                    isSuccess = false,
                    message = resultMessage,
                    merchantUrl = it
                )
            } ?: navigateToErrorDialog(resultMessage)
        } else {
            if (isPG) {
                offlinePaymentViewModel.pushOfflinePayment(completePaymentViewInfo)
            } else {
                merchantUrl?.let {
                    configureIntent(
                        isSuccess = true,
                        message = resultMessage,
                        merchantUrl = it,
                        paymentProcessState = PaymentProcessState.Complete(completePaymentViewInfo)
                    )
                }
            }
        }
    }
}