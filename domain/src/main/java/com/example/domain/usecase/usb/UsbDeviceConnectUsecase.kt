package com.example.domain.usecase.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.USB_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.MutableLiveData
import com.example.domain.usecaseinterface.usb.UsbDeviceConnectUsecaseImpl
import com.example.domain.service.UsbConnectService
import com.example.domain.usecase.DeviceSettingSharedPreferenceImpl
import com.example.domain.usecase.ResponseDeviceSerialCommunicationImpl
import com.example.domain.usecase.bluetooth.BluetoothDeviceConnectUseCase
import com.example.domain.usecase.bluetooth.BluetoothDeviceSetting
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event
import javax.inject.Inject

class UsbDeviceConnectUsecase {
    private var actionGrantUsb = "ACTION_GRANT_USB"
    private val usbPermissionReceiver = UsbPermissionReceiver()

    private fun getUsbDevice(context: Context): UsbDevice? {
        return UsbDeviceSearchUseCase(context).getUsbDevice()
    }

    fun isDevicePermissionGrant(context: Context): Boolean {
        val usbManager: UsbManager = context.getSystemService(USB_SERVICE) as UsbManager
        val usbDevice = getUsbDevice(context)
        return usbManager.hasPermission(usbDevice)
    }

    fun usbDeviceConnect(context: Context) {
//        BluetoothDeviceSetting(context, ResponseDeviceSerialCommunicationImpl()).unBindingService()
        BluetoothDeviceConnectUseCase().bluetoothDeviceDisConnect(context)
        val intent = Intent(context, UsbConnectService::class.java)
        startForegroundService(context, intent)
    }

    fun usbDevicePermissionRequest(context: Context) {
        val usbManager: UsbManager = context.getSystemService(USB_SERVICE) as UsbManager
        val filter = IntentFilter(actionGrantUsb)
        context.registerReceiver(usbPermissionReceiver, filter)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        var intent = Intent(actionGrantUsb)
        val permissionIntent: PendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, flags)
        usbManager.requestPermission(getUsbDevice(context), permissionIntent)
    }

    private inner class UsbPermissionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == actionGrantUsb) {
                if (isDevicePermissionGrant(context)) { usbDeviceConnect(context) }
                context.unregisterReceiver(this)
            }
        }
    }

    fun usbDeviceDisConnect(context: Context) {
        DeviceSettingSharedPreferenceImpl(context).setUsbDisConnectButtonClick(true)
        val intent = Intent(context, UsbConnectService::class.java)
        context.stopService(intent)
    }


}