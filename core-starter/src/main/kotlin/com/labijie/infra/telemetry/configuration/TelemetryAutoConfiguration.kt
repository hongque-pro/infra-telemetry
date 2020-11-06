package com.labijie.infra.telemetry.configuration

import com.labijie.infra.IIdGenerator
import com.labijie.infra.spring.configuration.getApplicationName
import com.labijie.infra.telemetry.TelemetryBootstrapRunner
import com.labijie.infra.telemetry.tracing.TracingManager
import com.labijie.infra.telemetry.configuration.tracing.TracerFactoryBean
import com.labijie.infra.telemetry.tracing.export.KafkaSpanExporter
import com.labijie.infra.telemetry.tracing.export.LoggingSpanExporter
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.trace.propagation.HttpTraceContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.util.stream.Collectors

@Configuration
@EnableConfigurationProperties(TelemetryProperties::class)
class TelemetryAutoConfiguration {

    companion object {
        const val MetricEnabledConfigurationKey = "infra.telemetry.tracing.metric.enabled"
        const val TracingEnabledConfigurationKey = "infra.telemetry.tracing.tracing.enabled"
        const val TracingExporterConfigurationKey = "infra.telemetry.tracing.tracing.built-in-exporter"
        const val TracingPropertiesConfigurationKey = "infra.telemetry.tracing.processor-properties"
    }

    @Bean
    fun telemetryBootstrapRunner(): TelemetryBootstrapRunner {
        return TelemetryBootstrapRunner()
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
        @ConditionalOnClass(name = ["org.apache.kafka.clients.producer.KafkaProducer"])
        @ConditionalOnProperty(
            name = [TracingExporterConfigurationKey],
            havingValue = "Kafka",
            matchIfMissing = true
        )
        fun kafkaSpanExporter(environment: Environment, properties: TelemetryProperties): KafkaSpanExporter {
            return KafkaSpanExporter(environment, properties.tracing)
        }


        @Bean
        fun tracingManager(
            environment: Environment,
            idGenerator: IIdGenerator,
            telemetryProperties: TelemetryProperties,
            exporters: ObjectProvider<SpanExporter>,
            @Autowired(required = false)
            textMapPropagator: TextMapPropagator?
        ): TracingManager {

            val exporterList = exporters.orderedStream().collect(Collectors.toList())
            val applicationName: String? = environment.getProperty("spring.application.name")
            return TracingManager(
                applicationName,
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