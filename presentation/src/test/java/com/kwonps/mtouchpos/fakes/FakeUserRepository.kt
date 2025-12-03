package com.kwonps.mtouchpos.fakes

import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.user.CachedUserInformation
import com.kwonps.domain.model.user.UserData
import com.kwonps.domain.model.user.UserDetailData
import com.kwonps.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeUserRepository : UserRepository {
    var currentUserJson: String? = null

    override fun getCurrentLoginUserInformation(): CachedUserInformation =
        currentUserJson?.let(CachedUserInformation::Raw) ?: CachedUserInformation.Empty

    override fun setCurrentLoginUserInformation(responseLoginModelString: String) {
        currentUserJson = responseLoginModelString
    }

    override fun insertUserInformation(userInfo: UserDetailData) {}

    override fun getAllUserInformation(): Flow<List<UserDetailData>> = flowOf(emptyList())

    override fun deleteUserInformation(tmnId: String) {}

    override suspend fun validateLoginInfo(userData: UserData): Flow<ApiResult<UserDetailData>> = flowOf()
}
