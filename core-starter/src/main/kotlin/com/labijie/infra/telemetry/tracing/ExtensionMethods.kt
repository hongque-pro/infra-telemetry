package com.labijie.infra.telemetry.tracing

import io.grpc.Context
import io.opentelemetry.context.ContextUtils
import io.opentelemetry.context.Scope
import io.opentelemetry.trace.Span
import io.opentelemetry.trace.Tracer
import io.opentelemetry.trace.TracingContextUtils

fun Tracer.extractSpan(map: Map<String, Any>, context: Context? = null): Span = TracingManager.extractSpan(map, context)

fun Tracer.injectSpan(map: MutableMap<String, in String>, context: Context? = null) = TracingManager.injectSpan(map, context)


val Context.span: Span?
    get(){
        val span = TracingContextUtils.getSpanWithoutDefault(this)
        return if(span != null && span.context.isValid){
            span
        }else{
            null
        }
    }

fun Context.startScope(span: Span): Scope {
    val context = TracingContextUtils.withSpan(span, this)
    return ContextUtils.withScopedContext(context)
}