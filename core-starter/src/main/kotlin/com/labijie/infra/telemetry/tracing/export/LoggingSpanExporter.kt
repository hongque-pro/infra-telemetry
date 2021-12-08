package com.labijie.infra.telemetry.tracing.export

import com.labijie.infra.telemetry.tracing.Utils.toJson
import io.opentelemetry.exporter.otlp.internal.traces.TraceRequestMarshaler
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import org.slf4j.LoggerFactory

class LoggingSpanExporter : AbstractOltpSpanExporter() {
    companion object {
        private val logger = LoggerFactory.getLogger(LoggingSpanExporter::class.java)
    }


    override fun exportRequest(request: TraceRequestMarshaler) {
        val json = request.toJson()
        logger.info("tracing-span: ${System.lineSeparator()}$json")
    }

    override fun flush(): CompletableResultCode {
        return CompletableResultCode.ofSuccess()
    }

    override fun shutdown(): CompletableResultCode {
        return CompletableResultCode.ofSuccess()
    }
}