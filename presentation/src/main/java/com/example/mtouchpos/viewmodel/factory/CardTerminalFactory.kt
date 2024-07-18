package com.example.mtouchpos.viewmodel.factory

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.example.domain.usecase.device.manager.CommunicateCardTerminalManager
import com.example.mtouchpos.viewmodel.usecasemanager.terminal.CommunicatePM500
import com.example.mtouchpos.viewmodel.usecasemanager.terminal.CommunicateQ2
import com.example.mtouchpos.viewmodel.usecasemanager.terminal.CommunicateXPDA

class CardTerminalFactory(
    private val context: Context,
    private val launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    fun getCommunicateManger(model: String): CommunicateCardTerminalManager? {
        val terminalMap = mapOf(
            "Q2" to { CommunicateQ2(context, launcher) },
            "PM500" to { CommunicatePM500(context, launcher) },
            "XPDA" to { CommunicateXPDA(context, launcher) }
        )
        return terminalMap.entries.find { model.contains(it.key) }?.value?.invoke()
    }
}