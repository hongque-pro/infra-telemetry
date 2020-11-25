package com.labijie.infra.telemetry.configuration.tracing

import com.labijie.infra.telemetry.tracing.TracingManager
import io.opentelemetry.api.trace.Tracer
import org.springframework.beans.factory.FactoryBean

class TracerFactoryBean(private val tracingManager: TracingManager) : FactoryBean<Tracer> {

    override fun getObject(): Tracer = tracingManager.tracer

    override fun getObjectType(): Class<*>  = Tracer::class.java
}