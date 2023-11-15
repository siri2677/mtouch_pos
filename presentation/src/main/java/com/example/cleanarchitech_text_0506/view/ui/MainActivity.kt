package com.example.cleanarchitech_text_0506.view.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.example.cleanarchitech_text_0506.R
import com.example.cleanarchitech_text_0506.enum.MainView
import com.example.cleanarchitech_text_0506.enum.MainViewFunction
import com.example.cleanarchitech_text_0506.enum.SerialCommunicationUsbDialogData
import com.example.cleanarchitech_text_0506.sealed.DeviceConnectSharedFlow
import com.example.cleanarchitech_text_0506.view.ui.theme.CleanArchitech_text_0506Theme
import com.example.cleanarchitech_text_0506.viewmodel.MainActivityViewModel
import com.example.cleanarchitech_text_0506.viewmodel.DeviceCommunicationViewModel
import com.example.cleanarchitech_text_0506.vo.CompletePaymentViewVO
import com.example.domain.dto.response.tms.ResponseGetPaymentListBody
import com.google.accompanist.flowlayout.FlowRow
import com.kizitonwose.calendar.sample.compose.CalendarView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CleanArchitech_text_0506Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = MainView.Main.name,
                    ) {
                        composable(MainView.Main.name) {
                            Log.w("Main.name", "Main.name")
                            mainView(navHostController = navController)
                        }
                        dialog(MainView.Login.name) {
                            LoginView().loginDialog(navController)
                        }
                        dialog(MainView.PgIdLogin.name) {
                            LoginView().pgIdLoginDialog(navController)
                        }
                        dialog(MainView.VanIdLogin.name) {
                            LoginView().vanIdLoginDialog(navHostController = navController)
                        }
                        dialog(MainView.RegisteredId.name) {
                            LoginView().registeredIdDialog(navHostController = navController)
                        }
                        composable(MainView.DeviceSettingBluetooth.name) { backStackEntry ->
                            Log.w("DeviceSettingBluetooth", "DeviceSettingBluetooth")
                            DeviceSettingView().bluetoothDevice(
                                context = this@MainActivity,
                                owner = this@MainActivity,
                                navHostController = navController
                            )
                        }
                        composable(MainView.DeviceSettingUSB.name) {
                            DeviceSettingView().usbDevice(
                                context = this@MainActivity,
                                navHostController = navController
                            )
                        }
                        composable(MainView.CreditPayment.name) { backStackEntry ->
                            CreditPaymentView().creditPaymentView(
                                navHostController = navController,
                            )
                        }
                        dialog(MainView.CreditPaymentUsbDialog.name) { backStackEntry ->
                            val deviceCommunicationViewModel =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    backStackEntry.arguments?.getSerializable(
                                        SerialCommunicationUsbDialogData.ViewModel.name,
                                        DeviceConnectSharedFlow::class.java
                                    )!!
                                } else {
                                    backStackEntry.arguments?.getSerializable(
                                        SerialCommunicationUsbDialogData.ViewModel.name
                                    )!!
                                } as DeviceCommunicationViewModel

                            val deviceConnectSharedFlow =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    backStackEntry.arguments?.getSerializable(
                                        SerialCommunicationUsbDialogData.DeviceConnectSharedFlow.name,
                                        DeviceConnectSharedFlow::class.java
                                    )!!
                                } else {
                                    backStackEntry.arguments?.getSerializable(
                                        SerialCommunicationUsbDialogData.DeviceConnectSharedFlow.name
                                    )!!
                                } as DeviceConnectSharedFlow

                            CreditPaymentView().usbDevicePaymentDialog(
                                navHostController = navController,
                                deviceCommunicationViewModel = deviceCommunicationViewModel,
                                deviceConnectSharedFlow = deviceConnectSharedFlow

                            )
                        }
                        composable(MainView.DirectPayment.name) {
                            DirectPaymentView(
                                navHostController = navController
                            )
                        }
                        composable(MainView.CompletePayment.name) { backStackEntry ->
                            val completePaymentViewVO =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    backStackEntry.arguments?.getSerializable(
                                        "responsePayAPI",
                                        CompletePaymentViewVO::class.java
                                    )!!
                                } else {
                                    backStackEntry.arguments?.getSerializable(
                                        "responsePayAPI"
                                    )!!
                                } as CompletePaymentViewVO
                            CompletePaymentView(
                                navHostController = navController,
                                completePaymentViewVO
                            )
                        }
                        composable(MainView.PaymentHistory.name) { backStackEntry ->
//                            val startDay: String
//                            val endDay: String
//                            val tab = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                                backStackEntry.arguments?.getSerializable(
//                                    "tab",
//                                    PaymentHistoryViewTopTab::class.java
//                                )!!
//                            } else {
//                                backStackEntry.arguments?.getSerializable(
//                                    "tab"
//                                )!!
//                            } as PaymentHistoryViewTopTab
//
//                            when(tab) {
//                                PaymentHistoryViewTopTab.Today -> {
//                                    startDay = getDay(0)
//                                    endDay = getDay(0)
//                                }
//                                PaymentHistoryViewTopTab.Week -> {
//                                    startDay = getDay(1)
//                                    endDay = getDay(0)
//                                }
//                                PaymentHistoryViewTopTab.Selection -> {
//                                    startDay = getDay(7)
//                                    endDay = getDay(0)
//                                }
//                                PaymentHistoryViewTopTab.Yesterday -> {
//                                    startDay = backStackEntry.arguments?.getString("startDate")?: ""
//                                    endDay = backStackEntry.arguments?.getString("endDate")?: ""
//                                }
                                PaymentHistoryView(
                                    navHostController = navController,
                                    paymentHistoryViewType = PaymentHistoryViewType.MainView(
                                        SearchPeriod(backStackEntry.arguments?.getString("startDate")?: "", backStackEntry.arguments?.getString("endDate")?: "")
                                    )
                                )
                        }
//                            paymentHistoryViewToday(
//                                paymentHistoryViewTopTab = tab,
//                                navHostController = navController,
//                                startDay = startDay,
//                                endDay = endDay
//                            )

//                        composable(PaymentHistoryViewTopTab.Yesterday.name) {
//                            paymentHistoryViewYesterday(
//                                navHostController = navController
//                            )
//                        }
//                        composable(PaymentHistoryViewTopTab.Week.name) {
//                            paymentHistoryViewWeek(
//                                NavHostController = navController
//                            )
//                        }
//                        composable(PaymentHistoryViewTopTab.Selection.name) { backStackEntry ->
//                            paymentHistoryViewSelection(
//                                NavHostController = navController,
//                                startDay = backStackEntry.arguments?.getString("startDate")?: "",
//                                endDay = backStackEntry.arguments?.getString("endDate")?: "",
//                            )
//                        }
                        composable(MainView.PaymentHistoryDetail.name) { backStackEntry ->
                            val responseGetPaymentListBody =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    backStackEntry.arguments?.getSerializable(
                                        "responseGetPaymentListBody",
                                        ResponseGetPaymentListBody::class.java
                                    )!!
                                } else {
                                    backStackEntry.arguments?.getSerializable(
                                        "responseGetPaymentListBody"
                                    )!!
                                } as ResponseGetPaymentListBody
                            paymentHistoryDetailMainView(
                                navHostController = navController,
                                responseGetPaymentListBody = responseGetPaymentListBody
                            )
                        }
                        composable(MainView.Calendar.name) {
                            CalendarView(
                                close = { navController.popBackStack() },
                                dateSelected = { startDate, endDate ->
                                    navController?.navigate(
                                        MainView.PaymentHistory.name,
                                        bundleOf(
                                            "startDate" to startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                                            "endDate" to endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                        ),
                                        NavOptions.Builder().setLaunchSingleTop(true).setPopUpTo(MainView.Calendar.name, true).build()
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun mainView(
        navHostController: NavHostController,
        mainViewModel: MainActivityViewModel = hiltViewModel()
    ) {
//        LaunchedEffect(Unit) {
//            mainViewModel.login(
//                RequestGetUserInformationDto(
//                    mchtId = "ktest",
//                    tmnId = "test0005",
//                    serial = "12345"
//                )
//            )
//        }
        val screenWidth = LocalConfiguration.current.screenWidthDp
        Scaffold(
            bottomBar = {
                bottomNavigation(navHostController)
            },
            topBar = {
                topNavigation()
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                loginStatus(mainViewModel.getUserInformation().tmnId!!)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item {
                        dailySalesAmount(screenWidth)
                        monthlySalesAmount(screenWidth)
                        announcement(screenWidth)
                    }

                    item {
                        val gridItems = MainViewFunction.values()
                        FlowRow(
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            gridMenu(
                                screenWidth = screenWidth,
                                mainActivityFunctionList = gridItems,
                                navHostController = navHostController
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun announcement(screenWidth: Int) {
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

    private fun gridItemSetting(type: MainViewFunction): MainGridItem {
        return when (type) {
            MainViewFunction.CreditCardPayment ->
                return MainGridItem(
                    R.drawable.card_icon_main,
                    "신용결제",
                    MainView.CreditPayment
                )
            MainViewFunction.ManualPayment ->
                return MainGridItem(
                    R.drawable.card_icon_main,
                    "수기결제",
                    MainView.DirectPayment
                )
            MainViewFunction.DeviceManagement ->
                return MainGridItem(
                    R.drawable.card_icon_main,
                    "장치관리",
                    MainView.DeviceSettingBluetooth
                )
            MainViewFunction.CashReceipt ->
                return MainGridItem(
                    R.drawable.card_icon_main,
                    "현금영수증",
                    MainView.Main
                )
            MainViewFunction.TransactionHistory -> {
                return MainGridItem(
                    R.drawable.card_icon_main,
                    "거래내역",
                    MainView.PaymentHistory
                )
            }
            MainViewFunction.AggregateSummary ->
                return MainGridItem(
                    R.drawable.card_icon_main,
                    "집계내역",
                    MainView.Main
                )
            MainViewFunction.NoticeAnnouncement ->
                return MainGridItem(
                    R.drawable.card_icon_main,
                    "공지사항",
                    MainView.Main
                )
        }
    }

    @Composable
    fun gridMenu(
        screenWidth: Int,
        mainActivityFunctionList: Array<MainViewFunction>,
        navHostController: NavHostController
    ) {
        mainActivityFunctionList.forEachIndexed { index, mainActivityFunctionList ->
            val mainGridItem = gridItemSetting(mainActivityFunctionList)
            if (index % 2 == 0) {
                Column(
                    modifier = Modifier
                        .padding(top = 5.dp, start = 10.dp)
                        .paint(
                            painterResource(id = R.drawable.card_bt),
                            contentScale = ContentScale.FillBounds
                        )
                        .width((screenWidth * 0.45).dp)
                        .height(40.dp)
                        .clickable(onClick = {
                            navHostController.navigate(
                                mainGridItem.mainView.name,
                                NavOptions
                                    .Builder()
                                    .setLaunchSingleTop(true)
                                    .build()
                            )
                        }),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(mainGridItem!!.imageRes),
                        contentDescription = null
                    )
                    Text(
                        text = mainGridItem!!.text,
                        fontSize = 12.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(top = 5.dp, end = 10.dp)
                        .paint(
                            painterResource(id = R.drawable.card_bt),
                            contentScale = ContentScale.FillBounds
                        )
                        .width((screenWidth * 0.45).dp)
                        .height(40.dp)
                        .clickable(onClick = {
                            navHostController.navigate(
                                mainGridItem.mainView.name,
                                NavOptions
                                    .Builder()
                                    .setLaunchSingleTop(true)
                                    .build()
                            )
                        }),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(mainGridItem!!.imageRes),
                        contentDescription = null
                    )
                    Text(
                        text = mainGridItem!!.text,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun topNavigation() {
        CenterAlignedTopAppBar(
            title = {
                Image(
                    painter = painterResource(id = R.drawable.logo3),
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
            },
            navigationIcon = {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        )
    }

    @Composable
    fun bottomNavigation(navHostController: NavHostController) {
        BottomNavigation(
            backgroundColor = colorResource(id = R.color.white)
        ) {
            val navBackStackEntry by navHostController.currentBackStackEntryAsState()
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
                selected = currentRoute == MainView.Login.name,
                onClick = { navHostController.navigate(MainView.Login.name) }
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
                selected = currentRoute == MainView.Main.name,
                onClick = { }
            )
        }
    }

    @Composable
    fun dailySalesAmount(screenWidth: Int) {
        salesAmount(screenWidth, "일간 매출 내역", "일간 취소 내역")
    }

    @Composable
    fun monthlySalesAmount(screenWidth: Int) {
        salesAmount(screenWidth, "월간 매출 내역", "월간 취소 내역")
    }

    @Composable
    fun salesAmount(
        screenWidth: Int,
        payment: String,
        refund: String,
        mainActivityViewModel: MainActivityViewModel = hiltViewModel()
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

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview7() {
//        DeviceSettingView().connectDeviceView(
//            viewModelFactory = viewModelFactory,
//            context = this@MainActivity,
//            owner = this@MainActivity
//        )
    }

}

fun NavController.navigate(route: String, bundle: Bundle = Bundle(), navOptions: NavOptions?) {
    navigate(NavDestination.createRoute(route).hashCode(), bundle, navOptions)
}

@Composable
fun <T> Flow<T>.CollectAsEffect(
    context: CoroutineContext = EmptyCoroutineContext,
    block: (T) -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        onEach(block).flowOn(context).launchIn(this)
    }
}

@Composable
fun loginStatus(
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

@Preview(showBackground = true)
@Composable
fun mainViewPreview() {
    val navController = rememberNavController()
//    MainActivity().mainView(navHostController = navController)
}
