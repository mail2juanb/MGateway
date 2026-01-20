package com.microdiab.mgateway.configuration;

import feign.RequestInterceptor;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class FeignConfig {

    @Autowired
    private Tracer tracer;

    /**
     * Propagation of B3 headers (TraceId + SpanId + Sampled) for Feign calls
     * made by mgateway to internal microservices.
     */
    @Bean
    public RequestInterceptor b3HeadersRequestInterceptor() {
        return requestTemplate -> {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                requestTemplate.header("X-B3-TraceId", currentSpan.context().traceId());
                requestTemplate.header("X-B3-SpanId", currentSpan.context().spanId());
                requestTemplate.header("X-B3-ParentSpanId", currentSpan.context().spanId());
                requestTemplate.header("X-B3-Sampled", "1");
            }
        };
    }
}
