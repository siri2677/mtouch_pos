package com.kwonps.domain.usecase.user

import com.kwonps.domain.model.user.UserDetailData
import com.kwonps.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class FetchSavedUserInfo(private val userRepository: UserRepository) {
    operator fun invoke(): StateFlow<List<UserDetailData>> =
        userRepository.getAllUserInformation().stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, emptyList())
}