package com.example.mtouchpos.view.ui

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mtouchpos.R
import com.example.mtouchpos.coordinator.DirectPaymentCoordinator
import com.example.mtouchpos.coordinator.OfflinePaymentCoordinator
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.theme.TopNavigation
import com.example.mtouchpos.view.util.ColumnKeyValueTextBox
import com.example.mtouchpos.view.util.RowSmallSizeTextBox
import com.example.mtouchpos.viewmodel.DirectPaymentViewModel
import com.example.mtouchpos.viewmodel.OfflinePaymentViewModel
import com.example.mtouchpos.viewmodel.factory.CardTerminalFactory
import com.example.mtouchpos.vo.data.ApprovedPaymentType
import com.example.mtouchpos.vo.type.PurchaseType

@Composable
fun CompletePaymentPage(
    navController: NavController = rememberNavController(),
    completePaymentViewInfo: ApprovedPaymentType.CompletePaymentViewInfo,
    beforeNavGraph: String
) {
    val context = LocalContext.current as ComponentActivity

    when (beforeNavGraph) {
        NavigationGraphState.DirectPaymentView.DirectPayment.name -> {
            val directPaymentViewModel = hiltViewModel<DirectPaymentViewModel>()

            DirectPaymentCoordinator(navController).observeResultPaymentData(
                directPaymentViewModel.reactDirectPaymentInfo
                    .collectAsStateWithLifecycle().value
            )

            CompletePaymentPage(navController, completePaymentViewInfo) {
                directPaymentViewModel.requestDirectCancelPayment(completePaymentViewInfo)
            }
        }

        NavigationGraphState.CreditPaymentView.BluetoothDialog.name,
        NavigationGraphState.CreditPaymentView.UsbDialog.name,
        NavigationGraphState.CreditPaymentView.CreditPayment.name -> {
            val offlinePaymentViewModel = hiltViewModel<OfflinePaymentViewModel>()
            val offlinePaymentCoordinator = OfflinePaymentCoordinator(
                navController = navController,
                offlinePaymentViewModel = offlinePaymentViewModel,
                componentActivity = context
            )
            val paymentProcessState = offlinePaymentViewModel.paymentProcessState
                .collectAsStateWithLifecycle(OfflinePaymentViewModel.PaymentProcessState.Init).value

//            offlinePaymentCoordinator.observeResultPaymentData(
//                paymentProcessState = offlinePaymentViewModel.paymentProcessState
//                    .collectAsStateWithLifecycle(OfflinePaymentViewModel.PaymentProcessState.Init).value,
//                communicateCardTerminalManager = communicateCardTerminalManager
//            )

            offlinePaymentCoordinator.cardTerminalNewIntent(
                paymentProcessState = paymentProcessState,
                callBack = CardTerminalFactory.CallBack.Home,
                merchantUrl = null
            )

            CompletePaymentPage(navController, completePaymentViewInfo) {
                OfflinePaymentCoordinator.PaymentProcess(
                    merchantUrl = null,
                    offlinePaymentInfo = offlinePaymentViewModel.offlinePaymentInfo.value
                ).let { offlinePaymentCoordinator.navigateToDeviceDialog(it) }
            }
        }

        NavigationGraphState.CommonView.CompletePayment.name,
        NavigationGraphState.PaymentHistoryView.PaymentHistoryDetail.name -> {
            CompletePaymentPage(navController, completePaymentViewInfo)
        }
    }
}

@Composable
fun CompletePaymentPage(
    navController: NavController,
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
                    modifier = Modifier.padding(top = 10.dp, start = 30.dp, bottom = 40.dp)
                ) {
                    listOf(
                        "전표번호" to paymentDetailInfo.trackId,
                        "카드번호" to paymentDetailInfo.cardNumber,
                        "금액" to paymentDetailInfo.amount,
                        "승인일자" to paymentDetailInfo.authDate,
                        "승인번호" to paymentDetailInfo.authCode,
                        "거래번호" to paymentDetailInfo.trxId
                    ).forEach { (key, value) ->
                        ColumnKeyValueTextBox(
                            modifier = Modifier.padding(top = 20.dp),
                            key = key,
                            value = value
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 20.dp, bottom = 50.dp)
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
                                .clickable { if (value == "취소") cancelPayment() }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompletePaymentMainPreView() {
//    CleanArchitech_text_0506Theme{
//        CompletePaymentPage(
//            completePaymentViewVO = CompletePaymentViewVO(
//                TransactionType.Offline,
//                PaymentType.Approve,
//                "00",
//                "TX200316016511",
//                "5409-26**-****-****",
//                "1,806,004원",
//                "2020-03-16 14:02:02",
//                "30034798",
//                "T200316016511",
//            ),
//            directPaymentViewModel = null,
//            deviceCommunicationViewModel = null
//        )
//    }
}

