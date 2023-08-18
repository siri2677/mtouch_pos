package com.example.cleanarchitech_text_0506.view.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.example.cleanarchitech_text_0506.R
import com.example.cleanarchitech_text_0506.enum.DeviceConnectView
import com.example.cleanarchitech_text_0506.enum.MainViewFunction
import com.example.cleanarchitech_text_0506.enum.SerialCommunicationUsbDialogData
import com.example.cleanarchitech_text_0506.util.NavigationView
import com.example.cleanarchitech_text_0506.view.ui.theme.CleanArchitech_text_0506Theme
import com.example.cleanarchitech_text_0506.viewmodel.BluetoothViewModel
import com.example.cleanarchitech_text_0506.viewmodel.DeviceSerialCommunicateViewModel
import com.example.cleanarchitech_text_0506.viewmodel.MainActivityViewModel
import com.example.domain.enumclass.DeviceType
import com.google.accompanist.flowlayout.FlowRow
import com.mtouch.ksr02_03_04_v2.Ui.UsbViewModel
import dagger.android.AndroidInjection
import javax.inject.Inject

class MainActivity : ComponentActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mainActivityViewModel: MainActivityViewModel

    @Inject
    lateinit var bluetoothViewModel: BluetoothViewModel

    @Inject
    lateinit var usbViewModel: UsbViewModel

    @Inject lateinit var deviceSerialCommunicateViewModel: DeviceSerialCommunicateViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this@MainActivity)
        mainActivityViewModel =
            ViewModelProvider(this, viewModelFactory)[MainActivityViewModel::class.java]
        usbViewModel = ViewModelProvider(this, viewModelFactory)[UsbViewModel::class.java]
        bluetoothViewModel =
            ViewModelProvider(this, viewModelFactory)[BluetoothViewModel::class.java]
        deviceSerialCommunicateViewModel =
            ViewModelProvider(this, viewModelFactory)[DeviceSerialCommunicateViewModel::class.java]

        super.onCreate(savedInstanceState)
        setContent {
            CleanArchitech_text_0506Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = NavigationView.main.name
                    ) {
                        composable(NavigationView.main.name) {
                            mainView(navHostController = navController)
                        }
                        dialog(NavigationView.login.name) {
                            LoginView().loginDialog(navController)
                        }
                        dialog(NavigationView.pgIdLogin.name) {
                            LoginView().pgIdLoginDialog(navController)
                        }
                        dialog(NavigationView.vanIdLogin.name) {
                            LoginView().vanIdLoginDialog(navHostController = navController)
                        }
                        dialog(NavigationView.registeredId.name) {
                            LoginView().registeredIdDialog(navHostController = navController)
                        }
//                        composable(NavigationView.deviceSetting.name){
//
//                            DeviceSettingView().bluetoothDevice(
//                                context = this@MainActivity,
//                                owner = this@MainActivity,
//                                bluetoothViewModel
//                            )
//                        }

                        composable(NavigationView.deviceSetting.name) { backStackEntry ->
//                            DeviceSettingView().connectDeviceView(
//                                usbViewModel = usbViewModel,
//                                bluetoothViewModel = bluetoothViewModel,
//                                context = this@MainActivity,
//                                owner = this@MainActivity
//                            )
//                            DeviceSettingView().bluetoothDevice(
//                                bluetoothViewModel = bluetoothViewModel,
//                                context = this@MainActivity,
//                                owner = this@MainActivity,
//                                navHostController = navController
//                            )

                            var deviceType = backStackEntry.arguments?.getString(DeviceConnectView.DeviceType.name)
                            if(deviceType == null) {
                                deviceType = DeviceType.BLUETOOTH.name
                            }

                            when(deviceType) {
                                DeviceType.BLUETOOTH.name -> {
                                    DeviceSettingView().bluetoothDevice(
                                        bluetoothViewModel = bluetoothViewModel,
                                        context = this@MainActivity,
                                        owner = this@MainActivity,
                                        navHostController = navController
                                    )
                                }
                                DeviceType.USB.name -> {
                                    DeviceSettingView().usbDevice(
                                        usbViewModel = usbViewModel,
                                        context = this@MainActivity,
                                        owner = this@MainActivity,
                                        navHostController = navController
                                    )
                                }
                                else -> {}
                            }
                        }

                        composable(NavigationView.creditPayment.name) {
//                            test(bluetoothViewModel = bluetoothViewModel, context = this@MainActivity3, owner = this@MainActivity3)
                            CreditPaymentView().creditPaymentView(
                                navHostController = navController,
                                deviceSerialCommunicateVieModel = deviceSerialCommunicateViewModel,
                                context = this@MainActivity,
                                owner = this@MainActivity
                            )
                        }
                        dialog(NavigationView.creditPaymentUsbDialog.name) { backStackEntry ->
                            var viewModel: DeviceSerialCommunicateViewModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                backStackEntry.arguments?.getSerializable(
                                    SerialCommunicationUsbDialogData.ViewModel.name,
                                    DeviceSerialCommunicateViewModel::class.java
                                )!!
                            } else {
                                backStackEntry.arguments?.getSerializable(
                                    SerialCommunicationUsbDialogData.ViewModel.name
                                )!!
                            } as DeviceSerialCommunicateViewModel
                            CreditPaymentView().usbDevicePaymentDialog(
                                navHostController = navController,
//                                message = backStackEntry.arguments?.getString(SerialCommunicationUsbDialogData.Message.name)!!,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun mainView(navHostController: NavHostController) {
        val screenWidth = LocalConfiguration.current.screenWidthDp
        Scaffold(
            bottomBar = {
                bottomNavigation(navHostController, screenWidth)
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
                loginStatus(screenWidth)
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
                        val itemSize = (LocalConfiguration.current.screenWidthDp.dp / 2)
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

    private fun gridItemSetting(type: String): MainGridItem? {
        when (type) {
            MainViewFunction.CreditCardPayment.name ->
                return MainGridItem(R.drawable.card_icon_main, "신용결제", NavigationView.creditPayment.name)
            MainViewFunction.ManualPayment.name ->
                return MainGridItem(R.drawable.card_icon_main, "수기결제", NavigationView.main.name)
            MainViewFunction.DeviceManagement.name ->
                return MainGridItem(R.drawable.card_icon_main, "장치관리", NavigationView.deviceSetting.name)
            MainViewFunction.CashReceipt.name ->
                return MainGridItem(R.drawable.card_icon_main, "현금영수증", NavigationView.main.name)
            MainViewFunction.TransactionHistory.name ->
                return MainGridItem(R.drawable.card_icon_main, "거래내역", NavigationView.main.name)
            MainViewFunction.AggregateSummary.name ->
                return MainGridItem(R.drawable.card_icon_main, "집계내역", NavigationView.main.name)
            MainViewFunction.NoticeAnnouncement.name ->
                return MainGridItem(R.drawable.card_icon_main, "공지사항", NavigationView.main.name)
        }
        return null
    }

    @Composable
    fun gridMenu(
        screenWidth: Int,
        mainActivityFunctionList: Array<MainViewFunction>,
        navHostController: NavHostController
    ) {
        mainActivityFunctionList.forEachIndexed { index, mainActivityFunctionList ->
            val mainGridItem = gridItemSetting(mainActivityFunctionList.name)
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
                        .clickable(onClick = { navHostController.navigate(mainGridItem!!.navigator) }),
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
                        .clickable(onClick = { navHostController.navigate(mainGridItem!!.navigator) }),
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
    fun bottomNavigation(navHostController: NavHostController, screenWidth: Int) {
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
                selected = currentRoute == NavigationView.login.name,
                onClick = { navHostController.navigate(NavigationView.login.name) }
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
                        contentDescription = NavigationView.main.name,
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
                selected = currentRoute == NavigationView.main.name,
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
    fun salesAmount(screenWidth: Int, payment: String, refund: String) {
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
    fun loginStatus(screenWidth: Int) {
        Row(
            modifier = Modifier
                .width((screenWidth * 0.9).dp)
                .height(30.dp)
                .padding(bottom = 5.dp)
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
            var text by remember { mutableStateOf("로그인 아이디") }
            Text(
                textAlign = TextAlign.Left,
                text = text,
                fontSize = 15.sp,
                color = colorResource(id = R.color.white),
                fontFamily = FontFamily(Font(R.font.ns_acr)),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp)
            )
            Text(
                textAlign = TextAlign.Right,
                text = "TMN041165",
                fontSize = 15.sp,
                color = colorResource(id = R.color.white),
                fontFamily = FontFamily(Font(R.font.ns_acr)),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 20.dp)
            )
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

@Preview(showBackground = true)
@Composable
fun mainViewPreview() {
    val navController = rememberNavController()
    MainActivity().mainView(navHostController = navController)
}
