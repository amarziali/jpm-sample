package com.example.jpm.service;

import datadog.trace.api.Trace;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SampleService {
    @Trace
    public String fakeESCall() {
        // simulate some sync call like ES query
        try {
            //simulate some time-consuming stuff
            Thread.sleep(120);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return UUID.randomUUID().toString();
    }

    @Trace
    public String processMessage(final ConsumerRecord<String, String> record) {
        try {
            //simulate some time-consuming stuff
            Thread.sleep(200);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return record.value();
    }
}
