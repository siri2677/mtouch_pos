package com.example.cleanarchitech_text_0506.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleanarchitech_text_0506.util.SingleLiveEvent
import com.mtouch.domain.model.tmsApiRequest.KeyTmsRequestData
import com.mtouch.domain.useCaseInterface.LoginUseCaseImpl
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.KeyTmsResponseData
import com.mtouch.ksr02_03_04_v2.Domain.Model.TmsApiResponse.SummaryTmsResponseData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.HttpException

import javax.inject.Inject
import kotlin.coroutines.resumeWithException


class MainActivityViewModel @Inject constructor(
    private val loginUseCaseInterface: LoginUseCaseImpl
) : ViewModel(){

    private val _KeyTmsResponseData = MutableLiveData<KeyTmsResponseData>()
    val data: MutableLiveData<KeyTmsResponseData> = _KeyTmsResponseData

    private val _SummaryTmsResponseData = MutableLiveData<SummaryTmsResponseData>()
    val data1: MutableLiveData<SummaryTmsResponseData> = _SummaryTmsResponseData

    private val _ApiErrorMessage = MutableLiveData<String>()
    val data2: MutableLiveData<String> = _ApiErrorMessage

    fun login(keyTmsRequestData: KeyTmsRequestData) {
        viewModelScope.launch {
            val data = loginUseCaseInterface.getLoginToken(keyTmsRequestData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { successData ->
                        _KeyTmsResponseData.value = successData
                        loginUseCaseInterface.getPaymentSummaryAboutTerminalId(successData.data?.key.toString())
                            .subscribe(
                                { successData ->
                                    _SummaryTmsResponseData.value = successData
                                },
                                { error ->
                                    if (error is HttpException) {
                                        val errorResponse = error.response()?.errorBody()
                                        val statusCode = error.code()

                                        Log.w("test", errorResponse.toString())
                                        _ApiErrorMessage.value = errorResponse.toString()!!
                                    }
                                }
                            )
                    },
                    { error ->
                        if (error is HttpException) {
                            val errorResponse = error.response()
                            val statusCode = error.code()

                            Log.w("errorResponse", errorResponse.toString())
                            val test =  errorResponse?.errorBody()?.string()
                            Log.w("errorResponse", test.toString())
//                            Log.w("errorResponse", errorResponse.toString())
//                            Log.w("errorResponse", errorResponse.toString())
//                            Log.w("errorResponse", errorResponse.toString())
//                            Log.w("errorResponse", errorResponse.toString())
//                            Log.w("errorResponse", errorResponse.toString())

                            Log.w("statusCode", statusCode.toString())
                            _ApiErrorMessage.value = test.toString()
                        }
                    }
                )
        }
    }


    fun getDailyAndMonthlyPaymentDataAboutTerminalId(keyTmsResponseDataToken: String) {
        loginUseCaseInterface.getPaymentSummaryAboutTerminalId(keyTmsResponseDataToken)
            .subscribe(
                { successData ->
                    _SummaryTmsResponseData.value = successData
                },
                { error ->
                    _SummaryTmsResponseData.value = error.message as SummaryTmsResponseData
                }
            )
    }

    fun checkAppDestroy() {
        loginUseCaseInterface.checkAppDestloy()
    }
}
