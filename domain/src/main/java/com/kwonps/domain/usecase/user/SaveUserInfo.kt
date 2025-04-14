package com.kwonps.domain.usecase.user

import com.kwonps.domain.model.user.UserDetailData
import com.kwonps.domain.repository.UserRepository
import com.google.gson.Gson

class SaveUserInfo(private val userRepository: UserRepository) {
    operator fun invoke(userInfo: UserDetailData) {
        with(userRepository){
            insertUserInformation(userInfo)
            setCurrentLoginUserInformation(Gson().toJson(userInfo))
        }
    }
}