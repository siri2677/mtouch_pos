package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.user.DeleteUserInfo
import com.example.domain.usecase.user.FetchConnectedUserInfo
import com.example.domain.usecase.user.FetchSavedUserInfo
import com.example.domain.usecase.user.LoginUser
import com.example.mtouchpos.viewmodel.mapper.toUseCaseResult
import com.example.mtouchpos.viewmodel.mapper.toUserData
import com.example.mtouchpos.vo.type.UseCaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val fetchSavedUserInfoUseCase: FetchSavedUserInfo,
    private val deleteUserInfoUseCase: DeleteUserInfo,
    private val fetchConnectedUserInfoUseCase: FetchConnectedUserInfo,
    private val loginUserUseCase: LoginUser
) : ViewModel(){
    data class UserInfo (
        val tmnId: String = "",
        val serial: String = "",
        val mchtId: String = ""
    )

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


    private val _reactLogin = MutableSharedFlow<UseCaseResult<String>>()
    val reactLogin = _reactLogin.asSharedFlow()

    private val _loginInfo = MutableStateFlow(UserInfo())
    val loginInfo = _loginInfo.asStateFlow()

    private val _loginInfoList = MutableStateFlow<List<UserInfo>>(emptyList())
    val loginInfoList = _loginInfoList.asStateFlow()

    fun updateUserInfo(userInfo: UserInfo) { _loginInfo.value = userInfo }

    fun deleteUserInfo(tmnId: String) { deleteUserInfoUseCase(tmnId) }

    fun fetchUserInfoList() {
        viewModelScope.launch {
            fetchSavedUserInfoUseCase().map { userDetailData ->
                userDetailData.map { UserInfo(it.tmnId, it.serial, it.mchtId) }
            }.collect{ _loginInfoList.emit(it) }
        }
    }

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

    fun login() {
        viewModelScope.launch {
            loginUserUseCase(
                loginInfo.value.toUserData()
            ).map { apiResult ->
                apiResult.toUseCaseResult { it.tmnId }
            }.collect { _reactLogin.emit(it) }
        }
    }
}

//    fun summary() {
//        viewModelScope.launch {
//            requestRemoteTmsRepositoryImpl(RequestOffPaymentModel.GetSummaryPaymentStatistics()).collect {
//                when(it) {
//                    is ApiResult.Error -> _responseFlowData.emit(ResponseFlowData.Error(it.message))
//                    is ApiResult.Exception ->  _responseFlowData.emit(ResponseFlowData.Error(it.exception.message.toString()))
//                    is ApiResult.Success -> (it.value as ResponseLoginModel.GetUserInformation).handleResponseModel(this@with)
//                }
//            }
//        }
//
//        requestRemoteTmsRepositoryImpl(
//            requestModel = RequestOffPaymentModel.GetSummaryPaymentStatistics(),
//            responseModel = responseModel
//        )
////        handleResponseModel()
//    }
