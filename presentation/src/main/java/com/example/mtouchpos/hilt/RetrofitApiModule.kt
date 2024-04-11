package com.example.mtouchpos.hilt

import android.content.Context
import com.example.data.room.UserInformationDatabase
import com.example.data.room.repositoryimplement.UserInformationRepositoryImpl
import com.example.data.sharedpreference.UserInformationSharedPreferenceImpl
import com.example.domain.repositoryInterface.UserInformationRepository
import com.example.domain.repositoryInterface.UserInformationSharedPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object RetrofitApiModule {
    @Provides
    @ViewModelScoped
    fun provideUserInformationSharedPreference(
        @ApplicationContext context: Context
    ): UserInformationSharedPreference = UserInformationSharedPreferenceImpl(context)

    @Provides
    @ViewModelScoped
    fun provideUserInformationRepository(
        @ApplicationContext context: Context
    ): UserInformationRepository = UserInformationRepositoryImpl(UserInformationDatabase.getInstance(context).userInformationDao())
}