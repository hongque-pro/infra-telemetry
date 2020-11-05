package com.labijie.infra.telemetry.configuration

import com.labijie.infra.telemetry.configuration.metric.MetricProperties
import com.labijie.infra.telemetry.configuration.tracing.TracingProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("infra.telemetry")
data class TelemetryProperties(
    var tracing: TracingProperties = TracingProperties(),
    var metric: MetricProperties = MetricProperties()
)