package com.kwonps.mtouchpos.intent

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult

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
        Build.MODEL.contains("PM500") -> CommunicatePM500(context, launcher)
        Build.MODEL.contains("XPDA") -> CommunicateXPDA(context, callBack)
        else -> null
    }
}