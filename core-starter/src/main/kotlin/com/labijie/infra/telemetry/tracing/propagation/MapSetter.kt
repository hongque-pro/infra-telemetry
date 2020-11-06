package com.labijie.infra.telemetry.tracing.propagation

import io.opentelemetry.context.propagation.TextMapPropagator

object MapSetter : TextMapPropagator.Setter<MutableMap<String, Any>> {
    override fun set(carrier: MutableMap<String, Any>?, key: String?, value: String?) {
        if(!key.isNullOrBlank() && !value.isNullOrBlank()) {
            carrier?.put("${MapGetter.Prefix}$key", value)
        }
    }
}