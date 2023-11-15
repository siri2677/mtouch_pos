package com.example.cleanarchitech_text_0506.view.ui

import android.os.Build
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cleanarchitech_text_0506.R
import com.example.cleanarchitech_text_0506.enum.MainView
import com.example.cleanarchitech_text_0506.view.ui.PaymentHistoryViewTab.*
import com.example.cleanarchitech_text_0506.view.ui.theme.CleanArchitech_text_0506Theme
import com.example.cleanarchitech_text_0506.viewmodel.PaymentHistoryViewModel
import com.example.domain.dto.request.tms.RequestGetPaymentListDto
import com.example.domain.dto.response.tms.ResponseGetPaymentListBody
import com.example.domain.sealed.ResponseTmsAPI
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class PaymentHistoryViewTab(val value: String): Serializable {
    Today("당일"), Yesterday("전일"), Week("7일"), Selection("직접설정")
}

data class SearchPeriod(var startDay: String, var endDay: String)

sealed interface PaymentHistoryViewType {
    data class MainView(
        val searchPeriod: SearchPeriod
    ): PaymentHistoryViewType
    data class PreView(
        val value: ArrayList<ResponseGetPaymentListBody>
    ): PaymentHistoryViewType
}

fun getDay(days: Long): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    LocalDateTime.now().minusDays(days).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
} else {
    TODO("VERSION.SDK_INT < O")
}

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryView(
    paymentHistoryViewModel: PaymentHistoryViewModel? = hiltViewModel(),
    navHostController: NavHostController = rememberNavController(),
    paymentHistoryViewType: PaymentHistoryViewType,
) {
    val pages = PaymentHistoryViewTab.values()
    Scaffold(
        topBar = {
            topNavigation("결제 내역")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val pagerState = rememberPagerState()
            val coroutineScope = rememberCoroutineScope()
            var paymentHistoryViewTab by rememberSaveable { mutableStateOf(Today) }

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
                            paymentHistoryViewTab = pages[index]
                            coroutineScope.launch {
                                pagerState.scrollToPage(index)
                            }
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
                    when (paymentHistoryViewType) {
                        is PaymentHistoryViewType.MainView -> {
                            var paymentList by rememberSaveable { mutableStateOf(ArrayList<ResponseGetPaymentListBody>()) }
                            var searchPeriod = when (paymentHistoryViewTab) {
                                Today -> SearchPeriod(getDay(0), getDay(0))
                                Yesterday -> SearchPeriod(getDay(1), getDay(0))
                                Week -> SearchPeriod(getDay(7), getDay(0))
                                Selection -> paymentHistoryViewType.searchPeriod
                            }

                            selectionView(
                                pages[pagerState.currentPage],
                                navHostController,
                                searchPeriod
                            )
                            if (searchPeriod.startDay != "" && searchPeriod.endDay != "") {
                                LaunchedEffect(Unit) {
                                    paymentHistoryViewModel!!.getPaymentList(
                                        RequestGetPaymentListDto(
                                            startDay = searchPeriod.startDay,
                                            endDay = searchPeriod.endDay,
                                            lastRegTime = null,
                                            lastRegDay = null
                                        )
                                    )
                                }
                                paymentHistoryViewModel!!.responseTmsAPI.CollectAsEffect {
                                    when (it) {
                                        is ResponseTmsAPI.GetPaymentList -> paymentList =
                                            it.responseGetPaymentListDto.list

                                        is ResponseTmsAPI.ErrorMessage -> paymentList.clear()
                                        else -> {}
                                    }
                                }
                            }
                            paymentHistoryLazyColumn(navHostController, paymentList)
                        }

                        is PaymentHistoryViewType.PreView -> {
                            selectionView(
                                pages[pagerState.currentPage],
                                navHostController,
                                SearchPeriod("20231023", "20231023")
                            )
                            paymentHistoryLazyColumn(
                                navHostController,
                                paymentHistoryViewType.value
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun selectionView(
    paymentHistoryViewTab: PaymentHistoryViewTab,
    NavHostController: NavHostController,
    searchPeriod: SearchPeriod
) {
    if(paymentHistoryViewTab == Selection) {
        selection(searchPeriod, NavHostController)
    }
}

@Composable
fun selection(
    searchPeriod: SearchPeriod,
    NavHostController: NavHostController
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    var openCalendar by remember { mutableStateOf(false) }
    if(openCalendar) {
        openCalendar = false
        NavHostController.navigate(MainView.Calendar.name)
    }

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
                    text = searchPeriod.startDay,
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
                    text = searchPeriod.endDay,
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
                        .clickable { openCalendar = true }
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
fun paymentHistoryLazyColumn(
    NavHostController: NavHostController,
    paymentList: ArrayList<ResponseGetPaymentListBody>
) {
    LazyColumn(
        modifier = Modifier.fillMaxHeight()
    ) {
        item {
            paymentList.forEachIndexed { index, gridItem ->
                paymentHistoryList(NavHostController, gridItem)
            }
        }
    }
}

@Composable
fun paymentHistoryList(
    NavHostController: NavHostController,
    responseGetPaymentListBody: ResponseGetPaymentListBody
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable {
                NavHostController?.navigate(
                    NavDestination
                        .createRoute(MainView.PaymentHistoryDetail.name)
                        .hashCode(),
                    bundleOf("responseGetPaymentListBody" to responseGetPaymentListBody)
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var colorId by remember { mutableIntStateOf(R.color.red) }
        var transactionDate by remember { mutableStateOf("") }
        if(responseGetPaymentListBody.trxResult == "취소"){
            colorId = R.color.red
            transactionDate = responseGetPaymentListBody.rfdDay!! + responseGetPaymentListBody.rfdTime!!
        } else {
            colorId = R.color.teal_200
            transactionDate = responseGetPaymentListBody.regDay!! + responseGetPaymentListBody.regTime!!
        }
        Text(
            textAlign = TextAlign.Left,
            text = responseGetPaymentListBody.trxResult,
            fontSize = 13.sp,
            color = colorResource(id = colorId),
            fontFamily = FontFamily(Font(R.font.ns_acr)),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 35.dp)
        )
        Row {
            Text(
                textAlign = TextAlign.Left,
                text = transactionDate,
                fontSize = 10.sp,
                color = colorResource(id = R.color.black),
                fontFamily = FontFamily(Font(R.font.ns_acr)),
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp, start = 35.dp)
            )
            Text(
                textAlign = TextAlign.Right,
                text = responseGetPaymentListBody.authCd,
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
                text = responseGetPaymentListBody.brand,
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
                text = responseGetPaymentListBody.amount + " 원",
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

@Preview(showBackground = true)
@Composable
fun paymentHistorySelectionPreview() {
    CleanArchitech_text_0506Theme{
        val arrayList = ArrayList<ResponseGetPaymentListBody>()
        for (i in 1..10){
            arrayList.add(ResponseGetPaymentListBody(
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
            ))
        }
        PaymentHistoryView(
            null,
            paymentHistoryViewType = PaymentHistoryViewType.PreView(arrayList),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun paymentHistorySelectionPreview1() {
    CleanArchitech_text_0506Theme{
        val arrayList = ArrayList<ResponseGetPaymentListBody>()
        for (i in 1..10){
            arrayList.add(ResponseGetPaymentListBody(
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
            ))
        }
        PaymentHistoryView(
            null,
            paymentHistoryViewType = PaymentHistoryViewType.PreView(arrayList)
        )
    }
}


