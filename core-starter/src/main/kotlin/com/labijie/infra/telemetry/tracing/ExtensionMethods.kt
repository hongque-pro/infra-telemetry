package com.labijie.infra.telemetry.tracing

import com.labijie.infra.telemetry.tracing.propagation.MapGetter
import com.labijie.infra.telemetry.tracing.propagation.MapSetter
import io.grpc.Context
import io.opentelemetry.OpenTelemetry
import io.opentelemetry.trace.Span
import io.opentelemetry.trace.Tracer
import io.opentelemetry.trace.TracingContextUtils

fun Tracer.extractSpan(map: Map<String, Any>): Span = TracingManager.extractSpan(map)

fun Tracer.injectSpan(map: MutableMap<String, Any>) = TracingManager.injectSpan(map)