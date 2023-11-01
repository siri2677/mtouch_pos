package com.example.cleanarchitech_text_0506.deviceinterface

import com.example.cleanarchitech_text_0506.sealed.DeviceConnectSharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow

interface DeviceServiceController {
    var deviceConnectSharedFlow: MutableSharedFlow<DeviceConnectSharedFlow>
//    fun getDeviceConnectServiceImpl(): BluetoothDeviceConnectServiceImpl?
    fun connect(deviceInformation: String)
    fun disConnect()
    fun bindingService(process: (DeviceConnectService) -> Unit)
    fun unBindingService()
}