package com.example.mtouchpos.view.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.mtouchpos.R
import com.example.mtouchpos.coordinator.OfflinePaymentCoordinator
import com.example.mtouchpos.view.navgraph.NavigationBundleKey
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.theme.TopNavigation
import com.example.mtouchpos.view.util.GradientButton
import com.example.mtouchpos.view.util.SelectDialog
import com.example.mtouchpos.viewmodel.LoginViewModel
import com.example.mtouchpos.viewmodel.OfflinePaymentViewModel
import com.example.mtouchpos.viewmodel.factory.CardTerminalFactory

@Composable
fun CreditPaymentView(navController: NavController) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val context = LocalContext.current as ComponentActivity

    val loginViewModel = hiltViewModel<LoginViewModel>()
    val offlinePaymentViewModel = hiltViewModel<OfflinePaymentViewModel>()

    val offlinePaymentCoordinator = OfflinePaymentCoordinator(
        navController = navController,
        offlinePaymentViewModel = offlinePaymentViewModel,
        componentActivity = context
    )

    val offlinePaymentInfo = offlinePaymentViewModel.offlinePaymentInfo
        .collectAsStateWithLifecycle().value
    val paymentProcessState = offlinePaymentViewModel.paymentProcessState
        .collectAsStateWithLifecycle(OfflinePaymentViewModel.PaymentProcessState.Init).value

    offlinePaymentCoordinator.cardTerminalNewIntent(
        paymentProcessState = paymentProcessState,
        callBack = CardTerminalFactory.CallBack.Home,
        merchantUrl = null
    )


//    offlinePaymentCoordinator.getConsumer().let {
//        DisposableEffect(context, navController) {
//            context.addOnNewIntentListener(it)
//            onDispose { context.removeOnNewIntentListener(it) }
//        }
//    }
//
//    if (communicateCardTerminalManager != null) {
//        Log.w("communicate", communicateCardTerminalManager.toString())
//        Log.w("paymentProcessState", paymentProcessState.toString())
//        offlinePaymentCoordinator.paymentResult(paymentProcessState)
//    }

//    offlinePaymentCoordinator.observeResultPaymentData(
//        paymentProcessState = offlinePaymentViewModel.paymentProcessState
//            .collectAsState(OfflinePaymentViewModel.PaymentProcessState.Init).value,
//        communicateCardTerminalManager = communicateCardTerminalManager
//    )

    Scaffold(
        topBar = { TopNavigation("신용 결제", navController) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .drawBehind {
                    drawLine(
                        Color.LightGray, Offset(0f, 0f), Offset(size.width, 0f), 2 * density
                    )
                },
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(start = 5.dp, end = 5.dp)
            ) {
                Text(
                    modifier = Modifier.padding(top = 15.dp, start = 5.dp, end = 5.dp),
                    text = "결제 금액을 입력해주세요."
                )
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(colorResource(id = R.color.white))
                        .border(
                            width = 1.dp,
                            color = colorResource(id = R.color.grey4),
                            shape = RectangleShape
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.End
                ) {
                    Row {
                        Text(
                            modifier = Modifier.padding(end = 5.dp),
                            text = offlinePaymentInfo.totalAmount.toString()
                        )
                        Text(
                            modifier = Modifier.padding(end = 5.dp),
                            text = "원"
                        )
                    }
                }

                Text(
                    modifier = Modifier.padding(top = 8.dp, start = 5.dp, end = 5.dp),
                    text = "할부기간을 선택해주세요."
                )
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            navController.navigate(
                                NavigationGraphState.CommonView.ItemListDialog.name,
                                bundleOf(
                                    NavigationBundleKey.ITEM_LIST to SelectDialog(
                                        title = "할부기간",
                                        list = (1..Integer.parseInt(loginViewModel.fetchCurrentConnectedUserInfo()?.apiMaxInstall) + 1).map {
                                            if (it == 1) "일시불" else String.format("%02d", it)
                                        },
                                        initValue = "일시불",
                                        onTextChange = {
                                            offlinePaymentViewModel.updateOfflinePaymentInfo(
                                                offlinePaymentInfo.copy(installment = it)
                                            )
                                        }
                                    )
                                ),
                                NavOptions.Builder().setLaunchSingleTop(true).build()
                            )
                        }
                        .background(colorResource(id = R.color.white))
                        .border(
                            width = 1.dp,
                            color = colorResource(id = R.color.grey4),
                            shape = RectangleShape
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.padding(end = 5.dp),
                        text = offlinePaymentInfo.installment
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GradientButton(
                        text = "결제하기",
                        modifier = Modifier
                            .width((screenWidth * 0.95).dp)
                            .padding(vertical = 5.dp)
                            .height(60.dp),
                        fontSize = 20.sp,
                        onClick = {
                            OfflinePaymentCoordinator.PaymentProcess(
                                merchantUrl = null,
                                offlinePaymentInfo = offlinePaymentInfo
                            ).let { offlinePaymentCoordinator.navigateToDeviceDialog(it) }
                        }
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(15.dp),
                    modifier = Modifier.fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "00", "0", "delete")) { item ->
                        Column(
                            modifier = Modifier
                                .paint(
                                    painterResource(id = R.drawable.card_bt),
                                    contentScale = ContentScale.FillBounds
                                )
                                .height(60.dp)
                                .background(color = colorResource(id = R.color.grey4))
                                .clickable(onClick = {
                                    val totalAmount = offlinePaymentInfo.totalAmount.toString().run {
                                        when (item) {
                                            "delete" -> if (length > 1) removeSuffix(last().toString()) else "0"
                                            else -> if (this == "0") item else this + item
                                        }.toInt()
                                    }
                                    offlinePaymentViewModel.updateOfflinePaymentInfo(
                                        offlinePaymentInfo.copy(totalAmount = totalAmount)
                                    )
                                }),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if(item == "delete") {
                                Image(
                                    painter = painterResource(R.drawable.ic_back),
                                    contentDescription = null
                                )
                            } else {
                                Text(
                                    text = item,
                                    fontSize = 35.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreviewTest() {
//    BluetoothPaymentDialog("test", {}, true)
}