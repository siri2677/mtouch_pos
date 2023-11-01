package com.example.data.sharedpreference

import android.content.Context
import com.example.domain.dto.response.tms.ResponseGetUserInformationDto
import com.example.domain.repositoryInterface.UserInformationSharedPreference
import com.google.gson.Gson
import javax.inject.Inject

class UserInformationSharedPreferenceImpl @Inject constructor(
    private val context: Context
): UserInformationSharedPreference {
    override fun getUserInformation(): ResponseGetUserInformationDto {
        val sharedPreferences = context.getSharedPreferences(
            "UserInformation",
            Context.MODE_PRIVATE
        )
        val userDataJson = sharedPreferences.getString("test", "")
        return Gson().fromJson(userDataJson, ResponseGetUserInformationDto::class.java)
    }

    override fun setUserInformation(responseGetUserInformationDto: ResponseGetUserInformationDto) {
        val sharedPreferences = context.getSharedPreferences(
            "UserInformation",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        val gson = Gson().toJson(responseGetUserInformationDto)
        editor.putString("test", gson)
        editor.apply()
    }
}