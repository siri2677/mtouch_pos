package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.device.DeviceConnectStatus
import com.example.domain.model.device.DeviceInfo
import com.example.domain.usecase.device.ConnectCardReader
import com.example.domain.usecase.device.SearchBluetoothDevice
import com.example.domain.usecase.device.SearchUsbDevice
import com.example.domain.usecase.device.UpdateConnectedDeviceInfo
import com.example.domain.usecase.device.manager.ConnectDeviceManager
import com.example.mtouchpos.viewmodel.usecasemanager.reader.bluetooth.ConnectBluetooth
import com.example.mtouchpos.viewmodel.usecasemanager.reader.usb.ConnectUsb
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@HiltViewModel
class DeviceSettingViewModel @Inject constructor(
    private val searchBluetoothDeviceUseCase: SearchBluetoothDevice,
    private val searchUsbDeviceUseCase: SearchUsbDevice,
    private val connectDeviceUseCase: ConnectCardReader,
    private val updateConnectedDeviceInfoUseCase: UpdateConnectedDeviceInfo
) : ViewModel() {
    enum class DeviceType { Bluetooth, Usb }

    sealed class DeviceConnectState {
        data object Connected : DeviceConnectState()
        data object Disconnected : DeviceConnectState()
    }

    data class BluetoothDeviceInfo (
        override val deviceInformation: String,
        val deviceName: String
    ) : DeviceInfo

    data class UsbDeviceInfo (
        override val deviceInformation: String,
        val deviceName: String,
        val productName: String
    ) : DeviceInfo

    private val _deviceConnectStatus: MutableStateFlow<DeviceConnectState> = MutableStateFlow(DeviceConnectState.Disconnected)
    val deviceConnectStatus = _deviceConnectStatus.asStateFlow()

    private val _deviceInfoList: MutableStateFlow<List<DeviceInfo>> = MutableStateFlow(emptyList())
    val deviceInfoList = _deviceInfoList.asStateFlow()

    private val _deviceInfo: MutableStateFlow<DeviceInfo> = MutableStateFlow(
        object: DeviceInfo { override val deviceInformation: String = "" }
    )
    val deviceInfo = _deviceInfo.asStateFlow()

    fun updateConnectedDeviceInfo(deviceInfo: DeviceInfo) {
        _deviceInfo.value = deviceInfo
        updateConnectedDeviceInfoUseCase(deviceInfo)
    }

    fun searchBluetoothDevice() {
        viewModelScope.launch {
            searchBluetoothDeviceUseCase.scan().collect { _deviceInfoList.emit(it) }
        }
    }

    fun pauseSearchBluetoothDevice() { searchBluetoothDeviceUseCase.cancel() }

    fun searchUsbDevice() { _deviceInfoList.value = searchUsbDeviceUseCase() }

    fun connectDevice(cardReaderConnectManager: ConnectDeviceManager) {
        viewModelScope.launch {
            connectDeviceUseCase(deviceInfo.value, cardReaderConnectManager).map {
                when(it) {
                    DeviceConnectStatus.ConnectComplete -> DeviceConnectState.Connected
                    DeviceConnectStatus.DisConnected -> DeviceConnectState.Disconnected
                }
            }.collect { _deviceConnectStatus.emit(it) }
        }
    }
}