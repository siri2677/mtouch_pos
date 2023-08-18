package com.example.cleanarchitech_text_0506.di.module
import android.content.Context
import com.example.cleanarchitech_text_0506.ditest.ActivityScope
import com.example.data.repository.LoginRelatedRepository
import com.example.domain.repositoryInterface.LoginRelatedRepositoryImpl
import com.mtouch.domain.useCaseInterface.LoginUseCaseImpl
import com.mtouch.domain.usecase.LoginUseCase
import dagger.Module
import dagger.Provides

@Module
class LoginUseCaseInjectModule{
    @Provides
    fun provideLoginRelatedRepositoryImpl(): LoginRelatedRepositoryImpl {
        return LoginRelatedRepository()
    }
    @Provides
    fun provideGetResponseDataKeyApiInterface(context: Context, loginRelatedRepositoryImpl: LoginRelatedRepositoryImpl) : LoginUseCaseImpl {
        return LoginUseCase(context, loginRelatedRepositoryImpl)
    }

}