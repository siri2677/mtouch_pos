package com.kwonps.domain.usecase.user

import com.kwonps.domain.model.user.CachedUserInformation
import com.kwonps.domain.model.user.UserDetailData
import com.kwonps.domain.parser.UserDetailParser
import com.kwonps.domain.repository.UserRepository

class FetchConnectedUserInfo(
    private val userRepository: UserRepository,
    private val userDetailParser: UserDetailParser
) {
    operator fun invoke(): UserDetailData? = when (val cachedInfo = userRepository.getCurrentLoginUserInformation()) {
        is CachedUserInformation.Parsed -> cachedInfo.data
        is CachedUserInformation.Raw -> userDetailParser.parse(cachedInfo.json)
        CachedUserInformation.Empty -> null
    }
}