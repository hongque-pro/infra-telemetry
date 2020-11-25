package com.labijie.infra.telemetry.tracing.propagation

import io.opentelemetry.context.propagation.TextMapPropagator

class MapSetter private constructor(prefix: String? = null) : TextMapPropagator.Setter<MutableMap<String,in String>> {
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