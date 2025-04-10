package com.example.mtouchpos.print

import kotlinx.coroutines.InternalCoroutinesApi

interface CardTerminalPrintManager {
    @OptIn(InternalCoroutinesApi::class)
    operator fun invoke()
}