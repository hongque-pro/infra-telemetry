package com.labijie.infra.telemetry.proto

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.sdk.resources.Resource

/**
 *
 * @Auther: AndersXiao
 * @Date: 2021-04-24 12:49
 * @Description:
 */

internal object ResourceAdapter {
    fun toProtoResource(resource: Resource): io.opentelemetry.proto.resource.v1.Resource {
        val builder = io.opentelemetry.proto.resource.v1.Resource.newBuilder()
        resource
                .attributes
                .forEach { key: AttributeKey<*>?, value: Any? -> builder.addAttributes(CommonAdapter.toProtoAttribute(key!!, value!!)) }
        return builder.build()
    }
}
