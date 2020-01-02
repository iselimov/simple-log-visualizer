package com.defrag.log.visualizer.graylog.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
class GraylogSessionRequest {

    @JsonProperty("username")
    private final String userName;

    private final String password;

    private final String host = "";
}
