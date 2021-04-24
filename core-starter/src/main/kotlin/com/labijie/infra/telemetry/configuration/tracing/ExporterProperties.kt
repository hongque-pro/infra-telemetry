package com.labijie.infra.telemetry.configuration.tracing

import com.labijie.infra.telemetry.tracing.ExportStrategy

/**
 *
 * @Auther: AndersXiao
 * @Date: 2021-04-24 13:41
 * @Description:
 */

data class ExporterProperties(
        var strategy: ExportStrategy = ExportStrategy.Simple,
        var provider: String = EXPORTER_KAFKA,
        var properties: MutableMap<String, String> = mutableMapOf(),
        val batch: BatchProperties = BatchProperties(),
)

const val EXPORTER_KAFKA = "kafka"
const val EXPORTER_LOGGING = "logging"