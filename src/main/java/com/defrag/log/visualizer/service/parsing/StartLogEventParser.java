package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.defrag.log.visualizer.service.parsing.LoggingConstants.*;
import static com.defrag.log.visualizer.service.parsing.utils.ParserUtils.positionAfterString;

@Service
@Slf4j
class StartLogEventParser implements LogEventParser {

    private static final Pattern PATIENT_PATTERN = Pattern.compile("(?<=PAT)\\d+");

    @Override
    public LogEventType eventType() {
        return LogEventType.ACTION_START;
    }

    @Override
    public void fill(LogDefinition.Builder logBuilder) {
        int nextInd = 0;
        String parsingMessage = logBuilder.getFullMessage();

        int ioPrefixInd = parsingMessage.indexOf(INVOCATION_ORDER, nextInd);
        if (ioPrefixInd == -1) {
            log.error("Invocation order was not found in {}", parsingMessage);
            return;
        }
        nextInd = ioPrefixInd;

        int depthPrefixInd = parsingMessage.indexOf(DEPTH, nextInd);
        if (depthPrefixInd == -1) {
            log.error("Depth was not found in {}", parsingMessage);
            return;
        }
        nextInd = depthPrefixInd;

        String ioStr = parsingMessage.substring(positionAfterString(INVOCATION_ORDER, ioPrefixInd), depthPrefixInd).trim();
        try {
            logBuilder.invocationOrder(Integer.parseInt(ioStr));
        } catch (NumberFormatException e) {
            log.error("Couldn't parse invocation order {} in {}", ioStr, parsingMessage);
            return;
        }

        int nameInd = parsingMessage.indexOf(NAME, nextInd);
        if (nameInd == -1) {
            log.error("Name was not found in {}", parsingMessage);
            return;
        }
        nextInd = nameInd;

        String depthStr = parsingMessage.substring(positionAfterString(DEPTH, depthPrefixInd), nameInd).trim();
        try {
            logBuilder.depth(Integer.parseInt(depthStr));
        } catch (NumberFormatException e) {
            log.error("Couldn't parse depth {} in {}", depthStr, parsingMessage);
            return;
        }

        int argsInd = parsingMessage.indexOf(ARGS, nextInd);
        if (argsInd == -1) {
            log.error("Args was not found in {}", parsingMessage);
            return;
        }

        String name = parsingMessage.substring(positionAfterString(NAME, nameInd), argsInd).trim();
        logBuilder.actionName(name);

        String args = parsingMessage.substring(positionAfterString(ARGS, argsInd)).trim();
        logBuilder.args(args);
        logBuilder.patientId(parsePatientId(args));
    }

    private Long parsePatientId(String parsingMessage) {
        final Matcher patientMatcher = PATIENT_PATTERN.matcher(parsingMessage);
        String patientStr;
        if (patientMatcher.find()) {
            patientStr = patientMatcher.group();
        } else {
            log.error("Couldn't parse patient in {}", parsingMessage);
            return null;
        }

        try {
            return Long.parseLong(patientStr);
        } catch (NumberFormatException e) {
            log.error("Couldn't translate patient str {} -> digit in {}", patientStr, parsingMessage);
            return null;
        }
    }
}
