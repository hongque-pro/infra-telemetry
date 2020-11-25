package com.labijie.infra.telemetry.tracing

import com.labijie.infra.IIdGenerator
import com.labijie.infra.telemetry.configuration.tracing.TracingProperties
import com.labijie.infra.telemetry.tracing.propagation.MapGetter
import com.labijie.infra.telemetry.tracing.propagation.MapSetter
import com.labijie.infra.utils.ifNullOrBlank
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.propagation.HttpTraceContext
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.DefaultContextPropagators
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.common.export.ConfigBuilder
import io.opentelemetry.sdk.trace.MultiSpanProcessor
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.TracerSdkProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.util.ClassUtils
import java.util.*


class TracingManager(
    private val applicationName: String?,
    private val idGenerator: IIdGenerator,
    private val properties: TracingProperties,
    private val exporters: List<SpanExporter>,
    private val textMapPropagator: TextMapPropagator? = null
) : DisposableBean {
    companion object {
        private val logger = LoggerFactory.getLogger(TracingManager::class.java)

        fun extractSpan(map: Map<String, Any>, context: Context? = null): Span {
            val propagator = OpenTelemetry.getGlobalPropagators().textMapPropagator
            val ctx = context ?: Context.current()
            propagator.extract(ctx, map, MapGetter.INSTANCE)
            return Span.fromContext(ctx)
        }

        fun injectSpan(map: MutableMap<String, in String>, context: Context? = null) {
            val propagator = OpenTelemetry.getGlobalPropagators().textMapPropagator
            propagator.inject(context ?: Context.current(), map, MapSetter.INSTANCE)
        }
    }

    private var version: String = "0.0.0.0"

    init {
        ClassUtils.getDefaultClassLoader()?.getResourceAsStream("telemetry-git.properties")?.let {
            Properties().apply {
                this.load(it)
            }
        }?.run {
            version = (this["git.build.version"]?.toString()).ifNullOrBlank { "0.0.0" }
        }
    }

    private fun configureSdk() {
        val defaultContextPropagators = DefaultContextPropagators.builder()
            .addTextMapPropagator(this.textMapPropagator ?: HttpTraceContext.getInstance())
            .build()

        val tracerSdkProvider= TracerSdkProvider
            .builder()
            .setIdsGenerator(TelemetryIdsGenerator(idGenerator))
            .build()

        val sdk = OpenTelemetrySdk.builder()
            .setPropagators(defaultContextPropagators)
            .setTracerProvider(tracerSdkProvider)
            .build()
        OpenTelemetry.set(sdk)
    }

    private fun <T : ConfigBuilder<*>> T.configureProcessorBuilder(): T {
        val props = Properties()
        properties.processorProperties.forEach { (key, value) ->
            if (value.isNotBlank()) {
                props[key] = value
            }
        }
        this.readProperties(props)
        this.readEnvironmentVariables()
        return this
    }

    private fun SpanExporter.createProcessor(exportStrategy: ExportStrategy): SpanProcessor {
        return when (exportStrategy) {
            ExportStrategy.Batch -> BatchSpanProcessor.builder(this).configureProcessorBuilder().build()
            ExportStrategy.Simple -> SimpleSpanProcessor.builder(this).configureProcessorBuilder().build()
        }
    }

    val tracer: Tracer by lazy {
        configureSdk()
        val tracer = OpenTelemetry.getGlobalTracer(
            "com.labijie.infra.telemetry", version)

        val tracerSdkManagement = OpenTelemetrySdk.getGlobalTracerManagement()
        if (exporters.count() > 0) {
            val processors = exporters.map { it.createProcessor(properties.exportStrategy) }
            val processor =
                if (processors.count() > 1) MultiSpanProcessor.create(processors) else processors.first()
            tracerSdkManagement.addSpanProcessor(processor)
        } else {
            logger.warn("Can not found any trace exportor, configured exporter: ${properties.exporter}, make sure 'org.apache.kafka:kafka-clients' package is in your classpath.")
        }

        tracer
    }

    override fun destroy() {
        OpenTelemetrySdk.getGlobalTracerManagement().shutdown()
    }

}