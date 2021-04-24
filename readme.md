# 分布式遥测 (trace/metric) 基础包

![maven central version](https://img.shields.io/maven-central/v/com.labijie.infra/telemetry-core-starter?style=flat-square)
![workflow status](https://img.shields.io/github/workflow/status/hongque-pro/infra-telemetry/Gradle%20Build%20And%20Release?label=CI%20publish&style=flat-square)
![license](https://img.shields.io/github/license/hongque-pro/infra-telemetry?style=flat-square)

## 引入依赖（Gradle）

```groovy
    compile "com.labijie.infra:telemetry-core-starter:$infra_telemetry_version"
```
## 基本功能

- 自动开启 spring actuator 上的 prometheus 导出 endpoint

- 使用 OpenTelemetry 作为追踪系统，通过注入 **Tracer** 类直接调用

- 使用通过将 SpanExporter 注册为 bean 来添加自定义导出器

- 默认内置 kafka 导出器（通过 **infra.telemetry.tracing.exporter.provider** 设置为 **none** 可以关闭）

## 默认配置参考（如果不添加任何配置以下配置将生效）

```yaml
infra:
  telemetry:
    metric:
      enabled: true
    tracing:
      enabled: true
      exporter:
        provider: kafka
        properties:
          bootstrap.servers: '127.0.0.1:9092'
          topic: telemetry-spans
        batch:
          export-timeout: 10s
          max-export-batch-Size: 512
          max-queue-size: 1024
          scheduled-delay: 5s
```

## 非 Web 应用

通过应用 EnableMiniWebServerForPrometheus 注解开启 actuator endpoint，需要引入 webflux 包

## 遥测数据采集

metric: 使用 prometheus 采集(参考：[Prometheus 文档](https://prometheus.io/docs/introduction/overview/))

trace: 使用 opentelemetry-collector 采集（参考：[OpenTelemetry 文档](https://opentelemetry.io/docs/collector/configuration/)）
> - 内置的 kafka 导出器需要配合 opentelemetry-collector-contrib 中的 kafka 采集器
> - 推荐 collector 使用 elastic-exporter 直接导入 elastic amp 以减少中间件依赖

## OpenTelemetry 使用帮助

参考[OpenTelemetry文档](https://github.com/open-telemetry/opentelemetry-java/blob/master/QUICKSTART.md)


