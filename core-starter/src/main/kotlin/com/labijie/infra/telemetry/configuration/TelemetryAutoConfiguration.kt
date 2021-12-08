package com.labijie.infra.telemetry.configuration

import com.labijie.infra.IIdGenerator
import com.labijie.infra.telemetry.TelemetryBootstrapRunner
import com.labijie.infra.telemetry.configuration.tracing.TracerFactoryBean
import com.labijie.infra.telemetry.tracing.TracingManager
import com.labijie.infra.telemetry.tracing.export.KafkaSpanExporter
import com.labijie.infra.telemetry.tracing.export.LoggingSpanExporter
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.sdk.trace.export.SpanExporter
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.core.env.Environment
import java.util.stream.Collectors

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TelemetryProperties::class)
class TelemetryAutoConfiguration {

    companion object {
        const val MetricEnabledConfigurationKey = "infra.telemetry.tracing.metric.enabled"
        const val TracingEnabledConfigurationKey = "infra.telemetry.tracing.enabled"
        const val TracingExporterProviderConfigurationKey = "infra.telemetry.tracing.exporter.provider"
    }

    @Bean
    fun telemetryBootstrapRunner(): TelemetryBootstrapRunner {
        return TelemetryBootstrapRunner()
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = [TracingEnabledConfigurationKey], havingValue = "true",matchIfMissing = true)
    protected class TracingAutoConfiguration {

        @Bean
        @ConditionalOnProperty(name = [TracingExporterProviderConfigurationKey], havingValue = "logging", matchIfMissing = false)
        fun loggingSpanExporter(): LoggingSpanExporter = LoggingSpanExporter()

        @Bean
        @ConditionalOnClass(name = ["org.apache.kafka.clients.producer.KafkaProducer"])
        @ConditionalOnProperty(name = [TracingExporterProviderConfigurationKey],havingValue = "kafka",matchIfMissing = false)
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

            val propagator = textMapPropagator ?:W3CTraceContextPropagator.getInstance()

            val exporterList = exporters.orderedStream().collect(Collectors.toList())
            val applicationName: String? = environment.getProperty("spring.application.name")
            TracingManager.instance = TracingManager(
                    applicationName,
                    idGenerator,
                    telemetryProperties.tracing,
                    exporterList,
                    propagator
            )
            return TracingManager.instance
        }

        @Bean
        @Lazy
        fun tracerFactoryBean(tracingManager: TracingManager): TracerFactoryBean {
            return TracerFactoryBean(tracingManager)
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
            name = [MetricEnabledConfigurationKey],
            havingValue = "true",
            matchIfMissing = true
    )
    protected class MetricAutoConfiguration {

    }

}