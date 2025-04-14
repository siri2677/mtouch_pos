package com.kwonps.mtouchpos.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kwonps.mtouchpos.R
import com.kwonps.mtouchpos.coordinator.PaymentHistoryCoordinator
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState
import com.kwonps.mtouchpos.view.ui.theme.TopNavigation
import com.kwonps.mtouchpos.viewmodel.PaymentHistoryVM
import com.kwonps.mtouchpos.viewmodel.PaymentHistoryVM.PaymentStatisticInfo
import com.kwonps.mtouchpos.viewmodel.PaymentHistoryVM.StatisticType
import com.kwonps.mtouchpos.vo.type.UseCaseResult
import kotlinx.coroutines.delay

@Composable
fun PaymentStatisticsView(
    navController: NavController,
    paymentHistoryViewModel: PaymentHistoryVM = hiltViewModel(),
    customPaymentPeriod: PaymentHistoryVM.PeriodInfo?
) {
    data class ButtonData(
        val text: String,
        val onClickAction: () -> Unit
    )

    val paymentPeriod = paymentHistoryViewModel.periodInfo.collectAsStateWithLifecycle().value
    val paymentStatisticInfo = paymentHistoryViewModel.paymentStatisticInfo.collectAsStateWithLifecycle(UseCaseResult.Init).value
    val buttons = paymentHistoryViewModel.run {
        listOf(
            ButtonData("오늘") {
                updatePeriodInfoAndFetchPaymentList(0)
                fetchPaymentStatistic()
            },
            ButtonData("1일") {
                updatePeriodInfoAndFetchPaymentList(1)
                fetchPaymentStatistic()
            },
            ButtonData("7일") {
                updatePeriodInfoAndFetchPaymentList(7)
                fetchPaymentStatistic()
            },
            ButtonData("직접설정") {
                navController.navigate(NavigationGraphState.PaymentHistoryView.Calendar.name)
            },
        )
    }

    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }

    PaymentHistoryCoordinator(navController).observePaymentStatisticInfoList(paymentStatisticInfo)

    LaunchedEffect(Unit) {
        delay(1)
        customPaymentPeriod?.let {
            if(selectedIndex == 3) {
                paymentHistoryViewModel.updatePeriodInfoAndFetchPaymentList(customPaymentPeriod)
                paymentHistoryViewModel.fetchPaymentList()
            }
        }
    }

    Scaffold(
        topBar = { TopNavigation("집계 내역", navController) }
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
            Selection(
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
                        onTap = { selectedIndex = index },
                    )
                }
            }
            ReactPaymentStatisticsInfo(paymentStatisticInfo)
        }
    }
}

@Composable
fun PaymentStatisticsContents(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(start = 30.dp, end = 30.dp)
            .then(modifier)
    ) {
        Text(
            textAlign = TextAlign.Left,
            text = title,
            fontSize = 20.sp,
            color = colorResource(id = R.color.black),
            fontFamily = FontFamily(Font(R.font.ns_acr)),
            modifier = Modifier.weight(1f)
        )
        Text(
            textAlign = TextAlign.Right,
            text = content,
            fontSize = 20.sp,
            color = colorResource(id = R.color.teal_700),
            fontFamily = FontFamily(Font(R.font.ns_acr)),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ReactPaymentStatisticsInfo(
    useCaseResult: UseCaseResult<HashMap<StatisticType, PaymentStatisticInfo>>
) {
    when(useCaseResult) {
        is UseCaseResult.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .paint(
                        painterResource(id = R.drawable.payment_box),
                        contentScale = ContentScale.FillBounds
                    )
            ) {
                PaymentStatisticsContents(
                    title = "승인건수",
                    content = "${useCaseResult.value.get(StatisticType.APPROVE)?.count}건",
                    modifier = Modifier.padding(top = 15.dp)
                )
                PaymentStatisticsContents(
                    title = "승인금액",
                    content = "${useCaseResult.value.get(StatisticType.APPROVE)?.amount}원",
                    modifier = Modifier.padding(top = 10.dp)
                )
                Divider(
                    color = colorResource(id = R.color.grey7),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 10.dp)
                )
                PaymentStatisticsContents(
                    title = "취소건수",
                    content = "${useCaseResult.value.get(StatisticType.CANCEL)?.count}건",
                    modifier = Modifier.padding(top = 10.dp)
                )
                PaymentStatisticsContents(
                    title = "취소금액",
                    content = "${useCaseResult.value.get(StatisticType.CANCEL)?.amount}원",
                    modifier = Modifier.padding(top = 10.dp)
                )
                Divider(
                    color = colorResource(id = R.color.grey7),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 10.dp)
                )
                PaymentStatisticsContents(
                    title = "합계건수",
                    content = "${useCaseResult.value.get(StatisticType.TOTAL)?.count}건",
                    modifier = Modifier.padding(top = 10.dp)
                )
                PaymentStatisticsContents(
                    title = "합계금액",
                    content = "${useCaseResult.value.get(StatisticType.TOTAL)?.amount}원",
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        is UseCaseResult.Error, is UseCaseResult.Exception, UseCaseResult.Init, UseCaseResult.Loading -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .paint(
                        painterResource(id = R.drawable.payment_box),
                        contentScale = ContentScale.FillBounds
                    )
            ) {
                PaymentStatisticsContents(
                    title = "승인건수",
                    content = "0건",
                    modifier = Modifier.padding(top = 15.dp)
                )
                PaymentStatisticsContents(
                    title = "승인금액",
                    content = "0원",
                    modifier = Modifier.padding(top = 10.dp)
                )
                Divider(
                    color = colorResource(id = R.color.grey7),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 10.dp)
                )
                PaymentStatisticsContents(
                    title = "취소건수",
                    content = "0건",
                    modifier = Modifier.padding(top = 10.dp)
                )
                PaymentStatisticsContents(
                    title = "취소금액",
                    content = "0원",
                    modifier = Modifier.padding(top = 10.dp)
                )
                Divider(
                    color = colorResource(id = R.color.grey7),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 10.dp)
                )
                PaymentStatisticsContents(
                    title = "합계건수",
                    content = "0건",
                    modifier = Modifier.padding(top = 10.dp)
                )
                PaymentStatisticsContents(
                    title = "합계금액",
                    content = "0원",
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}