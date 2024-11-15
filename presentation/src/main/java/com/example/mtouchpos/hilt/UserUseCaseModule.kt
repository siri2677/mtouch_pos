package com.example.mtouchpos.hilt

import android.content.Context
import com.example.domain.repositoryInterface.UserRepository
import com.example.domain.usecase.user.DeleteUserInfo
import com.example.domain.usecase.user.FetchConnectedUserInfo
import com.example.domain.usecase.user.FetchSavedUserInfo
import com.example.domain.usecase.user.LoginUser
import com.example.domain.usecase.user.SaveUserInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
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