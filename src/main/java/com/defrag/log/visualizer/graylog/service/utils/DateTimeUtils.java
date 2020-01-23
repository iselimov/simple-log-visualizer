package com.defrag.log.visualizer.graylog.service.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimeUtils {

    private static final String DATE_TIME_FORMAT = "uuuu-MM-dd'T'HH:mm:ss.SSS";
    private static final String TIME_FORMAT = "HH:mm:ss.SSS";

    public static LocalDateTime convertDateTimeInZone(LocalDateTime current, ZoneId fromZone, ZoneId toZone) {
        return ZonedDateTime.of(current, fromZone)
                .withZoneSameInstant(toZone)
                .toLocalDateTime();
    }

    public static String toStr(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    public static String toStr(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern(TIME_FORMAT));
    }
}
