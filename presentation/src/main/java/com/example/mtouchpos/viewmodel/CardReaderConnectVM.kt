package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.cardreader.CardReaderStatus
import com.example.domain.model.cardreader.CardReaderData
import com.example.domain.usecase.cardreader.ConnectCardReader
import com.example.domain.usecase.cardreader.SearchBluetoothDevice
import com.example.domain.usecase.cardreader.SearchUsbDevice
import com.example.domain.usecase.cardreader.UpdateConnectedDeviceInfo
import com.example.domain.manager.cardreader.CardReaderConnectManager
import com.example.mtouchpos.viewmodel.CardReaderConnectVM.DeviceConnectState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardReaderConnectVM @Inject constructor(
    private val searchBluetoothDeviceUseCase: SearchBluetoothDevice,
    private val searchUsbDeviceUseCase: SearchUsbDevice,
    private val connectDeviceUseCase: ConnectCardReader,
    private val updateConnectedDeviceInfoUseCase: UpdateConnectedDeviceInfo
) : ViewModel() {
    enum class DeviceType { Bluetooth, Usb }

    sealed class DeviceConnectState {
        data object Connected : DeviceConnectState()
        data object Disconnected : DeviceConnectState()
        data object Registered : DeviceConnectState()
        data class Error(val message: String) : DeviceConnectState()
    }

    data class BluetoothDeviceInfo (
        override val deviceInformation: String,
        val deviceName: String
    ) : CardReaderData

    data class UsbDeviceInfo (
        override val deviceInformation: String,
        val deviceName: String,
        val productName: String
    ) : CardReaderData

    private val _deviceConnectState: MutableStateFlow<DeviceConnectState> = MutableStateFlow(Disconnected)
    val deviceConnectState = _deviceConnectState.asStateFlow()

    private val _deviceInfoList: MutableStateFlow<List<CardReaderData>> = MutableStateFlow(emptyList())
    val deviceInfoList = _deviceInfoList.asStateFlow()

    private val _deviceInfo: MutableStateFlow<CardReaderData> = MutableStateFlow(
        object: CardReaderData { override val deviceInformation: String = "" }
    )
    val deviceInfo = _deviceInfo.asStateFlow()

    fun updateConnectedDeviceInfo(deviceInfo: CardReaderData) {
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

    fun connectDevice(cardReaderConnectManager: CardReaderConnectManager) {
        viewModelScope.launch {
            connectDeviceUseCase(deviceInfo.value, cardReaderConnectManager).map {
                when(it) {
                    CardReaderStatus.Connected -> Connected
                    is CardReaderStatus.DisConnected -> Disconnected
                    is CardReaderStatus.Error -> Error(it.message)
                    CardReaderStatus.Registered -> Registered
                }
            }.collect { _deviceConnectState.emit(it) }
        }
    }
}