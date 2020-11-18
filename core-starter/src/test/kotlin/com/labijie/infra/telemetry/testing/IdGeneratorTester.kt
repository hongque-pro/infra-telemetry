package com.labijie.infra.telemetry.testing

import com.labijie.infra.impl.DebugIdGenerator
import com.labijie.infra.telemetry.tracing.TelemetryIdsGenerator
import io.opentelemetry.trace.TraceId
import org.junit.jupiter.api.Assertions
import kotlin.test.Test

class IdGeneratorTester {
    private val idGen = TelemetryIdsGenerator(DebugIdGenerator())

    @Test
    fun traceIdTest(){
        val id = idGen.generateTraceId()
        Assertions.assertTrue(TraceId.isValid(id))
    }

    @Test
    fun spanIdTest(){
        val id = idGen.generateTraceId()
        Assertions.assertTrue(TraceId.isValid(id))
    }
}