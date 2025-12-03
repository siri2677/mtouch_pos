package com.kwonps.mtouchpos.view.ui

import androidx.activity.ComponentActivity
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kwonps.mtouchpos.R
import com.kwonps.mtouchpos.navigation.rememberOfflinePaymentRouter
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState

import com.kwonps.mtouchpos.view.util.ColumnKeyValueTextBox
import com.kwonps.mtouchpos.view.util.RowSmallSizeTextBox
import com.kwonps.mtouchpos.viewmodel.LoginVM
import com.kwonps.mtouchpos.viewmodel.OfflinePaymentVM
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.type.PurchaseType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryDetailView(
    navController: NavController = rememberNavController(),
    offlinePaymentViewModel: OfflinePaymentVM,
    paymentHistoryInfo: ApprovedPaymentType.PaymentHistoryViewInfo,
    mainViewModel: LoginVM = hiltViewModel()
) {
    val context = LocalContext.current as ComponentActivity
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val offlinePaymentCoordinator = rememberOfflinePaymentRouter(
        navController = navController,
        offlinePaymentViewModel = offlinePaymentViewModel,
        componentActivity = context,
        route = NavigationGraphState.PaymentHistoryView.PaymentHistoryDetail.name
    )

    offlinePaymentCoordinator.cardTerminalNewIntent()

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
            LoginStatus(
                navController,
                mainViewModel.fetchCurrentConnectedUserInfo()?.tmnId ?: ""
            )

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
                        "카드번호" to paymentHistoryInfo.number
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
                        "승인일자" to paymentHistoryInfo.regDay,
                        "승인번호" to paymentHistoryInfo.authCd
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
                        if (paymentHistoryInfo.purchaseType == PurchaseType.APPROVE) {
                            it.add("취소" to R.color.red)
                        }
                    }.forEach { (key, value) ->
                        RowSmallSizeTextBox(
                            modifier = Modifier
                                .weight(1f)
                                .background(colorResource(id = value))
                                .clickable {
                                    if (key == "취소") {
                                        offlinePaymentViewModel.updateOfflinePaymentInfo(paymentHistoryInfo.toCancelPaymentInfo())
                                        offlinePaymentCoordinator.navigateToDeviceDialog()
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

