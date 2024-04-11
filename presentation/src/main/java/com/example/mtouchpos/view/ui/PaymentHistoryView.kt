package com.example.mtouchpos.view.ui

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
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import com.example.mtouchpos.R
import com.example.mtouchpos.dto.PaymentPeriod
import com.example.mtouchpos.dto.ResponseFlowData
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.PaymentHistoryViewTab.*
import com.example.mtouchpos.view.ui.theme.MtouchPos
import com.example.mtouchpos.view.ui.theme.NavigationBundleKey
import com.example.mtouchpos.view.ui.theme.TopNavigation
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import java.io.Serializable

enum class PaymentHistoryViewTab(val value: String) : Serializable {
    Today("당일"), Yesterday("전일"), Week("7일"), Custom("직접설정")
}
@Composable
fun PaymentHistoryLazyColumn(
    navController: NavController,
    paymentList: ArrayList<ResponseFlowData.CreditPayment> = ArrayList()
) {
    LazyColumn(
        modifier = Modifier.fillMaxHeight()
    ) {
        item {
            paymentList.forEachIndexed { index, gridItem ->
                PaymentHistoryList(navController, gridItem)
            }
        }
    }
}

@Composable
fun CollectResponseTmsDTO(
    responseFlowData: ResponseFlowData,
    navController: NavController
) {
    when(responseFlowData) {
        is ResponseFlowData.CreditPaymentList -> {
            PaymentHistoryLazyColumn(
                navController = navController,
                paymentList = responseFlowData.list
            )
        }
        is ResponseFlowData.Init -> {
            PaymentHistoryLazyColumn(
                navController = navController
            )
        }
        is ResponseFlowData.Error -> {
            navController.navigate(
                route = NavigationGraphState.CommonView.ErrorDialog.name,
                bundle = bundleOf(NavigationBundleKey.message to responseFlowData.message),
                navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
            )
        }
        else -> { }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PaymentHistoryView(
    responseFlowData: ResponseFlowData,
    paymentPeriod: PaymentPeriod,
    navController: NavController = rememberNavController(),
    pagerState: PagerState = rememberPagerState(),
    pages: Array<PaymentHistoryViewTab> = PaymentHistoryViewTab.values(),
) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopNavigation("결제 내역")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        height = 2.dp,
                        color = colorResource(id = R.color.watermelon)
                    )
                }
            ) {
                pages.forEachIndexed { index, title ->
                    Tab(
                        text = {
                            Text(
                                text = title.value,
                                color = colorResource(id = R.color.black)
                            )
                        },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.scrollToPage(index) }
                        }
                    )
                }
            }
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { page ->
                Column(
                    modifier = Modifier.padding(page.dp),
                ) {
                    if(pages[pagerState.currentPage] == Custom) selection(paymentPeriod, navController)
                    CollectResponseTmsDTO(
                        responseFlowData = responseFlowData,
                        navController = navController,
                    )
                }
            }
        }
    }
}

@Composable
fun selection(
    paymentPeriod: PaymentPeriod,
    navController: NavController
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Column(
        modifier = Modifier
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
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
                        .clickable { navController.navigate(NavigationGraphState.PaymentHistoryView.Calendar.name) }
                )
            }
        }
    }
    Divider(
        color = colorResource(id = R.color.grey7),
        thickness = 1.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PaymentHistoryList(
    navController: NavController,
    creditPayment: ResponseFlowData.CreditPayment
) {
    data class ModifierSetting(
        val colorId: Int,
        val transactionDate: String
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable {
                navController?.navigate(
                    NavDestination
                        .createRoute(NavigationGraphState.PaymentHistoryView.PaymentHistoryDetail.name)
                        .hashCode(),
                    bundleOf("responseGetPaymentListBody" to creditPayment)
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val modifierSetting = when(creditPayment.trxResult) {
            "취소" -> {
                ModifierSetting(
                    colorId = R.color.red ,
                    transactionDate = creditPayment.rfdDay!! + creditPayment.rfdTime!!
                )
            }
            else -> {
                ModifierSetting(
                    colorId = R.color.teal_200 ,
                    transactionDate = creditPayment.regDay!! + creditPayment.regTime!!
                )
            }
        }

        Text(
            textAlign = TextAlign.Left,
            text = creditPayment.trxResult,
            fontSize = 13.sp,
            color = colorResource(id = modifierSetting.colorId),
            fontFamily = FontFamily(Font(R.font.ns_acr)),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 35.dp)
        )
        Row {
            Text(
                textAlign = TextAlign.Left,
                text = modifierSetting.transactionDate,
                fontSize = 10.sp,
                color = colorResource(id = R.color.black),
                fontFamily = FontFamily(Font(R.font.ns_acr)),
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp, start = 35.dp)
            )
            Text(
                textAlign = TextAlign.Right,
                text = creditPayment.authCd,
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
                text = creditPayment.brand,
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
                text = creditPayment.amount + " 원",
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


