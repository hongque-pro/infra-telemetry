package com.labijie.infra.telemetry.proto

import com.labijie.infra.telemetry.proto.CommonAdapter.toProtoAttribute
import io.opentelemetry.common.AttributeConsumer
import io.opentelemetry.common.AttributeKey
import io.opentelemetry.proto.resource.v1.Resource


internal object ResourceAdapter {
    fun toProtoResource(resource: io.opentelemetry.sdk.resources.Resource): Resource {
        val builder: Resource.Builder = Resource.newBuilder()
        resource
            .attributes
            .forEach(
                object : AttributeConsumer {
                    override fun <T : Any?> consume(key: AttributeKey<T>, value: T) {
                        builder.addAttributes(toProtoAttribute(key, value))
                    }
                })
        return builder.build()
    }
}