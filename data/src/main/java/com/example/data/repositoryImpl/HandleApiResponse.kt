package com.example.data.repositoryImpl

import com.example.domain.model.ApiResult
import retrofit2.Response

fun <T, R> Response<T>.handleApiResult(
    response: (T) -> R
): ApiResult<R> = if (isSuccessful) {
    body()?.let { ApiResult.Success(response(it)) } ?: ApiResult.Error("검색된 내역이 존재하지 않습니다")
} else {
    ApiResult.Error(errorBody()?.string() ?: "${code()}: 요청 값을 확인해 주시기 바랍니다")
}

fun <T, R> Response<T>.handleApiResultDetail(
    response: (T) -> ApiResult<R>
): ApiResult<R> = if (isSuccessful) {
    body()?.let { response(it) } ?: ApiResult.Error("검색된 내역이 존재하지 않습니다")
} else {
    ApiResult.Error(errorBody()?.string() ?: "${code()}: 요청 값을 확인해 주시기 바랍니다")
}