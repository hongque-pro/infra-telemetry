package com.labijie.infra.telemetry.configuration.metric

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

open class ActuatorWebSecurityConfigurerAdapter : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    protected override fun configure(http: HttpSecurity) {
        val matcher = EndpointRequest.toAnyEndpoint()
        http.requestMatcher(matcher).authorizeRequests()
            .anyRequest().permitAll()
    }
}