package com.kwonps.mtouchpos.di

import com.kwonps.domain.parser.UserDetailParser
import com.kwonps.domain.repository.UserRepository
import com.kwonps.domain.usecase.user.DeleteUserInfo
import com.kwonps.domain.usecase.user.FetchConnectedUserInfo
import com.kwonps.domain.usecase.user.FetchSavedUserInfo
import com.kwonps.domain.usecase.user.LoginUser
import com.kwonps.domain.usecase.user.SaveUserInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserUseCaseModule {
    @Provides
    @Singleton
    fun provideDeleteUserInfo(
        userRepository: UserRepository
    ): DeleteUserInfo = DeleteUserInfo(userRepository)

    @Provides
    @Singleton
    fun provideFetchConnectedUserInfo(
        userRepository: UserRepository,
        userDetailParser: UserDetailParser
    ): FetchConnectedUserInfo = FetchConnectedUserInfo(userRepository, userDetailParser)

    @Provides
    @Singleton
    fun provideFetchSavedUserInfo(
        userRepository: UserRepository
    ): FetchSavedUserInfo = FetchSavedUserInfo(userRepository)

    @Provides
    @Singleton
    fun provideLoginUser(
        userRepository: UserRepository
    ): LoginUser = LoginUser(userRepository, SaveUserInfo(userRepository))
}