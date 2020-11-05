package com.labijie.infra.telemetry.tracing

import com.labijie.infra.IIdGenerator
import com.labijie.infra.telemetry.configuration.tracing.TracingProperties
import io.opentelemetry.OpenTelemetry
import io.opentelemetry.context.propagation.DefaultContextPropagators
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.sdk.common.export.ConfigBuilder
import io.opentelemetry.sdk.trace.*
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.trace.Tracer
import io.opentelemetry.trace.propagation.HttpTraceContext
import java.util.*


class TracingManager(
    idGenerator: IIdGenerator,
    private val properties: TracingProperties,
    private val exporters: List<SpanExporter>,
    private val textMapPropagator: TextMapPropagator? = null
) {
    private val sdkProvider = TracerSdkProvider.builder().setIdsGenerator(TracerIdsGenertor(idGenerator)).build().apply {
        if (exporters.count() > 0) {
            val processors = exporters.map { it.createProcessor(properties.exportStrategy) }
            val processor = if (processors.count() > 1) MultiSpanProcessor.create(processors) else processors.first()
            this.addSpanProcessor(processor)
        }
    }

    private fun configurePropagator() {
        val defaultContextPropagators = DefaultContextPropagators.builder()
            .addTextMapPropagator(textMapPropagator ?: HttpTraceContext.getInstance())
            .build()
        OpenTelemetry.setPropagators(defaultContextPropagators)
    }

    private fun <T : ConfigBuilder<*>> T.configureProcessorBuilder(): T {
        val properties = Properties(properties.processorProperties)
        this.readProperties(properties)
        this.readEnvironmentVariables()
        return this
    }

    private fun SpanExporter.createProcessor(exportStrategy: ExportStrategy): SpanProcessor {
        return when (exportStrategy) {
            ExportStrategy.Batch -> BatchSpanProcessor.newBuilder(this).configureProcessorBuilder().build()
            ExportStrategy.Simple -> SimpleSpanProcessor.newBuilder(this).configureProcessorBuilder().build()
        }
    }

    val tracer: Tracer by lazy {
        configurePropagator()
        sdkProvider.get("infra-telemetry")
    }

    private class TracerIdsGenertor(private val generator: IIdGenerator) : IdsGenerator {
        override fun generateSpanId(): String = generator.newId().toString()
        override fun generateTraceId(): String = generator.newId().toString()
    }
}