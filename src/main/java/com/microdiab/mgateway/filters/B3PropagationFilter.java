package com.microdiab.mgateway.filters;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * A {@link GlobalFilter} implementation for propagating B3 tracing headers
 * in a Spring Cloud Gateway environment.
 *
 * <p>This filter ensures that the B3 tracing headers (TraceId, SpanId, and Sampled)
 * are added to the outgoing HTTP requests. It is designed to work with distributed tracing
 * systems like Zipkin or other OpenTelemetry-compatible tracing tools.</p>
 *
 * <p>The filter is executed early in the filter chain (order = -2) to ensure
 * that tracing headers are propagated before other filters process the request.</p>
 */
@Component
@Order(-2) // Executé avant le CustomGlobalFilter (-1)
public class B3PropagationFilter implements GlobalFilter {

    private final Tracer tracer;

    /**
     * Constructs a new {@code B3PropagationFilter} with the provided {@link Tracer}.
     *
     * @param tracer The {@link Tracer} used to access the current span and its context.
     */
    public B3PropagationFilter(Tracer tracer) {
        this.tracer = tracer;
    }


    /**
     * Filters the incoming request and adds B3 tracing headers to the mutated request.
     *
     * <p>If a current span is available, the following B3 headers are added to the request:</p>
     * <ul>
     *   <li>{@code X-B3-TraceId}: The trace ID from the current span context.</li>
     *   <li>{@code X-B3-SpanId}: The span ID from the current span context.</li>
     *   <li>{@code X-B3-Sampled}: Set to "1" to indicate that the request should be sampled.</li>
     * </ul>
     *
     * <p>If no current span is available, the request is passed through the filter chain unchanged.</p>
     *
     * @param exchange The current {@link ServerWebExchange}.
     * @param chain The {@link GatewayFilterChain} to continue processing the request.
     * @return A {@link Mono<Void>} indicating the completion of the filter processing.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Span currentSpan = tracer.currentSpan();

        if (currentSpan == null) {
            return chain.filter(exchange);
        }

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("X-B3-TraceId", currentSpan.context().traceId())
                        .header("X-B3-SpanId", currentSpan.context().spanId())
                        .header("X-B3-Sampled", "1")
                        .build())
                .build();

        return chain.filter(mutatedExchange);
    }
}
