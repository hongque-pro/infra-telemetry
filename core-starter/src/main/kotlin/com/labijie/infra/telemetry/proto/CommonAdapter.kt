package com.labijie.infra.telemetry.proto

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.AttributeType
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.ArrayValue
import io.opentelemetry.proto.common.v1.InstrumentationLibrary
import io.opentelemetry.proto.common.v1.KeyValue
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo

/**
 *
 * @Auther: AndersXiao
 * @Date: 2021-04-24 12:41
 * @Description:
 */

internal object CommonAdapter {
    @Suppress("UNCHECKED_CAST")
    fun toProtoAttribute(key: AttributeKey<*>, value: Any): KeyValue {
        return when (key.type) {
            AttributeType.STRING -> makeStringKeyValue(key, value as String)
            AttributeType.BOOLEAN -> makeBooleanKeyValue(key, value as Boolean)
            AttributeType.LONG -> makeLongKeyValue(key, value as Long)
            AttributeType.DOUBLE -> makeDoubleKeyValue(key, value as Double)
            AttributeType.BOOLEAN_ARRAY -> makeBooleanArrayKeyValue(key, value as List<Boolean>)
            AttributeType.LONG_ARRAY -> makeLongArrayKeyValue(key, value as List<Long>)
            AttributeType.DOUBLE_ARRAY -> makeDoubleArrayKeyValue(key, value as List<Double>)
            AttributeType.STRING_ARRAY -> makeStringArrayKeyValue(key, value as List<String>)
            else -> {
                val builder = KeyValue.newBuilder().setKey(key.key)
                builder.value = AnyValue.getDefaultInstance()
                builder.build()
            }
        }

    }

    private fun makeLongArrayKeyValue(key: AttributeKey<*>, value: List<Long>): KeyValue {
        val keyValueBuilder = KeyValue.newBuilder()
                .setKey(key.key)
                .setValue(AnyValue.newBuilder().setArrayValue(makeLongArrayAnyValue(value)).build())
        return keyValueBuilder.build()
    }

    private fun makeDoubleArrayKeyValue(key: AttributeKey<*>, value: List<Double>): KeyValue {
        val keyValueBuilder = KeyValue.newBuilder()
                .setKey(key.key)
                .setValue(AnyValue.newBuilder().setArrayValue(makeDoubleArrayAnyValue(value)).build())
        return keyValueBuilder.build()
    }

    private fun makeBooleanArrayKeyValue(key: AttributeKey<*>, value: List<Boolean>): KeyValue {
        val keyValueBuilder = KeyValue.newBuilder()
                .setKey(key.key)
                .setValue(AnyValue.newBuilder().setArrayValue(makeBooleanArrayAnyValue(value)).build())
        return keyValueBuilder.build()
    }

    private fun makeStringArrayKeyValue(key: AttributeKey<*>, value: List<String>): KeyValue {
        val keyValueBuilder = KeyValue.newBuilder()
                .setKey(key.key)
                .setValue(AnyValue.newBuilder().setArrayValue(makeStringArrayAnyValue(value)).build())
        return keyValueBuilder.build()
    }

    private fun makeLongKeyValue(key: AttributeKey<*>, value: Long): KeyValue {
        val keyValueBuilder = KeyValue.newBuilder()
                .setKey(key.key)
                .setValue(AnyValue.newBuilder().setIntValue(value).build())
        return keyValueBuilder.build()
    }

    private fun makeDoubleKeyValue(key: AttributeKey<*>, value: Double): KeyValue {
        val keyValueBuilder = KeyValue.newBuilder()
                .setKey(key.key)
                .setValue(AnyValue.newBuilder().setDoubleValue(value).build())
        return keyValueBuilder.build()
    }

    private fun makeBooleanKeyValue(key: AttributeKey<*>, value: Boolean): KeyValue {
        val keyValueBuilder = KeyValue.newBuilder()
                .setKey(key.key)
                .setValue(AnyValue.newBuilder().setBoolValue(value).build())
        return keyValueBuilder.build()
    }

    private fun makeStringKeyValue(key: AttributeKey<*>, value: String): KeyValue {
        val keyValueBuilder = KeyValue.newBuilder()
                .setKey(key.key)
                .setValue(AnyValue.newBuilder().setStringValue(value).build())
        return keyValueBuilder.build()
    }

    private fun makeDoubleArrayAnyValue(doubleArrayValue: List<Double>): ArrayValue {
        val builder = ArrayValue.newBuilder()
        for (doubleValue in doubleArrayValue) {
            builder.addValues(AnyValue.newBuilder().setDoubleValue(doubleValue).build())
        }
        return builder.build()
    }

    private fun makeLongArrayAnyValue(longArrayValue: List<Long>): ArrayValue {
        val builder = ArrayValue.newBuilder()
        for (intValue in longArrayValue) {
            builder.addValues(AnyValue.newBuilder().setIntValue(intValue).build())
        }
        return builder.build()
    }

    private fun makeStringArrayAnyValue(stringArrayValue: List<String>): ArrayValue {
        val builder = ArrayValue.newBuilder()
        for (string in stringArrayValue) {
            builder.addValues(AnyValue.newBuilder().setStringValue(string).build())
        }
        return builder.build()
    }

    private fun makeBooleanArrayAnyValue(booleanArrayValue: List<Boolean>): ArrayValue {
        val builder = ArrayValue.newBuilder()
        for (bool in booleanArrayValue) {
            builder.addValues(AnyValue.newBuilder().setBoolValue(bool).build())
        }
        return builder.build()
    }

    fun toProtoInstrumentationLibrary(
            instrumentationLibraryInfo: InstrumentationLibraryInfo): InstrumentationLibrary {
        return InstrumentationLibrary.newBuilder()
                .setName(instrumentationLibraryInfo.name)
                .setVersion(
                        if (instrumentationLibraryInfo.version == null) "" else instrumentationLibraryInfo.version)
                .build()
    }
}