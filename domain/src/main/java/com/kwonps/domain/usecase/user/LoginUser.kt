package com.kwonps.domain.usecase.user

import com.kwonps.domain.repository.UserRepository
import com.kwonps.domain.model.ApiResult
import com.kwonps.domain.model.user.UserData
import com.kwonps.domain.model.user.UserDetailData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class LoginUser(
    private val userRepository: UserRepository,
    private val savedUserInfo: SaveUserInfo
) {
    operator fun invoke(
        userData: UserData
    ): Flow<ApiResult<UserDetailData>> = flow {
        userRepository.validateLoginInfo(userData).onEach {
            if (it is ApiResult.Success) savedUserInfo(it.value)
        }.collect { emit(it) }
    }
}
