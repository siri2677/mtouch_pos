package com.example.cleanarchitech_text_0506.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleanarchitech_text_0506.sealed.DeviceConnectSharedFlow
import com.example.domain.dto.request.tms.RequestGetUserInformationDto
import com.example.domain.dto.response.tms.ResponseGetSummaryPaymentStatisticsDto
import com.example.domain.dto.response.tms.ResponseGetUserInformationDto
import com.example.domain.repositoryInterface.LoginRepository
import com.example.domain.repositoryInterface.UserInformationRepository
import com.example.domain.repositoryInterface.UserInformationSharedPreference
import com.example.domain.sealed.ResponsePayAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val userInformationSharedPreference: UserInformationSharedPreference,
    private val userInformationRepository: UserInformationRepository
) : ViewModel(){
    val userInformation = MutableSharedFlow<ResponseGetUserInformationDto>()
    val summaryPaymentStatistics = MutableSharedFlow<ResponseGetSummaryPaymentStatisticsDto>()
    val responseErrorBody = MutableSharedFlow<String>()

    fun summary(token: String) {
        viewModelScope.launch {
            loginRepository.summary(
                onSuccess = { viewModelScope.launch{ summaryPaymentStatistics.emit(it) } },
                onError = { viewModelScope.launch{ responseErrorBody.emit(it) } },
                token = token
            )
        }
    }

    fun login(requestGetUserInformationDto: RequestGetUserInformationDto) {
        loginRepository.key(
            onSuccess = {
                viewModelScope.launch{
                    userInformation.emit(it)
                    summary(it.key!!)
                }
            },
            onError = { viewModelScope.launch{ responseErrorBody.emit(it) }},
            body = requestGetUserInformationDto
        )
    }

    fun getUserInformation(): ResponseGetUserInformationDto =
        userInformationSharedPreference.getUserInformation()

    fun getUserInformationRepository(): ArrayList<RequestGetUserInformationDto> {
        var responseGetUserInformationList = ArrayList<RequestGetUserInformationDto>()
        viewModelScope.launch {
            for (information in userInformationRepository.getAllUserInformation()) {
                responseGetUserInformationList.add(information)
            }
        }
        return responseGetUserInformationList
    }

    fun deleteUserInformationRepository(tmnId: String) {
        viewModelScope.launch {
            userInformationRepository.deleteUserInformation(tmnId)
        }
    }
}
