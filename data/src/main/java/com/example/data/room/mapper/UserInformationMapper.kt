package com.example.data.room.mapper

import com.example.data.room.entity.UserInformationEntity
import com.example.domain.model.request.tms.RequestTmsModel
import org.mapstruct.Mapper

@Mapper
interface UserInformationMapper {
    fun toUserInformationEntity(requestTmsModel: RequestTmsModel.GetUserInformation) : UserInformationEntity
    fun toRequestGetUserInformationModel(userInformationEntity: UserInformationEntity) : RequestTmsModel.GetUserInformation
}
