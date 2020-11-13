package com.labijie.infra.telemetry.configuration.tracing

import com.labijie.infra.telemetry.tracing.ExportStrategy
import java.util.*

data class TracingProperties(
    var exportStrategy: ExportStrategy = ExportStrategy.Simple,
    var enabled: Boolean = false,
    var processorProperties: MutableMap<String, String> = mutableMapOf(),
    var exporter: String = EXPORTER_KAFKA,
    var exporterProperties: MutableMap<String, String> = mutableMapOf()
){
    companion object{
        const val EXPORTER_KAFKA = "kafka"
        const val EXPORTER_LOGGING = "logging"
    }
}