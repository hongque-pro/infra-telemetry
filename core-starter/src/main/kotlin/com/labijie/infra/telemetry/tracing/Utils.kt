package com.labijie.infra.telemetry.tracing

import io.opentelemetry.exporter.otlp.internal.traces.TraceRequestMarshaler
import java.io.ByteArrayOutputStream

/**
 *
 * @Author: Anders Xiao
 * @Date: 2021/12/8
 * @Description:
 */
object Utils {
    fun TraceRequestMarshaler.toByteArray(): ByteArray {
        ByteArrayOutputStream().use {
            this.writeBinaryTo(it)
            it.flush()
            return it.toByteArray()
        }
    }

    fun TraceRequestMarshaler.toJson(): String {
        ByteArrayOutputStream().use {
            this.writeJsonTo(it)
            it.flush()
            return it.toByteArray().toString(Charsets.UTF_8)
        }
    }
}