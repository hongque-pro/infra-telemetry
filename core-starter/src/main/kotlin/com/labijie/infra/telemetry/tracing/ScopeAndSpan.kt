package com.labijie.infra.telemetry.tracing

import io.opentelemetry.context.Scope
import io.opentelemetry.trace.EndSpanOptions
import io.opentelemetry.trace.Span

class ScopeAndSpan(val scope: Scope, val span: Span) : AutoCloseable {
    private var hasEnded: Boolean = false
    private var syncRoot: Any = Any()

    override fun close() {
        endSpan(null)
        scope.close()
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
                }
            }
        }
    }

    fun close(options: EndSpanOptions) {
        endSpan(options)
        scope.close()
    }
}