package com.example.domain.usecase.user

import com.example.domain.model.user.UserDetailData
import com.example.domain.repositoryInterface.UserRepository
import kotlinx.coroutines.flow.Flow

class FetchSavedUserInfo(private val userRepository: UserRepository) {
    operator fun invoke(): Flow<List<UserDetailData>> = userRepository.getAllUserInformation()
}