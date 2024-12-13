package com.example.mtouchpos.managerImpl.factory

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.example.domain.manager.cardterminal.CardTerminalCommunicateManager
import com.example.mtouchpos.managerImpl.cardterminal.CommunicatePM500
import com.example.mtouchpos.managerImpl.cardterminal.CommunicateQ2
import com.example.mtouchpos.managerImpl.cardterminal.CommunicateXPDA

class CardTerminalFactory {
    enum class CallBack(val url: String) {
        Home("mtouchpos://default"), External("mtouchpos://appmodule")
    }

    fun getCommunicateManager(
        context: Context,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
        callBack: CallBack
    ): CardTerminalCommunicateManager? = when {
        Build.MODEL.contains("Q2") -> CommunicateQ2(context, callBack)
        Build.MODEL.contains("PM500") -> CommunicatePM500(launcher)
        Build.MODEL.contains("XPDA") -> CommunicateXPDA(context, launcher)
        else -> null
    }
}