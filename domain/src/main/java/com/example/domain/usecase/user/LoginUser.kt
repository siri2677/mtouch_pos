package com.example.domain.usecase.user

import com.example.domain.repositoryInterface.UserRepository
import com.example.domain.model.ApiResult
import com.example.domain.model.user.UserData
import com.example.domain.model.user.UserDetailData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class LoginUser(
    private val userRepository: UserRepository,
    private val savedUserInfo: SaveUserInfo
) {
    suspend operator fun invoke(
        userData: UserData
    ): Flow<ApiResult<UserDetailData>> = flow {
        userRepository.validateLoginInfo(userData).onEach {
            if (it is ApiResult.Success) savedUserInfo(it.value)
        }.collect { emit(it) }
    }
}
