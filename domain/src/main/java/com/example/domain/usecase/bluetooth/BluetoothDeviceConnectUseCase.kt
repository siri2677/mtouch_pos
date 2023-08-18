package com.example.domain.usecase.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.MutableLiveData
import com.example.domain.service.BluetoothConnectService
import com.example.domain.usecase.DeviceSettingSharedPreferenceImpl
import com.example.domain.usecase.ResponseDeviceSerialCommunicationImpl
import com.example.domain.usecase.usb.UsbDeviceConnectUsecase
import com.example.domain.usecase.usb.UsbDeviceSetting
import com.example.domain.usecaseinterface.bluetooth.BluetoothDeviceConnectUseCaseImpl
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

class BluetoothDeviceConnectUseCase {
    fun bluetoothDeviceConnect(bluetoothDeviceAddress: String, context: Context) {
        DeviceSettingSharedPreferenceImpl(context).setKeepBluetoothConnection(true)
        UsbDeviceConnectUsecase().usbDeviceDisConnect(context)
        val intent = Intent(context, BluetoothConnectService::class.java)
        startForegroundService(context, intent)
    }

    fun bluetoothDeviceDisConnect(context: Context) {
        DeviceSettingSharedPreferenceImpl(context).setBluetoothDisConnectButtonClick(true)
        val intent = Intent(context, BluetoothConnectService::class.java)
        context.stopService(intent)
    }

//    override fun bindingService(context: Context) {
//        DeviceSettingSharedPreferenceImpl(context).setBindingBluetoothService(true)
//        val intent = Intent(context, BluetoothConnectService::class.java)
//        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
//    }
//
//    override fun unBindingService() {
//        bluetoothConnectService = null
//        context.unbindService(BluetoothDeviceConnectUseCase())
//    }
//
//    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onServiceDisconnected(name: ComponentName?) {
//        TODO("Not yet implemented")
//    }


}