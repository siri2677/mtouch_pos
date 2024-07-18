package com.example.mtouchpos.hilt

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException

class RuntimeTypeAdapterFactory<T> private constructor(
    baseType: Class<*>?,
    typeFieldName: String?,
    maintainType: Boolean
) : TypeAdapterFactory {
    private val baseType: Class<*>
    private val typeFieldName: String
    private val labelToSubtype: MutableMap<String, Class<*>> = LinkedHashMap()
    private val subtypeToLabel: MutableMap<Class<*>, String> = LinkedHashMap()
    private val maintainType: Boolean
    private var recognizeSubtypes = false

    init {
        if (typeFieldName == null || baseType == null) {
            throw NullPointerException()
        }
        this.baseType = baseType
        this.typeFieldName = typeFieldName
        this.maintainType = maintainType
    }

    /**
     * Ensures that this factory will handle not just the given `baseType`, but any subtype of
     * that type.
     */
    @com.google.errorprone.annotations.CanIgnoreReturnValue
    fun recognizeSubtypes(): RuntimeTypeAdapterFactory<T> {
        this.recognizeSubtypes = true
        return this
    }

    /**
     * Registers `type` identified by `label`. Labels are case sensitive.
     *
     * @throws IllegalArgumentException if either `type` or `label` have already been
     * registered on this type adapter.
     */
    @com.google.errorprone.annotations.CanIgnoreReturnValue
    fun registerSubtype(type: Class<out T>?, label: String?): RuntimeTypeAdapterFactory<T> {
        if (type == null || label == null) {
            throw NullPointerException()
        }
        if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
            throw IllegalArgumentException("types and labels must be unique")
        }
        labelToSubtype[label] = type
        subtypeToLabel[type] = label
        return this
    }

    /**
     * Registers `type` identified by its [simple name][Class.getSimpleName]. Labels are
     * case sensitive.
     *
     * @throws IllegalArgumentException if either `type` or its simple name have already been
     * registered on this type adapter.
     */
    @com.google.errorprone.annotations.CanIgnoreReturnValue
    fun registerSubtype(type: Class<out T>): RuntimeTypeAdapterFactory<T> {
        return registerSubtype(type, type.simpleName)
    }

    override fun <R> create(gson: Gson, type: TypeToken<R>): TypeAdapter<R>? {
        if (type == null) {
            return null
        }
        val rawType: Class<*> = type.rawType
        val handle =
            if (recognizeSubtypes) baseType.isAssignableFrom(rawType) else baseType == rawType
        if (!handle) {
            return null
        }

        val jsonElementAdapter = gson.getAdapter(
            JsonElement::class.java
        )
        val labelToDelegate: MutableMap<String, TypeAdapter<*>> = LinkedHashMap()
        val subtypeToDelegate: MutableMap<Class<*>, TypeAdapter<*>> = LinkedHashMap()
        for (entry: Map.Entry<String, Class<*>> in labelToSubtype.entries) {
            val delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.value))
            labelToDelegate[entry.key] = delegate
            subtypeToDelegate[entry.value] = delegate
        }

        return object : TypeAdapter<R>() {
            @Throws(IOException::class)
            override fun read(`in`: JsonReader): R {
                val jsonElement = jsonElementAdapter.read(`in`)
                val labelJsonElement: JsonElement? = if (maintainType) {
                    jsonElement.asJsonObject[typeFieldName]
                } else {
                    jsonElement.asJsonObject.remove(typeFieldName)
                }

                if (labelJsonElement == null) {
                    throw JsonParseException(
                        "cannot deserialize "
                                + baseType
                                + " because it does not define a field named "
                                + typeFieldName
                    )
                }
                val label = labelJsonElement.asString
                val delegate// registration requires that subtype extends T
                        = labelToDelegate.get(label) as TypeAdapter<R>?
                    ?: throw JsonParseException(
                        ("cannot deserialize "
                                + baseType
                                + " subtype named "
                                + label
                                + "; did you forget to register a subtype?")
                    )
                return delegate.fromJsonTree(jsonElement)
            }

            @Throws(IOException::class)
            override fun write(out: JsonWriter, value: R) {
                val srcType: Class<*> = value!!::class.java
                val label = subtypeToLabel[srcType]
                val delegate// registration requires that subtype extends T
                        = subtypeToDelegate.get(srcType) as TypeAdapter<R>?
                    ?: throw JsonParseException(
                        "cannot serialize " + srcType.name + "; did you forget to register a subtype?"
                    )
                val jsonObject = delegate.toJsonTree(value).asJsonObject

                if (maintainType) {
                    jsonElementAdapter.write(out, jsonObject)
                    return
                }

                val clone = JsonObject()

                if (jsonObject.has(typeFieldName)) {
                    throw JsonParseException(
                        ("cannot serialize "
                                + srcType.name
                                + " because it already defines a field named "
                                + typeFieldName)
                    )
                }
                clone.add(typeFieldName, JsonPrimitive(label))

                for (e: Map.Entry<String?, JsonElement?> in jsonObject.entrySet()) {
                    clone.add(e.key, e.value)
                }
                jsonElementAdapter.write(out, clone)
            }
        }.nullSafe()
    }

    companion object {
        /**
         * Creates a new runtime type adapter for `baseType` using `typeFieldName` as the type
         * field name. Type field names are case sensitive.
         *
         * @param maintainType true if the type field should be included in deserialized objects
         */
        fun <T> of(
            baseType: Class<T>?, typeFieldName: String?, maintainType: Boolean
        ): RuntimeTypeAdapterFactory<T> {
            return RuntimeTypeAdapterFactory(baseType, typeFieldName, maintainType)
        }

        /**
         * Creates a new runtime type adapter for `baseType` using `typeFieldName` as the type
         * field name. Type field names are case sensitive.
         */
        fun <T> of(baseType: Class<T>, typeFieldName: String): RuntimeTypeAdapterFactory<T> {
            return RuntimeTypeAdapterFactory(baseType, typeFieldName, false)
        }

        /**
         * Creates a new runtime type adapter for `baseType` using `"type"` as the type field
         * name.
         */
        fun <T> of(baseType: Class<T>?): RuntimeTypeAdapterFactory<T> {
            return RuntimeTypeAdapterFactory(baseType, "type", false)
        }
    }
}