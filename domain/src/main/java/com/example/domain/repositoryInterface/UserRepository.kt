package com.example.domain.repositoryInterface

import com.example.domain.model.ApiResult
import com.example.domain.model.user.UserData
import com.example.domain.model.user.UserDetailData
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentLoginUserInformation(): String?
    fun setCurrentLoginUserInformation(responseLoginModelString: String)
    fun insertUserInformation(userInfo: UserDetailData)
    fun getAllUserInformation(): Flow<List<UserDetailData>>
    fun deleteUserInformation(tmnId: String)
    suspend fun validateLoginInfo(
        userData: UserData
    ): Flow<ApiResult<UserDetailData>>
}