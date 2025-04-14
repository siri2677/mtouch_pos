package com.kwonps.mtouchpos.view.ui

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState
import com.kwonps.mtouchpos.viewmodel.UsbCardReaderSettingVM
import com.google.gson.Gson

fun infoFormat(info: String) = info.replace(Regex("""/dev/bus/usb/\d+/\d+="""), "")
    .replace(Regex("""mSerialNumberReader=[^,]+,"""), "")
    .replace(Regex("""mName=[^,]+,"""), "")
    .replace(Regex(""" mHasAudioPlayback=[^,]+, """), "")
    .replace(Regex("""mHasAudioCapture=[^,]+, """), "")
    .replace(Regex("""mHasMidi=[^,]+, """), "")
    .replace(Regex("""mHasVideoCapture=[^,]+, """), "")
    .replace(Regex("""mHasVideoPlayback=[^,]+, """), "")

fun getUsbPermissionsArray() = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
        POST_NOTIFICATIONS
    )
    else -> emptyArray()
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
@Composable
fun UsbList(
    navController: NavController,
    usbCardReaderSettingVM: UsbCardReaderSettingVM = hiltViewModel()
) {
    val context = LocalContext.current
    val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    val permissionArray = getUsbPermissionsArray()
    val actionUSBPermission = "actionUSBPermission"
    val actionUSBPermissionAfterRegister = "actionUSBPermissionAfterRegister"

    lateinit var device: UsbDevice
    var devices by remember { mutableStateOf(usbManager.deviceList.values.toList()) }
    var isPermissionGranted = remember {
        mutableStateOf(
            permissionArray.all {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.checkSelfPermission(it) == PERMISSION_GRANTED
                } else {
                    true
                }
            }
        )
    }
    val permissionBroadcastReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                        devices = usbManager.deviceList.values.toList()
                    }

                    UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                        devices = usbManager.deviceList.values.toList()
                    }

                    actionUSBPermission -> {
                        val usbDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                        } else {
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        }

                        if (usbManager.hasPermission(usbDevice)) {
                            registerDevice(usbCardReaderSettingVM, usbDevice!!)
                            navigateErrorDialog(navController, "권한이 허용되었습니다.")
                        } else {
                            navigateErrorDialog(navController, "권한 허용 후 결제 진행할 수 있습니다.")
                        }
                    }

                    actionUSBPermissionAfterRegister -> {
                        val usbDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                        } else {
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        }

                        if (usbManager.hasPermission(usbDevice)) {
                            registerDevice(usbCardReaderSettingVM, usbDevice!!)
                        } else {
                            navigateErrorDialog(navController, "권한 허용 후 기기 등록할 수 있습니다.")
                        }
                    }
                }
            }
        }
    }
    val resultStateFlow = remember(navController.currentBackStackEntry) {
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Boolean?>("result", null)
    }
    var result = resultStateFlow?.collectAsStateWithLifecycle()?.value

    LaunchedEffect(result) {
        result?.let {
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("result")
            if (it) {
                isPermissionGranted.value = true
                processDevicePermission(
                    navController = navController,
                    context = context,
                    permissionArray = permissionArray,
                    isPermissionGranted = isPermissionGranted.value,
                    usbCardReaderSettingVM = usbCardReaderSettingVM,
                    usbManager = usbManager,
                    usbDeviceInfo = device,
                    action = actionUSBPermissionAfterRegister
                )
            } else {
                navigateErrorDialog(navController, "권한 설정 후 기기 검색할 수 있습니다.")
            }
        }
    }

    DisposableEffect(Unit) {
        val filter = IntentFilter().apply {
            addAction(actionUSBPermission)
            addAction(actionUSBPermissionAfterRegister)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(permissionBroadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(permissionBroadcastReceiver, filter)
        }
        onDispose {
            context.unregisterReceiver(permissionBroadcastReceiver)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if(devices.isEmpty()) {
            Text(text = "연결된 USB 장치가 없습니다")
        } else {
            LazyColumn(
                modifier = Modifier
                    .width((LocalConfiguration.current.screenWidthDp * 0.85).dp)
                    .fillMaxHeight()
                    .padding(top = 15.dp, bottom = 15.dp)
            ) {
                item {
                    devices.forEachIndexed { index, usbDeviceInfo ->
                        val connectedDeviceInfo = usbCardReaderSettingVM.connectedDeviceInfo.collectAsStateWithLifecycle("").value
                        val isSelected = infoFormat(connectedDeviceInfo) == infoFormat(usbDeviceInfo.toString())

                        ConnectButton(
                            firstLine = usbDeviceInfo.deviceName,
                            secondLine = usbDeviceInfo.productName!!,
                            isSelected = isSelected,
                            onTap = {
                                device = usbDeviceInfo
                                if(isSelected) {
                                    usbCardReaderSettingVM.unRegister()
                                } else {
                                    processDevicePermission(
                                        navController = navController,
                                        context = context,
                                        permissionArray = permissionArray,
                                        isPermissionGranted = isPermissionGranted.value,
                                        usbCardReaderSettingVM = usbCardReaderSettingVM,
                                        usbManager = usbManager,
                                        usbDeviceInfo = usbDeviceInfo,
                                        action = actionUSBPermissionAfterRegister
                                    )
                                }
                            },
                            additionButton = {
                                Row(modifier = Modifier.padding(bottom = 5.dp)) {
                                    if(!usbManager.hasPermission(usbDeviceInfo)) {
                                        AdditionButton("권한 재요청") {
                                            usbManager.requestPermission(usbDeviceInfo, requestPermission(context, actionUSBPermission, usbDeviceInfo))
                                        }
                                    } else {
                                        AdditionButton("연결 테스트") {
                                            navController.navigate(
                                                route = NavigationGraphState.DeviceSettingView.USBConnectDialog.name,
                                                navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun processDevicePermission(
    navController: NavController,
    context: Context,
    permissionArray: Array<String>,
    isPermissionGranted: Boolean,
    usbCardReaderSettingVM: UsbCardReaderSettingVM,
    usbManager: UsbManager,
    usbDeviceInfo: UsbDevice,
    action: String
) {
    if (!isPermissionGranted) {
        navController.navigate("connectScreen/${Gson().toJson(permissionArray)}")
    } else if (usbManager.hasPermission(usbDeviceInfo)) {
        registerDevice(usbCardReaderSettingVM, usbDeviceInfo)
    } else {
        usbManager.requestPermission(usbDeviceInfo, requestPermission(context, action, usbDeviceInfo))
    }
}

private fun registerDevice(
    usbCardReaderSettingVM: UsbCardReaderSettingVM,
    usbDeviceInfo: UsbDevice
) {
    usbCardReaderSettingVM.register(
        deviceInformation = usbDeviceInfo.toString(),
        deviceName = usbDeviceInfo.deviceName,
        productName = usbDeviceInfo.productName!!
    )
}

private fun requestPermission(
    context: Context,
    action: String,
    usbDeviceInfo: UsbDevice
) = PendingIntent.getBroadcast(
    context,
    0,
    Intent(action).apply { putExtra(UsbManager.EXTRA_DEVICE, usbDeviceInfo) },
    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
)