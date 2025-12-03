package com.kwonps.data.parser

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.kwonps.domain.model.user.UserDetailData
import com.kwonps.domain.parser.UserDetailParser
import javax.inject.Inject

class GsonUserDetailParser @Inject constructor(
    private val gson: Gson = Gson()
) : UserDetailParser {
    override fun parse(json: String?): UserDetailData? {
        if (json.isNullOrBlank()) return null

        return try {
            gson.fromJson(json, UserDetailData::class.java)
        } catch (e: JsonSyntaxException) {
            null
        }
    }
}
