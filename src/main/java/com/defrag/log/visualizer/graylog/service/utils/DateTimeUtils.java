package com.defrag.log.visualizer.graylog.service.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimeUtils {

    public static LocalDateTime convertDateTimeInZone(LocalDateTime current, ZoneId fromZone, ZoneId toZone) {
        return ZonedDateTime.of(current, fromZone)
                .withZoneSameInstant(toZone)
                .toLocalDateTime();
    }
}
