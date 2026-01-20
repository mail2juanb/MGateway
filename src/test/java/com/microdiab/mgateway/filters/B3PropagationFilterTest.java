package com.microdiab.mgateway.filters;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class B3PropagationFilterTest {

    @Mock
    private Tracer tracer;

    @Mock
    private GatewayFilterChain chain;

    @Mock
    private Span span;

    @Mock
    private TraceContext traceContext;

    private B3PropagationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new B3PropagationFilter(tracer);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    void shouldAddB3HeadersWhenSpanExists() {
        // Given
        String traceId = "1234567890abcdef";
        String spanId = "abcdef1234567890";

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn(traceId);
        when(traceContext.spanId()).thenReturn(spanId);

        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(exchangeCaptor.capture());

        ServerWebExchange capturedExchange = exchangeCaptor.getValue();
        HttpHeaders headers = capturedExchange.getRequest().getHeaders();

        assertThat(headers.getFirst("X-B3-TraceId")).isEqualTo(traceId);
        assertThat(headers.getFirst("X-B3-SpanId")).isEqualTo(spanId);
        assertThat(headers.getFirst("X-B3-Sampled")).isEqualTo("1");
    }

    @Test
    void shouldNotAddHeadersWhenSpanIsNull() {
        // Given
        when(tracer.currentSpan()).thenReturn(null);

        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(exchangeCaptor.capture());

        ServerWebExchange capturedExchange = exchangeCaptor.getValue();
        HttpHeaders headers = capturedExchange.getRequest().getHeaders();

        assertThat(headers.containsKey("X-B3-TraceId")).isFalse();
        assertThat(headers.containsKey("X-B3-SpanId")).isFalse();
        assertThat(headers.containsKey("X-B3-Sampled")).isFalse();
    }

    @Test
    void shouldPreserveExistingHeaders() {
        // Given
        String traceId = "1234567890abcdef";
        String spanId = "abcdef1234567890";

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn(traceId);
        when(traceContext.spanId()).thenReturn(spanId);

        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("Authorization", "Bearer token123")
                .header("Content-Type", "application/json")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(exchangeCaptor.capture());

        ServerWebExchange capturedExchange = exchangeCaptor.getValue();
        HttpHeaders headers = capturedExchange.getRequest().getHeaders();

        assertThat(headers.getFirst("Authorization")).isEqualTo("Bearer token123");
        assertThat(headers.getFirst("Content-Type")).isEqualTo("application/json");
        assertThat(headers.getFirst("X-B3-TraceId")).isEqualTo(traceId);
        assertThat(headers.getFirst("X-B3-SpanId")).isEqualTo(spanId);
    }

    @Test
    void shouldOverrideExistingB3Headers() {
        // Given
        String newTraceId = "newTrace123456789";
        String newSpanId = "newSpan987654321";

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn(newTraceId);
        when(traceContext.spanId()).thenReturn(newSpanId);

        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-B3-TraceId", "oldTraceId")
                .header("X-B3-SpanId", "oldSpanId")
                .header("X-B3-Sampled", "0")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(exchangeCaptor.capture());

        ServerWebExchange capturedExchange = exchangeCaptor.getValue();
        HttpHeaders headers = capturedExchange.getRequest().getHeaders();

        assertThat(headers.getFirst("X-B3-TraceId")).isEqualTo(newTraceId);
        assertThat(headers.getFirst("X-B3-SpanId")).isEqualTo(newSpanId);
        assertThat(headers.getFirst("X-B3-Sampled")).isEqualTo("1");
    }

    @Test
    void shouldCallChainFilterOnlyOnce() {
        // Given
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("trace123");
        when(traceContext.spanId()).thenReturn("span123");

        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        filter.filter(exchange, chain);

        // Then
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldHandleEmptyTraceIdAndSpanId() {
        // Given
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("");
        when(traceContext.spanId()).thenReturn("");

        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(exchangeCaptor.capture());

        ServerWebExchange capturedExchange = exchangeCaptor.getValue();
        HttpHeaders headers = capturedExchange.getRequest().getHeaders();

        assertThat(headers.getFirst("X-B3-TraceId")).isEqualTo("");
        assertThat(headers.getFirst("X-B3-SpanId")).isEqualTo("");
        assertThat(headers.getFirst("X-B3-Sampled")).isEqualTo("1");
    }
}