package com.labijie.infra.telemetry.tracing

import com.labijie.infra.IIdGenerator
import io.opentelemetry.sdk.trace.IdsGenerator
import io.opentelemetry.trace.SpanId
import io.opentelemetry.trace.TraceId
import java.util.*

class TelemetryIdsGenerator(private val generator: IIdGenerator) : IdsGenerator {
    override fun generateSpanId(): String {
        return SpanId.fromLong(generator.newId())
    }
    override fun generateTraceId(): String {
        val uuid = UUID.randomUUID()
        return TraceId.fromLongs(uuid.mostSignificantBits, uuid.leastSignificantBits)
    }
}