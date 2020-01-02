package com.defrag.log.visualizer.graylog.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GraylogMessage {

    @JsonProperty("full_message")
    private String message;
    private LocalDateTime timestamp;
}
