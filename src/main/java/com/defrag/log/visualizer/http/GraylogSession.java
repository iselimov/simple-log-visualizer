package com.defrag.log.visualizer.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
class GraylogSession {

    @JsonProperty("session_id")
    private String id;
    @JsonProperty("valid_until")
    private String validUntilStr;
    private LocalDateTime validUntil;

    boolean isValidYet() {
        return validUntil.isAfter(LocalDateTime.now().plusSeconds(30));
    }

    void setValidUntilStr(String v) {
        String validUntilStr = v;

        final int zoneIndex = validUntilStr.indexOf("+");
        if (zoneIndex != -1) {
            validUntilStr = validUntilStr.substring(0, zoneIndex);
        }
        this.validUntilStr = v;
        validUntil = LocalDateTime.parse(validUntilStr, DateTimeFormatter.ISO_DATE_TIME);
    }
}
