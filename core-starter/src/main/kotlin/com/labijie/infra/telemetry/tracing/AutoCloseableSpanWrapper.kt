package com.labijie.infra.telemetry.tracing

import io.opentelemetry.api.trace.Span

internal class AutoCloseableSpanWrapper(private val span: Span) : AutoCloseable {
    override fun close() {
        this.span.end()
    }
}