package com.example.data.mapper.db

import com.example.data.entity.db.UserInformationEntity
import com.example.domain.dto.request.tms.RequestGetUserInformationDto
import org.mapstruct.Mapper

@Mapper
interface UserInformationMapper {
    fun convertToEntity(requestGetUserInformationDto: RequestGetUserInformationDto) : UserInformationEntity
    fun convertToModel(userInformationEntity: UserInformationEntity) : RequestGetUserInformationDto
}
