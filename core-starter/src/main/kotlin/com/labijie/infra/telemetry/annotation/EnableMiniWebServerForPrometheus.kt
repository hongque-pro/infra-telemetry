package com.labijie.infra.telemetry.annotation

import com.labijie.infra.telemetry.configuration.metric.MiniNettyServerConfiguration
import org.springframework.context.annotation.Import

@Import(MiniNettyServerConfiguration::class)
class EnableMiniWebServerForPrometheus {
}