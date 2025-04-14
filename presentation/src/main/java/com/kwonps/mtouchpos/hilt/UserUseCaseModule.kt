package com.kwonps.mtouchpos.hilt

import com.kwonps.domain.repository.UserRepository
import com.kwonps.domain.usecase.user.DeleteUserInfo
import com.kwonps.domain.usecase.user.FetchConnectedUserInfo
import com.kwonps.domain.usecase.user.FetchSavedUserInfo
import com.kwonps.domain.usecase.user.LoginUser
import com.kwonps.domain.usecase.user.SaveUserInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UserUseCaseModule {
    @Provides
    @ViewModelScoped
    fun provideDeleteUserInfo(
        userRepository: UserRepository
    ): DeleteUserInfo = DeleteUserInfo(userRepository)

    @Provides
    @ViewModelScoped
    fun provideFetchConnectedUserInfo(
        userRepository: UserRepository
    ): FetchConnectedUserInfo = FetchConnectedUserInfo(userRepository)

    @Provides
    @ViewModelScoped
    fun provideFetchSavedUserInfo(
        userRepository: UserRepository
    ): FetchSavedUserInfo = FetchSavedUserInfo(userRepository)

    @Provides
    @ViewModelScoped
    fun provideLoginUser(
        userRepository: UserRepository
    ): LoginUser = LoginUser(userRepository, SaveUserInfo(userRepository))
}