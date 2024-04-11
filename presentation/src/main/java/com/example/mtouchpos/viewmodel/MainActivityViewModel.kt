package com.example.mtouchpos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.request.tms.RequestTmsModel
import com.example.domain.model.response.ResponseModel
import com.example.domain.model.response.tms.ResponseTmsModel
import com.example.domain.repositoryInterface.RequestRemoteRepository
import com.example.domain.repositoryInterface.UserInformationRepository
import com.example.domain.repositoryInterface.UserInformationSharedPreference
import com.example.mtouchpos.dto.LoginInfo
import com.example.mtouchpos.dto.ResponseFlowData
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.mtouchpos.hilt.TmsRepository
import com.example.mtouchpos.mapper.RequestDataMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.mapstruct.factory.Mappers
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    @TmsRepository private val RequestRemoteTmsRepositoryImpl: RequestRemoteRepository,
    private val userInformationSharedPreference: UserInformationSharedPreference,
    private val userInformationRepository: UserInformationRepository
) : ViewModel(){
    private val responseModel = MutableStateFlow<ResponseModel>(ResponseTmsModel.Init)
    val responseFlowData = MutableStateFlow<ResponseFlowData>(ResponseFlowData.Init)

    fun login(loginInfo: LoginInfo) {
        RequestRemoteTmsRepositoryImpl(
            requestModel = Mappers.getMapper(RequestDataMapper::class.java)
                .toRequestGetUserInformationModel(loginInfo),
            responseModel = responseModel
        )
        handleResponseModel()
    }

    private fun handleResponseModel() {
        if(responseModel.value == ResponseTmsModel.Init){
            viewModelScope.launch {
                responseModel.collect{
                    when(it) {
                        is ResponseTmsModel.GetUserInformation -> {
                            responseFlowData.emit(ResponseFlowData.CompleteLogin)
                        }
                        is ResponseTmsModel.Error -> {
                            responseFlowData.emit(ResponseFlowData.Error(it.message))
                        }
                    }
                }
            }
        }
    }

    fun getTmnId() = userInformationSharedPreference.getUserInformation()!!.tmnId
    fun getMaxInstallment() = userInformationSharedPreference.getUserInformation()!!.apiMaxInstall
    fun getSemiAuth() = userInformationSharedPreference.getUserInformation()!!.semiAuth

    fun getUserInformationRepository(): ArrayList<LoginInfo> {
        val loginInfoList = ArrayList<LoginInfo>()
        userInformationRepository.getAllUserInformation().map { listContents ->
            loginInfoList.add(
                Mappers.getMapper(RequestDataMapper::class.java)
                    .toLoginInfo(listContents)
            )
        }
        return loginInfoList
    }

    fun deleteUserInformationRepository(tmnId: String) {
        viewModelScope.launch {
            userInformationRepository.deleteUserInformation(tmnId)
        }
    }

    fun summary() {
        RequestRemoteTmsRepositoryImpl(
            requestModel = RequestTmsModel.GetSummaryPaymentStatistics(),
            responseModel = responseModel
        )
        handleResponseModel()
    }
}
