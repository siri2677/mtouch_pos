package com.example.mtouchpos.view.ui

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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.util.GradientButton
import com.example.mtouchpos.view.util.ItemListStatus
import com.example.mtouchpos.view.ui.theme.ObserveResultSerialCommunicate
import com.example.mtouchpos.view.ui.theme.NavigationBundleKey
import com.example.mtouchpos.view.ui.theme.TopNavigation
import com.example.mtouchpos.viewmodel.DeviceCommunicationViewModel
import com.example.mtouchpos.viewmodel.MainActivityViewModel
import com.example.mtouchpos.R
import com.example.mtouchpos.dto.AmountInfo
import com.example.mtouchpos.dto.ResponseFlowData
import com.example.mtouchpos.view.ui.theme.ObserveResultPayment

@Composable
fun CreditPaymentView(
    navController: NavController,
    mainActivityViewModel: MainActivityViewModel = hiltViewModel(),
    deviceCommunicationViewModel: DeviceCommunicationViewModel = hiltViewModel()
) {
    fun installmentFormat(installmentString: String): String {
        return if (installmentString == "일시불") {
            String.format("%02d", 0)
        } else {
            "0${installmentString.replace("0", "").replace("개월", "")}"
        }
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp
    var installment by remember { mutableStateOf("일시불") }
    var account by remember { mutableStateOf("0") }

    ObserveResultPayment(
        responseFlowData = deviceCommunicationViewModel.responseFlowData.collectAsStateWithLifecycle(
            ResponseFlowData.Init).value,
        navController = navController,
        responsePage = NavigationGraphState.CreditPaymentView.CreditPayment.name
    )

    ObserveResultSerialCommunicate(
        deviceCommunicationViewModel = deviceCommunicationViewModel,
        navController = navController
    )

    Scaffold(
        topBar = {
            TopNavigation("신용 결제")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .drawBehind {
                    drawLine(
                        Color.LightGray, Offset(0f, 0f), Offset(size.width, 0f), 2 * density
                    )
                },
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
                    Row() {
                        Text(
                            modifier = Modifier.padding(end = 5.dp),
                            text = account
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
                                    NavigationBundleKey.ItemList to ItemListStatus(
                                        title = "할부기간",
                                        list = (1..Integer.parseInt(mainActivityViewModel.getMaxInstallment()) + 1).map {
                                            if (it == 1) "일시불" else String.format("%02d", it)
                                        },
                                        initValue = "일시불",
                                        onTextChange = { installment = it }
                                    )
                                ),
                                NavOptions
                                    .Builder()
                                    .setLaunchSingleTop(true)
                                    .build()
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
                        text = installment
                    )
                }
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
                        if (deviceCommunicationViewModel.deviceSettingSharedPreference.getDeviceConnectSetting() != null) {
                            deviceCommunicationViewModel.requestOfflinePayment(
                                amountInfo = AmountInfo(
                                    amount = Integer.parseInt(account),
                                    installment = installmentFormat(installment)
                                )
                            )
                        } else {
                            navController.navigate(
                                NavigationGraphState.CommonView.ErrorDialog.name,
                                bundleOf( NavigationBundleKey.message to "장치 등록 후 결제 진행 바시기 바랍니다."),
                                NavOptions.Builder().setLaunchSingleTop(true).build()
                            )
                        }
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
                itemsIndexed(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "00", "0", "delete")) { index, item ->
                    Column(
                        modifier = Modifier
                            .paint(
                                painterResource(id = R.drawable.card_bt),
                                contentScale = ContentScale.FillBounds
                            )
                            .height(60.dp)
                            .background(color = colorResource(id = R.color.grey4))
                            .clickable(onClick = {
                                account = when (item) {
                                    "delete" -> {
                                        if (account.length > 1) account.removeSuffix(
                                            account
                                                .last()
                                                .toString()
                                        ) else "0"
                                    }

                                    else -> {
                                        if (account == "0") item else account + item
                                    }
                                }
                            }),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (item) {
                            "delete" -> {
                                Image(
                                    painter = painterResource(R.drawable.ic_back),
                                    contentDescription = null
                                )
                            }
                            else -> {
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