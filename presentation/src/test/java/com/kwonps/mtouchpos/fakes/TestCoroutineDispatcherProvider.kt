package com.kwonps.mtouchpos.fakes

import com.kwonps.domain.dispatcher.CoroutineDispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

class TestCoroutineDispatcherProvider(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : CoroutineDispatcherProvider {
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}
