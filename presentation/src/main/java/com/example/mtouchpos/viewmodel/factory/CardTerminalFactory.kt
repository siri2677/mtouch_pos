package com.example.mtouchpos.viewmodel.factory

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.example.domain.usecase.device.manager.CommunicateCardTerminalManager
import com.example.mtouchpos.viewmodel.usecasemanager.terminal.CommunicatePM500
import com.example.mtouchpos.viewmodel.usecasemanager.terminal.CommunicateQ2
import com.example.mtouchpos.viewmodel.usecasemanager.terminal.CommunicateXPDA

class CardTerminalFactory {
    enum class CallBack(val url: String) {
        Home("mtouchpos://default"), External("mtouchpos://appmodule")
    }

    companion object {
        operator fun invoke(
            context: Context,
            launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
            callBack: CallBack
        ): CommunicateCardTerminalManager? = when {
            Build.MODEL.contains("Q2") -> CommunicateQ2(context, callBack)
            Build.MODEL.contains("PM500") -> CommunicatePM500(launcher)
            Build.MODEL.contains("XPDA") -> CommunicateXPDA(context, launcher)
            else -> null
        }
    }
}