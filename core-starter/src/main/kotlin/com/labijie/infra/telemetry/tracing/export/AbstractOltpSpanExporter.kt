package com.labijie.infra.telemetry.tracing.export

import io.opentelemetry.exporter.otlp.internal.traces.TraceRequestMarshaler
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter

abstract class AbstractOltpSpanExporter : SpanExporter {

    override fun export(spans: MutableCollection<SpanData>): CompletableResultCode {
        if(spans.isNotEmpty()) {

            val request = TraceRequestMarshaler.create(spans)
            try {
                this.exportRequest(request)
            } catch (e: Exception) {
                return CompletableResultCode.ofFailure()
            }
        }
        return CompletableResultCode.ofSuccess()
    }

    protected  abstract fun exportRequest(request: TraceRequestMarshaler)
}