package com.mtouch.domain.model

import android.hardware.usb.UsbDevice
import java.io.Serializable

class UsbDeviceInfo : Serializable  {
    var deviceInfo: ArrayList<UsbDeviceInfo> = ArrayList()
    var device: UsbDevice? = null
    var port: Int? = null
//    var driver: UsbSerialDriver? = null
    var type: String? = null
    var deviceName: String? = null
}


//data class ListItem(
//    var device: UsbDevice? = null,
//    var port: Int = 0,
//    var driver: UsbSerialDriver? = null,
//    var type: String? = null
//)
