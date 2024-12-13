package com.example.data.repositoryImpl

import android.content.SharedPreferences
import com.example.domain.repository.DeviceRepository
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val key: String
): DeviceRepository {
    override fun getDeviceInformation(): String? = sharedPreferences.getString(key, null)

    override fun setDeviceInformation(deviceInformation: String) {
        sharedPreferences.edit().putString(key, deviceInformation).apply()
    }
}