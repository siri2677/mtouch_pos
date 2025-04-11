package com.example.mtouchpos.view.ui

import android.annotation.SuppressLint
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.mtouchpos.view.navgraph.NavigationBundleKey
import com.example.mtouchpos.view.ui.theme.TopNavigationMain
import com.example.mtouchpos.view.util.SelectDialog
import com.example.mtouchpos.viewmodel.LoginVM
import com.google.accompanist.flowlayout.FlowRow

@Composable
fun MainView(
    navController: NavController,
    mainViewModel: LoginVM = hiltViewModel()
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
        Surface(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues),
            color = Color.Transparent
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LoginStatus(
                    navController,
                    mainViewModel.fetchCurrentConnectedUserInfo()?.tmnId ?: "로그아웃 상태 입니다"
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item {
                        SalesAmount(screenWidth, "일간 매출 내역", "일간 취소 내역")
                        SalesAmount(screenWidth, "월간 매출 내역", "월간 취소 내역")
//                        Announcement(screenWidth)
                    }

                    item {
                        FlowRow(
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            GridMenu(
                                clickEvent = { navigationGraphState ->
                                    mainViewModel.fetchCurrentConnectedUserInfo()?.let {
                                        navController.navigate(
                                            route = navigationGraphState,
                                            navOptions = NavOptions.Builder().setLaunchSingleTop(true)
                                                .setPopUpTo(NavigationGraphState.HomeView.Home.name, false).build()
                                        )
                                    } ?: navController.navigate(
                                        route = NavigationGraphState.CommonView.MessageDialog.name,
                                        bundle = bundleOf(
                                            NavigationBundleKey.MESSAGE to SelectDialog(
                                                initValue = "로그인 후 서비스 이용하시기 바랍니다"
                                            )
                                        ),
                                        navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                                    )
                                }
                            )
                        }
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
fun GridMenu(clickEvent: (String) -> Unit) {
    val screenWidth = LocalConfiguration.current.screenWidthDp

    data class MainGridItem(
        val imageRes: Int = R.drawable.card_icon_main,
        val text: String,
        val navigationGraphState: NavigationGraphState
    )

    val mainGridItems = listOf(
        MainGridItem(imageRes = R.drawable.card_payment, text = "신용결제", navigationGraphState = NavigationGraphState.CreditPaymentView.CreditPayment),
        MainGridItem(imageRes = R.drawable.card_sugi, text = "수기결제", navigationGraphState = NavigationGraphState.DirectPaymentView.DirectPayment),
        MainGridItem(imageRes = R.drawable.device_srtting, text = "장치관리", navigationGraphState = NavigationGraphState.DeviceSettingView.Bluetooth),
        MainGridItem(imageRes = R.drawable.card_cash, text = "현금영수증", navigationGraphState = NavigationGraphState.DeviceSettingView.Bluetooth),
        MainGridItem(imageRes = R.drawable.history, text = "거래내역", navigationGraphState = NavigationGraphState.PaymentHistoryView.PaymentHistory),
        MainGridItem(imageRes = R.drawable.history_statistic, text = "집계내역", navigationGraphState = NavigationGraphState.PaymentHistoryView.PaymentStatistic)
    )

    mainGridItems.forEachIndexed { index, mainGridItem ->
        Column(
            modifier = Modifier
                .padding(top = 5.dp)
                .paint(painterResource(id = R.drawable.card_bt), contentScale = ContentScale.FillBounds)
                .width((screenWidth * 0.45).dp)
                .height(40.dp)
                .clickable(onClick = { clickEvent(mainGridItem.navigationGraphState.toString()) }),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.width(60.dp)
                    .height(60.dp),
                painter = painterResource(mainGridItem.imageRes),
                contentDescription = null
            )
            Text(
                text = mainGridItem.text,
                fontSize = 12.sp
            )
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
            label = { Text("로그인", fontSize = 12.sp) },
            selected = currentRoute == NavigationGraphState.HomeView.PgIdLogin.name,
            onClick = { navController.navigate(NavigationGraphState.HomeView.PgIdLogin.name) }
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
fun LoginStatus(
    navController: NavController,
    terminalId: String
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Column(
        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .width((screenWidth * 0.9).dp)
                .height(30.dp)
                .clickable(onClick = { navController.navigate(NavigationGraphState.HomeView.RegisteredId.name) })
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
                text = terminalId,
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

@SuppressLint("RestrictedApi")
fun NavController.navigate(
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