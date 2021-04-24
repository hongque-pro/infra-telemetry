package com.labijie.infra.telemetry.testing

import com.labijie.infra.impl.DebugIdGenerator
import com.labijie.infra.telemetry.configuration.tracing.TracingProperties
import com.labijie.infra.telemetry.tracing.TracingManager
import com.labijie.infra.telemetry.tracing.export.LoggingSpanExporter
import com.labijie.infra.telemetry.tracing.injectSpan
import com.labijie.infra.telemetry.tracing.use
import com.labijie.infra.telemetry.tracing.useScope
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.Context
import org.junit.jupiter.api.Assertions
import kotlin.test.*

class TracingManagerTester {

    private val tracingManager = TracingManager(
            "telemetry-test",
            DebugIdGenerator(),
            TracingProperties(),
            listOf(LoggingSpanExporter()),
            W3CTraceContextPropagator.getInstance()
    )

    init {
        TracingManager.instance = tracingManager
    }

    private val tracer = tracingManager.tracer

    @Test
    fun testInject() {
        tracer.spanBuilder("testInject").startSpan().useScope {
            val map = mutableMapOf<String, Any>()
            tracingManager.tracer.injectSpan(map, context = Context.current())

            Assertions.assertTrue(map.isNotEmpty())
        }
    }
}