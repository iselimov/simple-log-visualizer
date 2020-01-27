package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import com.defrag.log.visualizer.service.parsing.graylog.model.GraylogMessage;
import com.defrag.log.visualizer.service.parsing.graylog.model.GraylogMessageWrapper;
import com.defrag.log.visualizer.service.parsing.graylog.model.GraylogResponseWrapper;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.defrag.log.visualizer.service.parsing.LoggingConstants.ACTION;
import static com.defrag.log.visualizer.service.parsing.LoggingConstants.UUID;
import static com.defrag.log.visualizer.service.parsing.utils.ParserUtils.positionAfterString;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraylogParser {

    private final LogEventParserFactory parserFactory;

    public List<LogDefinition> parseDefinitions(GraylogResponseWrapper graylogResponseWrapper) {
        List<GraylogMessage> logsFromGraylog = graylogResponseWrapper.getMessages()
                .stream()
                .map(GraylogMessageWrapper::getMessage)
                .collect(Collectors.toList());

        return parseDefinitions(logsFromGraylog)
                .stream()
                .sorted(Comparator.comparing(LogDefinition::getInvocationOrder))
                .collect(Collectors.toList());
    }

    private List<LogDefinition> parseDefinitions(List<GraylogMessage> logsFromGraylog) {
        List<LogDefinition> logDefinitions = new ArrayList<>();

        for (GraylogMessage logFromGraylog : logsFromGraylog) {
            String logMessage = logFromGraylog.getMessage();

            int uidPrefixInd = logMessage.indexOf(UUID);
            if (uidPrefixInd == -1) {
                log.error("Uid in {} was not found", logMessage);
                continue;
            }

            int actionPrefixInd = logMessage.indexOf(ACTION, positionAfterString(UUID, uidPrefixInd));
            if (actionPrefixInd == -1) {
                log.error("Action in {} was not found", logMessage);
                continue;
            }

            LogEventType logEventType = defineLogEventType(logMessage, actionPrefixInd);
            if (logEventType == null) {
                continue;
            }
            String uid = logMessage.substring(uidPrefixInd, actionPrefixInd);

            LogDefinition.Builder logBuilder = new LogDefinition.Builder(uid, logEventType, logFromGraylog.getTimestamp(),
                    logMessage);
            parserFactory.getParser(logEventType).fill(logBuilder);
            logDefinitions.add(logBuilder.build());
        }

        return logDefinitions;
    }

    private LogEventType defineLogEventType(String logMessage, int actionPrefixInd) {
        int actionInd = positionAfterString(ACTION, actionPrefixInd);
        if (logMessage.startsWith(LogEventType.ACTION_START.getName(), actionInd)) {
            return LogEventType.ACTION_START;
        }
        if (logMessage.startsWith(LogEventType.ACTION_END.getName(), actionInd)) {
            return LogEventType.ACTION_END;
        }
        if (logMessage.startsWith(LogEventType.ACTION_ERROR.getName(), actionInd)) {
            return LogEventType.ACTION_ERROR;
        }
        if (logMessage.startsWith(LogEventType.SPARQL_QUERY.getName(), actionInd)) {
            return LogEventType.SPARQL_QUERY;
        }

        log.error("Log event type in {} was not found", logMessage);
        return null;
    }
}
