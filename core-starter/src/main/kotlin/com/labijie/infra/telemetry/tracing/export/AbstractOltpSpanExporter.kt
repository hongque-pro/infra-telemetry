package com.labijie.infra.telemetry.tracing.export

import com.labijie.infra.telemetry.proto.SpanAdapter
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter

abstract class AbstractOltpSpanExporter : SpanExporter {
    override fun export(spans: MutableCollection<SpanData>?): CompletableResultCode {
        if(spans != null) {
            val exportTraceServiceRequest = ExportTraceServiceRequest.newBuilder()
                .addAllResourceSpans(SpanAdapter.toProtoResourceSpans(spans))
                .build()

            try {
                this.exportRequest(exportTraceServiceRequest)
            } catch (e: Exception) {
                return CompletableResultCode.ofFailure()
            }
        }
        return CompletableResultCode.ofSuccess()
    }

    protected  abstract fun exportRequest(request: ExportTraceServiceRequest)
}