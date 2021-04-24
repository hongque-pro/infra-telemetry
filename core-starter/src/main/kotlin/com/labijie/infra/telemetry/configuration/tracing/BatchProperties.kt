package com.labijie.infra.telemetry.configuration.tracing

import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBuilder
import java.time.Duration

/**
 *
 * @Auther: AndersXiao
 * @Date: 2021-04-24 13:24
 * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/trace/sdk.md
 */
data class BatchProperties(
        var maxQueueSize: Int = 2048,
        var scheduledDelay: Duration = Duration.ofSeconds(5),
        var exportTimeout: Duration = Duration.ofSeconds(10),
        var maxExportBatchSize: Int = 512
)

fun BatchSpanProcessorBuilder.configure(properties: BatchProperties): BatchSpanProcessorBuilder {
    return this.apply {
        this.setExporterTimeout(properties.exportTimeout)
        this.setMaxQueueSize(properties.maxQueueSize)
        this.setScheduleDelay(properties.scheduledDelay)
        this.setMaxExportBatchSize(properties.maxExportBatchSize)
    }
}