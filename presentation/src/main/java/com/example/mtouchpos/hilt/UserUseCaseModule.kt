package com.example.mtouchpos.hilt

import android.content.Context
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
        @ApplicationContext context: Context
    ): DeleteUserInfo = DeleteUserInfo(RepositoryModule.provideUserRepository(context))

    @Provides
    @ViewModelScoped
    fun provideFetchConnectedUserInfo(
        @ApplicationContext context: Context
    ): FetchConnectedUserInfo = FetchConnectedUserInfo(RepositoryModule.provideUserRepository(context))

    @Provides
    @ViewModelScoped
    fun provideFetchSavedUserInfo(
        @ApplicationContext context: Context
    ): FetchSavedUserInfo = FetchSavedUserInfo(RepositoryModule.provideUserRepository(context))
    @Provides
    @ViewModelScoped
    fun provideLoginUser(
        @ApplicationContext context: Context
    ): LoginUser = LoginUser(
        RepositoryModule.provideUserRepository(context),
        SaveUserInfo(RepositoryModule.provideUserRepository(context))
    )
}