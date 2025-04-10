package com.example.domain.usecase.user

import com.example.domain.model.cardreader.CardReaderData
import com.example.domain.model.user.UserDetailData
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

class FetchSavedUserInfo(private val userRepository: UserRepository) {
    operator fun invoke(): StateFlow<List<UserDetailData>> =
        userRepository.getAllUserInformation().stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Lazily, emptyList())
}