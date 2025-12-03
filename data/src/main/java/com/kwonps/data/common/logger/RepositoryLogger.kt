package com.kwonps.data.common.logger

import android.util.Log
import com.kwonps.domain.model.ApiResult
import javax.inject.Inject

class RepositoryLogger @Inject constructor() {
    fun logStart(operationName: String) {
        Log.d(TAG, "Starting $operationName")
    }

    fun logRetry(operationName: String, cause: Throwable, attempt: Long) {
        Log.w(TAG, "Retrying $operationName after error: ${cause.message}, attempt=${attempt + 1}")
    }

    fun logFailure(operationName: String, cause: Throwable) {
        Log.e(TAG, "Failed $operationName: ${cause.message}", cause)
    }

    fun logCompletion(operationName: String, result: ApiResult<*>) {
        Log.d(TAG, "Completed $operationName with result=$result")
    }

    companion object {
        private const val TAG = "Repository"
    }
}
