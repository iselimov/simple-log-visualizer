package com.defrag.log.visualizer.service.parsing.graylog.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class GraylogStream {

    private String id;
    @JsonProperty("title")
    private String name;
}
