package com.example.jpm.controller;

import com.alibaba.fastjson2.JSON;
import com.example.jpm.service.CustomerService;
import com.example.jpm.service.KafkaSubscriber;
import com.example.jpm.service.SampleService;
import datadog.trace.api.Trace;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Controller
public class WebsocketController implements WebSocketHandler {
    private final SampleService sampleService;

    private final KafkaSubscriber kafkaSubscriber;

    private final CustomerService customerService;

    public WebsocketController(final SampleService sampleService, final KafkaSubscriber kafkaSubscriber, final CustomerService customerService) {
        this.sampleService = sampleService;
        this.kafkaSubscriber = kafkaSubscriber;
        this.customerService = customerService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        final Tracer tracer = GlobalTracer.get();
        final Instant sessionStart = Instant.now();
        return session.send(getRequestFromWebSocketMessage(session)
                .flatMap(request -> {
                    final Span span = tracer.buildSpan("websocket.newMessage").start();
                    try (final Scope outer = tracer.scopeManager().activate(span, false)) {
                        return processRequest(session.getId(), Flux.just(request))
                                .map(session::textMessage).flatMap(webSocketMessage -> {
                                    try (Scope inner = tracer.scopeManager().activate(span, true)) {
                                        span.setTag("custom.e2e_duration", Duration.between(sessionStart, Instant.now()).toMillis());
                                        return Flux.just(webSocketMessage);
                                    }
                                });
                    }
                }));

    }

    @Trace
    private String doKeepAlive() {
        throw new RuntimeException("Error");
    }

    private Flux<String> processRequest(String sessionId, Flux<String> requestMessage) {
        final Span conversationSpan = GlobalTracer.get().activeSpan();
        return requestMessage.groupBy(str -> "dummy").flatMap(instanceFlux -> instanceFlux.switchMap(requestObject -> {
            try (final var scope = GlobalTracer.get().scopeManager().activate(conversationSpan, false)) {
                return getData(sessionId, requestObject);
            }
        }));
    }

    private Flux<String> getData(String sessionId, String userMessage) {
        final Span conversationSpan = GlobalTracer.get().activeSpan();
        try (final var scope = GlobalTracer.get().scopeManager().activate(conversationSpan, false)) {
            return customerService.findAll().flatMapMany(customers -> Flux.just(JSON.toJSONString(customers)));
        }

    }

    public Flux<String> getRequestFromWebSocketMessage(WebSocketSession session) {
        return session.receive().map(WebSocketMessage::getPayloadAsText);
    }

}
