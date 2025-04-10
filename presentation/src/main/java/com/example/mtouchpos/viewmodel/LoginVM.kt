package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.user.UserData
import com.example.domain.model.user.UserDetailData
import com.example.domain.usecase.user.DeleteUserInfo
import com.example.domain.usecase.user.FetchConnectedUserInfo
import com.example.domain.usecase.user.FetchSavedUserInfo
import com.example.domain.usecase.user.LoginUser
import com.example.mtouchpos.viewmodel.mapper.toUseCaseResult
import com.example.mtouchpos.viewmodel.mapper.toUserData
import com.example.mtouchpos.viewmodel.mapper.toUserInfo
import com.example.mtouchpos.vo.info.UserInfo
import com.example.mtouchpos.vo.type.UseCaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.map

@HiltViewModel
class LoginVM @Inject constructor(
    private val fetchSavedUserInfoUseCase: FetchSavedUserInfo,
    private val deleteUserInfoUseCase: DeleteUserInfo,
    private val fetchConnectedUserInfoUseCase: FetchConnectedUserInfo,
    private val loginUserUseCase: LoginUser
) : ViewModel(){
    data class UserDetailInfo(
        val tmnId: String,
        val serial: String,
        val mchtId: String,
        val semiAuth: String,
        val appDirect: String,
        val key: String,
        val vat: String,
        val apiMaxInstall: String,
        val payKey: String
    )

    private val _userInfo: StateFlow<List<UserDetailData>> = fetchSavedUserInfoUseCase()
    val userInfo = _userInfo.map { userDetailData ->
        userDetailData.map { it.toUserInfo() }
    }

    private val _reactLogin = MutableSharedFlow<UseCaseResult<String>>()
    val reactLogin = _reactLogin.asSharedFlow()

    private val _loginInfo = MutableStateFlow(UserInfo())
    val loginInfo = _loginInfo.asStateFlow()

    private val _loginInfoList = MutableStateFlow<List<UserInfo>>(emptyList())
    val loginInfoList = _loginInfoList.asStateFlow()

    fun login() {
        viewModelScope.launch {
            _reactLogin.emit(UseCaseResult.Init)
            loginUserUseCase(
                loginInfo.value.toUserData()
            ).map { apiResult ->
                apiResult.toUseCaseResult { it.tmnId }
            }.collect { _reactLogin.emit(it) }
        }
    }

    fun fetchUserInfoList() {
        viewModelScope.launch {
            fetchSavedUserInfoUseCase().map { userDetailData ->
                userDetailData.map { it.toUserInfo() }
            }.collect{ _loginInfoList.emit(it) }
        }
    }

    fun updateUserInfo(userInfo: UserInfo) { _loginInfo.value = userInfo }

    fun deleteUserInfo(tmnId: String) { deleteUserInfoUseCase(tmnId) }

    fun fetchCurrentConnectedUserInfo() = fetchConnectedUserInfoUseCase()?.run {
        UserDetailInfo(
            tmnId = tmnId,
            serial = serial,
            mchtId = mchtId,
            semiAuth = semiAuth,
            appDirect = appDirect,
            key = key,
            vat = vat,
            apiMaxInstall = apiMaxInstall,
            payKey = payKey
        )
    }
}
