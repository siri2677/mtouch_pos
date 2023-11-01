package com.example.data.repository.db

import com.example.data.dao.UserInformationDao
import com.example.data.mapper.db.UserInformationMapper
import com.example.domain.dto.request.tms.RequestGetUserInformationDto
import com.example.domain.repositoryInterface.UserInformationRepository
import org.mapstruct.factory.Mappers

class UserInformationRepositoryImpl(private val userInformationDao: UserInformationDao): UserInformationRepository {
    override suspend fun getUserInformation(tmnId: String): RequestGetUserInformationDto {
        return Mappers.getMapper(UserInformationMapper::class.java).convertToModel(userInformationDao.getUserInformation(tmnId))
    }

    override suspend fun insertUserInformation(requestGetUserInformationDto: RequestGetUserInformationDto){
        userInformationDao.insertUserInformation(Mappers.getMapper(UserInformationMapper::class.java).convertToEntity(requestGetUserInformationDto))
    }

    override suspend fun getAllUserInformation(): ArrayList<RequestGetUserInformationDto> {
        var userInformationList = ArrayList<RequestGetUserInformationDto>()
        for(information in userInformationDao.getAllUserInformation()) {
            userInformationList.add(Mappers.getMapper(UserInformationMapper::class.java).convertToModel(information))
        }
        return userInformationList
    }

    override suspend fun deleteUserInformation(tmnId: String) {
        userInformationDao.deleteUserInformation(tmnId)
    }

}