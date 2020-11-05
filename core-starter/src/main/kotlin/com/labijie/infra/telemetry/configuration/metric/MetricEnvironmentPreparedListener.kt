package com.labijie.infra.telemetry.configuration.metric

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment

class MetricEnvironmentPreparedListener : ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    companion object {
        const val MANAGEMENT_ENDPOINT_CONFIG_KEY = "management.endpoints.web.exposure.include"
        const val HEALTH_DETAIL_CONFIG_KEY = "management.endpoint.health.show-details"
    }

    override fun onApplicationEvent(p0: ApplicationEnvironmentPreparedEvent) {
        val env = p0.environment
        env.merge(hardCodeEnvironment())
    }

    private fun hardCodeEnvironment(): ConfigurableEnvironment {
        val hardCodeEnvironment = StandardEnvironment()
        val propertySources = hardCodeEnvironment.propertySources
        val configMap = mutableMapOf<String, Any>()
        configMap[MANAGEMENT_ENDPOINT_CONFIG_KEY] = "health,info,prometheus"
        configMap[HEALTH_DETAIL_CONFIG_KEY] = "ALWAYS"
        propertySources.addFirst(MapPropertySource("infra-telemetry-metric-config", configMap))
        return hardCodeEnvironment
    }
}