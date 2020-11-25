package com.labijie.infra.telemetry.tracing.export

import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import org.slf4j.LoggerFactory

class LoggingSpanExporter : SpanExporter {
    companion object {
        private val logger = LoggerFactory.getLogger(LoggingSpanExporter::class.java)
    }

    override fun export(spans: MutableCollection<SpanData>): CompletableResultCode? {
        if (spans.isNotEmpty()) {
            for (span in spans) {
                logger.info("tracing-span: $span")
            }
        }
        return CompletableResultCode.ofSuccess()
    }

    override fun flush(): CompletableResultCode = CompletableResultCode.ofSuccess()

    override fun shutdown(): CompletableResultCode = CompletableResultCode.ofSuccess()
}