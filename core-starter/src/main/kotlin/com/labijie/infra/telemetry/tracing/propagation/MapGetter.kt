package com.labijie.infra.telemetry.tracing.propagation

import io.opentelemetry.context.propagation.TextMapPropagator

object MapGetter : TextMapPropagator.Getter<Map<String, Any>> {
    internal const val Prefix = "__span_"
    override fun get(carrier: Map<String, Any>?, key: String?): String? {
        if(carrier != null && !key.isNullOrBlank()){
            carrier["$Prefix$key"]
        }
        return null
    }
}