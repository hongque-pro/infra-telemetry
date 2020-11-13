package com.labijie.infra.telemetry.configuration.metric

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MiniNettyServerConfiguration {
    @Bean
    fun nettyReactiveWebServerFactory(): NettyReactiveWebServerFactory {
        System.setProperty("reactor.netty.ioWorkerCount", "1");
        System.setProperty("reactor.ipc.netty.workerCount", "1")
        return NettyReactiveWebServerFactory()
    }
}