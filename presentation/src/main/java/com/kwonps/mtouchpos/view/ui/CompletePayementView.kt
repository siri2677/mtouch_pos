package com.kwonps.mtouchpos.view.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kwonps.mtouchpos.R
import com.kwonps.mtouchpos.coordinator.DirectPaymentCoordinator
import com.kwonps.mtouchpos.coordinator.OfflinePaymentCoordinator
import com.kwonps.mtouchpos.print.CardTerminalPrintFactory
import com.kwonps.mtouchpos.print.CardTerminalPrintManager
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState
import com.kwonps.mtouchpos.view.ui.theme.TopNavigation
import com.kwonps.mtouchpos.view.util.ColumnKeyValueTextBox
import com.kwonps.mtouchpos.view.util.RowSmallSizeTextBox
import com.kwonps.mtouchpos.viewmodel.DirectPaymentVM
import com.kwonps.mtouchpos.viewmodel.OfflinePaymentVM
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.info.PaymentProcessState
import com.kwonps.mtouchpos.vo.info.UserInfo
import com.kwonps.mtouchpos.vo.type.PurchaseType

@Composable
fun CompletePaymentView(
    navController: NavController = rememberNavController(),
    offlinePaymentViewModel: OfflinePaymentVM,
    argument: String,
    complete: PaymentProcessState.Complete
) {
    val context = LocalContext.current as ComponentActivity
    val cardTerminalPrintManager = CardTerminalPrintFactory().getCommunicateManager(
        context = context,
        userInfo = offlinePaymentViewModel.getConnectedUserInfo() ?: UserInfo(),
        approvedPaymentType = complete.data,
    )

    when (argument) {
        NavigationGraphState.DirectPaymentView.DirectPayment.name -> {
            val directPaymentViewModel = hiltViewModel<DirectPaymentVM>()

            DirectPaymentCoordinator(navController).observeResultPaymentData(
                directPaymentViewModel.reactDirectPaymentInfo
                    .collectAsStateWithLifecycle().value
            )

            CompletePaymentView(navController, offlinePaymentViewModel, cardTerminalPrintManager, complete.data) {
                directPaymentViewModel.requestDirectCancelPayment(complete.data)
            }
        }

        NavigationGraphState.CreditPaymentView.CreditPayment.name -> {
            val offlinePaymentCoordinator = OfflinePaymentCoordinator(
                navController = navController,
                offlinePaymentViewModel = offlinePaymentViewModel,
                componentActivity = context,
                route = argument
            )

            offlinePaymentCoordinator.CardTerminalNewIntent()

            CompletePaymentView(navController, offlinePaymentViewModel, cardTerminalPrintManager, complete.data) {
                offlinePaymentViewModel.updateOfflinePaymentInfo(complete.data.toCancelPaymentInfo())
                offlinePaymentCoordinator.navigateToDeviceDialog()
            }
        }

        NavigationGraphState.PaymentHistoryView.PaymentHistoryDetail.name -> {
            CompletePaymentView(navController, offlinePaymentViewModel, cardTerminalPrintManager, complete.data)
        }
    }
}

@Composable
fun CompletePaymentView(
    navController: NavController,
    offlinePaymentViewModel: OfflinePaymentVM,
    cardTerminalPrintManager: CardTerminalPrintManager?,
    paymentDetailInfo: ApprovedPaymentType.CompletePaymentViewInfo,
    cancelPayment: () -> Unit = {}
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp

    Scaffold(
        topBar = {
            when (paymentDetailInfo.purchaseType) {
                PurchaseType.APPROVE -> {
                    TopNavigation("결제 완료 페이지", navController)
                }

                PurchaseType.REFUND -> {
                    TopNavigation("결제 취소 완료 페이지", navController)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = colorResource(id = R.color.grey7)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .width((screenWidth * 0.9).dp)
                    .wrapContentHeight()
                    .paint(
                        painterResource(id = R.drawable.payment_box),
                        contentScale = ContentScale.FillBounds
                    )
            ) {
                Column(
                    modifier = Modifier.padding(top = 10.dp, start = 30.dp, bottom = 30.dp)
                ) {
                    listOf(
                        "전표번호" to paymentDetailInfo.trackId!!,
                        "카드번호" to paymentDetailInfo.cardNumber,
                        "금액" to paymentDetailInfo.totalAmount,
                        "승인일자" to paymentDetailInfo.authDate,
                        "승인번호" to paymentDetailInfo.authCode,
                        "거래번호" to paymentDetailInfo.trxId!!
                    ).forEach { (key, value) ->
                        ColumnKeyValueTextBox(
                            modifier = Modifier.padding(top = 10.dp),
                            key = key,
                            value = value
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 20.dp, bottom = 10.dp)
                ) {
                    mutableListOf(
                        "PRINT" to R.color.teal_700,
                        "문자\n영수증" to R.color.blackbb,
                        "이미지\n영수증" to R.color.grey3
                    ).also {
                        if (paymentDetailInfo.purchaseType == PurchaseType.APPROVE) {
                            it.add("취소" to R.color.red)
                        }
                    }.forEach { (value, colorId) ->
                        RowSmallSizeTextBox(
                            value = value,
                            modifier = Modifier
                                .weight(1f)
                                .background(colorResource(id = colorId))
                                .clickable {
                                    when (value) {
                                        "PRINT" -> { cardTerminalPrintManager?.invoke() ?: offlinePaymentViewModel.print(paymentDetailInfo) }
                                        "문자\n영수증" -> {}
                                        "이미지\n영수증" -> {}
                                        "취소" -> cancelPayment()
                                        else -> {}
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

