package com.defrag.log.visualizer.service.parsing;

import com.defrag.log.visualizer.model.LogEventType;
import com.defrag.log.visualizer.service.parsing.graylog.model.LogDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

import static com.defrag.log.visualizer.service.parsing.LoggingConstants.DEPTH;
import static com.defrag.log.visualizer.service.parsing.LoggingConstants.TIMING;
import static com.defrag.log.visualizer.service.parsing.utils.ParserUtils.positionAfterString;

@Service
@Slf4j
class SparqlQueryLogEventParser implements LogEventParser {

    @Override
    public LogEventType eventType() {
        return LogEventType.SPARQL_QUERY;
    }

    @Override
    public void fill(LogDefinition.Builder logBuilder) {
        int nextInd = 0;
        String parsingMessage = logBuilder.getFullMessage();

        int depthPrefixInd = parsingMessage.indexOf(DEPTH, nextInd);
        if (depthPrefixInd == -1) {
            return;
        }
        nextInd = depthPrefixInd;

        int timingInd = parsingMessage.indexOf(TIMING, nextInd);
        if (timingInd == -1) {
            return;
        }

        String depthStr = parsingMessage.substring(positionAfterString(DEPTH, depthPrefixInd), timingInd).trim();
        try {
            logBuilder.depth(Integer.parseInt(depthStr));
        } catch (NumberFormatException e) {
            log.error("Couldn't parse depth {} in {}", depthStr, parsingMessage);
            return;
        }

        String timingStr = parsingMessage.substring(positionAfterString(TIMING, timingInd)).trim();
        try {
            logBuilder.timing(Long.parseLong(timingStr));
        } catch (NumberFormatException e) {
            log.error("Couldn't parse timing {} in {}", timingStr, parsingMessage);
        }
    }
}
