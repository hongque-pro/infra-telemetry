package com.labijie.infra.telemetry.tracing

import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.ReadWriteSpan
import io.opentelemetry.sdk.trace.ReadableSpan
import io.opentelemetry.sdk.trace.SpanProcessor

/**
 *
 * @Auther: AndersXiao
 * @Date: 2021-04-24 15:44
 * @Description:
 */
internal object NoopSpanProcessor:  SpanProcessor {
    override fun onStart(parentContext: Context, span: ReadWriteSpan) {

    }

    override fun isStartRequired(): Boolean {
        return false
    }

    override fun onEnd(span: ReadableSpan) {

    }

    override fun isEndRequired(): Boolean {
        return false
    }

}