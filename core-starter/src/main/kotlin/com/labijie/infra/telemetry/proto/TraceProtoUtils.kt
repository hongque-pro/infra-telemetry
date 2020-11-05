package com.labijie.infra.telemetry.proto

import com.google.protobuf.ByteString
import io.opentelemetry.proto.trace.v1.ConstantSampler.ConstantDecision
import io.opentelemetry.sdk.trace.Sampler
import io.opentelemetry.sdk.trace.Samplers
import io.opentelemetry.sdk.trace.config.TraceConfig
import io.opentelemetry.trace.SpanId
import io.opentelemetry.trace.TraceId


object TraceProtoUtils {
    /**
     * Converts a SpanId into a protobuf ByteString.
     *
     * @param spanId the spanId to convert.
     * @return a ByteString representation.
     */
    fun toProtoSpanId(spanId: String?): ByteString {
        return ByteString.copyFrom(SpanId.bytesFromHex(spanId, 0))
    }

    /**
     * Converts a TraceId into a protobuf ByteString.
     *
     * @param traceId the traceId to convert.
     * @return a ByteString representation.
     */
    fun toProtoTraceId(traceId: String?): ByteString {
        return ByteString.copyFrom(TraceId.bytesFromHex(traceId, 0))
    }

    /**
     * Returns a `TraceConfig` from the given proto.
     *
     * @param traceConfigProto proto format `TraceConfig`.
     * @return a `TraceConfig`.
     */
    fun traceConfigFromProto(
        traceConfigProto:  io.opentelemetry.proto.trace.v1.TraceConfig
    ): TraceConfig {
        return TraceConfig.getDefault().toBuilder()
            .setSampler(fromProtoSampler(traceConfigProto))
            .setMaxNumberOfAttributes(traceConfigProto.maxNumberOfAttributes.toInt())
            .setMaxNumberOfEvents(traceConfigProto.maxNumberOfTimedEvents.toInt())
            .setMaxNumberOfLinks(traceConfigProto.maxNumberOfLinks.toInt())
            .setMaxNumberOfAttributesPerEvent(
                traceConfigProto.maxNumberOfAttributesPerTimedEvent.toInt()
            )
            .setMaxNumberOfAttributesPerLink(traceConfigProto.maxNumberOfAttributesPerLink.toInt())
            .build()
    }

    private fun fromProtoSampler(
        traceConfigProto:  io.opentelemetry.proto.trace.v1.TraceConfig
    ): Sampler {
        if (traceConfigProto.hasConstantSampler()) {
            val constantSampler = traceConfigProto.constantSampler
            return when (constantSampler.decision ?: ConstantDecision.ALWAYS_ON) {
                ConstantDecision.ALWAYS_ON -> Samplers.alwaysOn()
                ConstantDecision.ALWAYS_OFF -> Samplers.alwaysOff()
                ConstantDecision.ALWAYS_PARENT, ConstantDecision.UNRECOGNIZED -> throw IllegalArgumentException("unrecognized constant sampling samplingResult")
            }
        }
        if (traceConfigProto.hasTraceIdRatioBased()) {
            return Samplers.traceIdRatioBased(traceConfigProto.traceIdRatioBased.samplingRatio)
        }
        if (traceConfigProto.hasRateLimitingSampler()) {
            // TODO: add support for RateLimiting Sampler
        }
        throw IllegalArgumentException("unknown sampler in the trace config proto")
    }
}