package com.labijie.infra.telemetry.proto

import io.opentelemetry.proto.trace.v1.ResourceSpans
import io.opentelemetry.sdk.trace.data.SpanData
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

object ReflectSpanAdapter {
    const val ADAPTER_CLASS_NAME = "io.opentelemetry.exporter.otlp.SpanAdapter"
    const val METHOD_NAME = "toProtoResourceSpans"
    const val FULL_METHOD_NAME = "$ADAPTER_CLASS_NAME.$METHOD_NAME"

    private val method: Method

    init {
        val adapterClass = ClassLoader.getSystemClassLoader().loadClass(ADAPTER_CLASS_NAME)
        val m = adapterClass.getDeclaredMethod(METHOD_NAME, Collection::class.java)

        assert( Modifier.isStatic(m.modifiers)) { "$FULL_METHOD_NAME is not static method" }

        assert(m.genericParameterTypes.size == 1) {"$FULL_METHOD_NAME invalid method parameter count ."}

        val parameter = m.genericParameterTypes.first()
        assert(parameter is ParameterizedType) {"$FULL_METHOD_NAME parameter is not ParameterizedType."}
        val pt = m.parameters.first().parameterizedType as ParameterizedType
        val genericTypes = pt.actualTypeArguments
        assert(genericTypes.size == 1) {"$FULL_METHOD_NAME parameter is not Collection<*> type."}
        val elementType = genericTypes.first()
        assert(elementType.typeName == SpanData::class.java.typeName) {"$FULL_METHOD_NAME parameter is not Collection<SpanData> type."}

        val returnType = m.genericReturnType as? ParameterizedType
        assert(returnType != null) {"$FULL_METHOD_NAME return type is not generic type."}
        val listItemTypes = returnType!!.actualTypeArguments
        assert(listItemTypes.size == 1) {"$FULL_METHOD_NAME parameter is not List<*> type."}

        val itemType = listItemTypes.first()
        assert(itemType.typeName == ResourceSpans::class.java.typeName) {"$FULL_METHOD_NAME return type is not List<ResourceSpans> type."}
        m.isAccessible = true;
        method = m
    }


    fun toProtoResourceSpans(spanData: Collection<SpanData>): List<ResourceSpans>{
        val returnData = method.invoke(null, spanData)
        @Suppress("UNCHECKED_CAST")
        return returnData as List<ResourceSpans>
    }
}