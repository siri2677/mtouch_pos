package com.example.mtouchpos.view.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.hardware.usb.UsbDevice
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
import com.example.mtouchpos.FlowManager
import com.example.mtouchpos.vo.DeviceList
import com.example.mtouchpos.device.bluetooth.BluetoothDeviceScan
import com.example.mtouchpos.device.usb.UsbDeviceScan
import com.example.mtouchpos.vo.DeviceConnectSharedFlow
import com.example.mtouchpos.view.navgraph.NavigationGraphState
import com.example.mtouchpos.device.DeviceConnectManagerFactory
import com.example.mtouchpos.view.ui.theme.TopNavigation
import com.example.mtouchpos.viewmodel.DeviceConnectViewModel
import com.example.domain.repositoryInterface.DeviceSettingSharedPreference
import com.example.domain.vo.DeviceType
import com.example.mtouchpos.R

class DeviceSettingView {
    @Composable
    fun bluetoothDevice(
        deviceConnectViewModel: DeviceConnectViewModel = hiltViewModel(),
        navHostController: NavController
    ) {
        DeviceSettingViewFrame(
            deviceType = DeviceType.Bluetooth,
            deviceConnectViewModel = deviceConnectViewModel,
            navController = navHostController
        )
    }

    @Composable
    fun usbDevice(
        deviceConnectViewModel: DeviceConnectViewModel = hiltViewModel(),
        navHostController: NavController
    ) {
        DeviceSettingViewFrame(
            deviceType = DeviceType.Usb,
            deviceConnectViewModel = deviceConnectViewModel,
            navController = navHostController
        )
    }

    @Composable
    fun TopView(
        deviceType: DeviceType,
        navController: NavController
    ) {
        Row(
            modifier = DeviceTypeSelection(deviceType),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 10.dp)
                    .clickable(onClick = {
                        navController.navigate(NavigationGraphState.DeviceSettingView.Bluetooth.name,)
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
    fun BluetoothDeviceListView(
        deviceConnectViewModel: DeviceConnectViewModel
    ) {
        val bluetoothDeviceScan = BluetoothDeviceScan(LocalContext.current)
        var isDeviceSearching by remember { mutableStateOf(false) }
        var isAlwaysConnecting by remember { mutableStateOf(false) }

        if (isDeviceSearching) bluetoothDeviceScan.scan() else bluetoothDeviceScan.cancel()

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

            DeviceListView(
                deviceConnectViewModel,
                isAlwaysConnecting
            )

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
    fun UsbDeviceListView(
        deviceConnectViewModel: DeviceConnectViewModel
    ) {
        val usbDeviceScan = UsbDeviceScan(LocalContext.current)
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

            DeviceListView(deviceConnectViewModel)

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
                            usbDeviceScan.scan()
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

    @Composable
    fun DeviceSettingViewFrame(
        deviceType: DeviceType,
        navController: NavController,
        deviceConnectViewModel: DeviceConnectViewModel
    ) {
//        deviceConnectViewModel.job.start()

        BackHandler {
            navController.popBackStack(
                route = NavigationGraphState.HomeView.Home.name,
                inclusive = false
            )
        }

        Scaffold(
            topBar = {
                TopNavigation("장치 관리")
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .drawBehind {
                        val strokeWidth = 2 * density
                        drawLine(
                            Color.LightGray,
                            Offset(0f, 0f),
                            Offset(size.width, 0f),
                            strokeWidth
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TopView(
                    deviceType = deviceType,
                    navController = navController
                )
                when (deviceType) {
                    DeviceType.Bluetooth -> BluetoothDeviceListView(deviceConnectViewModel = deviceConnectViewModel)
                    DeviceType.Usb -> UsbDeviceListView(deviceConnectViewModel = deviceConnectViewModel)
                }
            }
        }
    }
}

@Composable
fun DeviceTypeSelection(type: DeviceType): Modifier {
    val watermelonColor = colorResource(R.color.watermelon)
    val modifier = Modifier
        .fillMaxWidth()
        .padding(top = 20.dp)
        .drawBehind {
            val strokeWidth = 1 * density
            drawLine(
                watermelonColor,
                Offset(0f, size.height),
                Offset(size.width, size.height),
                strokeWidth
            )
        }
    when (type) {
        DeviceType.Bluetooth -> {
            return modifier.drawBehind {
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
        DeviceType.Usb -> {
            return modifier.drawBehind {
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
}

@SuppressLint("MissingPermission")
@Composable
fun SingleSelectButtonBluetoothPage(
    isAlwaysConnecting: Boolean,
    item: BluetoothDevice,
    isSelected: Boolean,
    onTap: () -> Unit,
    onTapCancel: () -> Unit,
    isConnectingDevice: Boolean,
    deviceConnectViewModel: DeviceConnectViewModel
) {
    val deviceConnectManagerFactory = DeviceConnectManagerFactory(LocalContext.current)

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
                    text = item.name,
                    color = Color.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = item.address,
                    color = Color.Black,
                    fontSize = 13.sp
                )
            }
            if (isSelected && !isAlwaysConnecting) {
                deviceConnectViewModel.setDeviceConnectSetting(
                    DeviceSettingSharedPreference.DeviceConnectSetting(
                        deviceType = DeviceType.Bluetooth,
                        deviceInformation = item.address,
                        isConnected = false,
                        isKeepConnect = false
                    )
                )

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
                if(isConnectingDevice) {
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
                                deviceConnectManagerFactory.getInstance(DeviceType.Bluetooth).disConnect()
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
                    deviceConnectViewModel.connectDevice(
                        DeviceSettingSharedPreference.DeviceConnectSetting(
                            deviceType = DeviceType.Bluetooth,
                            deviceInformation = item.address,
                            isConnected = false,
                            isKeepConnect = true
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun singleSelectButtonUsbPage(
    item: UsbDevice,
    isSelected: Boolean,
    onTap: () -> Unit,
    onTapCancel: () -> Unit,
    isPermissionGranted: Boolean,
    deviceConnectViewModel: DeviceConnectViewModel
) {
    val deviceConnectManagerFactory = DeviceConnectManagerFactory(LocalContext.current)

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
                    text = item.productName!!,
                    color = Color.Black,
                    fontSize = 13.sp
                )
            }
            if (isSelected) {
                if(isPermissionGranted) {
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
                                deviceConnectManagerFactory.getInstance(DeviceType.Usb).disConnect()
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
                    deviceConnectViewModel.connectDevice(
                        DeviceSettingSharedPreference.DeviceConnectSetting(
                            deviceType = DeviceType.Usb,
                            deviceInformation = item.toString(),
                            isConnected = false,
                            isKeepConnect = true
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceListView(
    deviceConnectViewModel: DeviceConnectViewModel,
    isAlwaysConnecting: Boolean = false
) {
    val deviceConnectSharedFlow =
        FlowManager.deviceConnectSharedFlow.collectAsStateWithLifecycle(initialValue = DeviceConnectSharedFlow.Init).value
    val deviceList =
        FlowManager.deviceListSharedFlow.collectAsStateWithLifecycle(initialValue = DeviceList.Init).value
    var isConnectComplete by remember { mutableStateOf(false) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(-2) }

    if (deviceConnectSharedFlow is DeviceConnectSharedFlow.ConnectCompleteFlow) { isConnectComplete = true }

    LazyColumn(
        modifier = Modifier
            .width((LocalConfiguration.current.screenWidthDp * 0.85).dp)
            .fillMaxHeight(0.875f)
            .padding(top = 10.dp)
    ) {
        item {
            when (deviceList) {
                is DeviceList.BluetoothList -> {
                    deviceList.devices.mapIndexed { index, device ->
                        SingleSelectButtonBluetoothPage(
                            isAlwaysConnecting = isAlwaysConnecting,
                            item = device,
                            isSelected = selectedIndex == index,
                            onTap = { selectedIndex = index },
                            onTapCancel = { selectedIndex = -2 },
                            deviceConnectViewModel = deviceConnectViewModel,
                            isConnectingDevice = isConnectComplete
                        )
                    }
                }
                is DeviceList.UsbList -> {
                    deviceList.devices.mapIndexed { index, device ->
                        singleSelectButtonUsbPage(
                            item = device,
                            isSelected = selectedIndex == index,
                            onTap = { selectedIndex = index },
                            onTapCancel = { selectedIndex = -2 },
                            isPermissionGranted = isConnectComplete,
                            deviceConnectViewModel = deviceConnectViewModel
                        )
                    }
                }
                is DeviceList.Init -> {}
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