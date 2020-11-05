package com.labijie.infra.telemetry.configuration.tracing

import com.labijie.infra.telemetry.tracing.ExportStrategy
import java.util.*

data class TracingProperties(
    var exportStrategy: ExportStrategy = ExportStrategy.Simple,
    var enabled: Boolean = false,
    var builtInExporter: BuiltInExporterType = BuiltInExporterType.Kafka,
    var processorProperties: Properties = Properties()
)