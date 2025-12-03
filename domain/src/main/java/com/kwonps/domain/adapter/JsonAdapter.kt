package com.kwonps.domain.adapter

interface JsonAdapter {
    fun <T> fromJson(json: String?, clazz: Class<T>): T?

    fun <T> toJson(value: T, clazz: Class<T>): String
}
