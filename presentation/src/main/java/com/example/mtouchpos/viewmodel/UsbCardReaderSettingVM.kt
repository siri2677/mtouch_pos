package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.cardreader.CardReaderData
import com.example.domain.usecase.cardreader.ConnectCardReader
import com.example.domain.usecase.cardreader.DeleteDeviceInfo
import com.example.domain.usecase.cardreader.FetchConnectedDeviceInfo
import com.example.domain.usecase.cardreader.UpdateConnectedDeviceInfo
import com.example.mtouchpos.viewmodel.mapper.toDeviceConnectState
import com.example.mtouchpos.vo.info.PaymentProcessState
import com.example.mtouchpos.vo.type.DeviceType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@HiltViewModel
class UsbCardReaderSettingVM @Inject constructor(
    private val connectTestDeviceUseCase: Provider<ConnectCardReader>,
    private val updateConnectedDeviceInfoUseCase: UpdateConnectedDeviceInfo,
    private val fetchConnectedDeviceInfoUseCase: FetchConnectedDeviceInfo,
    private val deleteDeviceInfoUseCase: DeleteDeviceInfo
) : ViewModel() {
    private val _connectedDeviceInfo: SharedFlow<CardReaderData> = fetchConnectedDeviceInfoUseCase()
    val connectedDeviceInfo = _connectedDeviceInfo.map { it.deviceInformation }

    private val _deviceConnectState: MutableSharedFlow<PaymentProcessState.CommunicateCardReader> = MutableSharedFlow()
    val deviceConnectState = _deviceConnectState.asSharedFlow()

    private val _deviceInfoList: MutableStateFlow<List<DeviceType.Usb>> = MutableStateFlow(emptyList())
    val deviceInfoList = _deviceInfoList.asStateFlow()

    fun init() { connectTestDeviceUseCase.get().init() }

    fun register(
        deviceInformation: String,
        deviceName: String,
        productName: String
    ) {
        viewModelScope.launch {
            updateConnectedDeviceInfoUseCase(
                CardReaderData.Usb(
                    deviceInformation = deviceInformation,
                    deviceName = deviceName,
                    productName = productName
                )
            )
        }
    }

    fun unRegister() { deleteDeviceInfoUseCase() }

    fun connect() {
        viewModelScope.launch {
            connectTestDeviceUseCase.get().invoke(fetchConnectedDeviceInfoUseCase.getCurrentCardReaderData().deviceInformation).map {
                it.toDeviceConnectState()
            }.collect {
                _deviceConnectState.emit(it)
            }
        }
    }
}