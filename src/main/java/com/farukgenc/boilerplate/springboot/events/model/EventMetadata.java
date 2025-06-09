package com.farukgenc.boilerplate.springboot.events.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMetadata {
    private String version;
    private String contentType;
    private String compressionAlgorithm;
}
