package com.example.data.room.repositoryimplement

import com.example.data.room.dao.UserInformationDAO
import com.example.data.room.mapper.UserInformationMapper
import com.example.domain.model.request.tms.RequestTmsModel
import com.example.domain.repositoryInterface.UserInformationRepository
import org.mapstruct.factory.Mappers

class UserInformationRepositoryImpl(private val userInformationDao: UserInformationDAO): UserInformationRepository {
    override suspend fun getUserInformation(tmnId: String): RequestTmsModel.GetUserInformation {
        return Mappers.getMapper(UserInformationMapper::class.java).toRequestGetUserInformationModel(userInformationDao.getUserInformation(tmnId))
    }

    override suspend fun insertUserInformation(requestGetUserInformationDto: RequestTmsModel.GetUserInformation){
        userInformationDao.insertUserInformation(Mappers.getMapper(UserInformationMapper::class.java).toUserInformationEntity(requestGetUserInformationDto))
    }

    override fun getAllUserInformation(): List<RequestTmsModel.GetUserInformation> {
        return userInformationDao.getAllUserInformation().map {
            Mappers.getMapper(UserInformationMapper::class.java).toRequestGetUserInformationModel(it)
        }
    }

    override suspend fun deleteUserInformation(tmnId: String) {
        userInformationDao.deleteUserInformation(tmnId)
    }
}

