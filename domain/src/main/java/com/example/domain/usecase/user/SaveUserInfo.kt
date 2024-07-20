package com.example.domain.usecase.user

import com.example.domain.model.user.UserDetailData
import com.example.domain.repositoryInterface.UserRepository
import com.google.gson.Gson

class SaveUserInfo(private val userRepository: UserRepository) {
    operator fun invoke(userInfo: UserDetailData) {
        with(userRepository){
            insertUserInformation(userInfo)
            setCurrentLoginUserInformation(Gson().toJson(userInfo))
        }
    }
}