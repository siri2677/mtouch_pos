package com.example.mtouchpos.view.ui

import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.mtouchpos.dto.AmountInfo
import com.example.mtouchpos.dto.DirectPaymentInfo
import com.example.mtouchpos.dto.ResponseFlowData
import com.example.mtouchpos.dto.RootPaymentInfo
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.theme.NavigationBundleKey
import com.example.mtouchpos.vo.PaymentType
import com.example.mtouchpos.vo.TransactionType
import com.example.mtouchpos.view.util.RowSmallSizeTextBox
import com.example.mtouchpos.view.util.ColumnKeyValueTextBox
import com.example.mtouchpos.view.ui.theme.ObserveResultSerialCommunicate
import com.example.mtouchpos.view.ui.theme.ObserveResultPayment
import com.example.mtouchpos.view.ui.theme.TopNavigation
import com.example.mtouchpos.viewmodel.DirectPaymentViewModel
import com.example.mtouchpos.viewmodel.DeviceCommunicationViewModel


@Composable
fun CompletePaymentPage(
    navController: NavController = rememberNavController(),
    completePayment: ResponseFlowData.CompletePayment,
    directPaymentViewModel: DirectPaymentViewModel = hiltViewModel(),
    deviceCommunicationViewModel: DeviceCommunicationViewModel = hiltViewModel()
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val context = LocalContext.current

    LaunchedEffect(Unit){
        val completeMessage = when(completePayment.paymentType) {
            PaymentType.Approve -> "결제가 완료 되었습니다"
            PaymentType.Refund -> "결제 취소가 완료 되었습니다"
        }
        Toast.makeText(context, completeMessage, Toast.LENGTH_LONG).show()
    }

    ObserveResultPayment(
        responseFlowData = deviceCommunicationViewModel.responseFlowData.collectAsStateWithLifecycle(ResponseFlowData.Init).value,
        navController = navController,
        responsePage = NavigationGraphState.CommonView.CompletePayment.name
    )

    ObserveResultPayment(
        responseFlowData = directPaymentViewModel.responseFlowData.collectAsStateWithLifecycle(ResponseFlowData.Init).value,
        navController = navController,
        responsePage = NavigationGraphState.CommonView.CompletePayment.name
    )

    ObserveResultSerialCommunicate(
        deviceCommunicationViewModel = deviceCommunicationViewModel,
        navController = navController
    )

    Scaffold(
        topBar = {
            when (completePayment.paymentType) {
                PaymentType.Approve -> {
                    TopNavigation("결제 완료 페이지")
                }
                PaymentType.Refund -> {
                    TopNavigation("결제 취소 완료 페이지")
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
                        "전표번호" to completePayment.trackId,
                        "카드번호" to completePayment.cardNumber,
                        "금액" to completePayment.amount,
                        "승인일자" to completePayment.authDate,
                        "승인번호" to completePayment.authCode,
                        "거래번호" to completePayment.trxId
                    ).map { (key, value) ->
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
                        if (completePayment.paymentType == PaymentType.Approve) {
                            it.add("취소" to R.color.red)
                        }
                    }.map { (value, colorId) ->
                        RowSmallSizeTextBox(
                            modifier = Modifier
                                .weight(1f)
                                .background(colorResource(id = colorId))
                                .clickable {
                                    if (value == "취소") {
                                        when (completePayment.transactionType) {
                                            TransactionType.Direct -> {
                                                directPaymentViewModel.requestDirectCancelPayment(
                                                    cardNumber = completePayment.cardNumber,
                                                    amountInfo = AmountInfo(
                                                        amount = Integer.parseInt(completePayment.amount),
                                                        installment = completePayment.installment
                                                    ),
                                                    rootPaymentInfo = RootPaymentInfo(
                                                        rootTrxId = completePayment.trxId,
                                                        rootTrxDay = completePayment.authDate,
                                                    )
                                                )
                                            }

                                            TransactionType.Offline -> {
                                                deviceCommunicationViewModel.requestOfflineCancelPayment(
                                                    amountInfo = AmountInfo(
                                                        amount = Integer.parseInt(completePayment.amount),
                                                        installment = completePayment.installment
                                                    ),
                                                    rootPaymentInfo = RootPaymentInfo(
                                                        rootTrxId = completePayment.trxId,
                                                        rootTrxDay = completePayment.authDate,
                                                    )
                                                )
                                            }
                                        }
                                    }
                                },
                            value = value
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

