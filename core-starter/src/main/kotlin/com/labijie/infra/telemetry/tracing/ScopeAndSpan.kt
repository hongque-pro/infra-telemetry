package com.labijie.infra.telemetry.tracing

import io.opentelemetry.context.ContextUtils
import io.opentelemetry.context.Scope
import io.opentelemetry.trace.EndSpanOptions
import io.opentelemetry.trace.Span

class ScopeAndSpan(val scope: Scope, val span: Span, private val closeCallbacks: (()->Unit)? = null) : AutoCloseable {
    private var hasEnded: Boolean = false
    private var syncRoot: Any = Any()

    override fun close() {
        this.close(null)
    }

    fun endSpan(options: EndSpanOptions? = null) {
        if (!hasEnded) {
            synchronized(syncRoot) {
                if (!hasEnded) {
                    if (options == null) {
                        span.end()
                    } else {
                        span.end(options)
                    }
                    hasEnded = true
                }
            }
        }
    }

    fun close(options: EndSpanOptions?) {
        endSpan(options)
        scope.close()
        this.closeCallbacks?.invoke()
    }
}