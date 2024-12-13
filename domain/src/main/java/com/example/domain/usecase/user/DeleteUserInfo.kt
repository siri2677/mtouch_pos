package com.example.domain.usecase.user

import com.example.domain.repository.UserRepository

class DeleteUserInfo(private val userRepository: UserRepository) {
    operator fun invoke(tmnId: String) { userRepository.deleteUserInformation(tmnId) }
}
