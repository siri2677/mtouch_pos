package com.example.domain.usecase.user

import com.example.domain.model.user.UserDetailData
import com.example.domain.repositoryInterface.UserRepository
import com.google.gson.Gson

class FetchConnectedUserInfo(
    private val userRepository: UserRepository
) {
    operator fun invoke(): UserDetailData? = try {
        Gson().fromJson(
            userRepository.getCurrentLoginUserInformation(),
            UserDetailData::class.java
        )
    } catch (e: NullPointerException) {
        null
    }
}
