package com.kwonps.data.common.interceptor

import com.kwonps.data.common.dispatcher.DispatcherProvider
import com.kwonps.data.common.logger.RepositoryLogger
import com.kwonps.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import javax.inject.Inject

class FlowCallDecorator @Inject constructor(
    private val logger: RepositoryLogger,
    private val retryPolicy: RetryPolicy,
    private val dispatcherProvider: DispatcherProvider
) {
    fun <T> asResultFlow(
        operationName: String,
        block: suspend () -> ApiResult<T>
    ): Flow<ApiResult<T>> = flow {
        logger.logStart(operationName)
        emit(block())
    }
        .retryWhen { cause, attempt ->
            val shouldRetry = retryPolicy.shouldRetry(cause, attempt)
            if (shouldRetry) {
                logger.logRetry(operationName, cause, attempt)
                retryPolicy.waitBeforeRetry(attempt)
            } else {
                logger.logFailure(operationName, cause)
            }
            shouldRetry
        }
        .onEach { result -> logger.logCompletion(operationName, result) }
        .catch { cause ->
            logger.logFailure(operationName, cause)
            emit(ApiResult.Exception(cause))
        }
        .flowOn(dispatcherProvider.io)
}
