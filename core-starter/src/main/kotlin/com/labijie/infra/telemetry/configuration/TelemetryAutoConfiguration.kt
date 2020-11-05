package com.labijie.infra.telemetry.configuration

import com.labijie.infra.IIdGenerator
import com.labijie.infra.telemetry.tracing.TracingManager
import com.labijie.infra.telemetry.configuration.tracing.TracerFactoryBean
import com.labijie.infra.telemetry.tracing.export.KafkaSpanExporter
import com.labijie.infra.telemetry.tracing.export.LoggingSpanExporter
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.trace.propagation.HttpTraceContext
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.stream.Collectors

@Configuration
@EnableConfigurationProperties(TelemetryProperties::class)
class TelemetryAutoConfiguration {

    companion object {
        const val MetricEnabledConfigurationKey = "infra.telemetry.tracing.metric.enabled"
        const val TracingEnabledConfigurationKey = "infra.telemetry.tracing.tracing.enabled"
        const val TracingExporterConfigurationKey = "infra.telemetry.tracing.tracing.built-in-exporter"
        const val TracingPropertiesConfigurationKey = "infra.telemetry.tracing.tracing.processor-properties"
    }

    @Configuration
    @ConditionalOnProperty(
        name = [TracingEnabledConfigurationKey],
        havingValue = "true",
        matchIfMissing = true
    )
    protected class TracingAutoConfiguration {

        @Bean
        @ConditionalOnProperty(
            name = [TracingExporterConfigurationKey],
            havingValue = "Logging",
            matchIfMissing = false
        )
        fun loggingSpanExporter(): LoggingSpanExporter = LoggingSpanExporter()

        @Bean
        @ConditionalOnProperty(
            name = [TracingExporterConfigurationKey],
            havingValue = "Kafka",
            matchIfMissing = true
        )
        fun kafkaSpanExporter(properties: TelemetryProperties): KafkaSpanExporter {
            return kafkaSpanExporter(properties)
        }


        @Bean
        fun tracingManager(
            idGenerator: IIdGenerator,
            telemetryProperties: TelemetryProperties,
            exporters: ObjectProvider<SpanExporter>,
            @Autowired(required = false)
            textMapPropagator: TextMapPropagator?
        ): TracingManager {

            val exporterList = exporters.orderedStream().collect(Collectors.toList())

            return TracingManager(
                idGenerator,
                telemetryProperties.tracing,
                exporterList,
                textMapPropagator ?: HttpTraceContext.getInstance()
            )
        }

        @Bean
        fun tracerFactoryBean(tracingManager: TracingManager): TracerFactoryBean {
            return TracerFactoryBean(tracingManager)
        }
    }

    @Configuration
    @ConditionalOnProperty(
        name = [MetricEnabledConfigurationKey],
        havingValue = "true",
        matchIfMissing = true
    )
    protected class MetricAutoConfiguration {

    }

}