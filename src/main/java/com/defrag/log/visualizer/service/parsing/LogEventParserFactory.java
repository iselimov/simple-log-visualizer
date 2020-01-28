package com.defrag.log.visualizer.service.parsing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class LogEventParserFactory {

    private final List<LogEventParser> logEventParsers;

    LogEventParser getParser(String logMessage, int eventTypeInd) {
        return logEventParsers
                .stream()
                .filter(logEventType -> logMessage.startsWith(logEventType.eventType().name(), eventTypeInd))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could't find appropriate parser for the message %s", logMessage)));
    }
}
