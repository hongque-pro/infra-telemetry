package com.labijie.infra.telemetry.tracing
import io.opentelemetry.api.trace.Span
import io.opentelemetry.context.Scope
import java.time.Instant

class ScopeAndSpan(val scope: Scope, val span: Span, private val closeCallbacks: (()->Unit)? = null) : AutoCloseable {
    private var hasEnded: Boolean = false
    private var syncRoot: Any = Any()

    override fun close() {
        this.close(null)
    }

    fun endSpan(timestamp: Instant? = null) {
        if (!hasEnded) {
            synchronized(syncRoot) {
                if (!hasEnded) {
                    if(timestamp != null) {
                        span.end(timestamp)
                    }else{
                        span.end()
                    }
                    hasEnded = true
                }
            }
        }
    }

    fun close(timestamp: Instant?) {
        endSpan(timestamp)
        scope.close()
        this.closeCallbacks?.invoke()
    }
}