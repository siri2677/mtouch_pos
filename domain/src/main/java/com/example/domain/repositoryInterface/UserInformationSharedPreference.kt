package com.example.domain.repositoryInterface

import com.example.domain.model.response.tms.ResponseTmsModel


interface UserInformationSharedPreference {
    fun getUserInformation(): ResponseTmsModel.GetUserInformation?
    fun setUserInformation(responseGetUserInformationDto: ResponseTmsModel.GetUserInformation)
}