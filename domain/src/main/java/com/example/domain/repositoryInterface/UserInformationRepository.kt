package com.example.domain.repositoryInterface

import com.example.domain.dto.request.tms.RequestGetUserInformationDto

interface UserInformationRepository {
    suspend fun getUserInformation(tmnId: String): RequestGetUserInformationDto
    suspend fun insertUserInformation(responseGetUserInformationDto: RequestGetUserInformationDto)
    suspend fun getAllUserInformation(): ArrayList<RequestGetUserInformationDto>
    suspend fun deleteUserInformation(tmnId: String)
}