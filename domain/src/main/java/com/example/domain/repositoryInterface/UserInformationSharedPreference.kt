package com.example.domain.repositoryInterface

import com.example.domain.dto.response.tms.ResponseGetUserInformationDto

interface UserInformationSharedPreference {
    fun getUserInformation(): ResponseGetUserInformationDto
    fun setUserInformation(responseGetUserInformationDto: ResponseGetUserInformationDto)
}