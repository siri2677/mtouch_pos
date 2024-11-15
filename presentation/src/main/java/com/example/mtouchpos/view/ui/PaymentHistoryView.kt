package com.example.mtouchpos.view.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.OutlinedButton
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.mtouchpos.R
import com.example.mtouchpos.coordinator.PaymentHistoryCoordinator
import com.example.mtouchpos.view.navgraph.NavigationBundleKey.Companion.RESPONSE_GET_PAYMENT_LIST
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.theme.MtouchPos
import com.example.mtouchpos.view.ui.theme.TopNavigation
import com.example.mtouchpos.viewmodel.PaymentHistoryViewModel
import com.example.mtouchpos.vo.data.ApprovedPaymentType
import com.example.mtouchpos.vo.type.PurchaseType
import com.example.mtouchpos.vo.type.UseCaseResult
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.delay


@Composable
fun PaymentHistoryLazyColumn(
    navController: NavController,
    paymentList: List<ApprovedPaymentType.PaymentHistoryViewInfo> = ArrayList()
) {
    LazyColumn(
        modifier = Modifier.fillMaxHeight()
    ) {
        item {
            paymentList.forEach { gridItem ->
                PaymentHistoryList(navController, gridItem)
            }
        }
    }
}

@Composable
fun ReactPaymentHistoryData(
    paymentHistoryData: UseCaseResult<List<ApprovedPaymentType.PaymentHistoryViewInfo>>,
    navController: NavController
) {
    when(paymentHistoryData) {
        is UseCaseResult.Success -> {
            PaymentHistoryLazyColumn(
                navController = navController,
                paymentList = paymentHistoryData.value
            )
        }

        is UseCaseResult.Error, is UseCaseResult.Exception, UseCaseResult.Init, UseCaseResult.Loading -> {
            PaymentHistoryLazyColumn(
                navController = navController
            )
        }
    }
}

@Composable
fun PaymentHistoryView(
    paymentHistoryViewModel: PaymentHistoryViewModel = hiltViewModel(),
    navController: NavController,
    customPaymentPeriod: PaymentHistoryViewModel.PeriodInfo?
) {
    data class ButtonData(
        val text: String,
        val onClickAction: () -> Unit
    )

    val paymentPeriod = paymentHistoryViewModel.periodInfo.collectAsStateWithLifecycle().value
    val paymentHistoryInfo = paymentHistoryViewModel.paymentHistoryInfo.collectAsStateWithLifecycle(UseCaseResult.Init).value
    val buttons = paymentHistoryViewModel.run {
        listOf(
            ButtonData("오늘") {
                updatePeriodInfoAndFetchPaymentList(0)
            },
            ButtonData("1일") {
                updatePeriodInfoAndFetchPaymentList(1)
            },
            ButtonData("7일") {
                updatePeriodInfoAndFetchPaymentList(7)
            },
            ButtonData("직접설정") {
                navController.navigate(NavigationGraphState.PaymentHistoryView.Calendar.name)
            },
        )
    }

    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }

    PaymentHistoryCoordinator(navController).observePaymentHistoryInfoList(paymentHistoryInfo)

    LaunchedEffect(Unit) {
        delay(1)
        customPaymentPeriod?.let { paymentHistoryViewModel.updatePeriodInfoAndFetchPaymentList(it) }
    }

    Scaffold(
        topBar = { TopNavigation("결제 내역", navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .drawBehind {
                    drawLine(
                        Color.LightGray, Offset(0f, 0f), Offset(size.width, 0f), 2 * density
                    )
                }
        ) {
            selection(
                paymentHistoryViewModel = paymentHistoryViewModel,
                paymentPeriod = paymentPeriod
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                buttons.forEachIndexed { index, buttonData ->
                    DateSelectButton(
                        text = buttonData.text,
                        clickEvent = buttonData.onClickAction,
                        isSelected = selectedIndex == index,
                        isFirst = index,
                        onTap = { selectedIndex = index },
                        paymentHistoryViewModel = paymentHistoryViewModel
                    )
                }
            }
            ReactPaymentHistoryData(
                paymentHistoryData = paymentHistoryInfo,
                navController = navController
            )
        }
    }
}

@Composable
fun selection(
    paymentHistoryViewModel: PaymentHistoryViewModel,
    paymentPeriod: PaymentHistoryViewModel.PeriodInfo
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Column(
        modifier = Modifier.padding(top = 15.dp, bottom = 5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .width((screenWidth * 0.4).dp)
                    .height(40.dp)
                    .border(
                        width = 1.dp,
                        color = colorResource(id = R.color.watermelon)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = paymentPeriod.startDay,
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.ns_acr)),
                    lineHeight = 15.sp
                )
            }
            Divider(
                modifier = Modifier
                    .width((screenWidth * 0.05).dp)
                    .padding(start = 5.dp, end = 5.dp),
                color = colorResource(id = R.color.watermelon),
                thickness = 1.dp,
            )
            Column(
                modifier = Modifier
                    .width((screenWidth * 0.4).dp)
                    .height(40.dp)
                    .border(
                        width = 1.dp,
                        color = colorResource(id = R.color.watermelon)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = paymentPeriod.endDay,
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.ns_acr)),
                    lineHeight = 15.sp
                )
            }
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .background(color = colorResource(id = R.color.watermelon)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.search_icon),
                    contentDescription = "Image",
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                        .clickable { paymentHistoryViewModel.fetchPaymentList() }
                )
            }
        }
    }
}

@Composable
fun DateSelectButton(
    text: String,
    clickEvent: () -> Unit,
    isSelected: Boolean,
    isFirst: Int,
    onTap: () -> Unit,
    paymentHistoryViewModel: PaymentHistoryViewModel
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val borderColor = colorResource(id = if (isSelected) R.color.black else R.color.grey4)

    OutlinedButton(
        onClick = {
            onTap()
            clickEvent()
        },
        modifier = Modifier
            .width((screenWidth * 0.24).dp),
//            .offset(x = (isFirst * (-1)).dp),
        shape = RectangleShape,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(R.font.ns_acr)),
            color = borderColor,
            text = text
        )
    }
}

@Composable
fun PaymentHistoryList(
    navController: NavController,
    paymentDetailInfo: ApprovedPaymentType.PaymentHistoryViewInfo
) {
    data class ModifierData(
        val colorId: Int,
        val paymentType: String,
        val transactionDate: String
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable {
                navController.navigate(
                    NavigationGraphState.PaymentHistoryView.PaymentHistoryDetail.name,
                    bundleOf(RESPONSE_GET_PAYMENT_LIST to paymentDetailInfo)
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val modifierData = when(paymentDetailInfo.purchaseType) {
            PurchaseType.REFUND -> {
                ModifierData(
                    colorId = R.color.red,
                    paymentType = "취소",
                    transactionDate = paymentDetailInfo.authDate
                )
            }

            PurchaseType.APPROVE -> {
                ModifierData(
                    colorId = R.color.teal_200,
                    paymentType = "승인",
                    transactionDate = paymentDetailInfo.authDate
                )
            }
        }

        Text(
            textAlign = TextAlign.Left,
            text = modifierData.paymentType,
            fontSize = 13.sp,
            color = colorResource(id = modifierData.colorId),
            fontFamily = FontFamily(Font(R.font.ns_acr)),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 35.dp)
        )

        Row {
            Text(
                textAlign = TextAlign.Left,
                text = modifierData.transactionDate,
                fontSize = 10.sp,
                color = colorResource(id = R.color.black),
                fontFamily = FontFamily(Font(R.font.ns_acr)),
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp, start = 35.dp)
            )
            Text(
                textAlign = TextAlign.Right,
                text = paymentDetailInfo.authCode,
                fontSize = 10.sp,
                color = colorResource(id = R.color.teal_700),
                fontFamily = FontFamily(Font(R.font.ns_acr)),
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp, end = 35.dp)
            )
        }

        Row {
            Text(
                textAlign = TextAlign.Left,
                text = paymentDetailInfo.issuerName ?: "",
                fontSize = 13.sp,
                color = colorResource(id = R.color.black),
                fontFamily = FontFamily(Font(R.font.ns_acr)),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp, start = 35.dp)
            )
            Text(
                textAlign = TextAlign.Right,
                text = paymentDetailInfo.amount + " 원",
                fontSize = 16.sp,
                color = colorResource(id = R.color.black),
                fontFamily = FontFamily(Font(R.font.ns_acr)),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp, end = 35.dp)
            )
        }
    }

    Divider(
        color = colorResource(id = R.color.grey7),
        thickness = 1.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalPagerApi::class)
@Preview(showBackground = true)
@Composable
fun paymentHistorySelectionPreview() {
    MtouchPos {
//        val arrayList = ArrayList<ResponseTmsDTO.PaymentContents>()
//        for (i in 1..10) {
//            arrayList.add(
//                ResponseTmsDTO.PaymentContents(
//                    rfdTime = "151309",
//                    amount = "11004",
//                    van = "KSPAY3",
//                    vanTrxId = "186950124096",
//                    authCd = "60446422",
//                    tmnId = "test0003",
//                    trackId = "TX_1697695965270",
//                    bin = "448125",
//                    cardType = "체크",
//                    trxId = "T231019667958",
//                    issuer = "비씨",
//                    regDay = "20231019",
//                    resultMsg = "정상승인",
//                    number = "448125**********",
//                    trxResult = "취소",
//                    regTime = "151257",
//                    vanId = "2006500004",
//                    _idx = "1",
//                    installment = "00",
//                    rfdDay = "20231019",
//                    mchtId = "ktest",
//                    brand = "하나비씨체크",
//                    rfdId = "T231019667960"
//                )
//            )
//        }

//        PaymentHistoryView(
//            responseDTO = ResponseTmsDTO.GetPaymentList(
//                result = "정상",
//                list = arrayList
//            )
//        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Preview(showBackground = true)
@Composable
fun paymentHistorySelectionPreview1() {
    MtouchPos {
//        val arrayList = ArrayList<ResponseTmsDTO.PaymentContents>()
//        for (i in 1..10) {
//            arrayList.add(
//                ResponseTmsDTO.PaymentContents(
//                    rfdTime = "151309",
//                    amount = "11004",
//                    van = "KSPAY3",
//                    vanTrxId = "186950124096",
//                    authCd = "60446422",
//                    tmnId = "test0003",
//                    trackId = "TX_1697695965270",
//                    bin = "448125",
//                    cardType = "체크",
//                    trxId = "T231019667958",
//                    issuer = "비씨",
//                    regDay = "20231019",
//                    resultMsg = "정상승인",
//                    number = "448125**********",
//                    trxResult = "취소",
//                    regTime = "151257",
//                    vanId = "2006500004",
//                    _idx = "1",
//                    installment = "00",
//                    rfdDay = "20231019",
//                    mchtId = "ktest",
//                    brand = "하나비씨체크",
//                    rfdId = "T231019667960"
//                )
//            )
//        }
//        PaymentHistoryView(
//            responseDTO = ResponseTmsDTO.GetPaymentList(
//                result = "정상",
//                list = arrayList
//            )
//        )
    }
}


