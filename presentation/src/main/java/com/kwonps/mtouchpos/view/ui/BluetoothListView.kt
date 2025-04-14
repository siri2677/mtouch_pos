package com.kwonps.mtouchpos.view.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.ParcelUuid
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.kwonps.mtouchpos.R
import com.kwonps.mtouchpos.view.navgraph.NavigationGraphState
import com.kwonps.mtouchpos.viewmodel.BluetoothCardReaderSettingVM
import com.google.gson.Gson
import java.util.UUID
import kotlin.Boolean

fun getBluetoothPermissionsArray() = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
        BLUETOOTH_SCAN,
        BLUETOOTH_CONNECT,
        POST_NOTIFICATIONS
    )
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> arrayOf(
        BLUETOOTH_SCAN,
        BLUETOOTH_CONNECT
    )
    else -> arrayOf(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION,
        BLUETOOTH
    )
}

@Composable
fun BluetoothList(
    navController: NavController,
    bluetoothCardReaderSettingVM: BluetoothCardReaderSettingVM = hiltViewModel()
) {
    val context = LocalContext.current
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
    val permissionArray = getBluetoothPermissionsArray()
    var devices by remember { mutableStateOf(listOf<ScanResult>()) }
    var isSearching = remember { mutableStateOf(false) }
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
    var isBluetoothEnabled = remember { mutableStateOf(bluetoothAdapter?.isEnabled == true) }
    val scannerCallback by remember {
        mutableStateOf(
            object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    if (devices.none { it.device.address == result.device.address }) {
                        result.let { devices = devices + it }
                    }
                }
            }
        )
    }
    val resultStateFlow = remember(navController.currentBackStackEntry) {
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Boolean?>("result", null)
    }
    var result = resultStateFlow?.collectAsStateWithLifecycle()?.value

    val bluetoothEnableScreen = bluetoothEnableScreen(
        navController = navController,
        scannerCallback = scannerCallback,
        isSearching = isSearching,
        isBluetoothEnabled = isBluetoothEnabled,
        bluetoothAdapter = bluetoothAdapter
    )

    LaunchedEffect(result) {
        result?.let {
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("result")
            if (it) {
                isPermissionGranted.value = true
                bluetoothEnableScreen.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } else {
                isSearching.value = false
                navigateErrorDialog(navController, "권한 설정 후 기기 검색할 수 있습니다.")
            }
        }
    }

    LaunchedEffect(isSearching.value) {
        handleSearchToggle(
            navController = navController,
            bluetoothAdapter = bluetoothAdapter,
            isSearching = isSearching.value,
            permissionArray = permissionArray,
            scannerCallback = scannerCallback
        )
    }

    BluetoothDevicesColumn(
        navController = navController,
        bluetoothCardReaderSettingVM = bluetoothCardReaderSettingVM,
        devices = devices,
        isSearching = isSearching.value && isPermissionGranted.value && isBluetoothEnabled.value,
        toggleSearch = { isSearching.value = !isSearching.value }
    )
}

@SuppressLint("MissingPermission")
@Composable
fun bluetoothEnableScreen(
    navController: NavController,
    scannerCallback: ScanCallback,
    isBluetoothEnabled: MutableState<Boolean>,
    isSearching: MutableState<Boolean>,
    bluetoothAdapter: BluetoothAdapter
): ManagedActivityResultLauncher<Intent, ActivityResult> = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        isBluetoothEnabled.value = true
        val serviceUUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
        val filters = listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(UUID.fromString(serviceUUID))).build())
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
        bluetoothAdapter.bluetoothLeScanner.startScan(filters, settings, scannerCallback)
    } else {
        isSearching.value = false
        navigateErrorDialog(navController, "블루투스 설정을 키신 후\n기기 검색 할 수 있습니다.")
    }
}

@SuppressLint("MissingPermission")
fun handleSearchToggle(
    navController: NavController,
    bluetoothAdapter: BluetoothAdapter,
    isSearching: Boolean,
    permissionArray: Array<String>,
    scannerCallback: ScanCallback
) {
    if (isSearching) {
        navController.navigate("connectScreen/${Gson().toJson(permissionArray)}")
    } else {
        bluetoothAdapter.bluetoothLeScanner?.stopScan(scannerCallback)
    }
}

@Composable
fun BluetoothDevicesColumn(
    navController: NavController,
    bluetoothCardReaderSettingVM: BluetoothCardReaderSettingVM,
    devices: List<ScanResult>,
    isSearching: Boolean,
    toggleSearch: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DeviceListScreen(
            modifier = Modifier.weight(0.9f),
            navController = navController,
            bluetoothCardReaderSettingVM = bluetoothCardReaderSettingVM,
            devices = devices,
            isSearching = isSearching
        )

        SearchToggleButton(
            modifier = Modifier.weight(0.08f),
            isSearching = isSearching,
            toggleSearching = toggleSearch
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceListScreen(
    modifier: Modifier,
    isSearching: Boolean,
    navController: NavController,
    bluetoothCardReaderSettingVM: BluetoothCardReaderSettingVM,
    devices: List<ScanResult>
) {
    if(devices.isEmpty() && isSearching) {
        Box(modifier = modifier.fillMaxSize()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "탐색된 블루투스 장치가 없습니다"
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .width((LocalConfiguration.current.screenWidthDp * 0.85).dp)
                .padding(top = 15.dp, bottom = 15.dp)
        ) {
            item {
                devices.forEachIndexed { index, bluetoothDeviceInfo ->
                    val connectedDeviceInfo = bluetoothCardReaderSettingVM.connectedDeviceInfo.collectAsStateWithLifecycle("").value
                    val isSelected = connectedDeviceInfo == bluetoothDeviceInfo.device.address

                    ConnectButton(
                        firstLine = bluetoothDeviceInfo.device.name,
                        secondLine = bluetoothDeviceInfo.device.address,
                        isSelected = isSelected,
                        onTap = {
                            if(isSelected) {
                                bluetoothCardReaderSettingVM.unRegister()
                            } else {
                                bluetoothCardReaderSettingVM.register(bluetoothDeviceInfo.device.name, bluetoothDeviceInfo.device.address)
                            }
                        },
                        additionButton = {
                            AdditionButton("연결 테스트") {
                                navController.navigate(
                                    route = NavigationGraphState.DeviceSettingView.BluetoothConnectDialog.name,
                                    navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun SearchToggleButton(
    modifier: Modifier,
    isSearching: Boolean,
    toggleSearching: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colorResource(id = R.color.pink))
            .clickable(onClick = toggleSearching),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSearching) "장치 검색중....." else "장치 검색 하기",
            fontSize = 17.sp,
            color = colorResource(id = R.color.white),
            fontFamily = FontFamily(Font(R.font.ns_acr))
        )
    }
}