package com.labijie.infra.telemetry.tracing.propagation

import io.opentelemetry.context.propagation.TextMapGetter

class MapGetter private constructor(prefix: String? = null) : TextMapGetter<Map<String, Any>> {
    companion object {
        @JvmStatic
       val INSTANCE: MapGetter by lazy {
            MapGetter()
        }

        @JvmStatic
        fun withPrefix(prefix: String): MapGetter {
            return MapGetter(prefix)
        }
    }

    private val keyPrefix = prefix ?: "__span_"
    override fun get(carrier: Map<String, Any>?, key: String): String? {
        if(carrier != null && !key.isBlank()){
            return carrier["$keyPrefix$key"]?.toString()
        }
        return null
    }

    override fun keys(carrier: Map<String, Any>): MutableIterable<String> {
         return carrier.keys.filter {
             keyPrefix.isEmpty() || it.startsWith(keyPrefix)
         }.map { it.removePrefix(keyPrefix) }.toMutableSet()
    }


}