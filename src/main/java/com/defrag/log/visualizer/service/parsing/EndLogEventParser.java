package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

import static com.defrag.log.visualizer.service.parsing.LoggingConstants.*;
import static com.defrag.log.visualizer.service.parsing.utils.ParserUtils.positionAfterString;

@Service
@Slf4j
class EndLogEventParser implements LogEventParser {

    @Override
    public Set<LogEventType> eventTypes() {
        return EnumSet.of(LogEventType.ACTION_END);
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

        int timingInd = parsingMessage.indexOf(TIMING, nextInd);
        if (timingInd == -1) {
            log.error("Timing was not found in {}", parsingMessage);
            return;
        }

        String name = parsingMessage.substring(positionAfterString(NAME, nameInd), timingInd).trim();
        logBuilder.actionName(name);

        String timingStr = parsingMessage.substring(positionAfterString(TIMING, timingInd)).trim();
        try {
            logBuilder.timing(Long.parseLong(timingStr));
        } catch (NumberFormatException e) {
            log.error("Couldn't parse timing {} in {}", timingStr, parsingMessage);
        }
    }
}
