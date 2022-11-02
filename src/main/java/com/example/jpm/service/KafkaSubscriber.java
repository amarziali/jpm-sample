package com.example.jpm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class KafkaSubscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSubscriber.class);
    private final ReactiveKafkaConsumerTemplate<String, String> reactiveKafkaConsumerTemplate;
    private final SampleService sampleService;

    public KafkaSubscriber(final ReactiveKafkaConsumerTemplate<String, String> reactiveKafkaConsumerTemplate,
                           final SampleService sampleService) {
        this.reactiveKafkaConsumerTemplate = reactiveKafkaConsumerTemplate;
        this.sampleService = sampleService;
    }


    public Flux<String> subscribe() {
        return reactiveKafkaConsumerTemplate
                .receiveAutoAck()
                .doOnNext(consumerRecord -> LOGGER.info("received key={}, value={} from topic={}, offset={}",
                        consumerRecord.key(), consumerRecord.value(), consumerRecord.topic(), consumerRecord.offset()))
                .map(sampleService::processMessage);
    }

}
