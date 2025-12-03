package com.kwonps.mtouchpos.fakes

import com.google.gson.GsonBuilder
import com.kwonps.data.common.serialization.RuntimeTypeAdapterFactory
import com.kwonps.domain.adapter.JsonAdapter
import com.kwonps.domain.model.cardreader.CardReaderData

class TestJsonAdapter : JsonAdapter {
    private val gson = GsonBuilder().registerTypeAdapterFactory(
        RuntimeTypeAdapterFactory.of(CardReaderData::class.java, "type")
            .registerSubtype(CardReaderData.Bluetooth::class.java)
            .registerSubtype(CardReaderData.Usb::class.java)
    ).create()

    override fun <T> fromJson(json: String?, clazz: Class<T>): T? = json?.let { gson.fromJson(it, clazz) }

    override fun <T> toJson(value: T, clazz: Class<T>): String = gson.toJson(value, clazz)
}
