package com.example.cleanarchitech_text_0506.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.apiService.LoginRelatedAPIService
import com.mtouch.domain.model.tmsApiRequest.KeyTmsRequestData
import com.mtouch.domain.useCaseInterface.LoginUseCaseImpl
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.KeyTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.SummaryTmsResponseData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import retrofit2.HttpException

import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val loginUseCaseInterface: LoginRelatedAPIService
) : ViewModel(){

    private val _KeyTmsResponseData = MutableLiveData<KeyTmsResponseData>()
    val data: MutableLiveData<KeyTmsResponseData> = _KeyTmsResponseData

    private val _SummaryTmsResponseData = MutableLiveData<SummaryTmsResponseData>()
    val data1: MutableLiveData<SummaryTmsResponseData> = _SummaryTmsResponseData

    private val _ApiErrorMessage = MutableLiveData<String>()
    val data2: MutableLiveData<String> = _ApiErrorMessage

    fun login(keyTmsRequestData: KeyTmsRequestData) {
        viewModelScope.launch {
            loginUseCaseInterface.key(keyTmsRequestData).let {
                if(it.isSuccessful) {
                    _KeyTmsResponseData.value = it.body()
                    loginUseCaseInterface.summary(it.body()?.data?.key).let { summary ->
                        if(summary.isSuccessful) {
                            _SummaryTmsResponseData.value = summary.body()
                        } else {
                            _SummaryTmsResponseData.value = summary.body()
                        }
                    }
                } else {
                    _KeyTmsResponseData.value = it.body()
                }
            }
        }
    }




//    fun getDailyAndMonthlyPaymentDataAboutTerminalId(keyTmsResponseDataToken: String) {
//        loginUseCaseInterface.getPaymentSummaryAboutTerminalId(keyTmsResponseDataToken)
//            .subscribe(
//                { successData ->
//                    _SummaryTmsResponseData.value = successData
//                },
//                { error ->
//                    _SummaryTmsResponseData.value = error.message as SummaryTmsResponseData
//                }
//            )
//    }
//
//    fun checkAppDestroy() {
//        loginUseCaseInterface.checkAppDestroy()
//    }
}
