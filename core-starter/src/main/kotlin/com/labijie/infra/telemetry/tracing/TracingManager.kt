package com.labijie.infra.telemetry.tracing

import com.labijie.infra.IIdGenerator
import com.labijie.infra.telemetry.configuration.tracing.TracingProperties
import com.labijie.infra.telemetry.tracing.propagation.MapGetter
import com.labijie.infra.telemetry.tracing.propagation.MapSetter
import com.labijie.infra.utils.ifNullOrBlank
import io.grpc.Context
import io.opentelemetry.OpenTelemetry
import io.opentelemetry.context.propagation.DefaultContextPropagators
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.sdk.common.export.ConfigBuilder
import io.opentelemetry.sdk.trace.*
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.trace.Span
import io.opentelemetry.trace.Tracer
import io.opentelemetry.trace.TracingContextUtils
import io.opentelemetry.trace.propagation.HttpTraceContext
import org.slf4j.LoggerFactory


class TracingManager(
    private val applicationName: String?,
    idGenerator: IIdGenerator,
    private val properties: TracingProperties,
    private val exporters: List<SpanExporter>,
    private val textMapPropagator: TextMapPropagator? = null
) {
    companion object {
        private val logger = LoggerFactory.getLogger(TracingManager::class.java)

        fun extractSpan(map: Map<String, Any>): Span {
            val context = OpenTelemetry.getPropagators()
                .textMapPropagator
                .extract(Context.current(), map, MapGetter)

            return TracingContextUtils.getSpan(context)
        }

        fun injectSpan(map: MutableMap<String, Any>) {
            OpenTelemetry.getPropagators()
                .textMapPropagator
                .inject(Context.current(), map, MapSetter)
        }
    }

    private val sdkProvider = TracerSdkProvider.builder()
        .setIdsGenerator(TracerIdsGenerator(idGenerator))
        .build().apply {
            if (exporters.count() > 0) {
                val processors = exporters.map { it.createProcessor(properties.exportStrategy) }
                val processor =
                    if (processors.count() > 1) MultiSpanProcessor.create(processors) else processors.first()
                this.addSpanProcessor(processor)
            } else {
                logger.warn("Can not found any trace exportor, configured built-in exporter: ${properties.builtInExporter}, make sure 'org.apache.kafka:kafka-clients' package is in your classpath.")
            }
        }

    private fun configurePropagator() {
        val defaultContextPropagators = DefaultContextPropagators.builder()
            .addTextMapPropagator(textMapPropagator ?: HttpTraceContext.getInstance())
            .build()
        OpenTelemetry.setPropagators(defaultContextPropagators)
    }

    private fun <T : ConfigBuilder<*>> T.configureProcessorBuilder(): T {
        val properties = properties.processorProperties
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
        sdkProvider.get(applicationName.ifNullOrBlank { "infra-telemetry" })
    }

    private class TracerIdsGenerator(private val generator: IIdGenerator) : IdsGenerator {
        override fun generateSpanId(): String = generator.newId().toString()
        override fun generateTraceId(): String = generator.newId().toString()
    }


}