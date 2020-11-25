package com.labijie.infra.telemetry.tracing

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.Scope

fun Tracer.extractSpan(map: Map<String, Any>, context: Context? = null): Span = TracingManager.extractSpan(map, context)

fun Tracer.injectSpan(map: MutableMap<String, in String>, context: Context? = null) =
    TracingManager.injectSpan(map, context)


val Context.span: Span?
    get() = Span.fromContextOrNull(this)

fun Context.startScope(span: Span): Scope {
    val context = this.with(span)
    return context.makeCurrent()
}

fun Span.use(execution: Span.() -> Unit) {
    AutoCloseableSpanWrapper(this).use {
        execution.invoke(this)
    }
}

fun Span.useScope(context: Context? = null, execution: ScopeAndSpan.() -> Unit) {
    val scope = (context ?: Context.current()).with(this).makeCurrent()
        ScopeAndSpan(scope, this).use {
            execution.invoke(it)
    }
}