package com.example.cleanarchitech_text_0506.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleanarchitech_text_0506.view.ui.theme.RunningState
import com.example.domain.usecaseinterface.RequestDeviceSerialCommunication
import com.example.domain.usecaseinterface.ResponseDeviceSerialCommunication
import com.mtouch.ksr02_03_04_v2.Utils.Device.Event
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class DeviceSerialCommunicateViewModel @Inject constructor(
    private val responseDeviceSerialCommunication: ResponseDeviceSerialCommunication,
    private val requestDeviceSerialCommunication: RequestDeviceSerialCommunication
): ViewModel(), Serializable{
    private var job: Job? = null

    private var _isRunning by mutableStateOf(RunningState.STOPPED)
    val isRunning
        get() = _isRunning

    private var _hour by mutableStateOf(0)
    var hour: Int
        get() = _hour
        set(value) {
            _hour = value
        }

    private var _minute by mutableStateOf(0)
    var minute: Int
        get() = _minute
        set(value) {
            _minute = value
        }

    private var _second by mutableStateOf(0)
    var second: Int
        get() = _second
        set(value) {
            _second = value
        }


    private val _leftTime = MutableLiveData<Event<Int>>()
    val leftTime: MutableLiveData<Event<Int>> = _leftTime

    private var dialogMessage: String = ""
    var dialogMessageProperty: String
        get() = dialogMessage
        set(value) { dialogMessage = value }

    private var owner: LifecycleOwner? = null
    var ownerProperty: LifecycleOwner?
        get() = owner
        set(value) { owner = value }

    val errorOccur: MutableLiveData<Event<Boolean>>
        get() = requestDeviceSerialCommunication.errorOccur

    val serialCommunicateMessage: MutableLiveData<Event<String?>?>
        get() = responseDeviceSerialCommunication.serialCommunicationMessage

    val notExistRegisteredDevice: MutableLiveData<Event<String>>
        get() = requestDeviceSerialCommunication.notExistRegisteredDevice

    val isCompletePayment: MutableLiveData<Event<Boolean?>?>
        get() = responseDeviceSerialCommunication.isCompletePayment


    fun requestDeviceSerialCommunication(byteArray: ByteArray) {
        requestDeviceSerialCommunication.getDeviceType()?.requestDeviceSerialCommunication(byteArray)
    }

    fun init() {
        responseDeviceSerialCommunication.init()
    }

    fun getCurrentRegisteredDeviceType(): String? {
        return requestDeviceSerialCommunication.getCurrentRegisteredDeviceType()
    }

    private fun decreaseSecond(minute: Int): Flow<Int> = flow {
        var minute = minute
        if(minute == 0) job?.cancel()
        while (minute > 0) {
            delay(1000)
            emit(--minute)
//            Log.w("minute1", minute.toString())
            _leftTime.value = Event(minute)
        }
    }

    fun startCountDownTimer(minute: Int) {
        job = decreaseSecond(minute).onEach {
//            _leftTime.value = it
        }.launchIn(viewModelScope)
    }

}