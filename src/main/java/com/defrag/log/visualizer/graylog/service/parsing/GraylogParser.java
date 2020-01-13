package com.defrag.log.visualizer.graylog.service.parsing;

import com.defrag.log.visualizer.common.model.LogMarker;
import com.defrag.log.visualizer.common.service.LoggingConstants;
import com.defrag.log.visualizer.graylog.service.parsing.model.GraylogMessage;
import com.defrag.log.visualizer.graylog.service.parsing.model.GraylogMessageWrapper;
import com.defrag.log.visualizer.graylog.service.parsing.model.GraylogResponseWrapper;
import com.defrag.log.visualizer.graylog.service.parsing.model.LogDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GraylogParser {

    private static final Pattern ACTION_PATTERN = Pattern.compile("\\w+(Action|Editor|Manager)");
    private static final Pattern PAYLOAD_PATTERN = Pattern.compile("\\w+Payload");
    private static final Pattern PATIENT_PATTERN = Pattern.compile("(?<=PAT)\\d+");

    public Set<LogDefinition> parseDefinitions(GraylogResponseWrapper graylogResponseWrapper) {
        List<GraylogMessage> logsFromGraylog = graylogResponseWrapper.getMessages()
                .stream()
                .map(GraylogMessageWrapper::getMessage)
                .collect(Collectors.toList());

        return parseDefinitions(logsFromGraylog);
    }

    private Set<LogDefinition> parseDefinitions(List<GraylogMessage> logsFromGraylog) {
        Set<LogDefinition> logDefinitions = new TreeSet<>();

        for (GraylogMessage logFromGraylog : logsFromGraylog) {
            String logMarker;
            final String currMsg = logFromGraylog.getMessage();

            if (currMsg.startsWith(LoggingConstants.START_ACTION)) {
                logMarker = LoggingConstants.START_ACTION;
            } else if (currMsg.startsWith(LoggingConstants.FINISH_ACTION)) {
                logMarker = LoggingConstants.FINISH_ACTION;
            } else if (currMsg.startsWith(LoggingConstants.EXCEPTION_IN_ACTION)) {
                logMarker = LoggingConstants.EXCEPTION_IN_ACTION;
            } else if (currMsg.startsWith(LoggingConstants.START_EXTERNAL_ACTION)) {
                logMarker = LoggingConstants.START_EXTERNAL_ACTION;
            } else if (currMsg.startsWith(LoggingConstants.FINISH_EXTERNAL_ACTION)) {
                logMarker = LoggingConstants.FINISH_EXTERNAL_ACTION;
            } else {
                continue;
            }

            final Matcher actionMatcher = ACTION_PATTERN.matcher(currMsg.substring(logMarker.length()));
            String actionName = null;
            if (actionMatcher.find()) {
                actionName = actionMatcher.group();
            }

            Matcher payloadMatcher = PAYLOAD_PATTERN.matcher(currMsg);
            String payloadName = null;
            if (payloadMatcher.find()) {
                payloadName = payloadMatcher.group();
            }

            Matcher patientMatcher = PATIENT_PATTERN.matcher(currMsg);
            Long patientId = null;
            if (patientMatcher.find()) {
                try {
                    patientId = Long.parseUnsignedLong(patientMatcher.group());
                } catch (NumberFormatException e) {
                    log.warn("Couldn't parse patient in log {}", currMsg);
                }
            }
            if (patientId == null) {
                continue;
            }

            LogDefinition lg;
            if (actionName != null
                    && (currMsg.startsWith(LoggingConstants.START_ACTION)
                    || currMsg.startsWith(LoggingConstants.FINISH_ACTION)
                    || currMsg.startsWith(LoggingConstants.EXCEPTION_IN_ACTION))) {
                lg = new LogDefinition(defineLogMarker(logMarker), patientId, logFromGraylog.getTimestamp(), currMsg);
                lg.setActionName(actionName);
            } else if (payloadName != null && currMsg.startsWith(LoggingConstants.START_EXTERNAL_ACTION)) {
                lg = new LogDefinition(LogMarker.START_EXT, patientId, logFromGraylog.getTimestamp(), currMsg);
                lg.setPayloadName(payloadName);
            } else if (currMsg.startsWith(LoggingConstants.FINISH_EXTERNAL_ACTION)) {
                lg = new LogDefinition(LogMarker.FINISH_EXT, patientId, logFromGraylog.getTimestamp(), currMsg);
                lg.setPayloadName(payloadName);
            } else {
                log.warn("Unknown log {} was found, skip it...", currMsg);
                continue;
            }

            logDefinitions.add(lg);
        }

        return logDefinitions;
    }

    private LogMarker defineLogMarker(String source) {
        switch (source) {
            case LoggingConstants.START_ACTION:
                return LogMarker.START;
            case LoggingConstants.EXCEPTION_IN_ACTION:
                return LogMarker.EXCEPTION;
            case LoggingConstants.FINISH_ACTION:
                return LogMarker.FINISH;
            default:
                throw new IllegalArgumentException("Unknown log marker");
        }
    }
}
