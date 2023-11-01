package com.example.cleanarchitech_text_0506.view.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cleanarchitech_text_0506.R
import com.example.cleanarchitech_text_0506.enum.PaymentType
import com.example.cleanarchitech_text_0506.view.ui.theme.CleanArchitech_text_0506Theme
import com.example.cleanarchitech_text_0506.viewmodel.DirectPaymentViewModel
import com.example.cleanarchitech_text_0506.viewmodel.MainActivityViewModel
import com.example.cleanarchitech_text_0506.vo.CompletePaymentViewVO
import com.example.domain.dto.request.pay.RequestDirectCancelPaymentDto
import com.example.domain.dto.response.tms.ResponseGetPaymentListBody
import com.example.domain.dto.response.tms.ResponseGetUserInformationDto

@Composable
fun paymentHistoryDetailMainView(
    navHostController: NavHostController,
    responseGetPaymentListBody: ResponseGetPaymentListBody,
    mainActivityViewModel: MainActivityViewModel = hiltViewModel(),
    directPaymentViewModel: DirectPaymentViewModel = hiltViewModel(),
) {
    paymentHistoryDetailView(
        navHostController,
        responseGetPaymentListBody,
        mainActivityViewModel,
        directPaymentViewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun paymentHistoryDetailView(
    navHostController: NavHostController = rememberNavController(),
    responseGetPaymentListBody: ResponseGetPaymentListBody,
    mainActivityViewModel: MainActivityViewModel?,
    directPaymentViewModel: DirectPaymentViewModel?
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val context = LocalContext.current

//    directPaymentViewModel?.responseDirectPayment?.CollectAsEffect(
//        block = {
//            when (it) {
//                is ResponsePayAPI.DirectCancelPaymentContent -> {
//                    val completePaymentViewVo = CompletePaymentViewVO(
//                        PaymentType.Refund,
//                        it.responseDirectCancelPaymentDto.refund?.trackId!!,
//                        responseGetPaymentListBody.number,
//                        it.responseDirectCancelPaymentDto.refund?.amount.toString(),
//                        it.responseDirectCancelPaymentDto.result.create,
//                        it.responseDirectCancelPaymentDto.refund?.authCd!!,
//                        it.responseDirectCancelPaymentDto.refund?.trxId!!
//                    )
//                    navHostController?.navigate(
//                        NavigationView.CompletePayment.name,
//                        bundleOf("responsePayAPI" to completePaymentViewVo),
//                        NavOptions.Builder().setLaunchSingleTop(true).build()
//                    )
//                    Toast.makeText(context, "결제 취소가 완료 되었습니다", Toast.LENGTH_LONG).show()
//                }
//                else -> {}
//            }
//        }
//    )

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
            loginStatus(mainActivityViewModel?.getUserInformation()?.tmnId.toString())
            Column(
                modifier = Modifier
                    .width((screenWidth * 0.9).dp)
                    .wrapContentHeight()
                    .paint(
                        painterResource(id = R.drawable.payment_box),
                        contentScale = ContentScale.FillBounds
                    )
            ) {
                paymentHistoryList(navHostController, responseGetPaymentListBody)

                Row(
                    modifier = Modifier
                        .padding(top = 20.dp, start = 35.dp)
                ){
                    CompletePaymentContent(
                        modifier = Modifier.weight(1f),
                        key = "할부기간",
                        value = responseGetPaymentListBody.installment,
                        keyFontSize = 14.sp,
                        valueFontSize = 15.sp,
                    )
                    CompletePaymentContent(
                        modifier = Modifier.weight(1f),
                        key = "카드번호",
                        value = responseGetPaymentListBody.number,
                        keyFontSize = 14.sp,
                        valueFontSize = 15.sp,
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(top = 20.dp, start = 35.dp)
                ){
                    CompletePaymentContent(
                        modifier = Modifier.weight(1f),
                        key = "승인일자",
                        value = responseGetPaymentListBody.regDay,
                        keyFontSize = 14.sp,
                        valueFontSize = 15.sp,
                    )
                    CompletePaymentContent(
                        modifier = Modifier.weight(1f),
                        key = "승인번호",
                        value = responseGetPaymentListBody.authCd,
                        keyFontSize = 14.sp,
                        valueFontSize = 15.sp,
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 20.dp, top = 30.dp, bottom = 20.dp)
                ) {
                    if (responseGetPaymentListBody.trxResult == "취소") {
                        bottomRowButton(
                            modifier = Modifier
                                .weight(1f)
                                .background(colorResource(id = R.color.red))
                                .clickable {
                                    directPaymentViewModel?.requestDirectCancelPayment(
                                        RequestDirectCancelPaymentDto(
                                            payKey = mainActivityViewModel?.getUserInformation()?.payKey!!,
                                            amount = responseGetPaymentListBody.amount,
                                            rootTrxId = responseGetPaymentListBody.trxId,
                                            rootTrxDay = responseGetPaymentListBody.regDay
                                        )
                                    )
                                },
                            value = "취소"
                        )
                    }
                    bottomRowButton(
                        modifier = Modifier
                            .weight(1f)
                            .background(colorResource(id = R.color.teal_700)),
                        value = "PRINT"
                    )
                    bottomRowButton(
                        modifier = Modifier
                            .weight(1f)
                            .background(colorResource(id = R.color.blackbb)),
                        value = "SMS"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentHistoryDetailPreView() {
    CleanArchitech_text_0506Theme{
        paymentHistoryDetailView(
            responseGetPaymentListBody = ResponseGetPaymentListBody(
                rfdTime = "151309",
                amount = "11004",
                van = "KSPAY3",
                vanTrxId = "186950124096",
                authCd = "60446422",
                tmnId = "test0003",
                trackId = "TX_1697695965270",
                bin = "448125",
                cardType = "체크",
                trxId = "T231019667958",
                issuer = "비씨",
                regDay = "20231019",
                resultMsg = "정상승인",
                number = "448125**********",
                trxResult = "취소",
                regTime = "151257",
                vanId = "2006500004",
                _idx = "1",
                installment = "00",
                rfdDay = "20231019",
                mchtId = "ktest",
                brand = "하나비씨체크",
                rfdId = "T231019667960"
            ),
            mainActivityViewModel = null,
            directPaymentViewModel = null
        )
    }
}

