package com.defrag.log.visualizer.service.parsing.graylog.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GraylogResponseWrapper {

    private List<GraylogMessageWrapper> messages = new ArrayList<>();
    @JsonProperty("total_results")
    private int totalAmount;
}
