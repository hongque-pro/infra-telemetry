import com.labijie.infra.telemetry.proto.CommonAdapter
import com.labijie.infra.telemetry.proto.ResourceAdapter
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.data.EventData
import io.opentelemetry.sdk.trace.data.LinkData
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.data.StatusData
import java.util.*
import kotlin.collections.Collection
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.set
import io.opentelemetry.proto.trace.v1.ResourceSpans
import io.opentelemetry.proto.trace.v1.Status

/** Converter from SDK [SpanData] to OTLP [ResourceSpans].  */
object SpanAdapter {
    /** Converts the provided [SpanData] to [ResourceSpans].  */
    fun toProtoResourceSpans(spanDataList: Collection<SpanData>): List<ResourceSpans> {
        val resourceAndLibraryMap: Map<Resource, MutableMap<InstrumentationLibraryInfo, MutableList<io.opentelemetry.proto.trace.v1.Span>?>> = groupByResourceAndLibrary(spanDataList)
        val resourceSpans: MutableList<ResourceSpans> = ArrayList<ResourceSpans>(resourceAndLibraryMap.size)
        for ((key, value) in resourceAndLibraryMap) {
            val instrumentationLibrarySpans: MutableList<InstrumentationLibrarySpans> = ArrayList<InstrumentationLibrarySpans>(value.size)
            for ((key1, value1) in value) {
                instrumentationLibrarySpans.add(
                        InstrumentationLibrarySpans.newBuilder()
                                .setInstrumentationLibrary(
                                        CommonAdapter.toProtoInstrumentationLibrary(key1))
                                .addAllSpans(value1)
                                .build())
            }
            resourceSpans.add(
                    ResourceSpans.newBuilder()
                            .setResource(ResourceAdapter.toProtoResource(key))
                            .addAllInstrumentationLibrarySpans(instrumentationLibrarySpans)
                            .build())
        }
        return resourceSpans
    }

    private fun groupByResourceAndLibrary(spanDataList: Collection<SpanData>): Map<Resource, MutableMap<InstrumentationLibraryInfo, MutableList<io.opentelemetry.proto.trace.v1.Span>?>> {
        val result: MutableMap<Resource, MutableMap<InstrumentationLibraryInfo, MutableList<io.opentelemetry.proto.trace.v1.Span>?>> = HashMap<Resource, MutableMap<InstrumentationLibraryInfo, MutableList<io.opentelemetry.proto.trace.v1.Span>?>>()
        for (spanData in spanDataList) {
            val resource = spanData.resource
            var libraryInfoListMap: MutableMap<InstrumentationLibraryInfo, MutableList<io.opentelemetry.proto.trace.v1.Span>?>? = result[spanData.resource]
            if (libraryInfoListMap == null) {
                libraryInfoListMap = HashMap()
                result[resource] = libraryInfoListMap
            }
            var spanList: MutableList<io.opentelemetry.proto.trace.v1.Span>? = libraryInfoListMap[spanData.instrumentationLibraryInfo]
            if (spanList == null) {
                spanList = mutableListOf()
                libraryInfoListMap[spanData.instrumentationLibraryInfo] = spanList
            }
            spanList.add(toProtoSpan(spanData))
        }
        return result
    }

    fun toProtoSpan(spanData: SpanData): io.opentelemetry.proto.trace.v1.Span {
        val builder: io.opentelemetry.proto.trace.v1.Span.Builder = io.opentelemetry.proto.trace.v1.Span.newBuilder()
        builder.setTraceId(com.google.protobuf.ByteString.copyFrom(spanData.spanContext.traceIdBytes))
        builder.setSpanId(com.google.protobuf.ByteString.copyFrom(spanData.spanContext.spanIdBytes))
        // TODO: Set TraceState;
        if (spanData.parentSpanContext.isValid) {
            builder.setParentSpanId(
                    com.google.protobuf.ByteString.copyFrom(spanData.parentSpanContext.spanIdBytes))
        }
        builder.setName(spanData.name)
        builder.setKind(toProtoSpanKind(spanData.kind))
        builder.setStartTimeUnixNano(spanData.startEpochNanos)
        builder.setEndTimeUnixNano(spanData.endEpochNanos)
        spanData
                .attributes
                .forEach { key: AttributeKey<*>, value: Any -> builder.addAttributes(CommonAdapter.toProtoAttribute(key, value)) }
        builder.setDroppedAttributesCount(
                spanData.totalAttributeCount - spanData.attributes.size())
        for (event in spanData.events) {
            builder.addEvents(toProtoSpanEvent(event))
        }
        builder.setDroppedEventsCount(spanData.totalRecordedEvents - spanData.events.size)
        for (link in spanData.links) {
            builder.addLinks(toProtoSpanLink(link))
        }
        builder.setDroppedLinksCount(spanData.totalRecordedLinks - spanData.links.size)
        builder.setStatus(toStatusProto(spanData.status))
        return builder.build()
    }

    fun toProtoSpanKind(kind: SpanKind?): io.opentelemetry.proto.trace.v1.Span.SpanKind {
        when (kind) {
            SpanKind.INTERNAL -> return io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_INTERNAL
            SpanKind.SERVER -> return io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_SERVER
            SpanKind.CLIENT -> return io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_CLIENT
            SpanKind.PRODUCER -> return io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_PRODUCER
            SpanKind.CONSUMER -> return io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_CONSUMER
        }
        return io.opentelemetry.proto.trace.v1.Span.SpanKind.UNRECOGNIZED
    }

    fun toProtoSpanEvent(event: EventData): io.opentelemetry.proto.trace.v1.Span.Event {
        val builder: io.opentelemetry.proto.trace.v1.Span.Event.Builder = io.opentelemetry.proto.trace.v1.Span.Event.newBuilder()
        builder.name = event.name
        builder.timeUnixNano = event.epochNanos
        event
                .attributes
                .forEach { key: AttributeKey<*>, value: Any -> builder.addAttributes(CommonAdapter.toProtoAttribute(key, value)) }
        builder.droppedAttributesCount = event.totalAttributeCount - event.attributes.size()
        return builder.build()
    }

    fun toProtoSpanLink(link: LinkData): io.opentelemetry.proto.trace.v1.Span.Link {
        val builder: io.opentelemetry.proto.trace.v1.Span.Link.Builder = io.opentelemetry.proto.trace.v1.Span.Link.newBuilder()
        builder.setTraceId(com.google.protobuf.ByteString.copyFrom(link.spanContext.traceIdBytes))
        builder.setSpanId(com.google.protobuf.ByteString.copyFrom(link.spanContext.spanIdBytes))
        // TODO: Set TraceState;
        val attributes = link.attributes
        attributes.forEach { key: AttributeKey<*>, value: Any -> builder.addAttributes(CommonAdapter.toProtoAttribute(key, value)) }
        builder.droppedAttributesCount = link.totalAttributeCount - attributes.size()
        return builder.build()
    }

    fun toStatusProto(status: StatusData): Status {
        var protoStatusCode: Status.StatusCode = Status.StatusCode.STATUS_CODE_UNSET
        var deprecatedStatusCode: Status.DeprecatedStatusCode = Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_OK
        if (status.statusCode == StatusCode.OK) {
            protoStatusCode = Status.StatusCode.STATUS_CODE_OK
        } else if (status.statusCode == StatusCode.ERROR) {
            protoStatusCode = Status.StatusCode.STATUS_CODE_ERROR
            deprecatedStatusCode = Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_UNKNOWN_ERROR
        }
        // setDeprecatedCode is deprecated.
        @Suppress("DEPRECATION")
        val builder: Status.Builder = Status.newBuilder().setCode(protoStatusCode).setDeprecatedCode(deprecatedStatusCode)
        if (status.description.isNotEmpty()) {
            builder.message = status.description
        }
        return builder.build()
    }
}