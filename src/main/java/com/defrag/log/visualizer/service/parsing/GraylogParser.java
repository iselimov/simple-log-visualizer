package com.defrag.log.visualizer.service.parsing;

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
import java.util.Objects;
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

        return logsFromGraylog.parallelStream()
                .map(this::parseMessage)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(LogDefinition::getInvocationOrder))
                .collect(Collectors.toList());

    }

    private LogDefinition parseMessage(final GraylogMessage logFromGraylog) {
        String logMessage = logFromGraylog.getMessage();

        int uidPrefixInd = logMessage.indexOf(UUID);
        if (uidPrefixInd == -1) {
            log.error("Uid in {} was not found", logMessage);
            return null;
        }

        int actionPrefixInd = logMessage.indexOf(ACTION, positionAfterString(UUID, uidPrefixInd));
        if (actionPrefixInd == -1) {
            log.error("Action in {} was not found", logMessage);
            return null;
        }

        LogEventParser parser = parserFactory.getParser(logMessage, positionAfterString(ACTION, actionPrefixInd));
        String uid = logMessage.substring(positionAfterString(UUID, uidPrefixInd), actionPrefixInd).trim();

        LogDefinition.Builder logBuilder = new LogDefinition.Builder(uid, parser.eventType(), logFromGraylog.getTimestamp(),
                logMessage);
        parser.fill(logBuilder);
        return logBuilder.build();
    }
}
