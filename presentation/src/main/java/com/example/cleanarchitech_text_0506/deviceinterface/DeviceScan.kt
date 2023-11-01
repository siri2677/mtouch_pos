package com.example.cleanarchitech_text_0506.deviceinterface

import com.example.cleanarchitech_text_0506.sealed.DeviceList
import kotlinx.coroutines.flow.MutableSharedFlow

interface DeviceScan {
    val listUpdate: MutableSharedFlow<DeviceList>
    fun scan()
    fun cancel()
}