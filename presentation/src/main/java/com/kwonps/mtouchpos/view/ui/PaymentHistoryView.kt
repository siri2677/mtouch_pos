package com.kwonps.mtouchpos.view.ui

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.kwonps.mtouchpos.R
import com.kwonps.mtouchpos.navigation.rememberPaymentHistoryRouter
import com.kwonps.mtouchpos.view.navgraph.NavigationBundleKey.Companion.RESPONSE_GET_PAYMENT_LIST
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState
import com.kwonps.mtouchpos.view.ui.theme.TopNavigation
import com.kwonps.mtouchpos.viewmodel.PaymentHistoryVM
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.type.PurchaseType
import com.kwonps.mtouchpos.vo.type.UseCaseResult
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
    paymentHistoryViewModel: PaymentHistoryVM = hiltViewModel(),
    navController: NavController,
    customPaymentPeriod: PaymentHistoryVM.PeriodInfo?
) {
    data class ButtonData(
        val text: String,
        val onClickAction: () -> Unit
    )

    val paymentPeriod = paymentHistoryViewModel.periodInfo.collectAsStateWithLifecycle().value
    val paymentHistoryInfo = paymentHistoryViewModel.paymentHistoryInfo.collectAsStateWithLifecycle(UseCaseResult.Init).value
    val paymentHistoryRouter = rememberPaymentHistoryRouter(navController)
    val buttons = paymentHistoryViewModel.run {
        listOf(
            ButtonData("오늘") {
                updatePeriodInfoAndFetchPaymentList(0)
                fetchPaymentList()
            },
            ButtonData("1일") {
                updatePeriodInfoAndFetchPaymentList(1)
                fetchPaymentList()
            },
            ButtonData("7일") {
                updatePeriodInfoAndFetchPaymentList(7)
                fetchPaymentList()
            },
            ButtonData("직접설정") {
                navController.navigate(NavigationGraphState.PaymentHistoryView.Calendar.name)
            },
        )
    }

    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }

    paymentHistoryRouter.observePaymentHistoryInfoList(paymentHistoryInfo)

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
                        onTap = { selectedIndex = index }
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
fun Selection(
    paymentHistoryViewModel: PaymentHistoryVM,
    paymentPeriod: PaymentHistoryVM.PeriodInfo
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
    onTap: () -> Unit,
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
                    route = NavigationGraphState.PaymentHistoryView.PaymentHistoryDetail.name,
                    bundle = bundleOf(RESPONSE_GET_PAYMENT_LIST to paymentDetailInfo),
                    navOptions = NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(
                        NavigationGraphState.PaymentHistoryView.PaymentHistory.name, false).build()
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val modifierData = when(paymentDetailInfo.purchaseType) {
            PurchaseType.APPROVE -> {
                ModifierData(
                    colorId = R.color.teal_200,
                    paymentType = "승인",
                    transactionDate = paymentDetailInfo.regDay
                )
            }

            PurchaseType.REFUND -> {
                ModifierData(
                    colorId = R.color.red,
                    paymentType = "취소",
                    transactionDate = paymentDetailInfo.regDay
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
                text = paymentDetailInfo.authCd,
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
                text = paymentDetailInfo.brand,
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

