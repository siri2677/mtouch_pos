package com.example.domain.usecase.user

import com.example.domain.repositoryInterface.UserRepository

class DeleteUserInfo(private val userRepository: UserRepository) {
    operator fun invoke(tmnId: String) { userRepository.deleteUserInformation(tmnId) }
}
