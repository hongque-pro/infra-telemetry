package com.labijie.infra.telemetry.tracing

import com.labijie.infra.IIdGenerator
import com.labijie.infra.telemetry.configuration.tracing.EXPORTER_KAFKA
import com.labijie.infra.telemetry.configuration.tracing.TracingProperties
import com.labijie.infra.telemetry.configuration.tracing.configure
import com.labijie.infra.telemetry.tracing.propagation.MapGetter
import com.labijie.infra.telemetry.tracing.propagation.MapSetter
import com.labijie.infra.utils.ifNullOrBlank
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.SpanProcessor
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
        private val propagators: TextMapPropagator
) : DisposableBean {
    companion object {
        private val logger = LoggerFactory.getLogger(TracingManager::class.java)

        @JvmStatic
        lateinit var instance: TracingManager

        @JvmStatic
        fun mustBeInstance(): TracingManager {
            if (!::instance.isInitialized){
                throw RuntimeException("TracingManager instance is not ready, please initialize it by assigning TracingManager.Instance")
            }
            return instance
        }
    }

    private var version: String = "0.0.0"

    init {
        ClassUtils.getDefaultClassLoader()?.getResourceAsStream("telemetry-git.properties")?.let {
            Properties().apply {
                this.load(it)
            }
        }?.run {
            version = (this["git.build.version"]?.toString()).ifNullOrBlank { "0.0.0" }
        }
    }

    fun extractSpan(map: Map<String, Any>, context: Context? = null): Span {
        val propagator = this.propagators
        val ctx = context ?: Context.current()
        propagator.extract(ctx, map, MapGetter.INSTANCE)
        return Span.fromContext(ctx)
    }

    fun injectSpan(map: MutableMap<String, in String>, context: Context? = null) {
        val propagator = this.propagators
        propagator.inject(context ?: Context.current(), map, MapSetter.INSTANCE)
    }

    private fun SpanExporter.createProcessor(properties: TracingProperties): SpanProcessor {
        return when (properties.exporter.strategy) {
            ExportStrategy.Batch -> BatchSpanProcessor.builder(this).configure(properties.exporter.batch).build()
            ExportStrategy.Simple -> SimpleSpanProcessor.create(this)
        }
    }

//    private fun <T : ConfigBuilder<*>> T.configureProcessorBuilder(): T {
//        val props = Properties()
//        properties.processorProperties.forEach { (key, value) ->
//            if (value.isNotBlank()) {
//                props[key] = value
//            }
//        }
//        this.readProperties(props)
//        this.readEnvironmentVariables()
//        return this
//    }

    private fun mapProcessor(): List<SpanProcessor> {
        if (exporters.count() > 0) {
            return exporters.map { it.createProcessor(properties) }
        }

        val sb = StringBuilder()
        sb.appendLine()
        if (properties.exporter.provider == EXPORTER_KAFKA) {
            sb.appendLine("configured exporter: ${properties.exporter}, make sure 'org.apache.kafka:kafka-clients' package is in your classpath.")
        }
        logger.warn(sb.toString())
        return listOf()
    }

    private val sdk: OpenTelemetry by lazy {

        val resource = Resource.builder()
                .put("application", this.applicationName)
                .build()


        val sdkTracerProvider = SdkTracerProvider.builder()
                .also {
                    this.mapProcessor().forEach { processor ->
                        it.addSpanProcessor(processor)
                    }
                }
                .setResource(resource)
                .setIdGenerator(TelemetryIdsGenerator(idGenerator))
                .build()


        OpenTelemetrySdk.builder()
                .setPropagators(ContextPropagators.create(this.propagators))
                .setTracerProvider(sdkTracerProvider)
                .build()
    }


    val tracer: Tracer by lazy {
        this.sdk.getTracer("labijie-infra-telemetry", this.version)
    }

    override fun destroy() {
        (this.sdk.tracerProvider as? SdkTracerProvider)?.shutdown()
    }

}