package com.kwonps.data.mapper

import com.kwonps.data.internal.entity.UserEntity
import com.kwonps.data.remote.dto.request.RequestUser
import com.kwonps.data.remote.dto.response.ResponseUser
import com.kwonps.domain.model.user.UserData
import com.kwonps.domain.model.user.UserDetailData
import javax.inject.Inject

class UserMapper @Inject constructor() {
    fun toLoginRequest(loginInfo: UserData) = RequestUser.Login(
        tmnId = loginInfo.tmnId,
        serial = loginInfo.serial,
        merchantId = loginInfo.mchtId,
        appId = null,
        version = null,
        telNo = null
    )

    fun toDomainUserDetail(response: ResponseUser.Login, loginInfo: UserData) = UserDetailData(
        tmnId = response.tmnId,
        serial = loginInfo.serial,
        mchtId = loginInfo.mchtId,
        mchtName = response.name,
        telNo = response.telNo,
        ceoName = response.ceoName,
        address = response.addr,
        identity = response.identity,
        semiAuth = response.semiAuth,
        appDirect = response.appDirect,
        key = response.key,
        vat = response.vat,
        apiMaxInstall = response.apiMaxInstall,
        payKey = response.payKey,
        van = response.van,
    )

    fun toEntity(userDetailData: UserDetailData) = UserEntity(
        tmnId = userDetailData.tmnId,
        serial = userDetailData.serial,
        mchtId = userDetailData.mchtId,
        mchtName = userDetailData.mchtName,
        telNo = userDetailData.telNo,
        ceoName = userDetailData.ceoName,
        address = userDetailData.address,
        identity = userDetailData.identity,
        semiAuth = userDetailData.semiAuth,
        appDirect = userDetailData.appDirect,
        key = userDetailData.key,
        vat = userDetailData.vat,
        apiMaxInstall = userDetailData.apiMaxInstall,
        payKey = userDetailData.payKey,
        van = userDetailData.van,
    )

    fun toDomain(userEntity: UserEntity) = UserDetailData(
        tmnId = userEntity.tmnId,
        serial = userEntity.serial,
        mchtId = userEntity.mchtId,
        mchtName = userEntity.mchtName,
        telNo = userEntity.telNo,
        ceoName = userEntity.ceoName,
        address = userEntity.address,
        identity = userEntity.identity,
        semiAuth = userEntity.semiAuth,
        appDirect = userEntity.appDirect,
        key = userEntity.key,
        vat = userEntity.vat,
        apiMaxInstall = userEntity.apiMaxInstall,
        payKey = userEntity.payKey,
        van = userEntity.van,
    )
}
