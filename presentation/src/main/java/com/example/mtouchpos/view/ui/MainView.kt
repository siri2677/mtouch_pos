package com.example.mtouchpos.view.ui

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mtouchpos.R
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.theme.NavigationBundleKey
import com.example.mtouchpos.view.ui.theme.TopNavigationMain
import com.example.mtouchpos.viewmodel.MainActivityViewModel
import com.google.accompanist.flowlayout.FlowRow

enum class MainViewFunction {
    CreditCardPayment,
    ManualPayment,
    DeviceManagement,
    CashReceipt,
    TransactionHistory,
    AggregateSummary,
    NoticeAnnouncement
}

@Composable
fun MainView(
    navController: NavController,
    mainViewModel: MainActivityViewModel = hiltViewModel()
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Scaffold(
        bottomBar = {
            BottomNavigation(navController)
        },
        topBar = {
            TopNavigationMain()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if(mainViewModel.getUserInformationRepository().isEmpty()) {
                LoginStatus("")
            } else {
                LoginStatus(mainViewModel.getTmnId())
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    SalesAmount(screenWidth, "일간 매출 내역", "일간 취소 내역")
                    SalesAmount(screenWidth, "월간 매출 내역", "월간 취소 내역")
                    Announcement(screenWidth)
                }

                item {
                    FlowRow(
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        GridMenu(
                            clickEvent = {
                                if(mainViewModel.getUserInformationRepository().isEmpty()) {
                                    navController.navigate(
                                        route = NavigationGraphState.CommonView.ErrorDialog.name,
                                        bundle = bundleOf(NavigationBundleKey.message to "로그인 후 서비스 이용하시기 바랍니다"),
                                        navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                                    )
                                } else {
                                    navController.navigate(
                                        route = it,
                                        navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                                    )
                                }
                            },
                            mainActivityFunctionList = MainViewFunction.values()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Announcement(screenWidth: Int) {
    Column(
        modifier = Modifier
            .width((screenWidth * 0.9).dp)
            .wrapContentHeight()
            .padding(top = 5.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colorResource(id = R.color.grey7)) // 회색 배경
    ) {
        Column(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colorResource(id = R.color.white))
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.grey6),
                    shape = RectangleShape
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(13.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "최근 공지사항",
                    fontSize = 15.sp,
                    color = colorResource(id = R.color.black),
                )
                Row(
                    modifier = Modifier.padding(top = 5.dp)
                ) {
                    Text(
                        textAlign = TextAlign.Left,
                        text = "2023/07/04",
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.black),
                        fontFamily = FontFamily(Font(R.font.ns_acr)),
                        modifier = Modifier
                            .weight(0.3f)
                    )
                    Text(
                        textAlign = TextAlign.Right,
                        text = " 1.0.1 버전 업데이트 알림",
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.black),
                        fontFamily = FontFamily(Font(R.font.ns_acr)),
                        modifier = Modifier
                            .weight(0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun GridMenu(
    clickEvent: (String) -> Unit,
    mainActivityFunctionList: Array<MainViewFunction>,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp

    data class MainGridItem(
        val imageRes: Int = R.drawable.card_icon_main,
        val text: String,
        val mainView: NavigationGraphState
    )

    mainActivityFunctionList.forEachIndexed { index, mainActivityFunction ->
        val mainGridItem = when(mainActivityFunction) {
            MainViewFunction.CreditCardPayment -> MainGridItem(text = "신용결제", mainView = NavigationGraphState.CreditPaymentView.CreditPayment)
            MainViewFunction.ManualPayment -> MainGridItem(text = "수기결제", mainView = NavigationGraphState.DirectPaymentView.DirectPayment)
            MainViewFunction.DeviceManagement -> MainGridItem(text = "장치관리", mainView = NavigationGraphState.DeviceSettingView.Bluetooth)
            MainViewFunction.CashReceipt -> MainGridItem(text = "현금영수증", mainView = NavigationGraphState.DeviceSettingView.Bluetooth)
            MainViewFunction.TransactionHistory -> MainGridItem(text = "거래내역", mainView = NavigationGraphState.PaymentHistoryView.PaymentHistory)
            MainViewFunction.AggregateSummary -> MainGridItem(text = "집계내역", mainView = NavigationGraphState.PaymentHistoryView.PaymentHistory)
            MainViewFunction.NoticeAnnouncement -> MainGridItem(text = "공지사항", mainView = NavigationGraphState.PaymentHistoryView.PaymentHistory)
        }

        Column(
            modifier = Modifier
                .padding(top = 5.dp)
                .paint(painterResource(id = R.drawable.card_bt), contentScale = ContentScale.FillBounds)
                .width((screenWidth * 0.45).dp)
                .height(40.dp)
                .clickable(onClick = { clickEvent(mainGridItem.mainView.toString()) })
                .then(if (index % 2 == 0) Modifier.padding(start = 10.dp) else Modifier.padding(end = 10.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(mainGridItem.imageRes), contentDescription = null)
            Text(text = mainGridItem.text, fontSize = 12.sp)
        }
    }
}

@Composable
fun BottomNavigation(navController: NavController) {
    BottomNavigation(
        backgroundColor = colorResource(id = R.color.white)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        BottomNavigationItem(
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "login"
                )
            },
            label = {
                Text(
                    "로그인",
                    fontSize = 12.sp,
                )
            },
            selected = currentRoute == NavigationGraphState.HomeView.Login.name,
            onClick = { navController.navigate(NavigationGraphState.HomeView.Login.name) }
        )

        BottomNavigationItem(
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("설정", fontSize = 12.sp) },
            selected = currentRoute == "settings",
            onClick = { }
        )

        BottomNavigationItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.customer_service_center_icon),
                    contentDescription = "D",
                    modifier = Modifier
                        .size(22.5.dp)
                        .padding(bottom = 2.dp)
                )
            },
            label = {
                Text(
                    "고객센터",
                    fontSize = 12.sp,
                )
            },
            selected = currentRoute == NavigationGraphState.HomeView.Home.name,
            onClick = { }
        )
    }
}

@Composable
fun SalesAmount(
    screenWidth: Int,
    payment: String,
    refund: String,
) {
    Column(
        modifier = Modifier
            .width((screenWidth * 0.9).dp)
            .height(115.dp)
            .padding(top = 5.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colorResource(id = R.color.orange_pink),
                        colorResource(id = R.color.watermelon)
                    )
                )
            )
    ) {
        Text(
            textAlign = TextAlign.Left,
            text = payment,
            fontSize = 15.sp,
            color = colorResource(id = R.color.white),
            fontFamily = FontFamily(Font(R.font.ns_acr)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 20.dp)
        )
        Text(
            textAlign = TextAlign.Right,
            text = "0 원",
            fontSize = 30.sp,
            color = colorResource(id = R.color.white),
            fontFamily = FontFamily(Font(R.font.ns_acr)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 20.dp)
        )
        Divider(
            color = colorResource(id = R.color.white),
            thickness = 0.5.dp,
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 16.dp)
        )
        Row {
            Text(
                textAlign = TextAlign.Left,
                text = refund,
                fontSize = 15.sp,
                color = colorResource(id = R.color.white),
                fontFamily = FontFamily(Font(R.font.ns_acr)),
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 5.dp, start = 20.dp)
            )
            Text(
                textAlign = TextAlign.Right,
                text = "0 원",
                fontSize = 15.sp,
                color = colorResource(id = R.color.white),
                fontFamily = FontFamily(Font(R.font.ns_acr)),
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 5.dp, end = 20.dp)
            )
        }
    }
}

@Composable
fun LoginStatus(terminalId: String) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Column(
        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .width((screenWidth * 0.9).dp)
                .height(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            colorResource(id = R.color.orange_pink),
                            colorResource(id = R.color.watermelon)
                        )
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                textAlign = TextAlign.Left,
                text = "로그인 아이디",
                fontSize = 15.sp,
                color = colorResource(id = R.color.white),
                fontFamily = FontFamily(Font(R.font.ns_acr)),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp)
            )
            Text(
                textAlign = TextAlign.Right,
                text = if(terminalId == "") { "로그아웃 상태 입니다" } else { terminalId },
                fontSize = 15.sp,
                color = colorResource(id = R.color.white),
                fontFamily = FontFamily(Font(R.font.ns_acr)),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 20.dp)
            )
        }
    }
}

fun NavController.navigate(
    route: String = "",
    bundle: Bundle = Bundle(),
    navOptions: NavOptions? = null
) = navigate(NavDestination.createRoute(route).hashCode(), bundle, navOptions)

fun NavController.storeBundle(
    route: String = "",
    bundle: Bundle = Bundle(),
    navOptions: NavOptions? = null
) = navigate(NavDestination.createRoute(route).hashCode(), bundle, navOptions)



@Preview(showBackground = true)
@Composable
fun mainViewPreview() {
    val navController = rememberNavController()
//    MainActivity().mainView(navHostController = navController)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview7() {
//        DeviceSettingView().connectDeviceView(
//            viewModelFactory = viewModelFactory,
//            context = this@MainActivity,
//            owner = this@MainActivity
//        )
}