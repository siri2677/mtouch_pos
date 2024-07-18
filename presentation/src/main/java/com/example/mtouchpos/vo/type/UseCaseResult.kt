package com.example.mtouchpos.vo.type

import java.io.Serializable

sealed class UseCaseResult<out T>: Serializable {
    data object Init: UseCaseResult<Nothing>()
    data class Success<out T>(val value: T) : UseCaseResult<T>()
    data class Error(val message: String) : UseCaseResult<Nothing>()
    data class Exception(val exception: Throwable) : UseCaseResult<Nothing>()
}