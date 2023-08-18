package com.example.domain.usecase.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.domain.service.BluetoothConnectService
import com.example.domain.usecaseinterface.DeviceSetting
import com.example.domain.usecase.DeviceSettingSharedPreferenceImpl
import com.example.domain.usecase.ResponseDeviceSerialCommunicationImpl
import com.example.domain.usecaseinterface.ResponseDeviceSerialCommunication
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class BluetoothDeviceSetting @Inject constructor(
    private val context: Context,
    private val responseDeviceSerialCommunication: ResponseDeviceSerialCommunication,
): DeviceSetting, ServiceConnection  {

    override val permissionCheckComplete = MutableLiveData<Event<Boolean>>()
    override val isFirstConnectComplete = MutableLiveData<Event<Boolean>>()
//    override val isExistRegisteredDevice = MutableLiveData<Event<Boolean>>()

    private var bluetoothConnectService: BluetoothConnectService? = null
    private var isFirstBindingService = false
    private var isConnectComplete = false
    private var requestDataSerialCommunication: ByteArray? = null
    private var isDeviceSerialCommunication = false

    private val compositeDisposable = CompositeDisposable()

//    private var deviceServiceBinding = DeviceServiceBinding(context, this)
//    val bluetoothDeviceConnectUseCase = BluetoothDeviceConnectUseCase(context, intent)

//    override fun deviceResister(bluetoothDevice: BluetoothDevice) {
//        BluetoothDeviceRegistUseCase(context).bluetoothDeviceRegister(bluetoothDevice)
//    }
//
//    override fun deviceUnResister() {
//        BluetoothDeviceRegistUseCase(context).bluetoothDeviceUnRegister()
//    }

    override fun deviceConnect(bluetoothDeviceAddress: String) {
        if (bluetoothConnectService == null) bindingService()
        BluetoothDeviceConnectUseCase().bluetoothDeviceConnect(bluetoothDeviceAddress, context)
    }

    override fun deviceDisConnect() {
        unBindingService()
        BluetoothDeviceConnectUseCase().bluetoothDeviceDisConnect(context)
    }

    override fun bindingService() {
        DeviceSettingSharedPreferenceImpl(context).setBindingBluetoothService(true)
        val intent = Intent(context, BluetoothConnectService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    override fun unBindingService() {
        bluetoothConnectService = null
        try {
            Log.w("unBindingService1", "unBindingService")
            context.unbindService(this)
        } catch (e: Exception) {
            Log.w("exception1", e.toString())
        }
    }

    override fun requestDeviceSerialCommunication(requestDataSerialCommunication: ByteArray) {
//        isExistRegisteredDevice
        if (bluetoothConnectService == null) {
            Log.w("requestDeviceSerial", "bluetoothConnectServicenull")
            isDeviceSerialCommunication = true
            this.requestDataSerialCommunication = requestDataSerialCommunication
            bindingService()
        } else {
            Log.w("requestDeviceSerial", "requestDeviceSerialCommunication")
            BluetoothDeviceSerialCommunicate(context).requestSerialCommunicate(bluetoothConnectService!!, isConnectComplete, requestDataSerialCommunication)
        }
    }

    private fun getResultDataSerialCommunication() {
        bluetoothConnectService?.dataSubject?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { successData ->
                    responseDeviceSerialCommunication?.receiveData(successData)
                },
                { error ->
                    error.printStackTrace()
                }
            )
    }

    private fun sendData() {
        bluetoothConnectService?.isAfterConnectComplete?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { successData ->
                    Log.w("successData", "test")
                    if(successData) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            Log.w("successData", successData.toString())
                            isConnectComplete = successData
                            bluetoothConnectService?.sendData(requestDataSerialCommunication)
                        },2000)
                    } else {
                        Log.w("successData", successData.toString())
                    }
                },
                { error ->
                    Log.w("successData", "error")
                    error.printStackTrace()
                }
            )
    }

    private fun sendConnectCompleteBoolean() {
        bluetoothConnectService?.isFirstConnectComplete?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { successData ->
                    isFirstConnectComplete.value = Event(successData)
                },
                { error ->
                    error.printStackTrace()
                }
            )
    }

    fun observe(bluetoothConnectService: BluetoothConnectService): Disposable? {
        return bluetoothConnectService?.isFirstConnectComplete?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { successData ->
                    isFirstConnectComplete.value = Event(successData)
                },
                { error ->
                    error.printStackTrace()
                }
            )
    }

    fun observe1(bluetoothConnectService: BluetoothConnectService): Disposable? {
        return bluetoothConnectService?.dataSubject?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { successData ->
                    responseDeviceSerialCommunication?.setDeviceSetting(context,this)?.receiveData(successData)
                },
                { error ->
                    error.printStackTrace()
                }
            )
    }

    fun observe2(bluetoothConnectService: BluetoothConnectService): Disposable? {
        return bluetoothConnectService?.isAfterConnectComplete?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { successData ->
                    Log.w("successData", "test")
                    if(successData) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            Log.w("successData", successData.toString())
                            isConnectComplete = successData
                            bluetoothConnectService?.sendData(requestDataSerialCommunication)
                        },2000)
                    } else {
                        Log.w("successData", successData.toString())
                    }
                },
                { error ->
                    Log.w("successData", "error")
                    error.printStackTrace()
                }
            )
    }



    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as? BluetoothConnectService.MyBinder
        Log.w("onServiceConnected", "onServiceConnected")

        bluetoothConnectService = binder?.getService()
        if(!isFirstBindingService) {
            Log.w("isFirstBindingFalse", isFirstBindingService.toString())
            isFirstBindingService = true
//            observe(bluetoothConnectService!!)
//            observe1(bluetoothConnectService!!)
//            observe2(bluetoothConnectService!!)
            observe(bluetoothConnectService!!)?.let { compositeDisposable.add(it) }
            observe1(bluetoothConnectService!!)?.let { compositeDisposable.add(it) }
            observe2(bluetoothConnectService!!)?.let { compositeDisposable.add(it) }
        }
        if(isDeviceSerialCommunication) {
            Log.w("isDeviceSerialTrue", isDeviceSerialCommunication.toString())
            isDeviceSerialCommunication = false
            BluetoothDeviceSerialCommunicate(context).requestSerialCommunicate(bluetoothConnectService!!, isConnectComplete, requestDataSerialCommunication!!)
        }
        Log.w("deviceConnect", bluetoothConnectService.toString())
        Log.w("isDeviceSerialFalse", isDeviceSerialCommunication.toString())
        Log.w("isFirstBindingTrue", isFirstBindingService.toString())
        Log.w("compositeDisposable", compositeDisposable.size().toString())
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.w("onServiceDisconnected", "onServiceDisconnected")
//        compositeDisposable.dispose()
//        test2?.dispose()
//        test3?.dispose()
    }
}