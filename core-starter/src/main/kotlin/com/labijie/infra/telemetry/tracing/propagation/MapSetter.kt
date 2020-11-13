package com.labijie.infra.telemetry.tracing.propagation

import io.opentelemetry.context.propagation.TextMapPropagator

object MapSetter : TextMapPropagator.Setter<MutableMap<String,in String>> {
    override fun set(carrier: MutableMap<String, in String>?, key: String?, value: String?) {
        if(!key.isNullOrBlank() && !value.isNullOrBlank()) {
            carrier?.put("${MapGetter.Prefix}$key", value)
        }
    }
}