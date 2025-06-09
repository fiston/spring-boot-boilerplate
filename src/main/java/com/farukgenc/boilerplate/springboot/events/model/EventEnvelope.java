package com.farukgenc.boilerplate.springboot.events.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope {
    private String messageId;
    private Long timestamp;
    private Long sequenceNumber;
    private String userId;
    private String serviceId;
    private String sessionId;
    private String previousHash;
    private String signature;
}
