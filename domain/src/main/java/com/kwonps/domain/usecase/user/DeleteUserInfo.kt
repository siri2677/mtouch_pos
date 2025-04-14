package com.kwonps.domain.usecase.user

import com.kwonps.domain.repository.UserRepository

class DeleteUserInfo(private val userRepository: UserRepository) {
    operator fun invoke(tmnId: String) { userRepository.deleteUserInformation(tmnId) }
}
