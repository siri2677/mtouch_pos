package com.example.data.sharedpreference

import android.content.Context
import com.example.domain.model.response.tms.ResponseTmsModel
import com.example.domain.repositoryInterface.UserInformationSharedPreference
import com.google.gson.Gson
import javax.inject.Inject

class UserInformationSharedPreferenceImpl @Inject constructor(
    private val context: Context
): UserInformationSharedPreference {
    override fun getUserInformation(): ResponseTmsModel.GetUserInformation? {
        return Gson().fromJson(
            context.getSharedPreferences(
                "UserInformation",
                Context.MODE_PRIVATE
            ).getString("UserInformation", ""),
            ResponseTmsModel.GetUserInformation::class.java
        )
    }

    override fun setUserInformation(responseGetUserInformationDto: ResponseTmsModel.GetUserInformation) {
        context.getSharedPreferences("UserInformation", Context.MODE_PRIVATE)
            .edit()
            .putString("UserInformation", Gson().toJson(responseGetUserInformationDto))
            .apply()
    }
}