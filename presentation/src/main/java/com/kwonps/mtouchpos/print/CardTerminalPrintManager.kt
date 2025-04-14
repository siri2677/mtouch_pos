package com.kwonps.mtouchpos.print

import kotlinx.coroutines.InternalCoroutinesApi

interface CardTerminalPrintManager {
    @OptIn(InternalCoroutinesApi::class)
    operator fun invoke()
}