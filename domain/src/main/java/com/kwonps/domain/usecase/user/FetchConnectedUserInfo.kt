package com.kwonps.domain.usecase.user

import com.kwonps.domain.model.user.UserDetailData
import com.kwonps.domain.repository.UserRepository
import com.google.gson.Gson

class FetchConnectedUserInfo(private val userRepository: UserRepository) {
    operator fun invoke(): UserDetailData? = try {
        Gson().fromJson(
            userRepository.getCurrentLoginUserInformation(),
            UserDetailData::class.java
        )
    } catch (e: NullPointerException) {
        null
    }
}