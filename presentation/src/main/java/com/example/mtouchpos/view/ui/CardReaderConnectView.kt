package com.example.mtouchpos.view.ui

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.mtouchpos.R
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.view.ui.theme.TopNavigation
import com.example.mtouchpos.viewmodel.CardReaderConnectVM
import com.example.mtouchpos.managerImpl.factory.CardReaderFactory

@Composable
fun CardReaderConnectView(
    deviceType: CardReaderConnectVM.DeviceType,
    navController: NavController,
    deviceConnectViewModel: CardReaderConnectVM
) {
    BackHandler {
        navController.popBackStack(
            route = NavigationGraphState.HomeView.Home.name,
            inclusive = false
        )
    }

    Scaffold(
        topBar = {
            TopNavigation("장치 관리", navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .drawBehind {
                    drawLine(
                        Color.LightGray,
                        Offset(0f, 0f),
                        Offset(size.width, 0f),
                        2 * density
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CardReaderSelectTab(
                deviceType = deviceType,
                navController = navController
            )

            when (deviceType) {
                CardReaderConnectVM.DeviceType.Bluetooth ->
                    BluetoothList(deviceConnectViewModel)
                CardReaderConnectVM.DeviceType.Usb ->
                    UsbList(deviceConnectViewModel)
            }
        }
    }
}

@Composable
fun CardReaderSelectTab(
    deviceType: CardReaderConnectVM.DeviceType,
    navController: NavController
) {
    val watermelonColor = colorResource(R.color.watermelon)

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(top = 20.dp)
            .drawBehind {
                drawLine(
                    watermelonColor,
                    Offset(0f, size.height),
                    Offset(size.width, size.height),
                    1 * density
                )
            }.CardReaderSelectTabModifier(deviceType, watermelonColor),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 10.dp)
                .clickable(onClick = {
                    navController.navigate(NavigationGraphState.DeviceSettingView.Bluetooth.name)
                }),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "블루투스", fontSize = 15.sp)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 10.dp)
                .clickable(onClick = {
                    navController.navigate(NavigationGraphState.DeviceSettingView.USB.name)
                }),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "USB", fontSize = 15.sp)
        }
    }
}

@Composable
fun BluetoothDevice(
    deviceConnectViewModel: CardReaderConnectVM = hiltViewModel(),
    navHostController: NavController
) {
    CardReaderConnectView(
        deviceType = CardReaderConnectVM.DeviceType.Bluetooth,
        deviceConnectViewModel = deviceConnectViewModel,
        navController = navHostController
    )
}

@Composable
fun UsbDevice(
    deviceConnectViewModel: CardReaderConnectVM = hiltViewModel(),
    navHostController: NavController
) {
    CardReaderConnectView(
        deviceType = CardReaderConnectVM.DeviceType.Usb,
        deviceConnectViewModel = deviceConnectViewModel,
        navController = navHostController
    )
}

@Composable
fun Modifier.CardReaderSelectTabModifier(
    deviceType: CardReaderConnectVM.DeviceType,
    watermelonColor: Color
): Modifier = when (deviceType) {
    CardReaderConnectVM.DeviceType.Bluetooth -> {
        drawBehind {
            val strokeWidth = 3 * density
            val y = size.height - strokeWidth / 2
            drawLine(
                watermelonColor,
                Offset(0f, y),
                Offset(size.width / 2, y),
                strokeWidth
            )
        }
    }

    CardReaderConnectVM.DeviceType.Usb -> {
        drawBehind {
            val strokeWidth = 3 * density
            val y = size.height - strokeWidth / 2
            drawLine(
                watermelonColor,
                Offset(size.width / 2, y),
                Offset(size.width, y),
                strokeWidth
            )
        }
    }
}

@Composable
fun BluetoothList(deviceSettingViewModel: CardReaderConnectVM) {
    var isDeviceSearching by remember { mutableStateOf(false) }
    var isAlwaysConnecting by remember { mutableStateOf(false) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(-2) }

    if (isDeviceSearching) {
        deviceSettingViewModel.searchBluetoothDevice()
    } else {
        deviceSettingViewModel.pauseSearchBluetoothDevice()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .padding(top = 10.dp)
        ) {
            Text(
                text = "장치 리스트",
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .width((LocalConfiguration.current.screenWidthDp * 0.85).dp)
                .fillMaxHeight(0.875f)
                .padding(top = 10.dp)
        ) {
            item {
                deviceSettingViewModel.deviceInfoList.collectAsStateWithLifecycle().value
                    .forEachIndexed { index, bluetoothDeviceInfo ->
                    BluetoothConnectButton(
                        isAlwaysConnecting = isAlwaysConnecting,
                        item = bluetoothDeviceInfo as CardReaderConnectVM.BluetoothDeviceInfo,
                        isSelected = selectedIndex == index,
                        onTap = { selectedIndex = index },
                        onTapCancel = { selectedIndex = -2 },
                        deviceConnectViewModel = deviceSettingViewModel
                    )
                }
            }
        }

        Row(
            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .height(60.dp)
                    .weight(1f)
                    .padding(start = 10.dp, end = 5.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = if (isDeviceSearching) {
                                listOf(
                                    colorResource(id = R.color.pink),
                                    colorResource(id = R.color.pink)
                                )
                            } else {
                                listOf(
                                    colorResource(id = R.color.orange_pink),
                                    colorResource(id = R.color.watermelon)
                                )
                            }
                        )
                    )
                    .clickable(onClick = {
                        isDeviceSearching = !isDeviceSearching
                    }),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isDeviceSearching) "장치 검색중..." else "장치 검색",
                    color = colorResource(id = R.color.white),
                    fontFamily = FontFamily(Font(R.font.ns_acr)),
                )
            }

            Row(
                modifier = Modifier
                    .height(60.dp)
                    .weight(1f)
                    .padding(start = 5.dp, end = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color = colorResource(id = R.color.lilly_color2))
                    .clickable(onClick = { isAlwaysConnecting = !isAlwaysConnecting }),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAlwaysConnecting) "연결 항상 유지" else "결제 시에만 연결",
                    color = colorResource(id = R.color.white),
                    fontFamily = FontFamily(Font(R.font.ns_acr))
                )
            }
        }
    }
}

@Composable
fun UsbList(deviceSettingViewModel: CardReaderConnectVM) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(-2) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.padding(top = 10.dp)) {
            Text(
                text = "장치 리스트",
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .width((LocalConfiguration.current.screenWidthDp * 0.85).dp)
                .fillMaxHeight(0.875f)
                .padding(top = 10.dp)
        ) {
            item {
                deviceSettingViewModel.deviceInfoList.collectAsStateWithLifecycle().value
                    .forEachIndexed { index, usbDeviceInfo ->
                    UsbConnectButton(
                        item = usbDeviceInfo as CardReaderConnectVM.UsbDeviceInfo,
                        isSelected = selectedIndex == index,
                        onTap = { selectedIndex = index },
                        onTapCancel = { selectedIndex = -2 },
                        deviceSettingViewModel = deviceSettingViewModel
                    )
                }
            }
        }

        Row(
            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .height(60.dp)
                    .weight(1f)
                    .padding(start = 10.dp, end = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                colorResource(id = R.color.orange_pink),
                                colorResource(id = R.color.watermelon)
                            )
                        )
                    )
                    .clickable(onClick = {
                        deviceSettingViewModel.searchUsbDevice()
                    }),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "장치 검색",
                    color = colorResource(id = R.color.white),
                    fontFamily = FontFamily(Font(R.font.ns_acr)),
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun BluetoothConnectButton(
    isAlwaysConnecting: Boolean,
    item: CardReaderConnectVM.BluetoothDeviceInfo,
    isSelected: Boolean,
    onTap: () -> Unit,
    onTapCancel: () -> Unit,
    deviceConnectViewModel: CardReaderConnectVM
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(bottom = 10.dp),
        onClick = { onTap() },
        shape = RoundedCornerShape(10),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = if (isSelected) R.color.grey7 else R.color.grey5)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = item.deviceName,
                    color = Color.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = item.deviceInformation,
                    color = Color.Black,
                    fontSize = 13.sp
                )
            }
            if (isSelected && !isAlwaysConnecting) {
                deviceConnectViewModel.updateConnectedDeviceInfo(item)

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Button(
                        modifier = Modifier
                            .width(120.dp)
                            .height(70.dp),
                        onClick = {
                            onTapCancel()
                        },
                        shape = RoundedCornerShape(10),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.orange_pink))
                    ) {
                        Text(
                            text = "등록해제",
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }
            }
            if (isSelected && isAlwaysConnecting) {
                if (deviceConnectViewModel.deviceConnectState.collectAsStateWithLifecycle().value ==
                    CardReaderConnectVM.DeviceConnectState.Connected) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Button(
                            modifier = Modifier
                                .width(120.dp)
                                .height(70.dp),
                            onClick = {
                                onTapCancel()
//                                deviceConnectManagerFactory.getInstance(DeviceType.Bluetooth).disConnect()
                            },
                            shape = RoundedCornerShape(10),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(
                                    id = R.color.orange_pink
                                )
                            )
                        ) {
                            Text(
                                text = "연결해제",
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.7f)
                            )
                        }
                    }
                } else {
                    with(deviceConnectViewModel) {
                        updateConnectedDeviceInfo(item)
                        connectDevice(CardReaderFactory(LocalContext.current)
                            .getConnectManger(CardReaderConnectVM.DeviceType.Bluetooth))
                    }
                }
            }
        }
    }
}

@Composable
fun UsbConnectButton(
    item: CardReaderConnectVM.UsbDeviceInfo,
    isSelected: Boolean,
    onTap: () -> Unit,
    onTapCancel: () -> Unit,
    deviceSettingViewModel: CardReaderConnectVM
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(bottom = 10.dp),
        onClick = {
            onTap()
        },
        shape = RoundedCornerShape(10),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = if (isSelected) R.color.grey7 else R.color.grey5)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = item.deviceName,
                    color = Color.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = item.productName,
                    color = Color.Black,
                    fontSize = 13.sp
                )
            }
            if (isSelected) {
                if (deviceSettingViewModel.deviceConnectState.collectAsStateWithLifecycle().value ==
                    CardReaderConnectVM.DeviceConnectState.Registered) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Button(
                            modifier = Modifier
                                .width(120.dp)
                                .height(70.dp),
                            onClick = {
                                onTapCancel()
//                                deviceConnectManagerFactory.getInstance(DeviceType.Usb).disConnect()
                            },
                            shape = RoundedCornerShape(10),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.orange_pink)
                            )
                        ) {
                            Text(
                                text = "등록됨",
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.7f)
                            )
                        }
                    }
                } else {
                    with(deviceSettingViewModel) {
                        updateConnectedDeviceInfo(item)
                        connectDevice(CardReaderFactory(LocalContext.current)
                            .getConnectManger(CardReaderConnectVM.DeviceType.Usb))
                    }
                }
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun GreetingPreview6() {
//    DeviceSettingView().connectDeviceView(
//        viewModelFactory = viewModelFactory{ },
//        context = null,
//        owner = null
//    )
}