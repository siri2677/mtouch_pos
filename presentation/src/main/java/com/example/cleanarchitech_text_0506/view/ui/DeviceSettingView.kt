package com.example.cleanarchitech_text_0506.view.ui

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.cleanarchitech_text_0506.R
import com.example.cleanarchitech_text_0506.sealed.Device
import com.example.cleanarchitech_text_0506.sealed.DeviceList
import com.example.cleanarchitech_text_0506.enum.DeviceType
import com.example.cleanarchitech_text_0506.enum.MainView
import com.example.cleanarchitech_text_0506.sealed.DeviceConnectSharedFlow
import com.example.cleanarchitech_text_0506.viewmodel.TestCommunicationViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class DeviceSettingView {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun topNavigationConnectDevice() {
        CenterAlignedTopAppBar(
            title = {
                Text("장치 관리", fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
                Icon(Icons.Default.ArrowBack, contentDescription = "Menu")
            }
        )
    }

    @Composable
    fun DeviceTypeSelection(content: String): Modifier {
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
        when (content) {
            "bluetooth" -> {
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
            "usb" -> {
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
            else -> {
                return modifier
            }
        }
    }

    @Composable
    fun bluetoothDeviceList(
        isAlwaysConnecting: Boolean,
        context: Context,
        testCommunicationViewModel: TestCommunicationViewModel,
        owner: LifecycleOwner?
    ) {
        val screenWidth = LocalConfiguration.current.screenWidthDp
        var selectedIndex by rememberSaveable { mutableStateOf(-2) }
        var observerDataSize by remember { mutableStateOf(0) }
        var isConnectingDevice by remember { mutableStateOf(false) }
        var bluetoothDeviceList: ArrayList<BluetoothDevice>? by remember { mutableStateOf(ArrayList()) }

        testCommunicationViewModel.deviceConnectScanFlow.CollectAsEffect(
            block = {
                when(it) {
                    is DeviceList.BluetoothList -> {
                        Log.w("bluetoothDevice", "Device")
                        if (observerDataSize != it.devices.size) {
                            bluetoothDeviceList = null
                        }
                        observerDataSize = it.devices.size
                        bluetoothDeviceList = it.devices
                    }
                    else -> {}
                }
            }
        )
        testCommunicationViewModel.deviceConnectSharedFlow?.CollectAsEffect(
            block = {
                when(it){
                    is DeviceConnectSharedFlow.ConnectCompleteFlow -> { isConnectingDevice = it.flow }
                    else -> {}
                }
            }
        )


//        testCommunicationViewModel?.listUpdate?.observe(owner!!) {
//            it.getContentIfNotHandled()?.let { scanResult ->
//                if (observerDataSize != scanResult.size) {
//                    bluetoothDeviceList = null
//                }
//                observerDataSize = scanResult.size
//                bluetoothDeviceList = scanResult
//                Log.w("bluetoothDeviceList",bluetoothDeviceList.toString())
//            }
//        }


        if(isConnectingDevice) Text(text = ("success"))
        else Text(text = ("fail"))

        LazyColumn(
            modifier = Modifier
                .width((screenWidth * 0.85).dp)
                .fillMaxHeight(0.875f)
                .padding(top = 10.dp)
        ) {
            item {
                bluetoothDeviceList?.forEachIndexed { index, bluetoothDevice ->
                    singleSelectButtonBluetoothPage(
                        isAlwaysConnecting = isAlwaysConnecting,
                        item = bluetoothDevice,
                        isSelected = selectedIndex == index,
                        context = context,
                        onTap = { selectedIndex = index },
                        onTapCancle = { selectedIndex = -2 },
                        testCommunicationViewModel = testCommunicationViewModel,
//                        bluetoothViewModel = bluetoothViewModel,
                        isConnectingDevice = isConnectingDevice
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun bluetoothDevice(
        context: Context?,
        owner: LifecycleOwner?,
        viewModel: TestCommunicationViewModel = hiltViewModel(),
//        bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
        navHostController: NavController
    ) {
        val testCommunicationViewModel = viewModel.setDeviceType(DeviceType.Bluetooth.name)!!
        var deviceType by remember { mutableStateOf("bluetooth") }
        var modifier = DeviceTypeSelection(deviceType)
        var isDeviceSearching by remember { mutableStateOf(false) }
        var isAlwaysConnecting by remember { mutableStateOf(false) }
        var deviceSearchingColor =
            if (isDeviceSearching) {
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
        var deviceSearchingText = if (isDeviceSearching) "장치 검색중..." else "장치 검색"
        var deviceContactSettingText = if (isAlwaysConnecting) "연결 항상 유지" else "결제 시에만 연결"
        BackHandler {
            navHostController.popBackStack(
                route = MainView.Main.name,
                inclusive = false
            )
        }
        DisposableEffect(Unit) {
            onDispose {
//                bluetoothViewModel.bluetoothDeviceUnBinding()
                testCommunicationViewModel.serviceUnbind()
            }
        }
        Scaffold(
            topBar = {
                topNavigationConnectDevice()
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
                Row(
                    modifier = modifier,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 10.dp),
//                            .clickable(onClick = { deviceType = "bluetooth" }),
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
                                navHostController.navigate(MainView.DeviceSettingUSB.name)
                            }),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "USB", fontSize = 15.sp)
                    }
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

                    bluetoothDeviceList(
                        isAlwaysConnecting,
                        context!!,
//                        bluetoothViewModel!!,
                        testCommunicationViewModel,
                        owner!!
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
                                        colors = deviceSearchingColor
                                    )
                                )
                                .clickable(onClick = {
                                    if (isDeviceSearching)
                                        testCommunicationViewModel.scanCancel()
//                                        bluetoothViewModel.stopScanBluetoothDevice()
                                    else
                                        testCommunicationViewModel.scan()
//                                        bluetoothViewModel.scanBluetoothDevice()
                                    isDeviceSearching = !isDeviceSearching
                                }),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = deviceSearchingText,
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
                                text = deviceContactSettingText,
                                color = colorResource(id = R.color.white),
                                fontFamily = FontFamily(Font(R.font.ns_acr))
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun usbDeviceList(
        context: Context,
        testCommunicationViewModel: TestCommunicationViewModel
    ) {
        val screenWidth = LocalConfiguration.current.screenWidthDp
        var isPermissionGranted by remember { mutableStateOf(false) }
        var selectedIndex by rememberSaveable { mutableStateOf(-2) }
        var usbDeviceList: ArrayList<UsbDevice>? by remember { mutableStateOf(ArrayList()) }

        testCommunicationViewModel.deviceConnectScanFlow.CollectAsEffect(
            block = {
                when(it) {
                    is DeviceList.USBList -> { usbDeviceList = it.devices }
                    else -> {}
                }
            }
        )
        testCommunicationViewModel.deviceConnectSharedFlow.CollectAsEffect(
            block = {
                when(it) {
                    is DeviceConnectSharedFlow.PermissionCheckCompleteFlow -> { isPermissionGranted = it.flow }
                    else -> {}
                }
            },
        )

        LazyColumn(
            modifier = Modifier
                .width((screenWidth * 0.85).dp)
                .fillMaxHeight(0.875f)
                .padding(top = 10.dp)
        ) {
            item {
                usbDeviceList?.forEachIndexed { index, usbDevice ->
                    singleSelectButtonUsbPage(
                        item = usbDevice,
                        isSelected = selectedIndex == index,
                        context = context,
                        onTap = { selectedIndex = index },
                        onTapCancel = { selectedIndex = -2 },
                        testCommunicationViewModel = testCommunicationViewModel,
                        isPermissionGranted = isPermissionGranted
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun usbDevice(
        context: Context,
        owner: LifecycleOwner,
        viewModel: TestCommunicationViewModel = hiltViewModel(),
        navHostController: NavController
    ) {
        val testCommunicationViewModel = viewModel.setDeviceType(DeviceType.Usb.name)!!
        var deviceType by remember { mutableStateOf("usb") }
        var modifier = DeviceTypeSelection(deviceType)
//        testCommunicationViewModel.serviceBind()
        BackHandler {
            navHostController.popBackStack(
                route = MainView.Main.name,
                inclusive = false
            )
        }
        DisposableEffect(Unit){
            onDispose {
                testCommunicationViewModel?.serviceUnbind()
            }
        }
        Scaffold(
            topBar = {
                topNavigationConnectDevice()
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
                Row(
                    modifier = modifier,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 10.dp)
                            .clickable(onClick = {
                                navHostController.navigate(MainView.DeviceSettingBluetooth.name)
                            }),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "블루투스", fontSize = 15.sp)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 10.dp),
//                            .clickable(onClick = { deviceType = "usb" }),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "USB", fontSize = 15.sp)
                    }
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

                    usbDeviceList(context, testCommunicationViewModel!!)

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
                                .clickable(onClick = { testCommunicationViewModel.scan() }),
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
        }
    }

    @Composable
    fun singleSelectButtonUsbPage(
        item: UsbDevice,
        isSelected: Boolean,
        context: Context,
        onTap: () -> Unit,
        onTapCancel: () -> Unit,
        testCommunicationViewModel: TestCommunicationViewModel,
        isPermissionGranted: Boolean
    ) {
        val backgroundColor = if (isSelected) R.color.grey7 else R.color.grey5

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(bottom = 10.dp),
            onClick = {
                onTap()
            },
            shape = RoundedCornerShape(10),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = backgroundColor))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Text(
                            text = item.deviceName,
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                        item.productName?.let {
                            Text(
                                text = it,
                                color = Color.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
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
                                    testCommunicationViewModel.disConnect()
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
                        testCommunicationViewModel.connect(Device.USB(item))
                    }
                }
            }
        }
    }

    @Composable
    fun singleSelectButtonBluetoothPage(
        isAlwaysConnecting: Boolean,
        item: BluetoothDevice,
        isSelected: Boolean,
        context: Context,
        onTap: () -> Unit,
        onTapCancle: () -> Unit,
        testCommunicationViewModel: TestCommunicationViewModel,
//        bluetoothViewModel: BluetoothViewModel,
        isConnectingDevice: Boolean
    ) {
        val backgroundColor = if (isSelected) R.color.grey7 else R.color.grey5

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(bottom = 10.dp),
            onClick = {
                onTap()
//            if(isAlwaysConnecting) {
//                bluetoothViewModel.bluetoothDeviceConnect(item, context)
//            } else {
//                bluetoothViewModel.bluetoothDeviceResist(item)
//            }
            },
            shape = RoundedCornerShape(10),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = backgroundColor))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
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
                }
                if (isSelected && !isAlwaysConnecting) {
                    testCommunicationViewModel.bluetoothDeviceResister(item)
//                    if(isConnectingDevice) {
//                        testCommunicationViewModel.disConnect()
//                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Button(
                            modifier = Modifier
                                .width(120.dp)
                                .height(70.dp),
                            onClick = {
                                onTapCancle()
                                testCommunicationViewModel.bluetoothDeviceUnResister()
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
                    Log.w("isConnectingDevice222", isConnectingDevice.toString())
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
                                    onTapCancle()
                                    testCommunicationViewModel.disConnect()
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
                        Log.w("notisConnectingDevice", "notisConnectingDevice")
                        testCommunicationViewModel.connect(Device.Bluetooth(item))
//                        bluetoothViewModel.bluetoothDeviceConnect(item)
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