package com.example.cleanarchitech_text_0506.view.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import com.example.cleanarchitech_text_0506.R
import com.example.cleanarchitech_text_0506.enum.MainView
import com.example.cleanarchitech_text_0506.enum.PaymentType
import com.example.cleanarchitech_text_0506.enum.TransactionType
import com.example.cleanarchitech_text_0506.view.ui.theme.CleanArchitech_text_0506Theme
import com.example.cleanarchitech_text_0506.viewmodel.DirectPaymentViewModel
import com.example.cleanarchitech_text_0506.viewmodel.MainActivityViewModel
import com.example.cleanarchitech_text_0506.viewmodel.DeviceCommunicationViewModel
import com.example.cleanarchitech_text_0506.vo.CompletePaymentViewVO
import com.example.domain.dto.request.pay.RequestDirectCancelPaymentDto
import com.example.domain.dto.request.tms.RequestInsertPaymentDataDTO
import com.example.domain.sealed.ResponsePayAPI

@Composable
fun CompletePaymentView(
    navHostController: NavController,
    completePaymentViewVO: CompletePaymentViewVO?,
    mainActivityViewModel: MainActivityViewModel = hiltViewModel(),
    directPaymentViewModel: DirectPaymentViewModel = hiltViewModel(),
    deviceCommunicationViewModel: DeviceCommunicationViewModel = hiltViewModel()
) {
    CompletePaymentMainView(
        navHostController,
        completePaymentViewVO!!,
        mainActivityViewModel,
        directPaymentViewModel,
        deviceCommunicationViewModel.setDeviceType()!!
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletePaymentMainView(
    navHostController: NavController = rememberNavController(),
    completePaymentViewVO: CompletePaymentViewVO,
    mainActivityViewModel: MainActivityViewModel?,
    directPaymentViewModel: DirectPaymentViewModel?,
    deviceCommunicationViewModel: DeviceCommunicationViewModel?
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val context = LocalContext.current
    val responseDirectPayment = directPaymentViewModel?.responseDirectPayment?.collectAsStateWithLifecycle(
        initialValue = ""
    )?.value
    val deviceConnectSharedFlow = deviceCommunicationViewModel?.deviceConnectSharedFlow?.collectAsStateWithLifecycle(
        initialValue = ""
    )?.value

    LaunchedEffect(key1 = Unit){
        val completeMessage = when(completePaymentViewVO.paymentType) {
            PaymentType.Approve -> "결제가 완료 되었습니다"
            PaymentType.Refund -> "결제 취소가 완료 되었습니다"
        }
        Toast.makeText(context, completeMessage, Toast.LENGTH_LONG).show()
    }

    when(responseDirectPayment) {
        is ResponsePayAPI.DirectCancelPaymentContent -> {
            val completePaymentViewVo = CompletePaymentViewVO(
                transactionType = TransactionType.Direct,
                paymentType = PaymentType.Refund,
                installment = completePaymentViewVO.installment,
                trackId = responseDirectPayment.responseDirectCancelPaymentDto.refund?.trackId!!,
                cardNumber = completePaymentViewVO.cardNumber,
                amount = responseDirectPayment.responseDirectCancelPaymentDto.refund?.amount.toString(),
                regDay = responseDirectPayment.responseDirectCancelPaymentDto.result.create,
                authCode = responseDirectPayment.responseDirectCancelPaymentDto.refund?.authCd!!,
                trxId = responseDirectPayment.responseDirectCancelPaymentDto.refund?.trxId!!,
                prodQty = null,
                prodName = null,
                prodPrice = null,
                payerTel = null,
                payerName = null,
                payerEmail = null,
            )
            navHostController?.navigate(
                MainView.CompletePayment.name,
                bundleOf("responsePayAPI" to completePaymentViewVo),
                NavOptions.Builder().setLaunchSingleTop(true).build()
            )
        }
        else -> {}
    }

    serialCommunicationResult(
        deviceCommunicationViewModel = deviceCommunicationViewModel!!,
        navHostController = navHostController,
        dialogMessage = { errorDialog(message = it) },
    )

    Scaffold(
        topBar = {
            when (completePaymentViewVO.paymentType) {
                PaymentType.Approve -> {
                    TopNavigationCompletePaymentView("결제 완료 페이지")
                }
                PaymentType.Refund -> {
                    TopNavigationCompletePaymentView("결제 취소 완료 페이지")
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
                    CompletePaymentContent(
                        modifier = Modifier.padding(top = 20.dp),
                        key = "전표번호",
                        value = completePaymentViewVO.trackId!!
                    )
                    CompletePaymentContent(
                        modifier = Modifier.padding(top = 20.dp),
                        key = "카드번호",
                        value = completePaymentViewVO.cardNumber!!
                    )
                    CompletePaymentContent(
                        modifier = Modifier.padding(top = 20.dp),
                        key = "금액",
                        value = completePaymentViewVO.amount!!
                    )
                    CompletePaymentContent(
                        modifier = Modifier.padding(top = 20.dp),
                        key = "승인일자",
                        value = completePaymentViewVO.regDay!!
                    )
                    CompletePaymentContent(
                        modifier = Modifier.padding(top = 20.dp),
                        key = "승인번호",
                        value = completePaymentViewVO.authCode!!
                    )
                    CompletePaymentContent(
                        modifier = Modifier.padding(top = 20.dp),
                        key = "거래번호",
                        value = completePaymentViewVO.trxId!!
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 20.dp, bottom = 50.dp)
                ) {
                    if (completePaymentViewVO.paymentType == PaymentType.Approve) {
                        BottomRowButton(
                            modifier = Modifier
                                .weight(1f)
                                .background(colorResource(id = R.color.red))
                                .clickable {
                                    when (completePaymentViewVO.transactionType) {
                                        TransactionType.Direct -> {
                                            directPaymentViewModel?.requestDirectCancelPayment(
                                                RequestDirectCancelPaymentDto(
                                                    payKey = mainActivityViewModel?.getUserInformation()?.payKey!!,
                                                    amount = completePaymentViewVO.amount!!,
                                                    rootTrxId = completePaymentViewVO.trxId!!,
                                                    rootTrxDay = completePaymentViewVO.regDay!!,
                                                    udf1 = null,
                                                    udf2 = null,
                                                    rootTrackId = null,
                                                    trxId = null,
                                                    authCd = null,
                                                    settle = null,
                                                )
                                            )
                                        }
                                        TransactionType.Offline -> {
                                            deviceCommunicationViewModel?.requestOfflinePaymentCancel(
                                                RequestInsertPaymentDataDTO(
                                                    amount = Integer.parseInt(completePaymentViewVO.amount),
                                                    installment = completePaymentViewVO.installment,
                                                    token = mainActivityViewModel?.getUserInformation()?.key!!,
                                                    type = PaymentType.Refund.value,
                                                    trxId = completePaymentViewVO.trxId,
                                                    authCd = completePaymentViewVO.authCode,
                                                    regDate = completePaymentViewVO.regDay,
                                                    prodQty = null,
                                                    prodName = null,
                                                    prodPrice = null,
                                                    payerTel = null,
                                                    payerName = null,
                                                    payerEmail = null,
                                                    dealerRate = null,
                                                    distRate = null,
                                                    number = null,
                                                    van = null,
                                                    vanId = null,
                                                    vanTrxId = null,
                                                    trackId = null,
                                                    issuerCode = null,
                                                    acquirerCode = null,
                                                    resultMsg = null
                                                )
                                            )
                                        }
                                    }
                                },
                            value = "취소"
                        )
                    }
                    BottomRowButton(
                        modifier = Modifier
                            .weight(1f)
                            .background(colorResource(id = R.color.teal_700)),
                        value = "PRINT"
                    )
                    BottomRowButton(
                        modifier = Modifier
                            .weight(1f)
                            .background(colorResource(id = R.color.blackbb)),
                        value = "문자\n영수증"
                    )
                    BottomRowButton(
                        modifier = Modifier
                            .weight(1f)
                            .background(colorResource(id = R.color.grey3)),
                        value = "이미지\n영수증"
                    )
                }
            }
        }
    }
}

@Composable
fun CompletePaymentContent(
    modifier: Modifier = Modifier,
    key: String,
    value: String,
    keyFontSize: TextUnit = 17.sp,
    valueFontSize: TextUnit = 17.sp
) {
    Column(
        modifier = Modifier
            .then(modifier)
    ) {
        Text(
            text = key,
            fontSize = keyFontSize,
            color = colorResource(id = R.color.grey2)
        )
        Text(
            text = value,
            fontSize = valueFontSize
        )
    }
}

@Composable
fun BottomRowButton(
    modifier: Modifier = Modifier,
    value: String
) {
    Column(
        modifier = Modifier
            .height(42.dp)
            .padding(start = 10.dp)
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text = value,
            lineHeight = 18.sp,
            color = colorResource(id = R.color.white),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationCompletePaymentView(title: String) {
    CenterAlignedTopAppBar(
        title = {
            Text(title, fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
            Icon(Icons.Default.ArrowBack, contentDescription = "Menu")
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CompletePaymentMainPreView() {
    CleanArchitech_text_0506Theme{
        CompletePaymentMainView(
            completePaymentViewVO = CompletePaymentViewVO(
                TransactionType.Offline,
                PaymentType.Approve,
                "00",
                "TX200316016511",
                "5409-26**-****-****",
                "1,806,004원",
                "2020-03-16 14:02:02",
                "30034798",
                "T200316016511" ,
                payerEmail = null,
                trackId = null,
                cardNumber = null,
                regDay = null,
                authCode = null,
                trxId = null,
            ),
            mainActivityViewModel = null,
            directPaymentViewModel = null,
            deviceCommunicationViewModel = null
        )
    }
}

