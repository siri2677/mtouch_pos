package com.kwonps.mtouchpos.fakes

import com.kwonps.domain.model.cardreader.CardReaderStatus
import com.kwonps.domain.repository.CardReaderCommunicateRepository
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeCardReaderCommunicateRepository : CardReaderCommunicateRepository {
    override val connectionStatus: MutableSharedFlow<CardReaderStatus.Connection> = MutableSharedFlow()
    override val dataStream: MutableSharedFlow<ByteArray> = MutableSharedFlow()

    var lastDeviceInfo: String? = null
    var lastPayload: ByteArray? = null
    var stopped = false

    override fun connect(deviceInfo: String) {
        lastDeviceInfo = deviceInfo
    }

    override fun disConnect() { stopped = true }

    override fun stopRetry() { stopped = true }

    override fun sendData(byteArray: ByteArray, isPrint: Boolean) {
        lastPayload = byteArray
    }
}
