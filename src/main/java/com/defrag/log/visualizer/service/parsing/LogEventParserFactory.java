package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class LogEventParserFactory {

    private final List<LogEventParser> logEventParsers;

    LogEventParser getParser(LogEventType logEventType) {
        return logEventParsers
                .stream()
                .filter(p -> p.eventTypes().contains(logEventType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could't find parser for %s", logEventType)));
    }
}
