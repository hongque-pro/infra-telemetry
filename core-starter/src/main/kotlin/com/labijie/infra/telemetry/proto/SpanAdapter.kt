package com.labijie.infra.telemetry.proto

import com.labijie.infra.telemetry.proto.CommonAdapter.toProtoAttribute
import com.labijie.infra.telemetry.proto.CommonAdapter.toProtoInstrumentationLibrary
import io.opentelemetry.common.AttributeConsumer
import io.opentelemetry.common.AttributeKey
import io.opentelemetry.common.Attributes
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans
import io.opentelemetry.proto.trace.v1.ResourceSpans
import io.opentelemetry.proto.trace.v1.Span
import io.opentelemetry.proto.trace.v1.Span.SpanKind
import io.opentelemetry.proto.trace.v1.Status
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.data.SpanData.Event
import io.opentelemetry.trace.Span.Kind
import io.opentelemetry.trace.SpanId
import io.opentelemetry.trace.StatusCanonicalCode
import java.util.*

//参考:
//https://github.com/open-telemetry/opentelemetry-java/blob/master/exporters/otlp/src/main/java/io/opentelemetry/exporter/otlp/SpanAdapter.java


internal object SpanAdapter {
    fun toProtoResourceSpans(spanDataList: Collection<SpanData>): List<ResourceSpans> {
        val resourceAndLibraryMap: Map<Resource, MutableMap<InstrumentationLibraryInfo, MutableList<Span>?>> =
            groupByResourceAndLibrary(spanDataList)
        val resourceSpans: MutableList<ResourceSpans> = ArrayList(resourceAndLibraryMap.size)
        for ((key, value) in resourceAndLibraryMap) {
            val instrumentationLibrarySpans: MutableList<InstrumentationLibrarySpans> =
                ArrayList<InstrumentationLibrarySpans>(
                    value.size
                )
            for ((key1, value1) in value.entries) {
                instrumentationLibrarySpans.add(
                    InstrumentationLibrarySpans.newBuilder()
                        .setInstrumentationLibrary(
                            toProtoInstrumentationLibrary(key1)
                        )
                        .addAllSpans(value1)
                        .build()
                )
            }
            resourceSpans.add(
                ResourceSpans.newBuilder()
                    .setResource(ResourceAdapter.toProtoResource(key))
                    .addAllInstrumentationLibrarySpans(instrumentationLibrarySpans)
                    .build()
            )
        }
        return resourceSpans
    }

    private fun groupByResourceAndLibrary(spanDataList: Collection<SpanData>): Map<Resource, MutableMap<InstrumentationLibraryInfo, MutableList<Span>?>> {
        val result: MutableMap<Resource, MutableMap<InstrumentationLibraryInfo, MutableList<Span>?>> =
            HashMap<Resource, MutableMap<InstrumentationLibraryInfo, MutableList<Span>?>>()
        for (spanData in spanDataList) {
            val resource: Resource = spanData.resource
            var libraryInfoListMap: MutableMap<InstrumentationLibraryInfo, MutableList<Span>?>? =
                result[spanData.resource]
            if (libraryInfoListMap == null) {
                libraryInfoListMap = HashMap<InstrumentationLibraryInfo, MutableList<Span>?>()
                result[resource] = libraryInfoListMap
            }
            var spanList: MutableList<Span>? = libraryInfoListMap[spanData.instrumentationLibraryInfo]
            if (spanList == null) {
                spanList = ArrayList<Span>()
                libraryInfoListMap[spanData.instrumentationLibraryInfo] = spanList
            }
            spanList.add(toProtoSpan(spanData))
        }
        return result
    }

    fun toProtoSpan(spanData: SpanData): Span {
        val builder: Span.Builder = Span.newBuilder()
        builder.setTraceId(TraceProtoUtils.toProtoTraceId(spanData.traceId))
        builder.setSpanId(TraceProtoUtils.toProtoSpanId(spanData.spanId))
        // TODO: Set TraceState;
        if (SpanId.isValid(spanData.parentSpanId)) {
            builder.parentSpanId = TraceProtoUtils.toProtoSpanId(spanData.parentSpanId)
        }
        builder.name = spanData.name
        builder.kind = toProtoSpanKind(spanData.kind)
        builder.startTimeUnixNano = spanData.startEpochNanos
        builder.endTimeUnixNano = spanData.endEpochNanos
        spanData
            .attributes
            .forEach(
                object : AttributeConsumer {
                    override fun <T : Any?> consume(key: AttributeKey<T>, value: T) {
                        builder.addAttributes(toProtoAttribute(key, value))
                    }
                })
        builder.droppedAttributesCount = spanData.totalAttributeCount - spanData.attributes.size()
        for (event in spanData.events) {
            builder.addEvents(toProtoSpanEvent(event))
        }
        builder.droppedEventsCount = spanData.totalRecordedEvents - spanData.events.size
        for (link in spanData.links) {
            builder.addLinks(toProtoSpanLink(link))
        }
        builder.droppedLinksCount = spanData.totalRecordedLinks - spanData.links.size
        builder.status = toStatusProto(spanData.status)
        return builder.build()
    }

    private fun toProtoSpanKind(kind: Kind?): SpanKind {
        when (kind) {
            Kind.INTERNAL -> SpanKind.SPAN_KIND_INTERNAL
            Kind.SERVER -> SpanKind.SPAN_KIND_SERVER
            Kind.CLIENT -> SpanKind.SPAN_KIND_CLIENT
            Kind.PRODUCER -> SpanKind.SPAN_KIND_PRODUCER
            Kind.CONSUMER -> SpanKind.SPAN_KIND_CONSUMER
            else -> SpanKind.UNRECOGNIZED
        }
        return SpanKind.UNRECOGNIZED
    }

    private fun toProtoSpanEvent(event: Event): Span.Event {
        val builder: Span.Event.Builder = Span.Event.newBuilder()
        builder.name = event.name
        builder.timeUnixNano = event.epochNanos
        event
            .attributes
            .forEach(
                object : AttributeConsumer {
                    override fun <T : Any?> consume(key: AttributeKey<T>, value: T) {
                        builder.addAttributes(toProtoAttribute(key, value))
                    }
                })
        builder.setDroppedAttributesCount(
            event.totalAttributeCount - event.attributes.size()
        )
        return builder.build()
    }

    private fun toProtoSpanLink(link: SpanData.Link): Span.Link {
        val builder: Span.Link.Builder = Span.Link.newBuilder()
        builder.traceId = TraceProtoUtils.toProtoTraceId(link.context.traceIdAsHexString)
        builder.spanId = TraceProtoUtils.toProtoSpanId(link.context.spanIdAsHexString)
        // TODO: Set TraceState;
        val attributes: Attributes = link.attributes
        attributes.forEach(
            object : AttributeConsumer {
                override fun <T : Any?> consume(key: AttributeKey<T>, value: T) {
                    builder.addAttributes(toProtoAttribute(key, value))
                }
            })
        builder.droppedAttributesCount = link.totalAttributeCount - attributes.size()
        return builder.build()
    }

    private fun toStatusProto(status: SpanData.Status): Status {
        val protoStatusCode = when (status.canonicalCode) {
            StatusCanonicalCode.OK -> Status.StatusCode.STATUS_CODE_OK
            StatusCanonicalCode.ERROR -> Status.StatusCode.STATUS_CODE_INTERNAL_ERROR
            StatusCanonicalCode.UNSET -> Status.StatusCode.STATUS_CODE_UNAVAILABLE
            else -> Status.StatusCode.STATUS_CODE_UNAVAILABLE
        }
        val builder:  // setDeprecatedCode is deprecated.
                Status.Builder = Status.newBuilder().setCode(protoStatusCode)
        if (status.description != null) {
            builder.message = status.description
        }
        return builder.build()
    }
}