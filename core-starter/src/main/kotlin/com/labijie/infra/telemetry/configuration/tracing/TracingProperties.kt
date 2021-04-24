package com.labijie.infra.telemetry.configuration.tracing

import com.labijie.infra.telemetry.tracing.ExportStrategy

data class TracingProperties(
        var enabled: Boolean = true,
        val exporter: ExporterProperties = ExporterProperties()
)