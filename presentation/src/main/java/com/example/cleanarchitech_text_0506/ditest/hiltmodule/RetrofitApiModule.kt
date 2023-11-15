package com.example.cleanarchitech_text_0506.ditest.hiltmodule

import android.content.Context
import com.example.data.dataBase.AppDatabase
import com.example.data.repository.api.DirectPaymentRepositoryImpl
import com.example.data.repository.api.GetPaymentInformationRepositoryImpl
import com.example.data.repository.api.LoginRepositoryImpl
import com.example.data.repository.api.OfflinePaymentRepositoryImpl
import com.example.data.repository.db.UserInformationRepositoryImpl
import com.example.data.sharedpreference.UserInformationSharedPreferenceImpl
import com.example.domain.repositoryInterface.DirectPaymentRepository
import com.example.domain.repositoryInterface.GetPaymentInformationRepository
import com.example.domain.repositoryInterface.LoginRepository
import com.example.domain.repositoryInterface.OfflinePaymentRepository
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
    fun provideLoginRelatedRepository(
        @ApplicationContext context: Context
    ): LoginRepository = LoginRepositoryImpl(context)

    @Provides
    @ViewModelScoped
    fun provideUserInformationSharedPreference(
        @ApplicationContext context: Context
    ): UserInformationSharedPreference = UserInformationSharedPreferenceImpl(context)

    @Provides
    @ViewModelScoped
    fun provideUserInformationRepository(
        @ApplicationContext context: Context
    ): UserInformationRepository = UserInformationRepositoryImpl(AppDatabase.getInstance(context).userInformationDao())
    @Provides
    @ViewModelScoped
    fun provideDirectPaymentRepository(): DirectPaymentRepository = DirectPaymentRepositoryImpl()

    @Provides
    @ViewModelScoped
    fun providePaymentHistoryRepository(): GetPaymentInformationRepository = GetPaymentInformationRepositoryImpl()

    @Provides
    @ViewModelScoped
    fun provideOfflinePaymentRepository(): OfflinePaymentRepository = OfflinePaymentRepositoryImpl()





}