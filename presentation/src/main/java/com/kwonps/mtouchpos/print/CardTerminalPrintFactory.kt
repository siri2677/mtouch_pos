package com.kwonps.mtouchpos.print

import android.content.Context
import android.os.Build
import com.kwonps.mtouchpos.vo.info.ApprovedPaymentType
import com.kwonps.mtouchpos.vo.info.UserInfo

class CardTerminalPrintFactory {
    fun getCommunicateManager(
        context: Context,
        userInfo: UserInfo,
        approvedPaymentType: ApprovedPaymentType
    ): CardTerminalPrintManager? = when {
        Build.MODEL.contains("Q2") -> PrintQ2(context, userInfo, approvedPaymentType)
        Build.MODEL.contains("PM500") -> PrintPM500(context, userInfo, approvedPaymentType)
        Build.MODEL.contains("XPDA") -> PrintXPDA(userInfo, approvedPaymentType)
        else -> null
    }
}