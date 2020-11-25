package com.labijie.infra.telemetry.tracing

import com.labijie.infra.IIdGenerator
import io.opentelemetry.api.trace.SpanId
import io.opentelemetry.api.trace.TraceId
import io.opentelemetry.sdk.trace.IdGenerator
import java.util.*

class TelemetryIdsGenerator(private val generator: IIdGenerator) : IdGenerator {
    override fun generateSpanId(): String {
        return SpanId.fromLong(generator.newId())
    }
    override fun generateTraceId(): String {
        val uuid = UUID.randomUUID()
        return TraceId.fromLongs(uuid.mostSignificantBits, uuid.leastSignificantBits)
    }
}