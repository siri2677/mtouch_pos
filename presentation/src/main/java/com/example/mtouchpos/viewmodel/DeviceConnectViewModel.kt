package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mtouchpos.FlowManager.Companion.deviceConnectSharedFlow
import com.example.mtouchpos.vo.DeviceConnectSharedFlow
import com.example.mtouchpos.device.DeviceConnectManagerFactory
import com.example.domain.repositoryInterface.DeviceSettingSharedPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceConnectViewModel @Inject constructor(
    private val deviceConnectManagerFactory: DeviceConnectManagerFactory,
    private val deviceSettingSharedPreference: DeviceSettingSharedPreference
): ViewModel() {
    init {
        deviceConnectSharedFlow.value == DeviceConnectSharedFlow.Init
    }

    fun setDeviceConnectSetting(deviceConnectSetting: DeviceSettingSharedPreference.DeviceConnectSetting) =
        deviceSettingSharedPreference.setDeviceConnectSetting(deviceConnectSetting)

    fun connectDevice(deviceConnectSetting: DeviceSettingSharedPreference.DeviceConnectSetting) {
        deviceConnectSetting.let {
            setDeviceConnectSetting(it)
            deviceConnectManagerFactory.getInstance(it.deviceType).connect(it.deviceInformation)
            handleDeviceConnect()
        }
    }

    private fun handleDeviceConnect() {
        if(deviceConnectSharedFlow.value == DeviceConnectSharedFlow.Init) {
            viewModelScope.launch {
                deviceConnectSharedFlow.collect {
                    when(it) {
                        is DeviceConnectSharedFlow.ConnectCompleteFlow -> {
                            deviceSettingSharedPreference.saveDeviceConnectStatus(true)
                        }
                        is DeviceConnectSharedFlow.IsDisConnected -> {
                            deviceSettingSharedPreference.saveDeviceConnectStatus(false)
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}