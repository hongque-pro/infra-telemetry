package com.labijie.infra.telemetry.configuration.metric

import com.labijie.infra.telemetry.configuration.TelemetryAutoConfiguration.Companion.MetricEnabledConfigurationKey
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
class WebSecurityAutoConfiguration {
    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnClass(ServerHttpSecurity::class, SecurityWebFilterChain::class)
    protected  class WebFluxAutoConfiguration {

        @Order(-1)
        @ConditionalOnProperty(name = [MetricEnabledConfigurationKey], havingValue = "true", matchIfMissing = true)
        @Bean
        fun actuatorSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
            val matcher = EndpointRequest.toAnyEndpoint()
            http.securityMatcher(matcher).authorizeExchange().anyExchange().permitAll()
            return http.build()
        }
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(WebSecurityConfiguration::class)
    protected class ServletAutoConfiguration {

        @Order(Ordered.HIGHEST_PRECEDENCE)
        @ConditionalOnBean(WebSecurityConfiguration::class)
        @ConditionalOnProperty(value = [MetricEnabledConfigurationKey], matchIfMissing = true)
        @Bean
        fun actuatorServletSecurity(httpSecurity: HttpSecurity): SecurityFilterChain? {
            val matcher = org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest.toAnyEndpoint()
            httpSecurity.requestMatcher(matcher).authorizeRequests()
                .anyRequest().permitAll()
            return httpSecurity.build()
        }
    }
}