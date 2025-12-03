package com.kwonps.domain.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

interface CoroutineDispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}
