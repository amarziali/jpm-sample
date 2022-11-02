package com.example.jpm.controller;

import com.example.jpm.service.KafkaSubscriber;
import com.example.jpm.service.SampleService;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class WebsocketController implements WebSocketHandler {
    private final SampleService sampleService;

    private final KafkaSubscriber kafkaSubscriber;

    public WebsocketController(final SampleService sampleService, final KafkaSubscriber kafkaSubscriber) {
        this.sampleService = sampleService;
        this.kafkaSubscriber = kafkaSubscriber;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(Flux.concat(Flux.just(sampleService.fakeESCall()), kafkaSubscriber.subscribe())
                        .map(session::textMessage))
                .and(session.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .log());
    }
}
