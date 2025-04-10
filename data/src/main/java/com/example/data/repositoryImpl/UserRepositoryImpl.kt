package com.example.data.repositoryImpl

import android.content.SharedPreferences
import com.example.data.remote.dto.request.RequestUser
import com.example.data.remote.dto.response.ResponseUser
import com.example.data.internal.dao.UserInformationDAO
import com.example.data.internal.entity.UserEntity
import com.example.data.remote.DataFormat
import com.example.data.remote.apiservice.TmsAPIService
import com.example.data.remote.handleApiResult
import com.example.domain.model.ApiResult
import com.example.domain.model.user.UserData
import com.example.domain.model.user.UserDetailData
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: TmsAPIService,
    private val userInformationDao: UserInformationDAO,
    private val sharedPreferences: SharedPreferences,
    private val key: String
): UserRepository {
    override fun getCurrentLoginUserInformation(): String? = sharedPreferences.getString(key, null)

    override fun setCurrentLoginUserInformation(responseLoginModelString: String) {
        sharedPreferences.edit().putString(key, responseLoginModelString).apply()
    }

    override fun insertUserInformation(userInfo: UserDetailData) {
        userInformationDao.insertUserInformation(userInfo.toUserInformationEntity())
    }

    override fun getAllUserInformation(): Flow<List<UserDetailData>> = userInformationDao.getAllUserInformation().map { userInformationEntity ->
        userInformationEntity.map { it.toRequestGetUserInformationModel() }
    }

    override fun deleteUserInformation(tmnId: String) {
        userInformationDao.deleteUserInformation(tmnId)
    }

    override suspend fun validateLoginInfo(loginInfo: UserData): Flow<ApiResult<UserDetailData>> = flow {
        val response = apiService.key(DataFormat(loginInfo.toRequestGetUserInformationModel()))
        emit(response.handleApiResult { it.data.toUserInformationEntity(loginInfo) })
    }.catch { e -> emit(ApiResult.Exception(e)) }

    private fun UserData.toRequestGetUserInformationModel() = RequestUser.Login(
        tmnId = tmnId,
        serial = serial,
        merchantId = mchtId,
        appId = null,
        version = null,
        telNo = null
    )

    private fun ResponseUser.Login.toUserInformationEntity(loginInfo: UserData) = UserDetailData(
        tmnId = tmnId,
        serial = loginInfo.serial,
        mchtId = loginInfo.mchtId,
        mchtName = name,
        telNo = telNo,
        ceoName = ceoName,
        address = addr,
        identity = identity,
        semiAuth = semiAuth,
        appDirect = appDirect,
        key = key,
        vat = vat,
        apiMaxInstall = apiMaxInstall,
        payKey = payKey
    )

    private fun UserDetailData.toUserInformationEntity() = UserEntity(
        tmnId = tmnId,
        serial = serial,
        mchtId = mchtId,
        mchtName = mchtName,
        telNo = telNo,
        ceoName = ceoName,
        address = address,
        identity = identity,
        semiAuth = semiAuth,
        appDirect = appDirect,
        key = key,
        vat = vat,
        apiMaxInstall = apiMaxInstall,
        payKey = payKey
    )

    private fun UserEntity.toRequestGetUserInformationModel() = UserDetailData(
        tmnId = tmnId,
        serial = serial,
        mchtId = mchtId,
        mchtName = mchtName,
        telNo = telNo,
        ceoName = ceoName,
        address = address,
        identity = identity,
        semiAuth = semiAuth,
        appDirect = appDirect,
        key = key,
        vat = vat,
        apiMaxInstall = apiMaxInstall,
        payKey = payKey
    )
}