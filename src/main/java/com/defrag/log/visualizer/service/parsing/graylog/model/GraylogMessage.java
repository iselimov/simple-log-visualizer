package com.defrag.log.visualizer.service.parsing.graylog.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GraylogMessage {

    @JsonProperty("full_message")
    private String message;
    private LocalDateTime timestamp;
}
