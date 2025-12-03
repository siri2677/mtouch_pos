package com.kwonps.mtouchpos.common.serialization

import com.google.gson.Gson
import com.kwonps.domain.adapter.JsonAdapter

class GsonJsonAdapter(
    private val gson: Gson
) : JsonAdapter {
    override fun <T> fromJson(json: String?, clazz: Class<T>): T? = json?.let {
        gson.fromJson(it, clazz)
    }

    override fun <T> toJson(value: T, clazz: Class<T>): String = gson.toJson(value, clazz)
}
