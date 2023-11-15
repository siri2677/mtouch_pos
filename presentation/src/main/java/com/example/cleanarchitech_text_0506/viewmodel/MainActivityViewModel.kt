package com.example.cleanarchitech_text_0506.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.dto.request.tms.RequestGetUserInformationDto
import com.example.domain.dto.response.tms.ResponseGetSummaryPaymentStatisticsDto
import com.example.domain.dto.response.tms.ResponseGetUserInformationDto
import com.example.domain.repositoryInterface.LoginRelatedRepository
import com.example.domain.repositoryInterface.UserInformationRepository
import com.example.domain.repositoryInterface.UserInformationSharedPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val loginRelatedRepository: LoginRelatedRepository,
    private val userInformationSharedPreference: UserInformationSharedPreference,
    private val userInformationRepository: UserInformationRepository
) : ViewModel(){
    private val _userInformation = MutableStateFlow(ResponseGetUserInformationDto())
    val userInformation: MutableStateFlow<ResponseGetUserInformationDto>
        get() = _userInformation

    private val _summaryPaymentStatistics = MutableStateFlow(ResponseGetSummaryPaymentStatisticsDto())
    val summaryPaymentStatistics: MutableStateFlow<ResponseGetSummaryPaymentStatisticsDto>
        get() = _summaryPaymentStatistics

    private val _responseErrorBody = MutableStateFlow(String())
    val responseErrorBody: MutableStateFlow<String>
        get() = _responseErrorBody

    fun summary(token: String) {
        viewModelScope.launch {
            loginRelatedRepository.summary(
                onSuccess = { summaryPaymentStatistics.value = it },
                onError = { responseErrorBody.value = it },
                token = token
            )
        }
    }

    fun login(requestGetUserInformationDto: RequestGetUserInformationDto) {
        loginRelatedRepository.key(
            onSuccess = { userInformation.value = it },
            summary = { summary(userInformation?.value?.key.toString()) },
            onError = { responseErrorBody.value = it },
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
