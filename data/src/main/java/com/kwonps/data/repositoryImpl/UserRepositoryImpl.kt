package com.kwonps.data.repositoryImpl

import com.kwonps.data.common.interceptor.FlowCallDecorator
import com.kwonps.data.mapper.UserMapper
import com.kwonps.data.remote.handleApiResult
import com.kwonps.data.source.user.UserLocalDataSource
import com.kwonps.data.source.user.UserRemoteDataSource
import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.user.CachedUserInformation
import com.kwonps.domain.model.user.UserData
import com.kwonps.domain.model.user.UserDetailData
import com.kwonps.domain.repository.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * User repository combining local cache and remote login validation. Cache strategy: the
 * last login payload is persisted in SharedPreferences and synchronized with the Room
 * backing store; remote validation remains the source of truth for credentials.
 */
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource,
    private val mapper: UserMapper,
    private val flowCallDecorator: FlowCallDecorator,
) : UserRepository {
    override fun getCurrentLoginUserInformation(): CachedUserInformation =
        localDataSource.getCurrentLoginUserInformation()?.let(CachedUserInformation::Raw)
            ?: CachedUserInformation.Empty

    override fun setCurrentLoginUserInformation(responseLoginModelString: String) {
        localDataSource.setCurrentLoginUserInformation(responseLoginModelString)
    }

    override fun insertUserInformation(userInfo: UserDetailData) {
        localDataSource.insertUserInformation(mapper.toEntity(userInfo))
    }

    override fun getAllUserInformation(): Flow<List<UserDetailData>> =
        localDataSource.getAllUserInformation().map { entityList -> entityList.map(mapper::toDomain) }

    override fun deleteUserInformation(tmnId: String) {
        localDataSource.deleteUserInformation(tmnId)
    }

    override suspend fun validateLoginInfo(userData: UserData): Flow<ApiResult<UserDetailData>> =
        flowCallDecorator.asResultFlow("user:validateLoginInfo") {
            remoteDataSource.validateLogin(mapper.toLoginRequest(userData)).handleApiResult {
                mapper.toDomainUserDetail(it.data, userData)
            }
        }
}
