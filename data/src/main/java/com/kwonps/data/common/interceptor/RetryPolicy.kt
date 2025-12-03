package com.kwonps.data.common.interceptor

import kotlinx.coroutines.delay
import javax.inject.Inject

class RetryPolicy @Inject constructor(
    private val maxRetries: Long = DEFAULT_MAX_RETRIES,
    private val delayMillis: Long = DEFAULT_RETRY_DELAY_MILLIS
) {
    fun shouldRetry(cause: Throwable, attempt: Long): Boolean =
        attempt < maxRetries

    suspend fun waitBeforeRetry(attempt: Long) {
        delay(delayMillis * (attempt + 1))
    }

    companion object {
        const val DEFAULT_MAX_RETRIES = 1L
        const val DEFAULT_RETRY_DELAY_MILLIS = 200L
    }
}
