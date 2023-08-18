package com.example.domain.usecase.usb

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.example.domain.usecase.DeviceSettingSharedPreferenceImpl
import com.example.domain.usecaseinterface.usb.UsbDeviceSearchUsecaseImpl
import javax.inject.Inject

class UsbDeviceSearchUseCase @Inject constructor(private val context: Context)
    : UsbDeviceSearchUsecaseImpl {

    override fun searchUsbDevice(): ArrayList<UsbDevice> {
        var usbDeviceArrayList: ArrayList<UsbDevice> = ArrayList()
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList: HashMap<String, UsbDevice> = manager.deviceList
        deviceList.values.forEach { device -> usbDeviceArrayList.add(device) }
        return usbDeviceArrayList
    }

    fun getUsbDevice(): UsbDevice? {
        for(device in UsbDeviceSearchUseCase(context).searchUsbDevice()) {
            if(stringFormat(device.toString()) == stringFormat(DeviceSettingSharedPreferenceImpl(context).getUsbDeviceInformation())) {
                return device
            }
        }
        return null
    }

    private fun stringFormat(string: String): String {
        val regex = Regex("""mSerialNumberReader=[^,]+""")
        return regex.replace(string, "")
    }
//        var usbDeviceArrayList: ArrayList<UsbDeviceInfo> = ArrayList()
//        var usbDeviceSet: MutableSet<String> = mutableSetOf()
//        var gson = GsonBuilder().create()
//        var contact_UsbDeviceInfo: String?
//
//        val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
//        val usbDefaultProber = UsbSerialProber.getDefaultProber()
//        val usbCustomProber = getCustomProber()
//        if(usbManager.deviceList.values != null) {
//            for (device in usbManager.deviceList.values) {
//                var driver = usbDefaultProber.probeDevice(device)
//                if (driver == null) {
//                    driver = usbCustomProber.probeDevice(device)
//                }
//                if (driver != null) {
//                    for (port in driver.ports.indices) {
//                        Log.w("indices", driver.ports.indices.toString())
//                        Log.w("deviceName", driver.device.deviceName)
//                        driver.device.manufacturerName?.let { Log.w("manufacturerName", it) }
//                        driver.device.productName?.let { Log.w("productName", it) }
////                        driver.device.serialNumber?.let { Log.w("driver4", it) }
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                            Log.w("version", driver.device.version)
//                        }
//                        Log.w("configurationCount", driver.device.configurationCount.toString())
//                        Log.w("deviceClass", driver.device.deviceClass.toString())
//                        Log.w("deviceId", driver.device.deviceId.toString())
//                        Log.w("deviceProtocol", driver.device.deviceProtocol.toString())
//                        Log.w("deviceSubclass", driver.device.deviceSubclass.toString())
//                        Log.w("interfaceCount", driver.device.interfaceCount.toString())
//                        Log.w("productId", driver.device.productId.toString())
//                        Log.w("vendorId", driver.device.vendorId.toString())
//                        Log.w("getInterface", driver.device.getInterface(0).toString())
//                        Log.w("getConfiguration", driver.device.getConfiguration(0).toString())
//                        Log.w("getConfiguration", driver.device.getConfiguration(0).toString())
//                        var usbDevice = UsbDeviceInfo()
//                        Log.w("scanUsb", device.toString())
//                        usbDevice.device = device
//                        usbDevice.driver = driver
//                        usbDevice.port = port
////                        connect(usbDeviceInfo)
////                        send(EncMSRManager.makeDongleInfo())
//                        contact_UsbDeviceInfo = gson.toJson(usbDevice, UsbDeviceInfo::class.java)
//                        usbDeviceSet.add(contact_UsbDeviceInfo)
//                        usbDeviceArrayList.add(usbDevice)
//                    }
//                } else {
//                    var usbDevice = UsbDeviceInfo()
//                    Log.w("scanUsb1", device.toString())
//                    usbDevice.device = device
//                    usbDevice.driver = null
//                    usbDevice.port = 0
//
//                    contact_UsbDeviceInfo = gson.toJson(usbDevice, UsbDeviceInfo::class.java)
//                    usbDeviceSet.add(contact_UsbDeviceInfo)
//                    usbDeviceArrayList.add(usbDevice)
//                }
//            }
//            Log.w("scanUsb2", "null")
//            SharedPreferenceUtil.putStringSet(context, "usbDevice", usbDeviceSet!!)
//            return usbDeviceArrayList
//        } else {
//            Log.w("scanUsb3", "null")
//            SharedPreferenceUtil.putData(context, "usbDevice", "null")
//            return null
//        }
//    }
}