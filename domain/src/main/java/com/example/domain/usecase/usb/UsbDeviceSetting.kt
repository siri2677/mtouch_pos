package com.example.domain.usecase.usb

import android.app.PendingIntent
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.domain.service.BluetoothConnectService
import com.example.domain.service.UsbConnectService
import com.example.domain.usecase.DeviceSettingSharedPreferenceImpl
import com.example.domain.usecaseinterface.DeviceSetting
import com.example.domain.usecase.ResponseDeviceSerialCommunicationImpl
import com.example.domain.usecase.bluetooth.BluetoothDeviceSerialCommunicate
import com.example.domain.usecase.bluetooth.BluetoothDeviceSetting
import com.example.domain.usecaseinterface.ResponseDeviceSerialCommunication
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.Exception
import javax.inject.Inject

class UsbDeviceSetting @Inject constructor(
    private val context: Context,
    private val responseDeviceSerialCommunication: ResponseDeviceSerialCommunication
) : DeviceSetting, ServiceConnection {

    override val isFirstConnectComplete = MutableLiveData<Event<Boolean>>()
    override val permissionCheckComplete = MutableLiveData<Event<Boolean>>()
    private val compositeDisposable = CompositeDisposable()

    lateinit var requestDataSerialCommunication: ByteArray
    private var usbConnectService: UsbConnectService? = null
    private var isDeviceSerialCommunication = false
    private var isFirstBindingService = true

    override fun deviceConnect(usbDeviceInformation: String) {
        if (usbConnectService == null) bindingService()
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        if (usbManager.hasPermission(UsbDeviceSearchUseCase(context).getUsbDevice())) {
            permissionCheckComplete.value = Event(true)
            UsbDeviceConnectUsecase().usbDeviceConnect(context)
        } else {
            permissionCheckComplete.value = Event(false)
            UsbDeviceConnectUsecase().usbDevicePermissionRequest(context)
        }
    }

    override fun deviceDisConnect() {
        unBindingService()
        UsbDeviceConnectUsecase().usbDeviceDisConnect(context)
    }

    override fun bindingService() {
        DeviceSettingSharedPreferenceImpl(context).setBindingBluetoothService(true)
        val intent = Intent(context, UsbConnectService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    override fun unBindingService() {
        usbConnectService = null
        try {
            Log.w("unBindingService2", "unBindingService")
            compositeDisposable.dispose()
            context.unbindService(this)
        } catch (e: Exception){
            Log.w("exception2", e.toString())
        }
    }

    override fun requestDeviceSerialCommunication(requestDataSerialCommunication: ByteArray) {
        if (usbConnectService == null) {
            isDeviceSerialCommunication = true
            this.requestDataSerialCommunication = requestDataSerialCommunication
            bindingService()
        } else {
            UsbDeviceSerialCommunicationUsecase().requestDeviceSerialCommunication(usbConnectService, requestDataSerialCommunication)
        }
    }

    private fun getResultDataSerialCommunication(usbConnectService: UsbConnectService): Disposable? {
        return usbConnectService?.dataSubject?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { successData ->
                    Log.w("successData", successData.size.toString())
                    responseDeviceSerialCommunication.setDeviceSetting(context,this).receiveData(successData)
                },
                { error ->
                    error.printStackTrace()
                }
            )
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as UsbConnectService.MyBinder
        usbConnectService = binder.getService()
        if(isFirstBindingService) {
            isFirstBindingService = false
            getResultDataSerialCommunication(usbConnectService!!)?.let { compositeDisposable.add(it) }
        }
        if(isDeviceSerialCommunication) {
            isDeviceSerialCommunication = false
            UsbDeviceSerialCommunicationUsecase().requestDeviceSerialCommunication(usbConnectService, requestDataSerialCommunication!!)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {}
}