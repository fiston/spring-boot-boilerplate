package com.farukgenc.boilerplate.springboot.events.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPayload<T> { // Generic to hold different types of data
    private boolean encrypted;
    private String data; // Will store Base64 encoded string of the actual payload data
}
