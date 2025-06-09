package com.farukgenc.boilerplate.springboot.events.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaEventMessage<T> {
    private EventEnvelope envelope;
    private EventPayload<T> payload;
    private EventMetadata metadata;
}
