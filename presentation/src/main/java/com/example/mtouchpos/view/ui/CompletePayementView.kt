package com.example.mtouchpos.view.ui

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.mtouchpos.viewmodel.factory.CardTerminalFactory
import com.example.mtouchpos.view.ui.theme.TopNavigation
import com.example.mtouchpos.view.ui.theme.observeCompletePaymentInfo
import com.example.mtouchpos.view.ui.theme.observeResultPaymentData
import com.example.mtouchpos.view.util.ColumnKeyValueTextBox
import com.example.mtouchpos.view.util.RowSmallSizeTextBox
import com.example.mtouchpos.viewmodel.DirectPaymentViewModel
import com.example.mtouchpos.viewmodel.OfflinePaymentViewModel
import com.example.mtouchpos.viewmodel.mapper.toDirectCancelPaymentInfo
import com.example.mtouchpos.viewmodel.mapper.toOfflineCancelPaymentInfo
import com.example.mtouchpos.vo.data.CompletePaymentInfo
import com.example.mtouchpos.vo.type.PaymentType
import com.example.mtouchpos.vo.type.TransactionType
import com.example.mtouchpos.vo.type.UseCaseResult

@Composable
fun CompletePaymentPage(
    navController: NavController = rememberNavController(),
    completePaymentInfo: CompletePaymentInfo,
    directPaymentViewModel: DirectPaymentViewModel = hiltViewModel(),
    offlinePaymentViewModel: OfflinePaymentViewModel = hiltViewModel()
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val context = LocalContext.current
    val setupWebPageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

    }

    fun cancelPayment(
        completePaymentInfo: CompletePaymentInfo,
        directPaymentViewModel: DirectPaymentViewModel,
        offlinePaymentViewModel: OfflinePaymentViewModel
    ) {
        when (completePaymentInfo.transactionType) {
            TransactionType.DIRECT -> {
                with(directPaymentViewModel) {
                    updateDirectCancelPaymentInfo(completePaymentInfo.toDirectCancelPaymentInfo())
                    requestDirectCancelPayment()
                }
            }

            TransactionType.OFFLINE -> {
                with(offlinePaymentViewModel) {
                    updateOfflineCancelPaymentInfo(completePaymentInfo.toOfflineCancelPaymentInfo(0, 0))
                    requestOfflineCancelPayment(
                        CardTerminalFactory(context, setupWebPageLauncher).getCommunicateManger(Build.MODEL)
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit){
        val completeMessage = when(completePaymentInfo.paymentType) {
            PaymentType.APPROVE -> "결제가 완료 되었습니다"
            PaymentType.REFUND -> "결제 취소가 완료 되었습니다"
        }
        Toast.makeText(context, completeMessage, Toast.LENGTH_LONG).show()
    }

    offlinePaymentViewModel.paymentProcessState
        .collectAsStateWithLifecycle(OfflinePaymentViewModel.PaymentProcessState.Init).value
        .observeResultPaymentData(navController, offlinePaymentViewModel.fetchConnectedDeviceInfo())

    directPaymentViewModel.reactDirectPaymentInfo
        .collectAsStateWithLifecycle(UseCaseResult.Init).value
        .observeCompletePaymentInfo(navController)

    Scaffold(
        topBar = {
            when (completePaymentInfo.paymentType) {
                PaymentType.APPROVE -> {
                    TopNavigation("결제 완료 페이지")
                }
                PaymentType.REFUND -> {
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
                        "전표번호" to completePaymentInfo.trackId,
                        "카드번호" to completePaymentInfo.cardNumber,
                        "금액" to completePaymentInfo.amount,
                        "승인일자" to completePaymentInfo.authDate,
                        "승인번호" to completePaymentInfo.authCode,
                        "거래번호" to completePaymentInfo.trxId
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
                        if (completePaymentInfo.paymentType == PaymentType.APPROVE) {
                            it.add("취소" to R.color.red)
                        }
                    }.forEach { (value, colorId) ->
                        RowSmallSizeTextBox(
                            value = value,
                            modifier = Modifier
                                .weight(1f)
                                .background(colorResource(id = colorId))
                                .clickable {
                                    if (value == "취소") {
                                        cancelPayment(
                                            completePaymentInfo = completePaymentInfo,
                                            directPaymentViewModel = directPaymentViewModel,
                                            offlinePaymentViewModel = offlinePaymentViewModel
                                        )
                                    }
                                }
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

