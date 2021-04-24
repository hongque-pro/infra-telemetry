package com.labijie.infra.telemetry.tracing.propagation

import io.opentelemetry.context.propagation.TextMapSetter

class MapSetter private constructor(prefix: String? = null) : TextMapSetter<MutableMap<String,in String>> {
    companion object {
        @JvmStatic
        val INSTANCE: MapSetter by lazy {
            MapSetter()
        }

        @JvmStatic
        fun withPrefix(prefix: String): MapSetter {
            return MapSetter(prefix)
        }
    }
    private val keyPrefix = prefix ?: "__span_"

    override fun set(carrier: MutableMap<String, in String>?, key: String, value: String) {
        if(key.isNotBlank() && value.isNotBlank()) {
            carrier?.put("${keyPrefix}$key", value)
        }
    }
}