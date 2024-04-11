package com.example.domain.repositoryInterface

import com.example.domain.model.request.tms.RequestTmsModel


interface UserInformationRepository {
    suspend fun getUserInformation(tmnId: String): RequestTmsModel.GetUserInformation
    suspend fun insertUserInformation(responseGetUserInformationDto: RequestTmsModel.GetUserInformation)
    fun getAllUserInformation(): List<RequestTmsModel.GetUserInformation>
    suspend fun deleteUserInformation(tmnId: String)
}