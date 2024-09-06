package com.example.mtouchpos.view.ui

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mtouchpos.R
import com.example.mtouchpos.viewmodel.factory.CardTerminalFactory
import com.example.mtouchpos.view.ui.theme.observeResultPaymentData
import com.example.mtouchpos.view.util.ColumnKeyValueTextBox
import com.example.mtouchpos.view.util.RowSmallSizeTextBox
import com.example.mtouchpos.viewmodel.LoginViewModel
import com.example.mtouchpos.viewmodel.OfflinePaymentViewModel
import com.example.mtouchpos.viewmodel.PaymentHistoryViewModel
import com.example.mtouchpos.viewmodel.mapper.toOfflineCancelPaymentInfo
import com.example.mtouchpos.vo.type.PaymentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryDetailView(
    navController: NavController = rememberNavController(),
    paymentHistoryInfo: PaymentHistoryViewModel.PaymentHistoryInfo,
    mainViewModel: LoginViewModel = hiltViewModel(),
    offlinePaymentViewModel: OfflinePaymentViewModel = hiltViewModel()
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val context = LocalContext.current
    val setupWebPageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

    }

    offlinePaymentViewModel.paymentProcessState
        .collectAsStateWithLifecycle(OfflinePaymentViewModel.PaymentProcessState.Init).value
        .observeResultPaymentData(navController, offlinePaymentViewModel.fetchConnectedDeviceInfo())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("거래 상세 내역", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Menu")
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = colorResource(id = R.color.grey7)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LoginStatus(mainViewModel.fetchCurrentConnectedUserInfo()?.tmnId ?: "")
            Column(
                modifier = Modifier
                    .width((screenWidth * 0.9).dp)
                    .wrapContentHeight()
                    .paint(
                        painterResource(id = R.drawable.payment_box),
                        contentScale = ContentScale.FillBounds
                    )
            ) {
                PaymentHistoryList(navController, paymentHistoryInfo)
                Row(
                    modifier = Modifier.padding(top = 20.dp, start = 35.dp)
                ) {
                    listOf(
                        "할부기간" to paymentHistoryInfo.installment,
                        "카드번호" to paymentHistoryInfo.cardNumber
                    ).map { (key, value) ->
                        ColumnKeyValueTextBox(
                            modifier = Modifier.weight(1f),
                            key = key,
                            value = value,
                            keyFontSize = 14.sp,
                            valueFontSize = 15.sp,
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(top = 20.dp, start = 35.dp)
                ) {
                    listOf(
                        "승인일자" to paymentHistoryInfo.regDate,
                        "승인번호" to paymentHistoryInfo.authCode
                    ).forEach { (key, value) ->
                        ColumnKeyValueTextBox(
                            modifier = Modifier.weight(1f),
                            key = key,
                            value = value,
                            keyFontSize = 14.sp,
                            valueFontSize = 15.sp,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 20.dp, top = 30.dp, bottom = 20.dp)
                ) {
                    mutableListOf(
                        "PRINT" to R.color.teal_700,
                        "SMS" to R.color.blackbb,
                    ).also {
                        if (paymentHistoryInfo.paymentType == PaymentType.REFUND) {
                            it.add("취소" to R.color.red)
                        }
                    }.forEach { (key, value) ->
                        RowSmallSizeTextBox(
                            modifier = Modifier
                                .weight(1f)
                                .background(colorResource(id = value))
                                .clickable {
                                    if (key == "취소") {
                                        with(offlinePaymentViewModel) {
                                            updateOfflineCancelPaymentInfo(paymentHistoryInfo.toOfflineCancelPaymentInfo(0, 0))
                                            requestOfflineCancelPayment(
                                                CardTerminalFactory(context, setupWebPageLauncher)
                                            )
                                        }
                                    }
                                },
                            value = key
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentHistoryDetailPreView() {
//    CleanArchitech_text_0506Theme{
//        PaymentHistoryDetailView(
//            responseGetPaymentListBody = ResponseGetPaymentListBody(
//                rfdTime = "151309",
//                amount = "11004",
//                van = "KSPAY3",
//                vanTrxId = "186950124096",
//                authCd = "60446422",
//                tmnId = "test0003",
//                trackId = "TX_1697695965270",
//                bin = "448125",
//                cardType = "체크",
//                trxId = "T231019667958",
//                issuer = "비씨",
//                regDay = "20231019",
//                resultMsg = "정상승인",
//                number = "448125**********",
//                trxResult = "취소",
//                regTime = "151257",
//                vanId = "2006500004",
//                _idx = "1",
//                installment = "00",
//                rfdDay = "20231019",
//                mchtId = "ktest",
//                brand = "하나비씨체크",
//                rfdId = "T231019667960"
//            ),
//            deviceCommunicationViewModel = DeviceCommunicationViewModel(MockView())
//        )
//    }
}

